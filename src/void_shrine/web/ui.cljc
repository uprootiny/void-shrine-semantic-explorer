(ns void-shrine.web.ui
  (:require [void-shrine.core :as core]
            [hiccup.core :as h]
            [clojure.string :as str]))

(defn void-particle
  "Render a single void particle"
  [{:keys [x y size opacity color]}]
  [:circle
   {:cx x
    :cy y
    :r size
    :fill color
    :opacity opacity
    :class "void-particle"}])

(defn entropy-visualization
  "Render entropy flow visualization"
  [entropy-flow]
  [:svg
   {:width "800"
    :height "600"
    :class "entropy-canvas"
    :viewBox "0 0 800 600"}
   [:defs
    [:linearGradient
     {:id "void-gradient"}
     [:stop {:offset "0%" :stop-color "#000000"}]
     [:stop {:offset "50%" :stop-color "#1a0d1a"}]
     [:stop {:offset "100%" :stop-color "#330033"}]]]
   [:rect
    {:width "100%"
     :height "100%"
     :fill "url(#void-gradient)"}]
   (for [particle entropy-flow]
     (void-particle particle))])

(defn chaos-metrics-panel
  "Display chaos metrics"
  [{:keys [total-entropy void-depth dissolution-rate]}]
  [:div.metrics-panel
   [:div.metric
    [:span.metric-label "Total Entropy: "]
    [:span.metric-value total-entropy]]
   [:div.metric
    [:span.metric-label "Void Depth: "]
    [:span.metric-value void-depth]]
   [:div.metric
    [:span.metric-label "Dissolution Rate: "]
    [:span.metric-value (format "%.3f" dissolution-rate)]]])

(defn void-manifestation-display
  "Display void manifestations"
  [manifestations]
  [:div.manifestations
   [:h3 "Current Manifestations"]
   (for [m (take 5 manifestations)]
     [:div.manifestation
      {:key (:timestamp m)}
      [:div.path (str/join " → " (map name (:path m)))]
      [:div.poetry
       [:pre (when (:poetry m) (:poetry m))]]])])

(defn chaos-controls
  "Interactive chaos controls"
  []
  [:div.controls
   [:button.chaos-btn
    {:onclick "window.voidShrine.triggerChaos()"}
    "Invoke Chaos"]
   [:button.entropy-btn
    {:onclick "window.voidShrine.harvestEntropy()"}
    "Harvest Entropy"]
   [:button.void-btn
    {:onclick "window.voidShrine.enterVoid()"}
    "Enter Void"]])

(defn void-spiral-svg
  "Render void spiral"
  [spiral-points]
  [:svg
   {:width "400"
    :height "400"
    :class "void-spiral"}
   [:g
    {:transform "translate(200,200)"}
    (for [point spiral-points]
      [:circle
       {:cx (:x point)
        :cy (:y point)
        :r (* 2 (:decay point))
        :fill "none"
        :stroke "#ff0066"
        :stroke-width "1"
        :opacity (:decay point)}])]])

(defn chaos-dashboard
  "Main chaos dashboard"
  [state]
  (let [{:keys [entropy-values void-manifestations chaos-metrics]} state
        entropy-flow (core/entropy-flow entropy-values (/ (js/Date.now) 16))
        spiral-points (core/void-fractal (first entropy-values) 0 4)]
    [:div.chaos-dashboard
     [:header.void-header
      [:h1 "VOID SHRINE"]
      [:div.subtitle "Harvesting Entropy from the Quantum Abyss"]]
     
     [:div.main-visualization
      (entropy-visualization entropy-flow)]
     
     [:div.side-panels
      [:div.left-panel
       (chaos-metrics-panel chaos-metrics)
       (chaos-controls)]
      
      [:div.right-panel
       (void-spiral-svg spiral-points)
       (void-manifestation-display void-manifestations)]]
     
     [:div.entropy-stream
      [:div.stream-title "Live Entropy Stream"]
      [:div.entropy-values
       (for [val (take 20 entropy-values)]
         [:span.entropy-byte
          {:style {:background-color (core/entropy-to-color val)}}
          (format "%02X" (mod val 256))])]]]))

(defn void-css
  "CSS for the void shrine"
  []
  "
  body {
    background: #000011;
    color: #ff0066;
    font-family: 'Courier New', monospace;
    margin: 0;
    overflow: hidden;
  }
  
  .chaos-dashboard {
    display: grid;
    grid-template-rows: auto 1fr auto;
    height: 100vh;
  }
  
  .void-header {
    text-align: center;
    padding: 1rem;
    background: linear-gradient(90deg, #000000, #1a0d1a, #000000);
    border-bottom: 2px solid #ff0066;
  }
  
  .void-header h1 {
    margin: 0;
    font-size: 3rem;
    text-shadow: 0 0 20px #ff0066;
    animation: pulse 2s infinite;
  }
  
  @keyframes pulse {
    0%, 100% { text-shadow: 0 0 20px #ff0066; }
    50% { text-shadow: 0 0 40px #ff0066, 0 0 60px #ff0066; }
  }
  
  .main-visualization {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 1rem;
  }
  
  .entropy-canvas {
    border: 2px solid #ff0066;
    border-radius: 10px;
    background: radial-gradient(circle, #001122, #000000);
  }
  
  .void-particle {
    filter: blur(0.5px);
    animation: float 3s infinite ease-in-out;
  }
  
  @keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
  }
  
  .side-panels {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
    padding: 1rem;
  }
  
  .metrics-panel {
    background: rgba(255, 0, 102, 0.1);
    border: 1px solid #ff0066;
    border-radius: 5px;
    padding: 1rem;
  }
  
  .metric {
    margin: 0.5rem 0;
    display: flex;
    justify-content: space-between;
  }
  
  .metric-value {
    color: #00ffff;
  }
  
  .controls {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-top: 1rem;
  }
  
  .chaos-btn, .entropy-btn, .void-btn {
    padding: 1rem;
    background: transparent;
    border: 2px solid #ff0066;
    color: #ff0066;
    font-family: inherit;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.3s;
  }
  
  .chaos-btn:hover, .entropy-btn:hover, .void-btn:hover {
    background: #ff0066;
    color: #000000;
    box-shadow: 0 0 20px #ff0066;
  }
  
  .entropy-stream {
    background: rgba(0, 0, 0, 0.8);
    border-top: 2px solid #ff0066;
    padding: 1rem;
    overflow-x: auto;
  }
  
  .stream-title {
    margin-bottom: 1rem;
    text-align: center;
    font-size: 1.2rem;
  }
  
  .entropy-values {
    display: flex;
    gap: 0.2rem;
    font-family: 'Courier New', monospace;
  }
  
  .entropy-byte {
    padding: 0.2rem 0.4rem;
    border: 1px solid #333;
    font-size: 0.8rem;
    min-width: 2rem;
    text-align: center;
  }
  
  .manifestation {
    margin: 1rem 0;
    padding: 0.5rem;
    background: rgba(255, 0, 102, 0.05);
    border-left: 3px solid #ff0066;
  }
  
  .path {
    font-weight: bold;
    color: #00ffff;
  }
  
  .poetry pre {
    color: #ff0066;
    font-style: italic;
    margin: 0.5rem 0;
  }
  
  .void-spiral {
    border: 1px solid #ff0066;
    border-radius: 50%;
    background: radial-gradient(circle, transparent, #001122);
  }")

(defn main-page
  "Generate the main page HTML"
  [state]
  (str
   "<!DOCTYPE html>"
   (h/html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title "Void Shrine - Chaos Harvester"]
      [:style (void-css)]]
     [:body
      [:div#app
       (chaos-dashboard state)]
      [:script
       "window.voidShrine = {
          triggerChaos: () => fetch('/api/chaos', {method: 'POST'}),
          harvestEntropy: () => fetch('/api/entropy', {method: 'POST'}),
          enterVoid: () => fetch('/api/void', {method: 'POST'})
        };"]]])))