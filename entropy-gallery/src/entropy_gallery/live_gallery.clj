(ns entropy-gallery.live-gallery
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [hiccup.core :as h]
            [cheshire.core :as cheshire]
            [entropy-gallery.working-sources :as sources])
  (:import [java.util.concurrent ConcurrentHashMap]))

;; Simple entropy cache
(def entropy-cache (ConcurrentHashMap.))

(defn cache-get [source]
  (let [data (.get entropy-cache source)]
    (when (and data (< (- (System/currentTimeMillis) (:timestamp data)) 30000))
      data)))

(defn cache-put [source data]
  (.put entropy-cache source (assoc data :timestamp (System/currentTimeMillis))))

(defn get-entropy [source]
  (or (cache-get source)
      (let [data (case source
                   "random-org" (sources/fetch-random-org)
                   "system-secure" (sources/fetch-system-entropy)
                   "timing-jitter" (sources/fetch-timing-jitter)
                   "memory-entropy" (sources/fetch-memory-entropy)
                   "network-latency" (sources/fetch-network-latency)
                   "thread-chaos" (sources/fetch-thread-chaos)
                   "gc-entropy" (sources/fetch-gc-entropy)
                   "mouse-entropy" (sources/fetch-mouse-entropy)
                   "filesystem-entropy" (sources/fetch-filesystem-entropy)
                   "process-entropy" (sources/fetch-process-entropy)
                   (sources/fetch-system-entropy))]
        (cache-put source data)
        data)))

(defn main-page []
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "🌊 Live Entropy Gallery - Working Sources Only"]
     [:script {:src "https://d3js.org/d3.v7.min.js"}]
     [:style "
       body { background: #0a0a0a; color: #00ff88; font-family: 'Courier New', monospace; 
              padding: 20px; line-height: 1.4; }
       .container { max-width: 1400px; margin: 0 auto; }
       h1 { text-align: center; color: #00ffff; text-shadow: 0 0 20px #00ffff; 
            margin-bottom: 20px; }
       
       .status-bar { background: rgba(0,50,50,0.8); padding: 15px; border-radius: 8px; 
                    margin: 20px 0; display: flex; justify-content: space-between; 
                    align-items: center; border: 1px solid #004444; }
       .status-left { display: flex; gap: 20px; align-items: center; }
       .status-indicator { width: 12px; height: 12px; border-radius: 50%; 
                          background: #ff4444; animation: pulse 2s infinite; }
       .status-indicator.active { background: #00ff88; }
       .status-indicator.warning { background: #ffaa00; }
       .status-text { font-size: 0.9em; }
       .live-counter { font-size: 0.8em; color: #888; }
       @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
       
       .controls { display: flex; justify-content: center; gap: 15px; margin: 25px 0; 
                  flex-wrap: wrap; }
       button { background: #003333; color: #00ff88; border: 1px solid #00ff88; 
               padding: 12px 20px; border-radius: 6px; cursor: pointer; 
               font-family: inherit; transition: all 0.3s; position: relative; }
       button:hover { background: #00ff88; color: #003333; transform: translateY(-2px); 
                     box-shadow: 0 4px 8px rgba(0,255,136,0.3); }
       button:active { transform: translateY(0); }
       button:disabled { opacity: 0.5; cursor: not-allowed; }
       
       .sources { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); 
                 gap: 25px; margin: 30px 0; }
       .source { background: rgba(0,40,40,0.3); padding: 20px; border-radius: 12px; 
                border: 1px solid #004444; transition: all 0.3s; cursor: pointer;
                position: relative; }
       .source:hover { border-color: #00ff88; transform: translateY(-3px);
                      box-shadow: 0 8px 16px rgba(0,255,136,0.2); }
       .source.fetching { border-color: #ffaa00; }
       .source.error { border-color: #ff4444; }
       .source.success { border-color: #00ff88; }
       
       .source-header { display: flex; justify-content: space-between; align-items: center; 
                       margin-bottom: 15px; }
       h3 { color: #00ffff; font-size: 1.1em; margin: 0; }
       .source-status { font-size: 0.7em; padding: 4px 8px; border-radius: 4px; 
                       text-transform: uppercase; font-weight: bold; }
       .status-live { background: #00ff88; color: #003333; }
       .status-cached { background: #ffaa00; color: #000; }
       .status-error { background: #ff4444; color: #fff; }
       .status-fetching { background: #0088ff; color: #fff; animation: pulse 1s infinite; }
       
       .chart { height: 200px; background: rgba(0,0,0,0.5); margin: 15px 0; 
               border: 1px solid #003333; border-radius: 8px; cursor: pointer;
               transition: all 0.3s; position: relative; }
       .chart:hover { border-color: #00ff88; background: rgba(0,255,136,0.05); }
       
       .info { font-size: 0.85em; color: #999; margin: 12px 0; line-height: 1.6; }
       .diagnostics { background: rgba(0,20,20,0.5); padding: 12px; 
                     border-radius: 6px; margin: 10px 0; border-left: 3px solid #00ff88;
                     transition: all 0.3s; }
       .diagnostics:hover { background: rgba(0,30,30,0.7); }
       
       .live-indicator { position: fixed; top: 20px; right: 20px; 
                        background: rgba(0,50,50,0.9); padding: 10px; 
                        border-radius: 6px; border: 1px solid #00ff88; 
                        font-size: 0.8em; z-index: 1000; }
       .live-stats { margin-top: 5px; font-size: 0.7em; color: #888; }
       
       .quality-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.75em;
                       font-weight: bold; text-transform: uppercase; }
       .true-random { background: #004400; color: #00ff00; }
       .cryptographic-random { background: #004488; color: #0088ff; }
       .hardware-timing { background: #440044; color: #ff00ff; }
       .system-dependent { background: #444400; color: #ffff00; }
       .network-dependent { background: #004444; color: #00ffff; }
       .scheduler-dependent { background: #440000; color: #ff4444; }
       .jvm-dependent { background: #402040; color: #ff80ff; }
       .simulated-input { background: #404020; color: #ffff80; }
       .filesystem-dependent { background: #204040; color: #80ffff; }
       .process-dependent { background: #404000; color: #ffff00; }
       .fallback-pseudo { background: #440000; color: #ff4444; }
     "]]
    [:body
     [:div.container
      [:h1 "🌊 Live Entropy Gallery"]
      [:div.subtitle "Real-time entropy from verified working sources"]
      
      [:div.status-bar
       [:div.status-left
        [:div.status-indicator {:id "global-status"}]
        [:div.status-text {:id "global-status-text"} "Initializing..."]]
       [:div.live-counter {:id "live-counter"} "Updates: 0"]]
      
      [:div.live-indicator {:id "live-indicator"}
       [:div "🔴 LIVE Gallery"]
       [:div.live-stats {:id "live-stats"} "0 active / 0 errors"]]
      
      [:div.controls
       [:button {:onclick "refreshAll()" :id "refresh-btn"} "🔄 Refresh All"]
       [:button {:onclick "toggleLiveMode()" :id "live-btn"} "⚡ Live Mode: OFF"]
       [:button {:onclick "clearCache()" :id "clear-btn"} "🗑️ Clear Cache"]]
      
      [:div.sources
       [:div.source {:id "source-random-org" :onclick "refreshSource('random-org')"}
        [:div.source-header
         [:h3 "🌪️ Random.org Atmospheric Noise"]
         [:div.source-status {:id "status-random-org"} "READY"]]
        [:div.chart {:id "chart-random-org"}]
        [:div.info {:id "info-random-org"} "True atmospheric radio noise from random.org..."]
        [:div.diagnostics {:id "diagnostics-random-org"}]]
       
       [:div.source {:id "source-system-secure" :onclick "refreshSource('system-secure')"}
        [:div.source-header
         [:h3 "🔧 System SecureRandom"]
         [:div.source-status {:id "status-system-secure"} "READY"]]
        [:div.chart {:id "chart-system-secure"}]
        [:div.info {:id "info-system-secure"} "OS entropy pool with cryptographically secure generation..."]
        [:div.diagnostics {:id "diagnostics-system-secure"}]]
       
       [:div.source {:id "source-timing-jitter" :onclick "refreshSource('timing-jitter')"}
        [:div.source-header
         [:h3 "⏱️ CPU Timing Jitter"]
         [:div.source-status {:id "status-timing-jitter"} "READY"]]
        [:div.chart {:id "chart-timing-jitter"}]
        [:div.info {:id "info-timing-jitter"} "Nanosecond CPU timing variations and scheduler chaos..."]
        [:div.diagnostics {:id "diagnostics-timing-jitter"}]]
       
       [:div.source {:id "source-memory-entropy" :onclick "refreshSource('memory-entropy')"}
        [:div.source-header
         [:h3 "🧠 Memory Allocation Entropy"]
         [:div.source-status {:id "status-memory-entropy"} "READY"]]
        [:div.chart {:id "chart-memory-entropy"}]
        [:div.info {:id "info-memory-entropy"} "Memory allocation patterns and address randomization..."]
        [:div.diagnostics {:id "diagnostics-memory-entropy"}]]
       
       [:div.source {:id "source-network-latency" :onclick "refreshSource('network-latency')"}
        [:div.source-header
         [:h3 "🌐 Network Latency Chaos"]
         [:div.source-status {:id "status-network-latency"} "READY"]]
        [:div.chart {:id "chart-network-latency"}]
        [:div.info {:id "info-network-latency"} "Local network timing variations..."]
        [:div.diagnostics {:id "diagnostics-network-latency"}]]
       
       [:div.source {:id "source-thread-chaos" :onclick "refreshSource('thread-chaos')"}
        [:div.source-header
         [:h3 "🔀 Thread Scheduling Chaos"]
         [:div.source-status {:id "status-thread-chaos"} "READY"]]
        [:div.chart {:id "chart-thread-chaos"}]
        [:div.info {:id "info-thread-chaos"} "Concurrent thread racing and scheduling chaos..."]
        [:div.diagnostics {:id "diagnostics-thread-chaos"}]]
       
       [:div.source {:id "source-gc-entropy" :onclick "refreshSource('gc-entropy')"}
        [:div.source-header
         [:h3 "♻️ Garbage Collection Timing"]
         [:div.source-status {:id "status-gc-entropy"} "READY"]]
        [:div.chart {:id "chart-gc-entropy"}]
        [:div.info {:id "info-gc-entropy"} "JVM garbage collection timing and memory changes..."]
        [:div.diagnostics {:id "diagnostics-gc-entropy"}]]
       
       [:div.source {:id "source-mouse-entropy" :onclick "refreshSource('mouse-entropy')"}
        [:div.source-header
         [:h3 "🖱️ User Input Simulation"]
         [:div.source-status {:id "status-mouse-entropy"} "READY"]]
        [:div.chart {:id "chart-mouse-entropy"}]
        [:div.info {:id "info-mouse-entropy"} "Simulated user input timing patterns..."]
        [:div.diagnostics {:id "diagnostics-mouse-entropy"}]]
       
       [:div.source {:id "source-filesystem-entropy" :onclick "refreshSource('filesystem-entropy')"}
        [:div.source-header
         [:h3 "📁 File System Timing"]
         [:div.source-status {:id "status-filesystem-entropy"} "READY"]]
        [:div.chart {:id "chart-filesystem-entropy"}]
        [:div.info {:id "info-filesystem-entropy"} "File system operation timing variations..."]
        [:div.diagnostics {:id "diagnostics-filesystem-entropy"}]]
       
       [:div.source {:id "source-process-entropy" :onclick "refreshSource('process-entropy')"}
        [:div.source-header
         [:h3 "🔄 Process Information Mix"]
         [:div.source-status {:id "status-process-entropy"} "READY"]]
        [:div.chart {:id "chart-process-entropy"}]
        [:div.info {:id "info-process-entropy"} "Process ID and state information mixing..."]
        [:div.diagnostics {:id "diagnostics-process-entropy"}]]]]
     
     [:script "
       let entropyData = {};
       let liveMode = false;
       let liveInterval = null;
       let updateCounter = 0;
       let activeRequests = 0;
       let errorCount = 0;
       let sourceStates = {};
       
       const sources = ['random-org', 'system-secure', 'timing-jitter', 'memory-entropy', 
                       'network-latency', 'thread-chaos', 'gc-entropy', 'mouse-entropy',
                       'filesystem-entropy', 'process-entropy'];
                       
       sources.forEach(source => {
         sourceStates[source] = { status: 'ready', lastFetch: 0, errorCount: 0, lastError: null };
       });
       
       function updateGlobalStatus() {
         const indicator = document.getElementById('global-status');
         const statusText = document.getElementById('global-status-text');
         const liveStats = document.getElementById('live-stats');
         const counter = document.getElementById('live-counter');
         
         if (activeRequests > 0) {
           indicator.className = 'status-indicator warning';
           statusText.textContent = `Fetching from ${activeRequests} source${activeRequests > 1 ? 's' : ''}...`;
         } else if (errorCount > 0) {
           indicator.className = 'status-indicator';
           statusText.textContent = `${errorCount} source${errorCount > 1 ? 's have' : ' has'} errors`;
         } else {
           indicator.className = 'status-indicator active';
           statusText.textContent = liveMode ? 'Live mode active' : 'All sources ready';
         }
         
         counter.textContent = `Updates: ${updateCounter}`;
         liveStats.textContent = `${activeRequests} active / ${errorCount} errors`;
       }
       
       function setSourceStatus(source, status, message) {
         const statusEl = document.getElementById(`status-${source}`);
         const sourceEl = document.getElementById(`source-${source}`);
         
         sourceStates[source].status = status;
         sourceStates[source].lastFetch = Date.now();
         
         statusEl.className = `source-status status-${status}`;
         statusEl.textContent = status.toUpperCase();
         
         sourceEl.className = sourceEl.className.replace(/\\b(fetching|error|success)\\b/g, '');
         sourceEl.classList.add(status);
         
         if (status === 'error') {
           sourceStates[source].errorCount++;
           sourceStates[source].lastError = message || 'Unknown error';
           errorCount++;
         } else if (status === 'success') {
           sourceStates[source].errorCount = 0;
           sourceStates[source].lastError = null;
           if (errorCount > 0) errorCount--;
         }
         
         updateGlobalStatus();
       }
       
       function fetchEntropy(source) {
         activeRequests++;
         setSourceStatus(source, 'fetching');
         updateGlobalStatus();
         
         fetch('/api/entropy?source=' + source)
           .then(r => r.json())
           .then(data => {
             entropyData[source] = data;
             updateInfo(source, data);
             drawVisualization(source, data.samples);
             setSourceStatus(source, 'success');
             updateCounter++;
             activeRequests--;
             updateGlobalStatus();
           })
           .catch(err => {
             setSourceStatus(source, 'error', err.message);
             activeRequests--;
             updateGlobalStatus();
           });
       }
       
       function updateInfo(source, data) {
         const info = document.getElementById('info-' + source);
         const diagnostics = document.getElementById('diagnostics-' + source);
         
         if (info) {
           info.innerHTML = `
             <strong>Quality:</strong> <span class='quality-badge ${data.quality}'>${data.quality}</span><br/>
             <strong>Source:</strong> ${data.source}<br/>
             <strong>Method:</strong> ${data.method}<br/>
             <strong>Samples:</strong> ${data.samples.length} entropy values<br/>
             <strong>Entropy/Sample:</strong> ${data['entropy-bits-per-sample'] || 'Unknown'} bits<br/>
             <strong>Collection:</strong> ${data['collection-method'] || 'Standard'}<br/>
             <strong>Limitations:</strong> <em>${data.limitations || 'None specified'}</em>
           `;
         }
         
         if (diagnostics && data.diagnostics) {
           diagnostics.innerHTML = `
             <div><strong>📊 Real-time Diagnostics:</strong></div>
             <div>⏱️ Response Time: <span style='color:#00ff88'>${data.diagnostics['response-time']}ms</span></div>
             <div>📈 Status: <span style='color:${data.diagnostics.status === 'success' ? '#00ff88' : '#ff4444'}'>${data.diagnostics.status}</span></div>
             <div>🔢 Sample Count: ${data.diagnostics['sample-count']}</div>
             <div>🕒 Last Updated: ${new Date(data.diagnostics.timestamp).toLocaleTimeString()}</div>
             ${data.diagnostics.error ? '<div style=\"color:#ff4444\">❌ Error: ' + data.diagnostics.error + '</div>' : ''}
           `;
         }
       }
       
       function drawVisualization(source, samples) {
         const container = d3.select('#chart-' + source);
         container.selectAll('*').remove();
         
         const svg = container.append('svg')
           .attr('width', '100%').attr('height', '100%')
           .attr('viewBox', '0 0 380 180');
         
         // Enhanced bar chart with animations
         const margin = {top: 20, right: 20, bottom: 30, left: 40};
         const width = 380 - margin.left - margin.right;
         const height = 180 - margin.top - margin.bottom;
         
         const x = d3.scaleBand()
           .domain(d3.range(Math.min(samples.length, 20)))
           .range([margin.left, width])
           .padding(0.1);
         
         const y = d3.scaleLinear()
           .domain([0, d3.max(samples.slice(0, 20))])
           .range([height, margin.top]);
         
         svg.selectAll('rect')
           .data(samples.slice(0, 20))
           .enter().append('rect')
           .attr('x', (d, i) => x(i))
           .attr('y', height)
           .attr('width', x.bandwidth())
           .attr('height', 0)
           .attr('fill', (d, i) => `hsl(${(d % 360)}, 70%, 60%)`)
           .attr('opacity', 0.8)
           .transition()
           .duration(1000)
           .delay((d, i) => i * 50)
           .attr('y', d => y(d))
           .attr('height', d => height - y(d));
           
         // Add value labels
         svg.selectAll('text')
           .data(samples.slice(0, 10))
           .enter().append('text')
           .attr('x', (d, i) => x(i) + x.bandwidth()/2)
           .attr('y', d => y(d) - 5)
           .attr('text-anchor', 'middle')
           .attr('font-size', '8px')
           .attr('fill', '#00ff88')
           .text(d => d.toString(16).substr(-3));
       }
       
       function refreshSource(source) {
         fetchEntropy(source);
       }
       
       function refreshAll() {
         sources.forEach(source => fetchEntropy(source));
       }
       
       function toggleLiveMode() {
         liveMode = !liveMode;
         const btn = document.getElementById('live-btn');
         
         if (liveMode) {
           btn.textContent = '⚡ Live Mode: ON';
           liveInterval = setInterval(refreshAll, 10000); // Refresh every 10 seconds
         } else {
           btn.textContent = '⚡ Live Mode: OFF';
           if (liveInterval) {
             clearInterval(liveInterval);
             liveInterval = null;
           }
         }
         updateGlobalStatus();
       }
       
       function clearCache() {
         fetch('/api/clear-cache', {method: 'POST'})
           .then(() => {
             document.getElementById('global-status-text').textContent = 'Cache cleared';
             setTimeout(refreshAll, 1000);
           });
       }
       
       // Initialize
       setTimeout(() => {
         updateGlobalStatus();
         refreshAll();
       }, 500);
     "]]]]))

(defroutes app-routes
  (GET "/" [] 
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (main-page)})
  
  (GET "/api/entropy" [source]
    (let [data (get-entropy source)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (cheshire/generate-string data)}))
  
  (POST "/api/clear-cache" []
    (.clear entropy-cache)
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (cheshire/generate-string {:status "cache-cleared"})})
  
  (route/not-found "Not Found"))

(def app (wrap-params app-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3002"))]
    (println "Starting Live Entropy Gallery on port" port)
    (jetty/run-jetty app {:port port :join? true})))