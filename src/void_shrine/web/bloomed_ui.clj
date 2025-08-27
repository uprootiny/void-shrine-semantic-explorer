(ns void-shrine.web.bloomed-ui
  (:require [void-shrine.chaos.bloomed-traversal :as traversal]
            [void-shrine.chaos.enhanced-manifestations :as manifestations]
            [void-shrine.chaos.bloomed-ontology :as ontology]
            [hiccup.core :as h]
            [clojure.string :as str]))

(defn void-fractal-visualization
  "Generate SVG visualization of void fractal structure"
  [fractal-paths canvas-width canvas-height]
  [:svg
   {:width canvas-width
    :height canvas-height
    :class "void-fractal-canvas"
    :viewBox (str "0 0 " canvas-width " " canvas-height)}
   
   [:defs
    [:radialGradient {:id "void-gradient"}
     [:stop {:offset "0%" :stop-color "#000000"}]
     [:stop {:offset "30%" :stop-color "#1a001a"}]
     [:stop {:offset "60%" :stop-color "#330033"}]
     [:stop {:offset "100%" :stop-color "#ff0066"}]]
    [:filter {:id "void-glow"}
     [:feGaussianBlur {:stdDeviation "3" :result "coloredBlur"}]
     [:feMerge
      [:feMergeNode {:in "coloredBlur"}]
      [:feMergeNode {:in "SourceGraphic"}]]]]
   
   ;; Central void
   [:circle
    {:cx (/ canvas-width 2)
     :cy (/ canvas-height 2)
     :r 50
     :fill "url(#void-gradient)"
     :filter "url(#void-glow)"
     :class "central-void"}]
   
   ;; Fractal branches
   (for [[idx path-data] (map-indexed vector (take 50 fractal-paths))]
     (let [angle (* idx (/ 360 (count fractal-paths)))
           radius (+ 80 (* (:depth path-data) 30))
           x (+ (/ canvas-width 2) (* radius (Math/cos (Math/toRadians angle))))
           y (+ (/ canvas-height 2) (* radius (Math/sin (Math/toRadians angle))))
           node-size (max 3 (- 15 (* (:depth path-data) 2)))]
       [:g
        ;; Connection line
        [:line
         {:x1 (/ canvas-width 2)
          :y1 (/ canvas-height 2)
          :x2 x
          :y2 y
          :stroke "#ff0066"
          :stroke-width (/ (:void-resonance path-data) 200)
          :opacity 0.6}]
        ;; Void node
        [:circle
         {:cx x
          :cy y
          :r node-size
          :fill (entropy-to-color (:entropy path-data))
          :stroke "#ff0066"
          :stroke-width 1
          :opacity 0.8
          :class "void-node"}]
        ;; Path label
        [:text
         {:x x
          :y (+ y 20)
          :fill "#00ffff"
          :font-size "8"
          :text-anchor "middle"
          :class "void-label"}
         (str/join "→" (take 2 (map name (:path path-data))))]]))))

(defn void-constellation-map
  "Render constellation of related void concepts"
  [constellation canvas-width canvas-height]
  [:svg
   {:width canvas-width
    :height canvas-height
    :class "void-constellation"
    :viewBox (str "0 0 " canvas-width " " canvas-height)}
   
   ;; Background void field
   [:rect
    {:width "100%"
     :height "100%"
     :fill "url(#void-field-pattern)"}]
   
   [:defs
    [:pattern {:id "void-field-pattern" :patternUnits "userSpaceOnUse" :width "20" :height "20"}
     [:rect {:width "20" :height "20" :fill "#000011"}]
     [:circle {:cx "10" :cy "10" :r "0.5" :fill "#ff0066" :opacity "0.3"}]]]
   
   ;; Central concept
   (let [center-x (/ canvas-width 2)
         center-y (/ canvas-height 2)]
     [:g
      [:circle
       {:cx center-x
        :cy center-y
        :r 25
        :fill "#ff0066"
        :stroke "#00ffff"
        :stroke-width 2
        :class "central-concept"}]
      [:text
       {:x center-x
        :y center-y
        :text-anchor "middle"
        :dy "0.35em"
        :fill "#000000"
        :font-weight "bold"
        :font-size "10"}
       "CENTRAL"]])
   
   ;; Constellation points
   (for [star constellation]
     (let [angle-rad (Math/toRadians (:angle star))
           radius (+ 60 (* (:connection-strength star) 100))
           x (+ (/ canvas-width 2) (* radius (Math/cos angle-rad)))
           y (+ (/ canvas-height 2) (* radius (Math/sin angle-rad)))]
       [:g
        ;; Connection line
        [:line
         {:x1 (/ canvas-width 2)
          :y1 (/ canvas-height 2)
          :x2 x
          :y2 y
          :stroke "#ff0066"
          :stroke-width (* (:connection-strength star) 3)
          :opacity (* (:connection-strength star) 0.8)
          :stroke-dasharray (if (> (:connection-strength star) 0.5) "none" "2,2")}]
        ;; Star point
        [:circle
         {:cx x
          :cy y
          :r (+ 3 (* (:connection-strength star) 8))
          :fill (entropy-to-color (:void-harmony star))
          :stroke "#00ffff"
          :stroke-width 1
          :opacity 0.9
          :class "constellation-star"}]
        ;; Star label
        [:text
         {:x x
          :y (+ y 20)
          :fill "#00ffff"
          :font-size "7"
          :text-anchor "middle"
          :class "star-label"}
         (str/join "→" (take 2 (map name (:satellite-path star))))]]))])

(defn void-weather-display
  "Display current void weather conditions"
  [weather-system]
  [:div.void-weather-panel
   [:h3.weather-title "Void Weather Conditions"]
   
   [:div.weather-metrics
    [:div.weather-metric
     [:span.metric-name "Void Pressure: "]
     [:span.metric-value 
      {:style {:color (entropy-to-color (get-in weather-system [:void-pressure :level]))}}
      (get-in weather-system [:void-pressure :level])]]
    
    [:div.weather-metric
     [:span.metric-name "Existential Temperature: "]
     [:span.metric-value
      {:style {:color (entropy-to-color (get-in weather-system [:existential-temperature :level]))}}
      (get-in weather-system [:existential-temperature :level])]]
    
    [:div.weather-metric
     [:span.metric-name "Meaninglessness Humidity: "]
     [:span.metric-value
      {:style {:color (entropy-to-color (get-in weather-system [:meaninglessness-humidity :level]))}}
      (str (int (* (get-in weather-system [:meaninglessness-humidity :saturation]) 100)) "%")]]]
   
   [:div.void-storms
    [:h4 "Active Void Storms"]
    (for [[idx storm] (map-indexed vector (:void-storms weather-system))]
      [:div.storm-report {:key idx}
       [:div.storm-header
        "Storm #" (inc idx) " - Intensity: " (:intensity storm)]
       [:div.storm-details
        "Eye Location: " (str/join " → " (map name (:eye-location storm)))]
       [:div.storm-duration
        "Duration: " (:duration storm) " temporal units"]
       [:div.void-lightning
        "Lightning Strikes: " (count (:void-lightning storm))]])]])

(defn manifestation-symphony-display
  "Display complete manifestation symphony"
  [symphony]
  [:div.manifestation-symphony
   [:div.symphony-header
    [:h2.entity-name (get-in symphony [:identity :void-name])]
    [:div.classification
     "Classification: " (name (get-in symphony [:identity :classification]))]
    [:div.power-level
     "Power Level: " (get-in symphony [:identity :power-level])]
    [:div.entropy-signature
     "Entropy Signature: " (format "0x%X" (get-in symphony [:identity :entropy-signature]))]]
   
   [:div.narrative-section
    [:h3 "Primary Narrative"]
    [:div.primary-text
     (get-in symphony [:narrative :primary-narrative])]
    
    [:div.interpretations
     [:h4 "Alternative Interpretations"]
     (for [interpretation (get-in symphony [:narrative :secondary-interpretations])]
       [:div.interpretation
        [:strong (str (name (:style interpretation)) ": ")]
        [:span (:text interpretation)]])]]
   
   [:div.dimensional-properties
    [:h3 "Dimensional Properties"]
    [:div.dimensions-grid
     (for [dim (get-in symphony [:dimensional-properties :dimensions])]
       [:div.dimension
        [:div.dim-id "Dimension " (:dimension-id dim)]
        [:div.dim-axis "Primary: " (str/join " → " (map name (:primary-axis dim)))]
        [:div.dim-density "Void Density: " (:void-density dim)]])]]
   
   [:div.temporal-aspects
    [:h3 "Temporal Properties"]
    [:div.duration
     "Manifestation Duration: " (get-in symphony [:temporal-aspects :manifestation-duration]) " seconds"]
    [:div.decay-pattern
     "Decay Pattern: " (name (get-in symphony [:temporal-aspects :decay-pattern :type]))]
    [:div.echoes
     "Temporal Echoes: " (count (get-in symphony [:temporal-aspects :temporal-echoes]))]]
   
   [:div.interactive-section
    [:h3 "Interactive Elements"]
    [:div.triggers
     [:h4 "Trigger Conditions"]
     [:div "Entropy Range: " 
      (get-in symphony [:interactive-elements :trigger-conditions :entropy-thresholds :minimum])
      " - "
      (get-in symphony [:interactive-elements :trigger-conditions :entropy-thresholds :maximum])]]
    [:div.responses
     [:h4 "Response Patterns"]
     (for [response (get-in symphony [:interactive-elements :response-patterns])]
       [:div.response
        (name (:type response)) " (probability: " 
        (format "%.2f" (:probability response)) ")"])]]])

(defn bloomed-dashboard
  "Main dashboard for the bloomed void system"
  [state]
  (let [current-entropy (first (:entropy-values state))
        fractal-paths (when current-entropy
                       (traversal/fractal-void-descent current-entropy 6))
        constellation (when current-entropy
                       (traversal/void-constellation current-entropy 12))
        weather (when current-entropy
                 (traversal/void-weather-system current-entropy))
        symphony (when current-entropy
                  (manifestations/void-manifestation-symphony current-entropy))]
    [:div.bloomed-dashboard
     [:header.void-header
      [:h1 "∞ BLOOMED VOID SHRINE ∞"]
      [:div.subtitle "Infinite Fractal Ontology of Nothingness"]
      [:div.node-count 
       "Active Nodes: " (traversal/count-total-nodes ontology/infinite-void-tree)]]
     
     [:div.main-visualization-grid
      [:div.fractal-panel
       [:h3 "Void Fractal Structure"]
       (when fractal-paths
         (void-fractal-visualization fractal-paths 400 400))]
      
      [:div.constellation-panel
       [:h3 "Void Constellation"]
       (when constellation
         (void-constellation-map constellation 400 400))]
      
      [:div.weather-panel
       (when weather
         (void-weather-display weather))]]
     
     [:div.manifestation-panel
      (when symphony
        (manifestation-symphony-display symphony))]
     
     [:div.controls-enhanced
      [:button.chaos-btn
       {:onclick "fetch('/api/bloom-chaos', {method: 'POST'}); location.reload();"}
       "Bloom Chaos"]
      [:button.fractal-btn
       {:onclick "fetch('/api/fractal-descent', {method: 'POST'}); location.reload();"}
       "Fractal Descent"]
      [:button.constellation-btn
       {:onclick "fetch('/api/void-constellation', {method: 'POST'}); location.reload();"}
       "Void Constellation"]
      [:button.symphony-btn
       {:onclick "fetch('/api/manifestation-symphony', {method: 'POST'}); location.reload();"}
       "Manifestation Symphony"]]]))

(defn bloomed-main-page
  "Generate the main page HTML for bloomed system"
  [state]
  (str
   "<!DOCTYPE html>"
   (h/html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title "Bloomed Void Shrine - Infinite Chaos Ontology"]
      [:style (bloomed-void-css)]]
     [:body
      [:div#app
       (bloomed-dashboard state)]]])))

(defn bloomed-void-css
  "Enhanced CSS for the bloomed void system"
  []
  "
  body {
    background: radial-gradient(circle at center, #000011, #000000);
    color: #ff0066;
    font-family: 'Courier New', monospace;
    margin: 0;
    overflow-x: auto;
    overflow-y: auto;
  }
  
  .bloomed-dashboard {
    min-height: 100vh;
    padding: 1rem;
  }
  
  .void-header {
    text-align: center;
    padding: 2rem;
    background: linear-gradient(45deg, #000000, #1a0d1a, #330033, #1a0d1a, #000000);
    border-bottom: 3px solid #ff0066;
    margin-bottom: 2rem;
  }
  
  .void-header h1 {
    margin: 0;
    font-size: 4rem;
    text-shadow: 0 0 30px #ff0066, 0 0 60px #ff0066, 0 0 90px #ff0066;
    animation: infinite-pulse 3s infinite;
    background: linear-gradient(45deg, #ff0066, #00ffff, #ff0066);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  @keyframes infinite-pulse {
    0%, 100% { 
      text-shadow: 0 0 30px #ff0066, 0 0 60px #ff0066; 
      transform: scale(1);
    }
    33% { 
      text-shadow: 0 0 40px #00ffff, 0 0 80px #00ffff; 
      transform: scale(1.02);
    }
    66% { 
      text-shadow: 0 0 50px #ff0066, 0 0 100px #ff0066; 
      transform: scale(0.98);
    }
  }
  
  .node-count {
    font-size: 1.5rem;
    color: #00ffff;
    margin-top: 1rem;
  }
  
  .main-visualization-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto auto;
    gap: 2rem;
    margin-bottom: 2rem;
  }
  
  .fractal-panel, .constellation-panel, .weather-panel {
    background: rgba(255, 0, 102, 0.1);
    border: 2px solid #ff0066;
    border-radius: 10px;
    padding: 1rem;
    backdrop-filter: blur(5px);
  }
  
  .weather-panel {
    grid-column: 1 / -1;
  }
  
  .void-fractal-canvas, .void-constellation {
    width: 100%;
    height: auto;
    border: 1px solid #00ffff;
    border-radius: 5px;
    background: radial-gradient(circle, #001122, #000000);
  }
  
  .central-void {
    animation: void-pulse 4s infinite ease-in-out;
  }
  
  @keyframes void-pulse {
    0%, 100% { r: 50; }
    50% { r: 55; }
  }
  
  .void-node {
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .void-node:hover {
    r: 8;
    stroke-width: 2;
  }
  
  .void-weather-panel h3 {
    color: #00ffff;
    margin-bottom: 1rem;
  }
  
  .weather-metrics {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
    margin-bottom: 2rem;
  }
  
  .weather-metric {
    background: rgba(0, 255, 255, 0.1);
    padding: 0.5rem;
    border-radius: 5px;
    border: 1px solid #00ffff;
  }
  
  .storm-report {
    background: rgba(255, 0, 102, 0.2);
    margin: 0.5rem 0;
    padding: 0.5rem;
    border-left: 3px solid #ff0066;
    font-size: 0.9rem;
  }
  
  .manifestation-symphony {
    background: rgba(0, 0, 0, 0.8);
    border: 2px solid #ff0066;
    border-radius: 10px;
    padding: 2rem;
    margin: 2rem 0;
    box-shadow: 0 0 30px rgba(255, 0, 102, 0.3);
  }
  
  .entity-name {
    font-size: 2.5rem;
    color: #00ffff;
    text-shadow: 0 0 20px #00ffff;
    margin-bottom: 1rem;
  }
  
  .classification, .power-level, .entropy-signature {
    color: #ff0066;
    margin: 0.5rem 0;
    font-size: 1.1rem;
  }
  
  .narrative-section, .dimensional-properties, .temporal-aspects, .interactive-section {
    margin: 2rem 0;
    padding: 1rem;
    border: 1px solid #333;
    border-radius: 5px;
    background: rgba(255, 255, 255, 0.02);
  }
  
  .primary-text {
    font-size: 1.2rem;
    line-height: 1.6;
    color: #ffffff;
    margin: 1rem 0;
    padding: 1rem;
    background: rgba(255, 0, 102, 0.1);
    border-left: 4px solid #ff0066;
  }
  
  .interpretation {
    margin: 0.5rem 0;
    padding: 0.5rem;
    background: rgba(0, 255, 255, 0.05);
    border-radius: 3px;
  }
  
  .dimensions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
    margin: 1rem 0;
  }
  
  .dimension {
    background: rgba(255, 0, 102, 0.1);
    padding: 0.5rem;
    border: 1px solid #ff0066;
    border-radius: 3px;
  }
  
  .controls-enhanced {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1rem;
    margin: 2rem 0;
    padding: 2rem;
    background: rgba(0, 0, 0, 0.8);
    border: 2px solid #ff0066;
    border-radius: 10px;
  }
  
  .chaos-btn, .fractal-btn, .constellation-btn, .symphony-btn {
    padding: 1.5rem;
    background: transparent;
    border: 2px solid #ff0066;
    color: #ff0066;
    font-family: inherit;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 5px;
    text-transform: uppercase;
    font-weight: bold;
  }
  
  .chaos-btn:hover, .fractal-btn:hover, .constellation-btn:hover, .symphony-btn:hover {
    background: #ff0066;
    color: #000000;
    box-shadow: 0 0 30px #ff0066;
    transform: scale(1.05);
  }
  
  .fractal-btn { border-color: #00ffff; color: #00ffff; }
  .fractal-btn:hover { background: #00ffff; box-shadow: 0 0 30px #00ffff; }
  
  .constellation-btn { border-color: #ffff00; color: #ffff00; }
  .constellation-btn:hover { background: #ffff00; box-shadow: 0 0 30px #ffff00; }
  
  .symphony-btn { border-color: #ff00ff; color: #ff00ff; }
  .symphony-btn:hover { background: #ff00ff; box-shadow: 0 0 30px #ff00ff; }
  
  @media (max-width: 768px) {
    .main-visualization-grid {
      grid-template-columns: 1fr;
    }
    
    .controls-enhanced {
      grid-template-columns: 1fr;
    }
    
    .void-header h1 {
      font-size: 2.5rem;
    }
  }")

(defn entropy-to-color [entropy]
  (let [r (bit-and entropy 0xFF)
        g (bit-and (bit-shift-right entropy 8) 0xFF) 
        b (bit-and (bit-shift-right entropy 16) 0xFF)]
    (str "rgb(" r "," g "," b ")")))