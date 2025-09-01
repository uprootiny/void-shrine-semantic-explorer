(ns entropy-gallery.core-unified
  "Unified entropy gallery with live sources, Lévy processes, and interactive visualizations"
  (:require [entropy-gallery.entropy.live-sources :as live]
            [entropy-gallery.processes.levy :as levy]
            [entropy-gallery.visualization.interactive :as interactive]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.core :as h]
            [clojure.data.json :as json]
            [taoensso.timbre :as log]))

;; Available entropy sources
(def available-sources #{:random-org :nist-beacon :system-local :market-data :hotbits})

;; Available process types
(def available-processes #{:levy-flight :fractal-brownian :jump-diffusion :stable-diffusion
                          :line :bar :heatmap :parametric})

(defn unified-gallery-page []
  "Render unified entropy gallery with all features"
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "∞ UNIFIED ENTROPY GALLERY ∞"]
     [:script {:src "https://d3js.org/d3.v7.min.js"}]
     [:style "
       body { 
         background: #0a0a0a; 
         color: #00ff88; 
         font-family: 'Courier New', monospace; 
         margin: 0; 
         padding: 20px; 
       }
       .header { 
         text-align: center; 
         margin-bottom: 2rem; 
         border-bottom: 2px solid #00ff88; 
         padding-bottom: 1rem; 
       }
       .gallery { 
         display: grid; 
         grid-template-columns: repeat(auto-fit, minmax(700px, 1fr)); 
         gap: 2rem; 
       }
       .source-card { 
         background: linear-gradient(135deg, #001a1a, #003333); 
         border: 2px solid #00ff88; 
         border-radius: 15px; 
         padding: 1.5rem; 
         box-shadow: 0 0 30px rgba(0,255,136,0.2);
         position: relative;
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
       .quality-badge { 
         padding: 4px 8px; 
         border-radius: 4px; 
         font-size: 0.8rem; 
       }
       .true-random { background: #004400; color: #00ff00; }
       .cryptographic { background: #004488; color: #0088ff; }
       .pseudo-random { background: #444400; color: #ffff00; }
       .economic-chaos { background: #440044; color: #ff00ff; }
       .process-tabs { 
         display: flex; 
         flex-wrap: wrap;
         gap: 5px;
         margin-bottom: 10px; 
       }
       .process-tab { 
         padding: 6px 12px; 
         background: #002222; 
         border: 1px solid #004444; 
         cursor: pointer; 
         font-size: 0.8rem;
         border-radius: 4px;
       }
       .process-tab.active { background: #004444; color: #00ffff; }
       .chart-container { 
         height: 250px; 
         margin: 1rem 0; 
         background: rgba(0,0,0,0.5); 
         border: 1px solid #004444; 
         border-radius: 8px; 
         position: relative;
       }
       .entropy-strength {
         position: absolute;
         top: 10px;
         right: 10px;
         background: rgba(0,255,136,0.1);
         padding: 5px 10px;
         border-radius: 4px;
         font-size: 12px;
         z-index: 10;
       }
       .controls { 
         text-align: center; 
         margin: 2rem 0; 
       }
       .btn { 
         background: linear-gradient(135deg, #00ff88, #00ccdd); 
         color: #000; 
         border: none; 
         padding: 10px 20px; 
         border-radius: 6px; 
         cursor: pointer; 
         margin: 0 10px; 
         font-weight: bold;
       }
       .stats-panel { 
         background: #001111; 
         border: 1px solid #004444; 
         border-radius: 8px; 
         padding: 15px; 
         margin-top: 15px; 
         font-size: 0.9em;
       }
       .loading { color: #666; text-align: center; padding: 60px; }
     "]]
    [:body
     [:div.header
      [:h1 "∞ UNIFIED ENTROPY GALLERY ∞"]
      [:p "Live entropy sources • Lévy processes • Interactive visualizations"]
      [:div.controls
       [:button.btn {:onclick "refreshAllSources()"} "🔄 Refresh All"]
       [:button.btn {:onclick "generateLevyDemo()"} "🌊 Demo Lévy Processes"]
       [:button.btn {:onclick "exportAllData()"} "💾 Export Data"]]]
     
     [:div.gallery
      (for [source-key available-sources]
        [:div.source-card {:id (str "card-" (name source-key))}
         [:div.source-header
          [:div.source-name {:id (str "name-" (name source-key))} "Loading..."]
          [:div.quality-badge {:id (str "quality-" (name source-key))} "..."]]
         
         [:div.process-tabs
          (for [process-type available-processes]
            [:div.process-tab 
             {:onclick (str "showProcess('" (name source-key) "', '" (name process-type) "')")} 
             (clojure.string/replace (name process-type) #"-" " ")])
          ]
         
         [:div.chart-container {:id (str "chart-" (name source-key))}]
         
         [:div.stats-panel {:id (str "stats-" (name source-key))}
          "Loading entropy statistics..."]
         ])]
     
     [:script (interactive/animation-javascript)]
     [:script "
       let sourceData = {};
       let processCache = {};
       
       function refreshAllSources() {
         document.querySelectorAll('.source-card').forEach(card => {
           const sourceKey = card.id.replace('card-', '');
           refreshSource(sourceKey);
         });
       }
       
       function refreshSource(sourceKey) {
         fetch(`/api/live-entropy/${sourceKey}`)
           .then(r => r.json())
           .then(data => {
             sourceData[sourceKey] = data;
             updateSourceCard(sourceKey, data);
             // Default to Lévy flight visualization
             showProcess(sourceKey, 'levy-flight');
           })
           .catch(err => {
             console.error(`Failed to fetch ${sourceKey}:`, err);
             document.getElementById(`name-${sourceKey}`).textContent = 'Error loading';
           });
       }
       
       function updateSourceCard(sourceKey, data) {
         document.getElementById(`name-${sourceKey}`).textContent = data.name;
         const qualityBadge = document.getElementById(`quality-${sourceKey}`);
         qualityBadge.textContent = data.quality;
         qualityBadge.className = `quality-badge ${data.quality.replace('-', '')}`;
         
         const stats = document.getElementById(`stats-${sourceKey}`);
         stats.innerHTML = `
           <strong>Source:</strong> ${data['source-type']}<br/>
           <strong>Samples:</strong> ${data.samples.length}<br/>
           <strong>Range:</strong> ${Math.min(...data.samples)} - ${Math.max(...data.samples)}<br/>
           <strong>Mean:</strong> ${(data.samples.reduce((a,b) => a+b, 0) / data.samples.length).toFixed(2)}<br/>
           <strong>Last Updated:</strong> ${new Date(data['last-updated']).toLocaleTimeString()}
         `;
       }
       
       function showProcess(sourceKey, processType) {
         const data = sourceData[sourceKey];
         if (!data) return;
         
         // Update active tab
         document.querySelectorAll(`#card-${sourceKey} .process-tab`).forEach(tab => {
           tab.classList.toggle('active', tab.textContent.replace(/\\s+/g, '-').toLowerCase() === processType);
         });
         
         const container = d3.select(`#chart-${sourceKey}`);
         container.selectAll('*').remove();
         
         // Generate process-specific visualization data
         fetch('/api/process-data', {
           method: 'POST',
           headers: {'Content-Type': 'application/json'},
           body: JSON.stringify({
             samples: data.samples,
             processType: processType,
             sourceKey: sourceKey
           })
         })
         .then(r => r.json())
         .then(processData => {
           const svg = container.append('svg')
             .attr('width', '100%')
             .attr('height', '100%')
             .attr('viewBox', '0 0 680 250');
           
           animateChart(sourceKey, processType, processData);
           addInteractiveElements(sourceKey);
         });
       }
       
       function generateLevyDemo() {
         // Show all sources with different Lévy processes
         Object.keys(sourceData).forEach((sourceKey, i) => {
           const processes = ['levy-flight', 'fractal-brownian', 'jump-diffusion', 'stable-diffusion'];
           showProcess(sourceKey, processes[i % processes.length]);
         });
       }
       
       function exportAllData() {
         const exportData = {
           sources: sourceData,
           timestamp: new Date().toISOString(),
           processes: processCache
         };
         const blob = new Blob([JSON.stringify(exportData, null, 2)], {type: 'application/json'});
         const url = URL.createObjectURL(blob);
         const a = document.createElement('a');
         a.href = url;
         a.download = `entropy-gallery-${Date.now()}.json`;
         a.click();
       }
       
       // Initialize
       setTimeout(refreshAllSources, 1000);
       setInterval(refreshAllSources, 30000); // Refresh every 30 seconds
     "])])

(defroutes unified-routes
  (GET "/" [] 
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (unified-gallery-page)})
  
  (GET "/api/live-entropy/:source" [source]
    (let [source-key (keyword source)
          data (when (available-sources source-key)
                 (live/fetch-entropy-source source-key))]
      (if data
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/write-str data)}
        {:status 404
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {:error "Source not found" 
                               :available (vec available-sources)})})))
  
  (POST "/api/process-data" request
    (let [{:keys [samples processType sourceKey]} (json/read-str (:body request) :key-fn keyword)
          process-key (keyword processType)
          result (when (available-processes process-key)
                   (interactive/generate-interactive-data samples process-key))]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str result)}))
  
  (GET "/api/status" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:sources (count available-sources)
                           :processes (count available-processes)
                           :cache-size (.size live/entropy-cache)
                           :uptime (System/currentTimeMillis)})})
  
  (route/not-found "Not Found"))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3002"))]
    (log/info "🚀 Starting Unified Entropy Gallery on port" port)
    (log/info "📊 Available sources:" available-sources)
    (log/info "🌊 Available processes:" available-processes)
    (jetty/run-jetty unified-routes {:port port :join? true})))