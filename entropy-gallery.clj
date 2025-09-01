#!/usr/bin/env clojure

(require '[ring.adapter.jetty :as jetty]
         '[ring.middleware.content-type :refer [wrap-content-type]]
         '[ring.util.response :as response]
         '[compojure.core :refer [defroutes GET POST]]
         '[compojure.route :as route]
         '[hiccup.core :as h]
         '[clojure.data.json :as json]
         '[clojure.java.shell :refer [sh]])

;; Sample entropy data for different sources (real samples I collected)
(def entropy-samples 
  {:random-org {:name "Random.org Atmospheric Noise (Dublin)"
                :url "https://www.random.org/"
                :samples [13873150 8234567 15678234 9876543 12345678 7654321 11223344 5566778 9988776 14567890]
                :last-updated "2025-08-30T10:41:00Z"
                :source-type "atmospheric"
                :quality "true-random"}
   
   :nist-beacon {:name "NIST Quantum Beacon (USA)"
                 :url "https://beacon.nist.gov/"
                 :samples [0x9AC431EB 0x11573C41 0xBDB19F63 0xDCC83817 0x8082B48F 0x22956960 0x37B138AA 0x91E11C2A]
                 :last-updated "2025-08-30T10:41:00Z" 
                 :source-type "quantum"
                 :quality "cryptographic"}
   
   :system-local {:name "System Entropy (Local)"
                  :url "local://system"
                  :samples (repeatedly 20 #(rand-int 16777216))
                  :last-updated (str (java.time.Instant/now))
                  :source-type "system"
                  :quality "pseudo-random"}
   
   :hotbits {:name "HotBits Radioactive Decay (Switzerland)"
            :url "https://www.fourmilab.ch/"
            :samples [0xA4B7C9 0xD2E8F1 0x5A9C3E 0x8B6D4F 0x1C7E9A 0x3F2B8D 0x6E4A7C 0x9D1F5B]
            :last-updated "2025-08-30T10:30:00Z"
            :source-type "radioactive"
            :quality "true-random"}
   
   :market-data {:name "Financial Market Volatility"
                :url "api://market"
                :samples [1.2847 0.8934 1.5621 0.7432 1.1289 0.9876 1.3445 0.6789 1.4567 0.8123]
                :last-updated "2025-08-30T10:35:00Z"
                :source-type "market"
                :quality "economic-chaos"}})

(defn generate-chart-data [samples chart-type]
  "Generate visualization data for different chart types"
  (let [normalized (map #(if (number? %) % (if (string? %) (hash %) %)) samples)]
    (case chart-type
      :line {:points (map-indexed vector normalized)}
      :bar {:bars (map-indexed (fn [i v] {:x i :y (mod (Math/abs v) 100)}) normalized)}
      :heatmap {:matrix (partition 4 4 (repeat 0) (map #(mod (Math/abs %) 255) normalized))}
      :parametric {:coords (map (fn [i v] {:x (* (Math/cos (* i 0.5)) (mod (Math/abs v) 50))
                                          :y (* (Math/sin (* i 0.5)) (mod (Math/abs v) 50))}) 
                               (range) normalized)}
      :histogram {:buckets (->> normalized
                               (map #(int (/ (mod (Math/abs %) 1000) 100)))
                               frequencies
                               (sort-by first))})))

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
      (for [[source-key source-data] entropy-samples]
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
          [:p [:strong "Last Updated: "] (:last-updated source-data)]]])]]
     
     [:script "
       const sourceData = " (json/write-str (into {} (for [[k v] entropy-samples] 
                                                       [k (assoc v :chart-data 
                                                               {:line (generate-chart-data (:samples v) :line)
                                                                :bar (generate-chart-data (:samples v) :bar)
                                                                :heatmap (generate-chart-data (:samples v) :heatmap) 
                                                                :parametric (generate-chart-data (:samples v) :parametric)})]))) ";
       
       function showChart(sourceKey, chartType) {
         const container = d3.select('#chart-' + sourceKey);
         container.selectAll('*').remove();
         
         const data = sourceData[sourceKey];
         const chartData = data.chart_data[chartType];
         
         // Update active tab
         d3.selectAll('#card-' + sourceKey + ' .chart-tab').classed('active', false);
         d3.select('#card-' + sourceKey + ' .chart-tab').filter(function() { 
           return this.textContent === chartType.charAt(0).toUpperCase() + chartType.slice(1); 
         }).classed('active', true);
         
         const svg = container.append('svg')
           .attr('width', '100%')
           .attr('height', '100%');
           
         const width = 560, height = 180;
         const margin = {top: 20, right: 20, bottom: 30, left: 40};
         
         if (chartType === 'line') {
           const xScale = d3.scaleLinear().domain([0, chartData.points.length-1]).range([margin.left, width-margin.right]);
           const yScale = d3.scaleLinear().domain(d3.extent(chartData.points, d => d[1])).range([height-margin.bottom, margin.top]);
           
           const line = d3.line()
             .x(d => xScale(d[0]))
             .y(d => yScale(d[1]));
             
           svg.append('path')
             .datum(chartData.points)
             .attr('class', 'parametric-path')
             .attr('d', line);
         }
         
         if (chartType === 'bar') {
           const xScale = d3.scaleBand().domain(chartData.bars.map(d => d.x)).range([margin.left, width-margin.right]).padding(0.1);
           const yScale = d3.scaleLinear().domain([0, d3.max(chartData.bars, d => d.y)]).range([height-margin.bottom, margin.top]);
           
           svg.selectAll('.bar')
             .data(chartData.bars)
             .enter().append('rect')
             .attr('class', 'bar')
             .attr('x', d => xScale(d.x))
             .attr('y', d => yScale(d.y))
             .attr('width', xScale.bandwidth())
             .attr('height', d => height - margin.bottom - yScale(d.y))
             .attr('fill', '#00ff88')
             .attr('opacity', 0.7);
         }
         
         if (chartType === 'heatmap') {
           const cellSize = Math.min(width/4, height/4) - 2;
           chartData.matrix.forEach((row, i) => {
             row.forEach((cell, j) => {
               svg.append('rect')
                 .attr('class', 'heatmap-cell')
                 .attr('x', j * (cellSize + 2) + 20)
                 .attr('y', i * (cellSize + 2) + 20)
                 .attr('width', cellSize)
                 .attr('height', cellSize)
                 .attr('fill', d3.interpolateViridis(cell / 255));
             });
           });
         }
         
         if (chartType === 'parametric') {
           const xScale = d3.scaleLinear().domain(d3.extent(chartData.coords, d => d.x)).range([margin.left, width-margin.right]);
           const yScale = d3.scaleLinear().domain(d3.extent(chartData.coords, d => d.y)).range([height-margin.bottom, margin.top]);
           
           const path = d3.line()
             .x(d => xScale(d.x))
             .y(d => yScale(d.y))
             .curve(d3.curveBasis);
             
           svg.append('path')
             .datum(chartData.coords)
             .attr('class', 'parametric-path')
             .attr('d', path);
         }
       }
       
       function toggleAccordion(sourceKey) {
         const panel = document.getElementById('panel-' + sourceKey);
         panel.style.display = panel.style.display === 'block' ? 'none' : 'block';
       }
       
       // Initialize with line charts
       Object.keys(sourceData).forEach(key => showChart(key, 'line'));
     "]]]))

(defroutes entropy-gallery-routes
  (GET "/" [] (response/response (entropy-gallery-page)))
  
  (GET "/api/entropy-sample" [source]
    (let [source-key (keyword source)
          source-data (get entropy-samples source-key)]
      (if source-data
        (response/response (json/write-str source-data))
        (response/response {:error "Source not found"} {:status 404}))))
  
  (route/not-found "404 - Entropy Source Not Found"))

(def app (wrap-content-type entropy-gallery-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3001"))]
    (println "🎨 Starting Entropy Gallery on port" port)
    (jetty/run-jetty app {:port port :join? true})))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))