(ns flamechart.sampling
  (:require [flamechart.config :as config]
            [taoensso.timbre :as log])
  (:import [java.util.concurrent ThreadLocalRandom]
           [java.util.concurrent.atomic AtomicLong LongAdder]))

;; Sampling statistics
(defonce ^AtomicLong total-calls (AtomicLong.))
(defonce ^LongAdder sampled-calls (LongAdder.))
(defonce ^LongAdder overhead-violations (LongAdder.))
(defonce ^ThreadLocal random-state 
  (ThreadLocal/withInitial 
    (reify java.util.function.Supplier
      (get [_] (ThreadLocalRandom/current)))))

(defn should-sample? []
  "Efficient thread-local sampling decision"
  (let [rate (get-in @config/config [:profiling :sampling-rate])]
    (and (get-in @config/config [:profiling :enabled])
         (< (.nextDouble (.get random-state)) rate))))

(defn record-call [sampled?]
  "Track sampling statistics"
  (.incrementAndGet total-calls)
  (when sampled? (.add sampled-calls 1)))

(defn record-overhead-violation []
  "Track when profiling takes too long"
  (.add overhead-violations 1))

(defn get-sampling-stats []
  {:total-calls (.get total-calls)
   :sampled-calls (.longValue sampled-calls)
   :overhead-violations (.longValue overhead-violations)
   :sampling-rate (get-in @config/config [:profiling :sampling-rate])
   :effective-rate (let [total (.get total-calls)]
                    (if (> total 0)
                      (/ (.longValue sampled-calls) (double total))
                      0.0))})

;; Adaptive sampling based on overhead
(defonce overhead-history (atom []))

(defn adaptive-sampling-adjustment []
  "Adjust sampling rate based on measured overhead"
  (let [recent-overhead (take-last 100 @overhead-history)
        violation-rate (if (seq recent-overhead)
                        (/ (count (filter pos? recent-overhead)) 
                           (count recent-overhead))
                        0.0)
        current-rate (get-in @config/config [:profiling :sampling-rate])
        new-rate (cond
                   (> violation-rate 0.1) (* current-rate 0.5) ; Too much overhead
                   (< violation-rate 0.01) (min 1.0 (* current-rate 1.1)) ; Can increase
                   :else current-rate)]
    (when (not= new-rate current-rate)
      (config/update-config! {:profiling {:sampling-rate new-rate}})
      (log/info "Adjusted sampling rate:" current-rate "→" new-rate))))