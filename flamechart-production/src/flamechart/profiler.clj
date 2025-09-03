(ns flamechart.profiler
  (:require [flamechart.config :as config]
            [flamechart.sampling :as sampling]
            [clojure.core.async :as async :refer [go-loop <! timeout chan dropping-buffer]]
            [taoensso.timbre :as log]
            [mount.core :as mount :refer [defstate]])
  (:import [java.util.concurrent ConcurrentHashMap ConcurrentLinkedQueue]
           [java.util.concurrent.atomic AtomicReference AtomicBoolean]))

;; Circuit breaker for profiling system
(defrecord CircuitBreaker [state failures last-failure-time config])

(defonce ^AtomicReference circuit-breaker-ref 
  (AtomicReference. (->CircuitBreaker :closed 0 0 (:circuit-breaker (:monitoring @config/config)))))

(defn circuit-breaker-open? []
  (let [cb (.get circuit-breaker-ref)
        now (System/currentTimeMillis)]
    (and (= (:state cb) :open)
         (< (- now (:last-failure-time cb)) 
            (get-in cb [:config :timeout-ms])))))

(defn record-circuit-breaker-failure []
  (let [cb (.get circuit-breaker-ref)
        new-failures (inc (:failures cb))
        threshold (get-in cb [:config :failure-threshold])]
    (.set circuit-breaker-ref
          (assoc cb 
                :failures new-failures
                :last-failure-time (System/currentTimeMillis)
                :state (if (>= new-failures threshold) :open :closed)))))

;; High-performance profiling data structures
(defonce sample-channel (chan (dropping-buffer 10000))) ; Backpressure protection
(defonce ^ConcurrentHashMap function-metrics (ConcurrentHashMap.))
(defonce profiling-active (AtomicBoolean. true))

;; Optimized stack capture
(defn capture-stack-optimized []
  "Optimized stack capture with configurable depth"
  (let [stack (.getStackTrace (Thread/currentThread))
        limit (get-in @config/config [:profiling :stack-depth-limit])]
    (->> stack
         (drop 3) ; Skip profiling internals
         (take limit)
         (filter #(and (.getFileName %)
                      (not (.startsWith (.getClassName %) "java."))
                      (not (.startsWith (.getClassName %) "clojure.core"))
                      (not (.contains (.getClassName %) "mount"))))
         (mapv #(hash-map :method (.getMethodName %)
                         :class (.getClassName %)
                         :file (.getFileName %)
                         :line (.getLineNumber %))))))

;; Enhanced profiling macro with circuit breaker
(defmacro profile-sampled
  "Production profiling macro with sampling and circuit breaking"
  [fn-name & body]
  `(let [should-sample# (sampling/should-sample?)
         start-time# (when should-sample# (System/nanoTime))]
     (sampling/record-call should-sample#)
     
     (if (and should-sample# 
              (not (circuit-breaker-open?))
              (.get profiling-active))
       (try
         (let [result# (do ~@body)
               end-time# (System/nanoTime)
               duration# (- end-time# start-time#)
               overhead-limit# (get-in @config/config [:profiling :overhead-threshold-ns])]
           
           ;; Check if profiling overhead is acceptable
           (if (> duration# overhead-limit#)
             (do (sampling/record-overhead-violation)
                 (record-circuit-breaker-failure))
             (let [stack-trace# (capture-stack-optimized)]
               (async/put! sample-channel
                          {:timestamp start-time#
                           :duration duration#
                           :function-name (str ~fn-name)
                           :thread-id (.getId (Thread/currentThread))
                           :stack stack-trace#})))
           result#)
         (catch Exception e#
           (record-circuit-breaker-failure)
           (log/error e# "Profiling error in" ~fn-name)
           (throw e#)))
       ;; Fast path when not sampling
       (do ~@body))))

;; Async sample processing
(defn process-samples []
  "Process profiling samples asynchronously"
  (go-loop []
    (when-let [sample (<! sample-channel)]
      (try
        ;; Update function metrics
        (.compute function-metrics 
                  (:function-name sample)
                  (fn [_key existing]
                    (let [current (or existing {:calls 0 :total-time 0 :min-time Long/MAX_VALUE :max-time 0})]
                      {:calls (inc (:calls current))
                       :total-time (+ (:total-time current) (:duration sample))
                       :min-time (min (:min-time current) (:duration sample))
                       :max-time (max (:max-time current) (:duration sample))
                       :last-seen (:timestamp sample)})))
        
        ;; Add to time series for chaos analysis
        (swap! sampling/overhead-history conj (:duration sample))
        (swap! sampling/overhead-history #(take-last 1000 %))
        
        (catch Exception e
          (log/error e "Error processing sample")))
      (recur))))

(defstate profiler-processor
  :start (do
          (log/info "🔍 Starting enhanced profiler...")
          (.set profiling-active true)
          (process-samples))
  :stop (do
         (log/info "🔍 Stopping profiler...")
         (.set profiling-active false)))