(ns flamechart.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup2.core :as hiccup]
            [cognitect.transit :as transit]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log]
            [flamechart.profiler :as profiler]
            [flamechart.analysis :as analysis]
            [flamechart.sampling :as sampling]
            [flamechart.config :as config])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

;; Transit serialization for performance
(def write-transform-map
  {"iM" (fn [rep] (into {} rep))
   "iV" (fn [rep] (vec rep))})

(def read-transform-map
  {"iM" (fn [rep] rep)
   "iV" (fn [rep] rep)})

(defn serialize-transit [data]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json {:transform write-transform-map})]
    (transit/write writer data)
    (.toString out "UTF-8")))

;; Enhanced HTML with advanced visualizations
(defn render-dashboard []
  "Render enhanced dashboard with chaos/complexity visualizations"
  (profiler/profile-sampled "render-dashboard"
    (str
      (hiccup/html
        [:html
         [:head
          [:title "🔥 Production Flamechart System - Void Shrine"]
          [:meta {:charset "utf-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
          [:style "
           * { box-sizing: border-box; margin: 0; padding: 0; }
           body { font-family: 'SF Pro Display', -apple-system, system-ui, sans-serif; 
                  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); 
                  min-height: 100vh; color: #333; overflow-x: hidden; }
           .container { max-width: 1600px; margin: 0 auto; padding: 20px; }
           .header { background: rgba(255,255,255,0.95); backdrop-filter: blur(20px); 
                     border-radius: 16px; padding: 30px; margin-bottom: 25px; 
                     box-shadow: 0 20px 60px rgba(0,0,0,0.15); text-align: center;
                     border: 1px solid rgba(255,255,255,0.2); }
           .header h1 { color: #1a365d; font-size: 3em; margin-bottom: 10px; 
                       background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                       -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
           .status-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); 
                         gap: 15px; margin: 25px 0; }
           .status-card { background: rgba(255,255,255,0.9); backdrop-filter: blur(10px);
                         border-radius: 12px; padding: 20px; text-align: center;
                         box-shadow: 0 8px 25px rgba(0,0,0,0.1); border: 1px solid rgba(255,255,255,0.3); }
           .status-value { font-size: 2em; font-weight: 700; margin-bottom: 5px; }
           .status-label { color: #666; font-size: 0.9em; text-transform: uppercase; letter-spacing: 1px; }
           .controls { text-align: center; margin: 25px 0; }
           .btn { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                  color: white; border: none; padding: 12px 25px; border-radius: 8px; 
                  cursor: pointer; font-size: 1em; margin: 0 10px; font-weight: 600;
                  transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }
           .btn:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6); }
           .btn.danger { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%); 
                        box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4); }
           .btn.success { background: linear-gradient(135deg, #51cf66 0%, #40c057 100%);
                         box-shadow: 0 4px 15px rgba(81, 207, 102, 0.4); }
           .dashboard { display: grid; grid-template-columns: repeat(auto-fit, minmax(500px, 1fr)); 
                       gap: 25px; margin: 25px 0; }
           .widget { background: rgba(255,255,255,0.95); backdrop-filter: blur(20px); 
                     border-radius: 16px; padding: 25px; box-shadow: 0 20px 60px rgba(0,0,0,0.1);
                     border: 1px solid rgba(255,255,255,0.2); }
           .widget.full-width { grid-column: 1 / -1; }
           .widget h3 { color: #1a365d; margin-bottom: 20px; font-size: 1.4em; 
                       font-weight: 700; border-bottom: 3px solid #667eea; padding-bottom: 10px; }
           .visualization { width: 100%; height: 400px; border-radius: 12px; 
                           background: #f8fafc; border: 2px solid #e2e8f0; }
           .flamechart { height: 600px; }
           .chaos-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 15px; }
           .chaos-metric { background: rgba(102, 126, 234, 0.1); border-radius: 8px; 
                          padding: 15px; border-left: 4px solid #667eea; }
           .chaos-value { font-size: 1.5em; font-weight: 700; color: #1a365d; }
           .chaos-label { color: #666; font-size: 0.9em; margin-top: 5px; }
           .loading { text-align: center; padding: 60px; color: #718096; font-size: 1.1em; }
           .error { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%); 
                   color: white; padding: 20px; border-radius: 12px; margin: 15px 0; }
           .correlation-matrix { display: grid; grid-template-columns: repeat(var(--cols), 1fr); 
                                gap: 2px; margin: 15px 0; }
           .correlation-cell { aspect-ratio: 1; display: flex; align-items: center; 
                              justify-content: center; border-radius: 4px; font-size: 0.8em;
                              font-weight: 600; color: white; text-shadow: 0 1px 2px rgba(0,0,0,0.5); }
           .tooltip { position: absolute; background: rgba(0,0,0,0.95); color: white; 
                     padding: 12px; border-radius: 8px; pointer-events: none; 
                     font-size: 14px; z-index: 10000; box-shadow: 0 8px 25px rgba(0,0,0,0.3); }
           "]]
         [:body
          [:div.container
           [:div.header
            [:h1 "🔥 Void Shrine Flamechart"]
            [:p "Advanced profiling with chaos theory and complexity analysis for the void shrine system"]]
           
           [:div.status-grid
            [:div.status-card
             [:div.status-value#sampling-rate "0.0%"]
             [:div.status-label "Sampling Rate"]]
            [:div.status-card
             [:div.status-value#circuit-status "CLOSED"]
             [:div.status-label "Circuit Breaker"]]
            [:div.status-card
             [:div.status-value#overhead-violations "0"]
             [:div.status-label "Overhead Violations"]]
            [:div.status-card
             [:div.status-value#functions-tracked "0"]
             [:div.status-label "Functions"]]
            [:div.status-card
             [:div.status-value#chaos-level "0.00"]
             [:div.status-label "Chaos Index"]]
            [:div.status-card
             [:div.status-value#complexity-score "0.00"]
             [:div.status-label "Complexity"]]]
           
           [:div.controls
            [:button.btn {:onclick "refreshData()"} "🔄 Refresh"]
            [:button.btn {:onclick "toggleSampling()"} "📊 Toggle Sampling"] 
            [:button.btn.success {:onclick "exportData()"} "💾 Export"]
            [:button.btn.danger {:onclick "resetCircuitBreaker()"} "⚡ Reset Circuit"]
            [:a.btn {:href "http://localhost:3002" :style "text-decoration: none;"} "🎲 Entropy Gallery"]]
           
           [:div.dashboard
            [:div.widget.full-width
             [:h3 "Real-Time Flamechart Visualization"]
             [:div.visualization.flamechart#flamechart]]
            
            [:div.widget
             [:h3 "Chaos & Complexity Analysis"]
             [:div.chaos-grid#chaos-analysis]]
            
            [:div.widget  
             [:h3 "Autocorrelation Patterns"]
             [:div.visualization#correlation-viz]]
            
            [:div.widget
             [:h3 "System Entropy Over Time"] 
             [:div.visualization#entropy-viz]]
            
            [:div.widget
             [:h3 "Fractal Dimension Analysis"]
             [:div.visualization#fractal-viz]]
            
            [:div.widget
             [:h3 "Cross-Correlation Matrix"]
             [:div#correlation-matrix]]]]]
        
        ;; Enhanced JavaScript with advanced visualizations
        [:script {:src "https://d3js.org/d3.v7.min.js"}]
        [:script (slurp "resources/flamechart.js")]])))

;; Async handlers for better performance
(defn async-profiling-data [request respond raise]
  (profiler/profile-sampled "async-profiling-api"
    (try
      (let [data {:flame_tree (into {} profiler/function-metrics)
                  :circuit_breaker (.get profiler/circuit-breaker-ref)
                  :timestamp (System/currentTimeMillis)}]
        (respond {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body (serialize-transit data)}))
      (catch Exception e
        (log/error e "Error in profiling data API")
        (raise e)))))

(defn async-chaos-analysis [request respond raise]
  (profiler/profile-sampled "async-chaos-api"
    (try
      (let [analysis-data (analysis/analyze-system-dynamics)]
        (respond {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body (serialize-transit analysis-data)}))
      (catch Exception e
        (log/error e "Error in chaos analysis API")
        (raise e)))))

(defn async-sampling-stats [request respond raise]
  (try
    (let [stats (sampling/get-sampling-stats)]
      (respond {:status 200
               :headers {"Content-Type" "application/json"}
               :body (serialize-transit stats)}))
    (catch Exception e
      (raise e))))

;; Route definitions
(defroutes app-routes
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html; charset=utf-8"}
               :body (render-dashboard)})
  
  (GET "/api/profiling-data" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (serialize-transit {:flame_tree (into {} profiler/function-metrics)
                              :circuit_breaker (.get profiler/circuit-breaker-ref)
                              :timestamp (System/currentTimeMillis)})})
  
  (GET "/api/chaos-analysis" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (serialize-transit (analysis/analyze-system-dynamics))})
  
  (GET "/api/sampling-stats" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (serialize-transit (sampling/get-sampling-stats))})
  
  (POST "/api/toggle-sampling" []
    (let [current-enabled (get-in @config/config [:profiling :enabled])
          new-enabled (not current-enabled)]
      (config/update-config! {:profiling {:enabled new-enabled}})
      {:status 200 :body {:enabled new-enabled}}))
  
  (POST "/api/reset-circuit" []
    (.set profiler/circuit-breaker-ref 
          (profiler/->CircuitBreaker :closed 0 0 
                                   (get-in @config/config [:monitoring :circuit-breaker])))
    {:status 200 :body {:message "Circuit breaker reset"}})
  
  (route/not-found "Not Found"))

;; Enhanced middleware stack
(def app
  (-> app-routes
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :options]
                 :access-control-allow-headers ["Content-Type"])
      (wrap-json-response)
      (wrap-defaults (-> site-defaults
                        (assoc-in [:security :anti-forgery] false)
                        (assoc-in [:session] false)))))

(defstate web-server
  :start (do
          (log/info "🌐 Starting production web server on port" 
                   (get-in @config/config [:server :port]))
          (jetty/run-jetty app {:port (get-in @config/config [:server :port])
                               :join? false
                               :max-threads 50
                               :min-threads 8}))
  :stop (do
         (log/info "🌐 Stopping web server...")
         (.stop web-server)))