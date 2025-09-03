(ns flamechart.config
  (:require [schema.core :as s]))

(def ConfigSchema
  {:profiling {:enabled s/Bool
               :sampling-rate s/Num  ; 0.0 to 1.0
               :max-samples s/Int
               :stack-depth-limit s/Int
               :overhead-threshold-ns s/Int}
   :monitoring {:collection-interval-ms s/Int
                :retention-samples s/Int
                :circuit-breaker {:failure-threshold s/Int
                                 :timeout-ms s/Int}}
   :server {:port s/Int
            :async-timeout-ms s/Int
            :max-concurrent-requests s/Int}
   :analysis {:window-size s/Int
              :correlation-lags [s/Int]
              :entropy-bins s/Int
              :chaos-dimensions s/Int}})

(def default-config
  {:profiling {:enabled true
               :sampling-rate 0.01  ; 1% sampling by default
               :max-samples 10000
               :stack-depth-limit 20
               :overhead-threshold-ns 1000000} ; 1ms max overhead
   :monitoring {:collection-interval-ms 1000
                :retention-samples 3600  ; 1 hour at 1s intervals
                :circuit-breaker {:failure-threshold 5
                                 :timeout-ms 30000}}
   :server {:port 3000
            :async-timeout-ms 5000
            :max-concurrent-requests 100}
   :analysis {:window-size 100
              :correlation-lags [1 5 10 20 50]
              :entropy-bins 32
              :chaos-dimensions 3}})

(def config (atom default-config))

(defn validate-config! [cfg]
  (s/validate ConfigSchema cfg)
  cfg)

(defn update-config! [new-config]
  (swap! config #(validate-config! (merge % new-config))))