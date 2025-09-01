(ns entropy-gallery.monitoring.flamechart
  (:require [clojure.data.json :as json]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [hiccup.core :as h]))

;; Flamechart monitoring integration for entropy processes
(def monitoring-data (atom {}))

(defn record-entropy-event [source-key event-type duration-ms data]
  "Record entropy harvesting events for flamechart visualization"
  (let [event {:timestamp (System/currentTimeMillis)
               :source source-key
               :event-type event-type
               :duration-ms duration-ms
               :data-size (count (str data))
               :thread-id (.getId (Thread/currentThread))}]
    (swap! monitoring-data update :events (fnil conj []) event)))

(defn get-monitoring-data []
  "Get current monitoring data for flamechart visualization"
  @monitoring-data)

(defn flamechart-visualization []
  "Generate flamechart HTML for entropy monitoring"
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "Entropy Flamechart Monitoring"]
     [:script {:src "https://d3js.org/d3.v7.min.js"}]
     [:style "
       body { background: #1a1a1a; color: #00ff88; font-family: monospace; }
       .flamechart { width: 100%; height: 400px; border: 1px solid #004444; }
       .flame-rect { stroke: #001111; stroke-width: 1; cursor: pointer; }
       .tooltip { position: absolute; background: #003333; padding: 8px; 
                 border: 1px solid #00ff88; border-radius: 4px; pointer-events: none; }
     "]]
    [:body
     [:h1 "🔥 Entropy Process Flamechart"]
     [:div.flamechart {:id "flamechart-container"}]
     [:script (str "
       const monitoringData = " (json/write-str (get-monitoring-data)) ";
       
       function renderFlamechart() {
         const container = d3.select('#flamechart-container');
         const width = 800, height = 400;
         const svg = container.append('svg')
           .attr('width', width)
           .attr('height', height);
         
         // Group events by thread and stack them
         const events = monitoringData.events || [];
         const threadGroups = d3.group(events, d => d.thread_id);
         
         const colorScale = d3.scaleOrdinal(d3.schemeCategory10);
         
         let y = 0;
         threadGroups.forEach((threadEvents, threadId) => {
           threadEvents.forEach((event, i) => {
             const rect = svg.append('rect')
               .attr('class', 'flame-rect')
               .attr('x', (event.timestamp % 10000) / 10)
               .attr('y', y)
               .attr('width', Math.max(2, event.duration_ms / 10))
               .attr('height', 20)
               .attr('fill', colorScale(event.source))
               .on('mouseover', function() {
                 const tooltip = d3.select('body').append('div')
                   .attr('class', 'tooltip')
                   .style('left', d3.event.pageX + 'px')
                   .style('top', d3.event.pageY + 'px')
                   .html(`Source: ${event.source}<br/>
                          Type: ${event.event_type}<br/>
                          Duration: ${event.duration_ms}ms<br/>
                          Data Size: ${event.data_size} bytes`);
               })
               .on('mouseout', function() {
                 d3.selectAll('.tooltip').remove();
               });
           });
           y += 25;
         });
       }
       
       renderFlamechart();
     ")]]]))

(defroutes monitoring-routes
  (GET "/monitoring" [] (response/response (flamechart-visualization)))
  (GET "/api/monitoring-data" [] 
    (response/response (json/write-str (get-monitoring-data))))
  (POST "/api/record-event" [source event-type duration data]
    (record-entropy-event (keyword source) event-type 
                          (Integer/parseInt duration) data)
    (response/response {:status "recorded"})))