(ns entropy-gallery.simple-working
  "Minimal working entropy gallery - properly plumbed"
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.core :as h]
            [clojure.data.json :as json])
  (:gen-class))

;; Simple working entropy storage
(def entropy-data (atom {}))

;; Generate basic entropy
(defn generate-entropy [source-type]
  (case source-type
    :random-org {:name "Random.org (Simulated)"
                 :samples (repeatedly 10 #(rand-int 16777216))
                 :quality "true-random"
                 :timestamp (System/currentTimeMillis)}
    :system {:name "System Entropy"
             :samples (repeatedly 10 #(rand-int 16777216))
             :quality "pseudo-random"
             :timestamp (System/currentTimeMillis)}
    {:name "Unknown" :samples [] :quality "unknown"}))

;; Lévy flight implementation
(defn levy-flight [n alpha scale]
  (let [steps (repeatedly n 
                #(let [u (rand)
                       v (* Math/PI (- (rand) 0.5))]
                   (* scale (/ (Math/sin (* alpha v))
                              (Math/pow (Math/cos v) (/ 1.0 alpha))))))]
    (reductions + 0 steps)))

;; Simple page
(defn main-page []
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "Working Entropy Gallery"]
     [:script {:src "https://d3js.org/d3.v7.min.js"}]
     [:style "
       body { background: #0a0a0a; color: #00ff88; font-family: monospace; padding: 20px; }
       .container { max-width: 1200px; margin: 0 auto; }
       .source { border: 1px solid #00ff88; padding: 15px; margin: 20px 0; }
       .chart { height: 200px; background: #001111; margin: 10px 0; }
       button { background: #00ff88; color: #000; border: none; padding: 10px 20px; 
                cursor: pointer; margin: 5px; }
     "]]
    [:body
     [:div.container
      [:h1 "Working Entropy Gallery"]
      [:div
       [:button {:onclick "fetchEntropy('random-org')"} "Get Random.org"]
       [:button {:onclick "fetchEntropy('system')"} "Get System"]
       [:button {:onclick "generateLevy()"} "Generate Lévy Flight"]]
      [:div#random-org.source
       [:h3 "Random.org"]
       [:div.chart {:id "chart-random-org"}]
       [:div#data-random-org "No data"]]
      [:div#system.source
       [:h3 "System Entropy"]
       [:div.chart {:id "chart-system"}]
       [:div#data-system "No data"]]]
     
     [:script "
       function fetchEntropy(source) {
         fetch('/api/entropy?source=' + source)
           .then(r => r.json())
           .then(data => {
             document.getElementById('data-' + source).textContent = 
               'Samples: ' + data.samples.slice(0, 5).join(', ') + '...';
             drawChart(source, data.samples);
           });
       }
       
       function drawChart(source, samples) {
         const container = d3.select('#chart-' + source);
         container.selectAll('*').remove();
         
         const svg = container.append('svg')
           .attr('width', '100%').attr('height', '100%')
           .attr('viewBox', '0 0 800 200');
         
         const xScale = d3.scaleLinear()
           .domain([0, samples.length - 1])
           .range([20, 780]);
         
         const yScale = d3.scaleLinear()
           .domain([0, Math.max(...samples)])
           .range([180, 20]);
         
         svg.selectAll('rect')
           .data(samples)
           .enter().append('rect')
           .attr('x', (d, i) => xScale(i))
           .attr('y', d => yScale(d))
           .attr('width', 780 / samples.length - 2)
           .attr('height', d => 180 - yScale(d))
           .attr('fill', '#00ff88')
           .attr('opacity', 0.7);
       }
       
       function generateLevy() {
         fetch('/api/levy')
           .then(r => r.json())
           .then(data => {
             const container = d3.select('#chart-system');
             container.selectAll('*').remove();
             
             const svg = container.append('svg')
               .attr('width', '100%').attr('height', '100%')
               .attr('viewBox', '0 0 800 200');
             
             const xScale = d3.scaleLinear()
               .domain([0, data.path.length - 1])
               .range([20, 780]);
             
             const yScale = d3.scaleLinear()
               .domain(d3.extent(data.path))
               .range([180, 20]);
             
             const line = d3.line()
               .x((d, i) => xScale(i))
               .y(d => yScale(d));
             
             svg.append('path')
               .datum(data.path)
               .attr('fill', 'none')
               .attr('stroke', '#00ff88')
               .attr('stroke-width', 2)
               .attr('d', line);
           });
       }
       
       // Auto-load on start
       setTimeout(() => {
         fetchEntropy('random-org');
         fetchEntropy('system');
       }, 500);
     "]]]))

(defroutes app-routes
  (GET "/" [] 
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (main-page)})
  
  (GET "/api/entropy" [source]
    (let [source-key (keyword source)
          data (generate-entropy source-key)]
      (swap! entropy-data assoc source-key data)
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str data)}))
  
  (GET "/api/levy" []
    (let [path (levy-flight 100 1.5 10.0)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:path path :alpha 1.5})}))
  
  (route/not-found "Not Found"))

(def app (wrap-params app-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3002"))]
    (println "Starting Simple Working Entropy Gallery on port" port)
    (jetty/run-jetty app {:port port :join? true})))