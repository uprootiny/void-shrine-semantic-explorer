(ns flamechart.core
  (:require [mount.core :as mount]
            [flamechart.config :as config]
            [flamechart.profiler :as profiler]
            [flamechart.analysis :as analysis]  
            [flamechart.server :as server]
            [taoensso.timbre :as log])
  (:gen-class))

(defn setup-logging! []
  (log/set-config!
    {:level (if (= "debug" (System/getProperty "log-level")) :debug :info)
     :appenders {:console {:enabled? true
                          :async? false
                          :output-fn :inherit}}}))

(defn simulate-realistic-workload []
  "Generate realistic application workload for demonstration"
  (future
    (while true
      (try
        ;; Simulate various application scenarios
        (profiler/profile-sampled "api-request"
          (profiler/profile-sampled "database-query"
            (Thread/sleep (+ 10 (rand-int 50))))
          (profiler/profile-sampled "business-logic"
            (Thread/sleep (+ 5 (rand-int 20)))
            ;; Add some data for analysis
            (analysis/add-execution-sample (+ 1000000 (rand-int 5000000)))
            (analysis/add-memory-sample (+ 1000000000 (rand-int 500000000)))
            (analysis/add-thread-sample (+ 10 (rand-int 20)))))
        
        (Thread/sleep (+ 100 (rand-int 500))))
      (catch Exception e
        (log/error e "Error in workload simulation")))))

(defn -main [& args]
  (setup-logging!)
  (log/info "🚀 Starting Production Flamechart System...")
  
  ;; Validate configuration
  (config/validate-config! @config/config)
  
  (mount/start)
  
  ;; Start workload simulation
  (simulate-realistic-workload)
  
  (log/info "✅ Production Flamechart System Started!")
  (log/info "📊 Web UI: http://localhost:{}" (get-in @config/config [:server :port]))
  (log/info "🔍 Profiling: {} ({}% sampling)" 
           (if (get-in @config/config [:profiling :enabled]) "Active" "Disabled")
           (* 100 (get-in @config/config [:profiling :sampling-rate])))
  (log/info "🧮 Chaos Analysis: Active")
  (log/info "⚡ Circuit Breaker: Armed")
  
  ;; Graceful shutdown
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(do (log/info "Shutting down...")
                                 (mount/stop))))
  
  ;; Block main thread
  @(promise))