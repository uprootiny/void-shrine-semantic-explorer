(ns flamechart.analysis
  (:require [clojure.core.matrix :as matrix]
            [clojure.math.numeric-tower :as math]
            [flamechart.config :as config]
            [taoensso.timbre :as log])
  (:import [java.util.concurrent ConcurrentLinkedQueue]))

(matrix/set-current-implementation :ndarray)

;; Time series data for analysis
(defonce ^ConcurrentLinkedQueue execution-times (ConcurrentLinkedQueue.))
(defonce ^ConcurrentLinkedQueue memory-samples (ConcurrentLinkedQueue.))
(defonce ^ConcurrentLinkedQueue thread-counts (ConcurrentLinkedQueue.))

(defn add-execution-sample [duration]
  (.offer execution-times duration)
  (when (> (.size execution-times) 10000)
    (.poll execution-times)))

(defn add-memory-sample [memory-usage]
  (.offer memory-samples memory-usage)
  (when (> (.size memory-samples) 10000)
    (.poll memory-samples)))

(defn add-thread-sample [thread-count]
  (.offer thread-counts thread-count)
  (when (> (.size thread-counts) 10000)
    (.poll thread-counts)))

;; Autocorrelation analysis
(defn autocorrelation [series lag]
  "Calculate autocorrelation at given lag"
  (when (< lag (count series))
    (let [n (count series)
          mean (/ (reduce + series) n)
          numerator (reduce + (map (fn [i]
                                    (* (- (nth series i) mean)
                                       (- (nth series (+ i lag)) mean)))
                                  (range (- n lag))))
          denominator (reduce + (map #(math/expt (- % mean) 2) series))]
      (if (zero? denominator) 0.0 (/ numerator denominator)))))

(defn staggered-autocorrelation [series]
  "Calculate autocorrelation at multiple lags"
  (let [lags (get-in @config/config [:analysis :correlation-lags])
        window-size (get-in @config/config [:analysis :window-size])
        recent-series (take-last window-size series)]
    (when (>= (count recent-series) (apply max lags))
      (into {} (map (fn [lag] 
                     [lag (autocorrelation recent-series lag)]) 
                   lags)))))

;; Entropy calculation
(defn shannon-entropy [series]
  "Calculate Shannon entropy of a time series"
  (when (seq series)
    (let [bins (get-in @config/config [:analysis :entropy-bins])
          min-val (apply min series)
          max-val (apply max series)
          bin-width (/ (- max-val min-val) bins)
          histogram (frequencies 
                     (map #(min (dec bins) 
                               (int (/ (- % min-val) bin-width))) 
                         series))
          n (count series)
          probabilities (map #(/ (val %) n) histogram)]
      (- (reduce + (map #(if (zero? %) 0 (* % (Math/log %))) probabilities))))))

;; Lyapunov exponent estimation (simplified)
(defn lyapunov-exponent [series]
  "Estimate largest Lyapunov exponent (chaos indicator)"
  (when (> (count series) 50)
    (let [window 10
          n (- (count series) window)
          divergences (for [i (range n)]
                       (let [x1 (nth series i)
                             x2 (nth series (+ i window))
                             initial-sep 1e-6
                             final-sep (math/abs (- x2 x1))]
                         (if (> final-sep initial-sep)
                           (/ (Math/log (/ final-sep initial-sep)) window)
                           0)))]
      (if (seq divergences)
        (/ (reduce + divergences) (count divergences))
        0.0))))

;; Fractal dimension (box-counting approximation)
(defn box-counting-dimension [series]
  "Estimate fractal dimension using box-counting"
  (when (> (count series) 20)
    (let [scales [2 4 8 16 32]
          counts (map (fn [scale]
                       (let [boxes (partition-all scale series)]
                         (count (filter seq boxes))))
                     scales)
          log-scales (map #(Math/log %) scales)
          log-counts (map #(Math/log %) counts)]
      (when (> (count log-scales) 1)
        ;; Simple linear regression slope
        (let [n (count log-scales)
              sum-x (reduce + log-scales)
              sum-y (reduce + log-counts)
              sum-xy (reduce + (map * log-scales log-counts))
              sum-xx (reduce + (map #(* % %) log-scales))]
          (- (/ (- (* n sum-xy) (* sum-x sum-y))
               (- (* n sum-xx) (* sum-x sum-x)))))))))

;; Complexity measures
(defn lempel-ziv-complexity [series]
  "Estimate Lempel-Ziv complexity"
  (when (seq series)
    ;; Simplified binary encoding and LZ77-style compression
    (let [binary-series (map #(if (> % (/ (+ (apply min series) (apply max series)) 2)) 1 0) series)
          n (count binary-series)]
      (loop [i 1
             complexity 1
             dictionary #{}]
        (if (>= i n)
          complexity
          (let [pattern (take i binary-series)]
            (if (dictionary pattern)
              (recur (inc i) complexity dictionary)
              (recur (inc i) (inc complexity) (conj dictionary pattern)))))))))

;; Real-time analysis
(defn analyze-system-dynamics []
  "Comprehensive system dynamics analysis"
  (let [exec-series (vec execution-times)
        mem-series (vec memory-samples)
        thread-series (vec thread-counts)]
    
    {:execution-analysis 
     {:autocorrelations (staggered-autocorrelation exec-series)
      :entropy (shannon-entropy exec-series)
      :lyapunov-exponent (lyapunov-exponent exec-series)
      :fractal-dimension (box-counting-dimension exec-series)
      :lz-complexity (lempel-ziv-complexity exec-series)
      :sample-count (count exec-series)}
     
     :memory-analysis
     {:autocorrelations (staggered-autocorrelation mem-series)
      :entropy (shannon-entropy mem-series)
      :trend-stability (when (> (count mem-series) 10)
                        (let [recent (take-last 10 mem-series)
                              variance (/ (reduce + (map #(* % %) recent)) 10)]
                          (/ 1.0 (+ 1.0 variance))))
      :sample-count (count mem-series)}
     
     :thread-analysis
     {:autocorrelations (staggered-autocorrelation thread-series)
      :entropy (shannon-entropy thread-series)
      :complexity (lempel-ziv-complexity thread-series)
      :sample-count (count thread-series)}
     
     :cross-correlations
     {:execution-memory (when (and (seq exec-series) (seq mem-series))
                         (let [min-len (min (count exec-series) (count mem-series))]
                           (autocorrelation 
                             (map * (take-last min-len exec-series) 
                                   (take-last min-len mem-series)) 0)))
      :execution-threads (when (and (seq exec-series) (seq thread-series))
                          (let [min-len (min (count exec-series) (count thread-series))]
                            (autocorrelation 
                              (map * (take-last min-len exec-series) 
                                    (take-last min-len thread-series)) 0)))}
     
     :timestamp (System/currentTimeMillis)}))