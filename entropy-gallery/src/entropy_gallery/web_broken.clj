(ns entropy-gallery.web
  (:require [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.core :as h]
            [clojure.data.json :as json]
            [entropy-gallery.entropy.sources :as sources]
            [entropy-gallery.visualization.charts :as charts]
            [entropy-gallery.monitoring.flamechart :as monitoring]))

(defn entropy-gallery-page []
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "∞ ENTROPY SOURCE GALLERY ∞"]
     [:script {:src "https://d3js.org/d3.v7.min.js"}]
     [:style "
       body { 
         background: #0a0a0a; 
         color: #00ff88; 
         font-family: 'Courier New', monospace; 
         margin: 0; 
         padding: 20px; 
       }
       .gallery { 
         display: grid; 
         grid-template-columns: repeat(auto-fit, minmax(600px, 1fr)); 
         gap: 2rem; 
       }
       .source-card { 
         background: linear-gradient(135deg, #001a1a, #003333); 
         border: 2px solid #00ff88; 
         border-radius: 15px; 
         padding: 1.5rem; 
         box-shadow: 0 0 30px rgba(0,255,136,0.2);
       }
       .source-header { 
         display: flex; 
         justify-content: space-between; 
         align-items: center; 
         margin-bottom: 1rem; 
       }
       .source-name { 
         font-size: 1.2rem; 
         font-weight: bold; 
         color: #00ffff; 
       }
       .source-quality { 
         padding: 4px 8px; 
         border-radius: 4px; 
         font-size: 0.8rem; 
       }
       .true-random { background: #004400; color: #00ff00; }
       .cryptographic { background: #004488; color: #0088ff; }
       .pseudo-random { background: #444400; color: #ffff00; }
       .economic-chaos { background: #440044; color: #ff00ff; }
       .radioactive { background: #440000; color: #ff4400; }
       .chart-container { 
         height: 200px; 
         margin: 1rem 0; 
         background: rgba(0,0,0,0.5); 
         border: 1px solid #004444; 
         border-radius: 8px; 
       }
       .data-table { 
         width: 100%; 
         border-collapse: collapse; 
         font-size: 0.8rem; 
       }
       .data-table th, .data-table td { 
         border: 1px solid #004444; 
         padding: 4px 8px; 
         text-align: left; 
       }
       .data-table th { 
         background: #002222; 
         color: #00ffff; 
       }
       .accordion { 
         cursor: pointer; 
         background: #002222; 
         padding: 10px; 
         border: 1px solid #004444; 
         margin-top: 10px; 
       }
       .accordion:hover { background: #003333; }
       .panel { 
         display: none; 
         background: #001111; 
         padding: 10px; 
         border: 1px solid #004444; 
         border-top: none; 
       }
       .chart-tabs { 
         display: flex; 
         margin-bottom: 10px; 
       }
       .chart-tab { 
         padding: 8px 16px; 
         background: #002222; 
         border: 1px solid #004444; 
         cursor: pointer; 
         margin-right: 5px; 
       }
       .chart-tab.active { background: #004444; color: #00ffff; }
       .parametric-path { 
         fill: none; 
         stroke: #00ff88; 
         stroke-width: 2; 
       }
       .heatmap-cell { 
         stroke: #001111; 
         stroke-width: 1; 
       }
     "]]
    [:body
     [:h1 {:style "text-align: center; color: #00ffff; text-shadow: 0 0 20px #00ffff;"} 
      "∞ ENTROPY SOURCE GALLERY ∞"]
     [:p {:style "text-align: center; color: #888; margin-bottom: 2rem;"} 
      "Visual representations of randomness from different entropy sources"]
     
     [:div.gallery
      (for [[source-key source-data] (sources/get-all-sources)]
        [:div.source-card {:id (str "card-" (name source-key))}
         [:div.source-header
          [:div.source-name (:name source-data)]
          [:div {:class (str "source-quality " (:quality source-data))} (:quality source-data)]]
         
         [:div.chart-tabs
          [:div.chart-tab.active {:onclick (str "showChart('" (name source-key) "', 'line')")} "Line"]
          [:div.chart-tab {:onclick (str "showChart('" (name source-key) "', 'bar')")} "Bar"] 
          [:div.chart-tab {:onclick (str "showChart('" (name source-key) "', 'heatmap')")} "Heatmap"]
          [:div.chart-tab {:onclick (str "showChart('" (name source-key) "', 'parametric')")} "Parametric"]]
         
         [:div.chart-container {:id (str "chart-" (name source-key))}]
         
         [:div.accordion {:onclick (str "toggleAccordion('" (name source-key) "')")} 
          "📊 Data Analysis & Raw Samples"]
         [:div.panel {:id (str "panel-" (name source-key))}
          [:table.data-table
           [:tr [:th "Index"] [:th "Raw Value"] [:th "Hex"] [:th "Normalized"] [:th "Entropy"]]
           (map-indexed (fn [i sample]
                         [:tr 
                          [:td i] 
                          [:td (str sample)]
                          [:td (format "0x%06X" (Math/abs (hash sample)))]
                          [:td (format "%.3f" (/ (mod (Math/abs (hash sample)) 1000) 1000.0))]
                          [:td (format "%.2f" (Math/log (+ 1 (Math/abs (hash sample)))))]]) 
                       (take 8 (:samples source-data)))]
          [:p [:strong "Source: "] (:url source-data)]
          [:p [:strong "Type: "] (:source-type source-data)]
          [:p [:strong "Last Updated: "] (:last-updated source-data)]]])]
     
     [:script 
      (str "const sourceData = " 
           (json/write-str (into {} (for [[k v] (sources/get-all-sources)] 
                                     [k (assoc v :chart-data 
                                             {:line (charts/generate-chart-data (:samples v) :line)
                                              :bar (charts/generate-chart-data (:samples v) :bar)
                                              :heatmap (charts/generate-chart-data (:samples v) :heatmap) 
                                              :parametric (charts/generate-chart-data (:samples v) :parametric)})])))
           ";" 
           (charts/chart-javascript)
           "
           // Initialize with line charts
           Object.keys(sourceData).forEach(key => showChart(key, 'line'));
           ")]]))

(defroutes entropy-gallery-routes
  (GET "/" [] (response/response (entropy-gallery-page)))
  
  (GET "/api/entropy-sample" [source]
    (let [source-key (keyword source)
          source-data (sources/get-entropy-source source-key)]
      (monitoring/record-entropy-event source-key "sample-request" 50 source-data)
      (if source-data
        (response/response (json/write-str source-data))
        (response/response {:error "Source not found"} {:status 404}))))
  
  (GET "/monitoring" [] (response/response (monitoring/flamechart-visualization)))
  (GET "/api/monitoring-data" [] 
    (response/response (json/write-str (monitoring/get-monitoring-data))))
  (POST "/api/record-event" [source event-type duration data]
    (monitoring/record-entropy-event (keyword source) event-type 
                          (Integer/parseInt duration) data)
    (response/response {:status "recorded"}))
  
  (route/not-found "404 - Entropy Source Not Found"))

(def app (wrap-content-type entropy-gallery-routes))