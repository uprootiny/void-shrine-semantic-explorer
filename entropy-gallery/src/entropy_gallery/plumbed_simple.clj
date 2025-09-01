(ns entropy-gallery.plumbed-simple
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [hiccup.core :as h]
            [cheshire.core :as cheshire]
            [clj-http.client :as http]
            [entropy-gallery.processes.levy :as levy]
            [entropy-gallery.extended-ontology :as ontology])
  (:import [java.security SecureRandom]
           [java.util.concurrent ConcurrentHashMap]))

;; Simple entropy cache
(def entropy-cache (ConcurrentHashMap.))

(defn cache-get [source]
  (let [data (.get entropy-cache source)]
    (when (and data (< (- (System/currentTimeMillis) (:timestamp data)) 30000))
      data)))

(defn cache-put [source data]
  (.put entropy-cache source (assoc data :timestamp (System/currentTimeMillis))))

;; Basic diagnostics
(defn source-diagnostics [source-name start-time end-time error samples]
  {:response-time (- end-time start-time)
   :timestamp end-time
   :sample-count (count samples)
   :status (if error "error" "success")
   :error error})

;; Enhanced entropy sources
(defn fetch-random-org []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🌪️ Fetching atmospheric noise from Random.org...")
      (let [resp (http/get "https://www.random.org/integers/?num=20&min=0&max=16777215&col=1&base=10&format=plain&rnd=new"
                          {:timeout 15000})
            numbers (map #(Integer/parseInt (.trim %)) 
                        (filter #(not (.isEmpty (.trim %))) 
                               (clojure.string/split (:body resp) #"\n")))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "random-org" start-time end-time nil numbers)]
        {:name "🌪️ Random.org Atmospheric Noise"
         :samples numbers
         :quality "true-random"
         :source "atmospheric"
         :method "radio-atmospheric-noise"
         :diagnostics diagnostics
         :limitations "Rate limited, weather dependent"
         :entropy-bits-per-sample 24
         :collection-method "Radio receiver atmospheric noise"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "random-org" start-time end-time (.getMessage e) [])]
          {:name "🌪️ Random.org (Unavailable)"
           :samples (repeatedly 20 #(rand-int 16777216))
           :quality "fallback-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-anu-qrng []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🎲 Fetching quantum entropy from ANU...")
      (let [resp (http/get "https://qrng.anu.edu.au/API/jsonI.php?length=16&type=uint16"
                          {:timeout 10000})
            data (cheshire/parse-string (:body resp) true)
            quantum-numbers (:data data)
            samples (map #(mod (* % 1021) 16777216) quantum-numbers)
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "anu-qrng" start-time end-time nil samples)]
        (let [base-data {:name "🎲 ANU Quantum Random Numbers"
                         :samples samples
                         :quality "quantum-true"
                         :source "quantum-photonic"
                         :method "quantum-vacuum-fluctuations"
                         :diagnostics diagnostics
                         :limitations "Rate limited, requires quantum lab uptime"
                         :entropy-bits-per-sample 24
                         :quantum-source "Photonic quantum vacuum fluctuations"
                         :collection-method "Quantum optics lab measurement"}
              ontological-mode (ontology/select-ontological-mode "anu-qrng" base-data)]
          (assoc base-data :ontological-mode ontological-mode
                          :void-concept (get-in ontology/extended-void-ontology [ontological-mode :description]))))
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "anu-qrng" start-time end-time (.getMessage e) [])]
          {:name "🎲 ANU Quantum (Error)"
           :samples (repeatedly 16 #(rand-int 16777216))
           :quality "fallback-pseudo" 
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-system-entropy []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🔧 Generating system entropy...")
      (let [secure-random (SecureRandom.)
            os-name (System/getProperty "os.name")
            samples (repeatedly 16 #(.nextInt secure-random 16777216))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "system-secure" start-time end-time nil samples)]
        {:name "🔧 System SecureRandom"
         :samples samples
         :quality "cryptographic-random"
         :source "system"
         :method "os-entropy-pool"
         :diagnostics diagnostics
         :limitations "OS implementation dependent"
         :entropy-bits-per-sample 24
         :os-platform os-name
         :collection-method "Operating system entropy pool + CSPRNG"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "system-secure" start-time end-time (.getMessage e) [])]
          {:name "🔧 System (Basic Random)"
           :samples (repeatedly 16 #(rand-int 16777216))
           :quality "basic-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

;; Additional entropy sources
(defn fetch-hotbits []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "☢️ Fetching radioactive decay from HotBits...")
      (let [resp (http/get "https://www.fourmilab.ch/cgi-bin/Hotbits?nbytes=64&fmt=bin"
                          {:timeout 12000})
            bytes (.getBytes (:body resp))
            samples (take 16 (map #(bit-and (int %) 0xFFFFFF) bytes))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "hotbits" start-time end-time nil samples)]
        (let [base-data {:name "☢️ HotBits Radioactive Decay"
                         :samples samples
                         :quality "nuclear-random"
                         :source "radioactive"
                         :method "krypton-85-decay"
                         :diagnostics diagnostics
                         :limitations "Rate limited, nuclear source dependent"
                         :entropy-bits-per-sample 24
                         :collection-method "Krypton-85 radioactive decay detection"}
              ontological-mode (ontology/select-ontological-mode "hotbits" base-data)]
          (assoc base-data :ontological-mode ontological-mode
                          :void-concept (get-in ontology/extended-void-ontology [ontological-mode :description]))))
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "hotbits" start-time end-time (.getMessage e) [])]
          {:name "☢️ HotBits (Unavailable)"
           :samples (repeatedly 16 #(rand-int 16777216))
           :quality "fallback-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-nist-beacon []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🏛️ Fetching NIST Quantum Beacon...")
      (let [resp (http/get "https://beacon.nist.gov/beacon/2.0/pulse/last"
                          {:timeout 10000})
            data (cheshire/parse-string (:body resp) true)
            output-value (:outputValue data)
            samples (when output-value
                     (take 16 (map #(Integer/parseInt (str %) 16) 
                                  (partition 6 output-value))))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "nist-beacon" start-time end-time nil samples)]
        {:name "🏛️ NIST Quantum Beacon"
         :samples (or samples (repeatedly 16 #(rand-int 16777216)))
         :quality "quantum-certified"
         :source "quantum-nist"
         :method "quantum-measurement"
         :diagnostics diagnostics
         :limitations "Updated every 60 seconds, government operated"
         :entropy-bits-per-sample 24
         :collection-method "NIST quantum measurement beacon"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "nist-beacon" start-time end-time (.getMessage e) [])]
          {:name "🏛️ NIST Beacon (Unavailable)"
           :samples (repeatedly 16 #(rand-int 16777216))
           :quality "fallback-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-atmospheric-pressure []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🌪️ Fetching atmospheric pressure entropy...")
      (let [resp (http/get "https://api.openweathermap.org/data/2.5/weather?q=London&appid=demo"
                          {:timeout 8000})
            data (cheshire/parse-string (:body resp) true)
            pressure (get-in data [:main :pressure])
            humidity (get-in data [:main :humidity])
            samples (when (and pressure humidity)
                     (repeatedly 18 #(mod (hash [(* pressure (rand)) 
                                               (* humidity (System/nanoTime))
                                               (System/currentTimeMillis)]) 16777216)))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "atmospheric" start-time end-time nil samples)]
        {:name "🌪️ Atmospheric Pressure Chaos"
         :samples (or samples (repeatedly 18 #(rand-int 16777216)))
         :quality "meteorological"
         :source "atmospheric"
         :method "pressure-fluctuations"
         :diagnostics diagnostics
         :limitations "Weather API dependent, rate limited"
         :entropy-bits-per-sample 20
         :collection-method "Barometric pressure micro-variations"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "atmospheric" start-time end-time (.getMessage e) [])]
          {:name "🌪️ Atmospheric (Simulated)"
           :samples (repeatedly 18 #(mod (hash [(rand) (System/nanoTime)]) 16777216))
           :quality "simulated-atmospheric"
           :source "simulation"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-timing-jitter []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "⏱️ Harvesting CPU timing jitter...")
      (let [samples (repeatedly 20 (fn []
                                    (let [t1 (System/nanoTime)
                                          _ (Thread/sleep 0 1)
                                          t2 (System/nanoTime)]
                                      (mod (- t2 t1) 16777216))))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "timing-jitter" start-time end-time nil samples)]
        {:name "⏱️ CPU Timing Jitter"
         :samples samples
         :quality "hardware-timing"
         :source "cpu-timing"
         :method "nanosecond-precision"
         :diagnostics diagnostics
         :limitations "CPU and OS scheduler dependent"
         :entropy-bits-per-sample 18
         :collection-method "High-precision timing variance measurement"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "timing-jitter" start-time end-time (.getMessage e) [])]
          {:name "⏱️ Timing Jitter (Error)"
           :samples (repeatedly 20 #(rand-int 16777216))
           :quality "fallback-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-memory-entropy []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🧠 Sampling memory allocation patterns...")
      (let [samples (repeatedly 15 (fn []
                                    (let [arr (byte-array 1024)
                                          _ (java.util.Arrays/fill arr (byte 0))
                                          hash-val (hash (seq arr))]
                                      (mod (+ hash-val (System/identityHashCode arr)) 16777216))))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "memory-entropy" start-time end-time nil samples)]
        {:name "🧠 Memory Allocation Entropy"
         :samples samples
         :quality "system-dependent"
         :source "memory-system"
         :method "allocation-patterns"
         :diagnostics diagnostics
         :limitations "JVM and OS memory manager dependent"
         :entropy-bits-per-sample 22
         :collection-method "Memory allocation address randomization"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "memory-entropy" start-time end-time (.getMessage e) [])]
          {:name "🧠 Memory (Error)"
           :samples (repeatedly 15 #(rand-int 16777216))
           :quality "fallback-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn fetch-network-latency []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "🌐 Measuring network latency chaos...")
      (let [targets ["8.8.8.8" "1.1.1.1" "208.67.222.222"]
            samples (repeatedly 12 (fn []
                                    (let [t1 (System/nanoTime)
                                          target (rand-nth targets)
                                          _ (try (java.net.InetAddress/getByName target)
                                                (catch Exception _ nil))
                                          t2 (System/nanoTime)]
                                      (mod (- t2 t1) 16777216))))
            end-time (System/currentTimeMillis)
            diagnostics (source-diagnostics "network-latency" start-time end-time nil samples)]
        {:name "🌐 Network Latency Chaos"
         :samples samples
         :quality "network-dependent"
         :source "network-timing"
         :method "dns-resolution-timing"
         :diagnostics diagnostics
         :limitations "Network conditions and DNS server dependent"
         :entropy-bits-per-sample 20
         :collection-method "DNS resolution timing variations"})
      (catch Exception e
        (let [end-time (System/currentTimeMillis)
              diagnostics (source-diagnostics "network-latency" start-time end-time (.getMessage e) [])]
          {:name "🌐 Network (Error)"
           :samples (repeatedly 12 #(rand-int 16777216))
           :quality "fallback-pseudo"
           :source "fallback"
           :diagnostics diagnostics
           :error (.getMessage e)})))))

(defn get-entropy [source]
  (or (cache-get source)
      (let [data (case source
                   "random-org" (fetch-random-org)
                   "anu-qrng" (fetch-anu-qrng)
                   "system-secure" (fetch-system-entropy)
                   "hotbits" (fetch-hotbits)
                   "nist-beacon" (fetch-nist-beacon)
                   "atmospheric" (fetch-atmospheric-pressure)
                   "timing-jitter" (fetch-timing-jitter)
                   "memory-entropy" (fetch-memory-entropy)
                   "network-latency" (fetch-network-latency)
                   (fetch-system-entropy))]
        (cache-put source data)
        data)))

;; Simple HTML page
(defn main-page []
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "🌊 Enhanced Entropy Gallery"]
     [:script {:src "https://d3js.org/d3.v7.min.js"}]
     [:style "
       body { background: #0a0a0a; color: #00ff88; font-family: 'Courier New', monospace; 
              padding: 20px; line-height: 1.4; }
       .container { max-width: 1400px; margin: 0 auto; }
       h1 { text-align: center; color: #00ffff; text-shadow: 0 0 20px #00ffff; 
            margin-bottom: 20px; }
       
       /* Live Status Bar */
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
       
       /* Enhanced Controls */
       .controls { display: flex; justify-content: center; gap: 15px; margin: 25px 0; 
                  flex-wrap: wrap; }
       button { background: #003333; color: #00ff88; border: 1px solid #00ff88; 
               padding: 12px 20px; border-radius: 6px; cursor: pointer; 
               font-family: inherit; transition: all 0.3s; position: relative; }
       button:hover { background: #00ff88; color: #003333; transform: translateY(-2px); 
                     box-shadow: 0 4px 8px rgba(0,255,136,0.3); }
       button:active { transform: translateY(0); }
       button:disabled { opacity: 0.5; cursor: not-allowed; }
       
       .button-loading::after { content: ''; position: absolute; width: 16px; height: 16px;
                               margin: auto; border: 2px solid transparent; 
                               border-top-color: #00ff88; border-radius: 50%;
                               animation: spin 1s linear infinite; }
       @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
       
       /* Interactive Sources Grid */
       .sources { display: grid; grid-template-columns: repeat(auto-fit, minmax(380px, 1fr)); 
                 gap: 25px; margin: 30px 0; }
       .source { background: rgba(0,40,40,0.3); padding: 20px; border-radius: 12px; 
                border: 1px solid #004444; transition: all 0.3s; cursor: pointer;
                position: relative; }
       .source:hover { border-color: #00ff88; transform: translateY(-3px);
                      box-shadow: 0 8px 16px rgba(0,255,136,0.2); }
       .source.fetching { border-color: #ffaa00; }
       .source.error { border-color: #ff4444; }
       .source.success { border-color: #00ff88; }
       
       /* Source Headers with Status */
       .source-header { display: flex; justify-content: space-between; align-items: center; 
                       margin-bottom: 15px; }
       h3 { color: #00ffff; font-size: 1.1em; margin: 0; }
       .source-status { font-size: 0.7em; padding: 4px 8px; border-radius: 4px; 
                       text-transform: uppercase; font-weight: bold; }
       .status-live { background: #00ff88; color: #003333; }
       .status-cached { background: #ffaa00; color: #000; }
       .status-error { background: #ff4444; color: #fff; }
       .status-fetching { background: #0088ff; color: #fff; animation: pulse 1s infinite; }
       
       /* Enhanced Charts with Click Areas */
       .chart { height: 200px; background: rgba(0,0,0,0.5); margin: 15px 0; 
               border: 1px solid #003333; border-radius: 8px; cursor: pointer;
               transition: all 0.3s; position: relative; }
       .chart:hover { border-color: #00ff88; background: rgba(0,255,136,0.05); }
       .chart.loading::after { content: 'Fetching...'; position: absolute; 
                              top: 50%; left: 50%; transform: translate(-50%, -50%);
                              color: #ffaa00; font-size: 0.9em; }
       
       /* Detailed Info and Diagnostics */
       .info { font-size: 0.85em; color: #999; margin: 12px 0; line-height: 1.6; }
       .diagnostics { background: rgba(0,20,20,0.5); padding: 12px; 
                     border-radius: 6px; margin: 10px 0; border-left: 3px solid #00ff88;
                     transition: all 0.3s; }
       .diagnostics:hover { background: rgba(0,30,30,0.7); }
       
       /* Error Details */
       .error-details { background: rgba(80,20,20,0.5); padding: 10px; 
                       border-radius: 4px; margin: 8px 0; border-left: 3px solid #ff4444;
                       font-size: 0.8em; display: none; }
       .error-details.show { display: block; }
       
       /* Live Updates Indicator */
       .live-indicator { position: fixed; top: 20px; right: 20px; 
                        background: rgba(0,50,50,0.9); padding: 10px; 
                        border-radius: 6px; border: 1px solid #00ff88; 
                        font-size: 0.8em; z-index: 1000; }
       .live-stats { margin-top: 5px; font-size: 0.7em; color: #888; }
       
       /* Response Animations */
       @keyframes fetchStart { 0% { opacity: 1; } 50% { opacity: 0.7; } 100% { opacity: 1; } }
       @keyframes fetchSuccess { 0% { border-color: #00ff88; } 100% { border-color: #004444; } }
       @keyframes fetchError { 0% { border-color: #ff4444; } 100% { border-color: #004444; } }
       
       .fetch-animation { animation: fetchStart 0.5s ease-in-out; }
       .success-animation { animation: fetchSuccess 2s ease-out; }
       .error-animation { animation: fetchError 2s ease-out; }
     "]]
    [:body
     [:div.container
      [:h1 "🌊 Enhanced Entropy Gallery"]
      
      ;; Live Status Bar
      [:div.status-bar
       [:div.status-left
        [:div.status-indicator {:id "global-status"}]
        [:div.status-text {:id "global-status-text"} "Initializing..."]]
       [:div.live-counter {:id "live-counter"} "Updates: 0"]]
      
      ;; Live Indicator
      [:div.live-indicator {:id "live-indicator"}
       [:div "🔴 LIVE Gallery"]
       [:div.live-stats {:id "live-stats"} "0 active / 0 errors"]]
      
      [:div.controls
       [:button {:onclick "refreshAll()" :id "refresh-btn"} "🔄 Refresh All"]
       [:button {:onclick "fetchReal()" :id "fetch-btn"} "🌍 Fetch Real Entropy"]
       [:button {:onclick "toggleLiveMode()" :id "live-btn"} "⚡ Live Mode: OFF"]
       [:button {:onclick "clearErrors()" :id "clear-btn"} "🗑️ Clear Errors"]]]
      
      [:div.sources
       ;; Quantum & Nuclear Sources
       [:div.source {:id "source-anu-qrng" :onclick "clickSource('anu-qrng')"}
        [:div.source-header
         [:h3 "🎲 ANU Quantum Random Numbers"]
         [:div.source-status {:id "status-anu-qrng"} "READY"]]
        [:div.chart {:id "chart-anu-qrng" :onclick "refreshSource('anu-qrng')"}]
        [:div.info {:id "info-anu-qrng"} "Quantum vacuum fluctuations from Australian National University quantum optics lab..."]
        [:div.diagnostics {:id "diagnostics-anu-qrng" :onclick "toggleDetails('anu-qrng')"}]
        [:div.error-details {:id "error-anu-qrng"}]]
       
       [:div.source {:id "source-nist-beacon" :onclick "clickSource('nist-beacon')"}
        [:div.source-header
         [:h3 "🏛️ NIST Quantum Beacon"]
         [:div.source-status {:id "status-nist-beacon"} "READY"]]
        [:div.chart {:id "chart-nist-beacon" :onclick "refreshSource('nist-beacon')"}]
        [:div.info {:id "info-nist-beacon"} "NIST government quantum measurements, updated every 60 seconds..."]
        [:div.diagnostics {:id "diagnostics-nist-beacon" :onclick "toggleDetails('nist-beacon')"}]
        [:div.error-details {:id "error-nist-beacon"}]]
       
       [:div.source {:id "source-hotbits" :onclick "clickSource('hotbits')"}
        [:div.source-header
         [:h3 "☢️ HotBits Radioactive Decay"]
         [:div.source-status {:id "status-hotbits"} "READY"]]
        [:div.chart {:id "chart-hotbits" :onclick "refreshSource('hotbits')"}]
        [:div.info {:id "info-hotbits"} "True radioactive decay from Krypton-85 nuclear source..."]
        [:div.diagnostics {:id "diagnostics-hotbits" :onclick "toggleDetails('hotbits')"}]
        [:div.error-details {:id "error-hotbits"}]]
       
       ;; Atmospheric & Environmental
       [:div.source {:id "source-random-org" :onclick "clickSource('random-org')"}
        [:div.source-header
         [:h3 "🌪️ Random.org Atmospheric Noise"]
         [:div.source-status {:id "status-random-org"} "READY"]]
        [:div.chart {:id "chart-random-org" :onclick "refreshSource('random-org')"}]
        [:div.info {:id "info-random-org"} "Radio receiver capturing atmospheric interference patterns..."]
        [:div.diagnostics {:id "diagnostics-random-org" :onclick "toggleDetails('random-org')"}]
        [:div.error-details {:id "error-random-org"}]]
       
       [:div.source {:id "source-atmospheric" :onclick "clickSource('atmospheric')"}
        [:div.source-header
         [:h3 "🌪️ Atmospheric Pressure Chaos"]
         [:div.source-status {:id "status-atmospheric"} "READY"]]
        [:div.chart {:id "chart-atmospheric" :onclick "refreshSource('atmospheric')"}]
        [:div.info {:id "info-atmospheric"} "Barometric pressure micro-variations from global weather systems..."]
        [:div.diagnostics {:id "diagnostics-atmospheric" :onclick "toggleDetails('atmospheric')"}]
        [:div.error-details {:id "error-atmospheric"}]]
       
       ;; System & Hardware Sources
       [:div.source {:id "source-system-secure" :onclick "clickSource('system-secure')"}
        [:div.source-header
         [:h3 "🔧 System SecureRandom"]
         [:div.source-status {:id "status-system-secure"} "READY"]]
        [:div.chart {:id "chart-system-secure" :onclick "refreshSource('system-secure')"}]
        [:div.info {:id "info-system-secure"} "Operating system entropy pool with cryptographically secure generation..."]
        [:div.diagnostics {:id "diagnostics-system-secure" :onclick "toggleDetails('system-secure')"}]
        [:div.error-details {:id "error-system-secure"}]]
       
       [:div.source {:id "source-timing-jitter" :onclick "clickSource('timing-jitter')"}
        [:div.source-header
         [:h3 "⏱️ CPU Timing Jitter"]
         [:div.source-status {:id "status-timing-jitter"} "READY"]]
        [:div.chart {:id "chart-timing-jitter" :onclick "refreshSource('timing-jitter')"}]
        [:div.info {:id "info-timing-jitter"} "High-precision nanosecond CPU timing variations and scheduler chaos..."]
        [:div.diagnostics {:id "diagnostics-timing-jitter" :onclick "toggleDetails('timing-jitter')"}]
        [:div.error-details {:id "error-timing-jitter"}]]
       
       [:div.source {:id "source-memory-entropy" :onclick "clickSource('memory-entropy')"}
        [:div.source-header
         [:h3 "🧠 Memory Allocation Entropy"]
         [:div.source-status {:id "status-memory-entropy"} "READY"]]
        [:div.chart {:id "chart-memory-entropy" :onclick "refreshSource('memory-entropy')"}]
        [:div.info {:id "info-memory-entropy"} "Memory address randomization patterns and allocation timing chaos..."]
        [:div.diagnostics {:id "diagnostics-memory-entropy" :onclick "toggleDetails('memory-entropy')"}]
        [:div.error-details {:id "error-memory-entropy"}]]
       
       [:div.source {:id "source-network-latency" :onclick "clickSource('network-latency')"}
        [:div.source-header
         [:h3 "🌐 Network Latency Chaos"]
         [:div.source-status {:id "status-network-latency"} "READY"]]
        [:div.chart {:id "chart-network-latency" :onclick "refreshSource('network-latency')"}]
        [:div.info {:id "info-network-latency"} "DNS resolution timing chaos across multiple global servers..."]
        [:div.diagnostics {:id "diagnostics-network-latency" :onclick "toggleDetails('network-latency')"}]
        [:div.error-details {:id "error-network-latency"}]]]]
     
     [:script "
       // Global state management
       let entropyData = {};
       let liveMode = false;
       let liveInterval = null;
       let updateCounter = 0;
       let activeRequests = 0;
       let errorCount = 0;
       let sourceStates = {};
       
       // Initialize source states
       const sources = ['anu-qrng', 'nist-beacon', 'hotbits', 'random-org', 'atmospheric', 
                       'system-secure', 'timing-jitter', 'memory-entropy', 'network-latency'];
       sources.forEach(source => {
         sourceStates[source] = { status: 'ready', lastFetch: 0, errorCount: 0, lastError: null };
       });
       
       // Live status management
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
         const errorEl = document.getElementById(`error-${source}`);
         
         sourceStates[source].status = status;
         sourceStates[source].lastFetch = Date.now();
         
         // Update status badge
         statusEl.className = `source-status status-${status}`;
         statusEl.textContent = status.toUpperCase();
         
         // Update source container class
         sourceEl.className = sourceEl.className.replace(/\\b(fetching|error|success)\\b/g, '');
         sourceEl.classList.add(status);
         
         // Handle errors
         if (status === 'error') {
           sourceStates[source].errorCount++;
           sourceStates[source].lastError = message || 'Unknown error';
           errorEl.innerHTML = `
             <strong>Error Details:</strong><br/>
             <div style='color: #ff6666'>${message || 'Request failed'}</div>
             <div style='color: #999; font-size: 0.9em; margin-top: 5px'>
               Time: ${new Date().toLocaleTimeString()}<br/>
               Error Count: ${sourceStates[source].errorCount}
             </div>
           `;
           errorCount++;
         } else if (status === 'success') {
           sourceStates[source].errorCount = 0;
           sourceStates[source].lastError = null;
           errorEl.innerHTML = '';
           if (errorCount > 0) errorCount--;
         }
         
         updateGlobalStatus();
       }
       
       function setStatus(msg) {
         console.log(`[${new Date().toLocaleTimeString()}] ${msg}`);
         document.getElementById('global-status-text').textContent = msg;
       }
       
       function fetchEntropy(source) {
         setStatus('Fetching ' + source + '...');
         fetch('/api/entropy?source=' + source)
           .then(r => r.json())
           .then(data => {
             entropyData[source] = data;
             updateInfo(source, data);
             drawVisualization(source, data.samples);
             setStatus('Updated ' + source);
           })
           .catch(err => {
             setStatus('Error: ' + err.message);
           });
       }
       
       function updateInfo(source, data) {
         const info = document.getElementById('info-' + source);
         const diagnostics = document.getElementById('diagnostics-' + source);
         
         if (info) {
           const voidConcept = data['void-concept'] || '';
           const ontologicalMode = data['ontological-mode'] || '';
           
           info.innerHTML = `
             <strong>Quality:</strong> ${data.quality}<br/>
             <strong>Source:</strong> ${data.source}<br/>
             <strong>Method:</strong> ${data.method}<br/>
             <strong>Samples:</strong> ${data.samples.length} entropy values<br/>
             <strong>Sample Preview:</strong> ${data.samples.slice(0, 3).join(', ')}...<br/>
             ${ontologicalMode ? `<strong>Ontological Mode:</strong> <span style='color: #00ffaa'>${ontologicalMode}</span><br/>` : ''}
             ${voidConcept ? `<strong>Void Concept:</strong> <em style='color: #888; font-size: 0.9em'>${voidConcept}</em>` : ''}
           `;
         }
         
         if (diagnostics && data.diagnostics) {
           diagnostics.innerHTML = `
             <div><strong>Diagnostics:</strong></div>
             <div>Response Time: ${data.diagnostics['response-time']}ms</div>
             <div>Status: ${data.diagnostics.status}</div>
             <div>Sample Count: ${data.diagnostics['sample-count']}</div>
             ${data.diagnostics.error ? '<div style=\"color:#ff4444\">Error: ' + data.diagnostics.error + '</div>' : ''}
           `;
         }
       }
       
       function drawVisualization(source, samples) {
         const container = d3.select('#chart-' + source);
         container.selectAll('*').remove();
         
         const svg = container.append('svg')
           .attr('width', '100%').attr('height', '100%')
           .attr('viewBox', '0 0 350 180');
         
         // Choose visualization based on ontological mode if available
         const sourceData = entropyData[source];
         const ontologicalMode = sourceData && sourceData['ontological-mode'];
         
         if (ontologicalMode) {
           drawOntologicalVisualization(svg, samples, 350, 180, ontologicalMode, sourceData);
         } else {
           // Fallback to source-based visualization
           if (source.includes('quantum') || source.includes('anu')) {
             drawQuantumField(svg, samples, 350, 180);
           } else if (source.includes('radioactive') || source.includes('hotbits')) {
             drawRadioactiveDecay(svg, samples, 350, 180);
           } else if (source.includes('atmospheric') || source.includes('random-org')) {
             drawAtmosphericTurbulence(svg, samples, 350, 180);
           } else if (source.includes('timing') || source.includes('jitter')) {
             drawTimingOscilloscope(svg, samples, 350, 180);
           } else if (source.includes('memory')) {
             drawMemoryGrid(svg, samples, 350, 180);
           } else if (source.includes('network')) {
             drawNetworkGraph(svg, samples, 350, 180);
           } else {
             drawCleanBars(svg, samples, 350, 180);
           }
         }
       }
       
       function drawQuantumField(svg, samples, width, height) {
         const particles = samples.slice(0, 40).map((sample, i) => ({
           x: (sample % width),
           y: ((sample >> 8) % height),
           size: 1 + (sample % 4),
           opacity: 0.3 + ((sample % 100) / 200)
         }));
         
         // Quantum field background
         svg.append('rect')
           .attr('width', width)
           .attr('height', height)
           .attr('fill', 'rgba(0, 20, 60, 0.1)');
         
         // Quantum particles
         svg.selectAll('circle.quantum')
           .data(particles)
           .enter().append('circle')
           .attr('class', 'quantum')
           .attr('cx', d => d.x)
           .attr('cy', d => d.y)
           .attr('r', d => d.size)
           .attr('fill', '#00ffff')
           .attr('opacity', d => d.opacity)
           .style('mix-blend-mode', 'screen');
         
         // Quantum entanglement lines
         for (let i = 0; i < particles.length - 1; i += 4) {
           svg.append('line')
             .attr('x1', particles[i].x)
             .attr('y1', particles[i].y)
             .attr('x2', particles[i + 1] ? particles[i + 1].x : particles[i].x)
             .attr('y2', particles[i + 1] ? particles[i + 1].y : particles[i].y)
             .attr('stroke', '#00ffff')
             .attr('stroke-width', 0.5)
             .attr('opacity', 0.2);
         }
       }
       
       function drawRadioactiveDecay(svg, samples, width, height) {
         const centerX = width / 2, centerY = height / 2;
         const decayEvents = samples.slice(0, 25);
         
         // Nuclear core
         svg.append('circle')
           .attr('cx', centerX)
           .attr('cy', centerY)
           .attr('r', 20)
           .attr('fill', '#ff4400')
           .attr('opacity', 0.8);
         
         // Decay particles radiating outward
         decayEvents.forEach((sample, i) => {
           const angle = (sample / 16777216) * Math.PI * 2;
           const distance = 25 + ((sample >> 8) % 100);
           const x = centerX + Math.cos(angle) * distance;
           const y = centerY + Math.sin(angle) * distance;
           
           svg.append('circle')
             .attr('cx', x)
             .attr('cy', y)
             .attr('r', 2 + (sample % 3))
             .attr('fill', '#ff6600')
             .attr('opacity', 0.7);
           
           // Decay trail
           svg.append('line')
             .attr('x1', centerX)
             .attr('y1', centerY)
             .attr('x2', x)
             .attr('y2', y)
             .attr('stroke', '#ff4400')
             .attr('stroke-width', 1)
             .attr('opacity', 0.3);
         });
       }
       
       function drawAtmosphericTurbulence(svg, samples, width, height) {
         // Create turbulent flow field
         const flowPoints = samples.slice(0, 30).map((sample, i) => ({
           x: (i / 30) * width,
           y: height / 2 + ((sample % 80) - 40),
           intensity: (sample % 100) / 100
         }));
         
         // Atmospheric layers
         for (let layer = 0; layer < 3; layer++) {
           const layerY = (layer + 1) * (height / 4);
           const path = d3.path();
           
           flowPoints.forEach((point, i) => {
             const x = point.x;
             const y = layerY + (point.y - height/2) * (1 - layer * 0.3);
             if (i === 0) path.moveTo(x, y);
             else path.lineTo(x, y);
           });
           
           svg.append('path')
             .attr('d', path.toString())
             .attr('stroke', `hsl(${180 + layer * 20}, 70%, 50%)`)
             .attr('stroke-width', 2)
             .attr('fill', 'none')
             .attr('opacity', 0.6);
         }
         
         // Wind particles
         flowPoints.forEach(point => {
           svg.append('circle')
             .attr('cx', point.x)
             .attr('cy', point.y)
             .attr('r', 2 + point.intensity * 3)
             .attr('fill', '#00ff88')
             .attr('opacity', point.intensity);
         });
       }
       
       function drawTimingOscilloscope(svg, samples, width, height) {
         const waveform = samples.slice(0, 50).map((sample, i) => ({
           x: (i / 50) * width,
           y: height/2 + ((sample % 120) - 60)
         }));
         
         // Grid lines (oscilloscope style)
         for (let i = 0; i <= 10; i++) {
           svg.append('line')
             .attr('x1', (i / 10) * width)
             .attr('y1', 0)
             .attr('x2', (i / 10) * width)
             .attr('y2', height)
             .attr('stroke', '#003333')
             .attr('stroke-width', 0.5);
           
           svg.append('line')
             .attr('x1', 0)
             .attr('y1', (i / 10) * height)
             .attr('x2', width)
             .attr('y2', (i / 10) * height)
             .attr('stroke', '#003333')
             .attr('stroke-width', 0.5);
         }
         
         // Waveform
         const line = d3.line()
           .x(d => d.x)
           .y(d => d.y)
           .curve(d3.curveMonotoneX);
         
         svg.append('path')
           .datum(waveform)
           .attr('d', line)
           .attr('stroke', '#00ff00')
           .attr('stroke-width', 2)
           .attr('fill', 'none')
           .attr('opacity', 0.8);
         
         // Timing markers
         waveform.filter((_, i) => i % 5 === 0).forEach(point => {
           svg.append('circle')
             .attr('cx', point.x)
             .attr('cy', point.y)
             .attr('r', 3)
             .attr('fill', '#ffff00')
             .attr('opacity', 0.8);
         });
       }
       
       function drawMemoryGrid(svg, samples, width, height) {
         const gridSize = 8;
         const cellWidth = width / gridSize;
         const cellHeight = height / gridSize;
         
         samples.slice(0, 64).forEach((sample, i) => {
           const row = Math.floor(i / gridSize);
           const col = i % gridSize;
           const intensity = (sample % 256) / 256;
           
           svg.append('rect')
             .attr('x', col * cellWidth)
             .attr('y', row * cellHeight)
             .attr('width', cellWidth - 1)
             .attr('height', cellHeight - 1)
             .attr('fill', `hsl(${sample % 360}, 70%, ${30 + intensity * 40}%)`)
             .attr('opacity', 0.7);
           
           // Memory address overlay
           if (intensity > 0.7) {
             svg.append('text')
               .attr('x', col * cellWidth + cellWidth/2)
               .attr('y', row * cellHeight + cellHeight/2)
               .attr('text-anchor', 'middle')
               .attr('font-size', '8px')
               .attr('fill', '#ffffff')
               .text(sample.toString(16).substr(-2));
           }
         });
       }
       
       function drawNetworkGraph(svg, samples, width, height) {
         const nodes = samples.slice(0, 12).map((sample, i) => ({
           x: (sample % width),
           y: ((sample >> 8) % height),
           id: i,
           latency: sample % 100
         }));
         
         // Network connections
         nodes.forEach((node, i) => {
           if (i < nodes.length - 1) {
             const target = nodes[i + 1];
             const opacity = Math.max(0.1, 1 - (node.latency / 100));
             
             svg.append('line')
               .attr('x1', node.x)
               .attr('y1', node.y)
               .attr('x2', target.x)
               .attr('y2', target.y)
               .attr('stroke', node.latency > 50 ? '#ff4444' : '#00ff88')
               .attr('stroke-width', 2)
               .attr('opacity', opacity);
           }
         });
         
         // Network nodes
         nodes.forEach(node => {
           svg.append('circle')
             .attr('cx', node.x)
             .attr('cy', node.y)
             .attr('r', 4 + (node.latency % 6))
             .attr('fill', node.latency > 70 ? '#ff0000' : node.latency > 40 ? '#ffaa00' : '#00ff00')
             .attr('stroke', '#ffffff')
             .attr('stroke-width', 1);
           
           // Latency indicator
           svg.append('text')
             .attr('x', node.x)
             .attr('y', node.y - 10)
             .attr('text-anchor', 'middle')
             .attr('font-size', '8px')
             .attr('fill', '#ffffff')
             .text(node.latency + 'ms');
         });
       }
       
       function drawOntologicalVisualization(svg, samples, width, height, ontologyMode, sourceData) {
         // Extended ontological visualization modes based on void concepts
         const voidConcept = sourceData && sourceData['void-concept'];
         
         // Set background based on ontological theme
         const backgroundMap = {
           'quantum-indeterminacy': 'rgba(0, 20, 80, 0.1)',
           'primordial-absence': 'rgba(0, 0, 0, 0.9)',
           'temporal-voids': 'rgba(40, 0, 40, 0.3)',
           'stellar-dissolution': 'rgba(80, 40, 0, 0.2)',
           'entropic-cascade': 'rgba(80, 20, 20, 0.3)',
           'neural-dissolution': 'rgba(20, 40, 20, 0.2)',
           'linguistic-void': 'rgba(40, 40, 0, 0.2)'
         };
         
         const modeCategory = ontologyMode.split('-')[0] + '-' + ontologyMode.split('-')[1];
         const bgColor = backgroundMap[modeCategory] || 'rgba(20, 20, 20, 0.3)';
         
         svg.append('rect')
           .attr('width', width)
           .attr('height', height)
           .attr('fill', bgColor);
         
         // Draw based on specific ontological mode
         switch(ontologyMode) {
           case 'superposition-states':
           case 'measurement-paradoxes':
           case 'entanglement-mysteries':
             drawQuantumIndeterminacyVisualization(svg, samples, width, height, ontologyMode);
             break;
           case 'pre-cosmic-silence':
           case 'original-darkness':
           case 'foundational-emptiness':
             drawPrimordialAbsenceVisualization(svg, samples, width, height, ontologyMode);
             break;
           case 'chronos-dissolution':
           case 'eternal-intervals':
           case 'anachronistic-zones':
             drawTemporalVoidsVisualization(svg, samples, width, height, ontologyMode);
             break;
           case 'nuclear-decay':
           case 'gravitational-collapse':
           case 'cosmic-cooling':
             drawStellarDissolutionVisualization(svg, samples, width, height, ontologyMode);
             break;
           case 'dissolution-waves':
           case 'chaos-emergence':
           case 'information-loss':
             drawEntropicCascadeVisualization(svg, samples, width, height, ontologyMode);
             break;
           case 'synaptic-degradation':
           case 'memory-dissolution':
           case 'consciousness-fragmentation':
             drawNeuralDissolutionVisualization(svg, samples, width, height, ontologyMode);
             break;
           case 'semantic-collapse':
           case 'communication-static':
           case 'silence-spaces':
             drawLinguisticVoidVisualization(svg, samples, width, height, ontologyMode);
             break;
           default:
             drawPrimordialAbsenceVisualization(svg, samples, width, height, 'foundational-emptiness');
         }
         
         // Add ontological overlay text
         if (voidConcept) {
           svg.append('text')
             .attr('x', width - 10)
             .attr('y', height - 10)
             .attr('text-anchor', 'end')
             .attr('font-size', '9px')
             .attr('fill', '#666')
             .attr('opacity', 0.7)
             .text(ontologyMode);
         }
       }
       
       function drawQuantumIndeterminacyVisualization(svg, samples, width, height, mode) {
         if (mode === 'superposition-states') {
           // Multiple ghostly states overlapping
           samples.slice(0, 15).forEach((sample, i) => {
             const states = 3; // Number of superposed states
             for (let s = 0; s < states; s++) {
               const x = ((sample + s * 1000) % width);
               const y = (((sample >> 8) + s * 2000) % height);
               svg.append('circle')
                 .attr('cx', x)
                 .attr('cy', y)
                 .attr('r', 3 + (sample % 4))
                 .attr('fill', `hsl(${(sample + s * 120) % 360}, 70%, 60%)`)
                 .attr('opacity', 0.3);
             }
           });
         } else if (mode === 'measurement-paradoxes') {
           // Measurement apparatus with uncertainty
           const centerX = width / 2, centerY = height / 2;
           samples.slice(0, 20).forEach((sample, i) => {
             const uncertainty = (sample % 50);
             const angle = (i / 20) * Math.PI * 2;
             const r = 60 + uncertainty;
             const x = centerX + Math.cos(angle) * r;
             const y = centerY + Math.sin(angle) * r;
             
             // Measurement line with uncertainty blur
             svg.append('line')
               .attr('x1', centerX)
               .attr('y1', centerY)
               .attr('x2', x)
               .attr('y2', y)
               .attr('stroke', '#00ffff')
               .attr('stroke-width', 1 + uncertainty/25)
               .attr('opacity', 0.6);
             
             svg.append('circle')
               .attr('cx', x)
               .attr('cy', y)
               .attr('r', 2)
               .attr('fill', '#ffffff')
               .attr('opacity', 0.8);
           });
         } else {
           // Entanglement - connected pairs
           const pairs = samples.slice(0, 12);
           for (let i = 0; i < pairs.length - 1; i += 2) {
             const x1 = pairs[i] % width;
             const y1 = (pairs[i] >> 8) % height;
             const x2 = pairs[i + 1] % width;
             const y2 = (pairs[i + 1] >> 8) % height;
             
             svg.append('line')
               .attr('x1', x1)
               .attr('y1', y1)
               .attr('x2', x2)
               .attr('y2', y2)
               .attr('stroke', '#ff00ff')
               .attr('stroke-width', 2)
               .attr('opacity', 0.5)
               .style('stroke-dasharray', '5,5');
             
             [x1, x2].forEach((x, idx) => {
               const y = idx === 0 ? y1 : y2;
               svg.append('circle')
                 .attr('cx', x)
                 .attr('cy', y)
                 .attr('r', 4)
                 .attr('fill', '#ff00ff')
                 .attr('opacity', 0.7);
             });
           }
         }
       }
       
       function drawPrimordialAbsenceVisualization(svg, samples, width, height, mode) {
         if (mode === 'pre-cosmic-silence') {
           // Vast emptiness with subtle potential
           samples.slice(0, 3).forEach((sample, i) => {
             const x = (sample % width);
             const y = ((sample >> 8) % height);
             svg.append('circle')
               .attr('cx', x)
               .attr('cy', y)
               .attr('r', 1)
               .attr('fill', '#444')
               .attr('opacity', 0.3);
           });
         } else if (mode === 'original-darkness') {
           // Creative darkness birthing light
           const centerX = width / 2, centerY = height / 2;
           samples.slice(0, 8).forEach((sample, i) => {
             const angle = (sample / 16777216) * Math.PI * 2;
             const radius = 20 + (sample % 80);
             const x = centerX + Math.cos(angle) * radius;
             const y = centerY + Math.sin(angle) * radius;
             
             svg.append('circle')
               .attr('cx', x)
               .attr('cy', y)
               .attr('r', 2 + (sample % 3))
               .attr('fill', `hsl(0, 0%, ${(sample % 30) + 10}%)`)
               .attr('opacity', 0.6);
           });
         } else {
           // Foundational emptiness - spacious openness
           const gridSize = 6;
           samples.slice(0, 36).forEach((sample, i) => {
             const row = Math.floor(i / gridSize);
             const col = i % gridSize;
             const x = (col + 1) * (width / (gridSize + 1));
             const y = (row + 1) * (height / (gridSize + 1));
             
             if (sample % 100 > 80) { // Only show occasional points
               svg.append('circle')
                 .attr('cx', x)
                 .attr('cy', y)
                 .attr('r', 1.5)
                 .attr('fill', '#888')
                 .attr('opacity', 0.4);
             }
           });
         }
       }
       
       function drawTemporalVoidsVisualization(svg, samples, width, height, mode) {
         if (mode === 'chronos-dissolution') {
           // Fragmenting timeline
           samples.slice(0, 25).forEach((sample, i) => {
             const x = (i / 25) * width;
             const y = height / 2 + ((sample % 60) - 30);
             const fragmentSize = (sample % 15) + 5;
             
             svg.append('rect')
               .attr('x', x - fragmentSize/2)
               .attr('y', y - 2)
               .attr('width', fragmentSize)
               .attr('height', 4)
               .attr('fill', `hsl(${(sample % 360)}, 60%, 50%)`)
               .attr('opacity', 0.7);
           });
         } else if (mode === 'eternal-intervals') {
           // Recursive loops
           const centerX = width / 2, centerY = height / 2;
           [30, 50, 70].forEach((radius, ring) => {
             const points = samples.slice(ring * 8, (ring + 1) * 8);
             points.forEach((sample, i) => {
               const angle = (i / 8) * Math.PI * 2;
               const x = centerX + Math.cos(angle) * radius;
               const y = centerY + Math.sin(angle) * radius;
               
               svg.append('circle')
                 .attr('cx', x)
                 .attr('cy', y)
                 .attr('r', 3)
                 .attr('fill', '#ffaa00')
                 .attr('opacity', 0.6);
               
               // Connect to next point in ring
               const nextAngle = ((i + 1) / 8) * Math.PI * 2;
               const nextX = centerX + Math.cos(nextAngle) * radius;
               const nextY = centerY + Math.sin(nextAngle) * radius;
               
               svg.append('line')
                 .attr('x1', x)
                 .attr('y1', y)
                 .attr('x2', nextX)
                 .attr('y2', nextY)
                 .attr('stroke', '#ffaa00')
                 .attr('stroke-width', 1)
                 .attr('opacity', 0.4);
             });
           });
         } else {
           // Anachronistic zones - backwards flow
           const flowPoints = samples.slice(0, 20).map((sample, i) => ({
             x: width - (i / 20) * width, // Reverse direction
             y: height / 2 + ((sample % 40) - 20)
           }));
           
           flowPoints.forEach((point, i) => {
             if (i > 0) {
               svg.append('line')
                 .attr('x1', flowPoints[i-1].x)
                 .attr('y1', flowPoints[i-1].y)
                 .attr('x2', point.x)
                 .attr('y2', point.y)
                 .attr('stroke', '#00aaff')
                 .attr('stroke-width', 2)
                 .attr('opacity', 0.6)
                 .style('marker-end', 'url(#backwardsArrow)');
             }
           });
         }
       }
       
       function drawStellarDissolutionVisualization(svg, samples, width, height, mode) {
         const centerX = width / 2, centerY = height / 2;
         
         if (mode === 'nuclear-decay') {
           // Nuclear core with decay particles
           svg.append('circle')
             .attr('cx', centerX)
             .attr('cy', centerY)
             .attr('r', 15)
             .attr('fill', '#ff4400')
             .attr('opacity', 0.9);
           
           samples.slice(0, 20).forEach((sample, i) => {
             const angle = (sample / 16777216) * Math.PI * 2;
             const distance = 20 + ((sample >> 8) % 100);
             const x = centerX + Math.cos(angle) * distance;
             const y = centerY + Math.sin(angle) * distance;
             
             svg.append('circle')
               .attr('cx', x)
               .attr('cy', y)
               .attr('r', 1 + (sample % 3))
               .attr('fill', '#ff6600')
               .attr('opacity', 0.8);
           });
         } else if (mode === 'gravitational-collapse') {
           // Collapsing matter spiraling inward
           samples.slice(0, 30).forEach((sample, i) => {
             const spiralAngle = (i / 30) * Math.PI * 6;
             const spiralRadius = 80 - (i / 30) * 60;
             const x = centerX + Math.cos(spiralAngle) * spiralRadius;
             const y = centerY + Math.sin(spiralAngle) * spiralRadius;
             
             svg.append('circle')
               .attr('cx', x)
               .attr('cy', y)
               .attr('r', 2 + (sample % 3))
               .attr('fill', `hsl(${30 - i}, 80%, 60%)`)
               .attr('opacity', 0.7);
           });
         } else {
           // Cosmic cooling - fading universe
           samples.slice(0, 40).forEach((sample, i) => {
             const x = (sample % width);
             const y = ((sample >> 8) % height);
             const brightness = Math.max(0, 50 - i);
             
             svg.append('circle')
               .attr('cx', x)
               .attr('cy', y)
               .attr('r', 1)
               .attr('fill', `hsl(0, 0%, ${brightness}%)`)
               .attr('opacity', brightness / 100);
           });
         }
       }
       
       function drawEntropicCascadeVisualization(svg, samples, width, height, mode) {
         // Cascading dissolution patterns
         samples.slice(0, 35).forEach((sample, i) => {
           const cascade_level = Math.floor(i / 7);
           const x = ((sample + cascade_level * 1000) % width);
           const y = cascade_level * (height / 5) + ((sample >> 8) % 30);
           const size = 5 - cascade_level;
           const opacity = 0.8 - (cascade_level * 0.15);
           
           svg.append('circle')
             .attr('cx', x)
             .attr('cy', y)
             .attr('r', Math.max(1, size))
             .attr('fill', `hsl(${(sample % 60) + 180}, 70%, 50%)`)
             .attr('opacity', Math.max(0.1, opacity));
         });
       }
       
       function drawNeuralDissolutionVisualization(svg, samples, width, height, mode) {
         // Neural network with weakening connections
         const nodes = samples.slice(0, 12).map((sample, i) => ({
           x: (sample % width),
           y: ((sample >> 8) % height),
           strength: (sample % 100) / 100
         }));
         
         // Draw weakening connections
         nodes.forEach((node, i) => {
           if (i < nodes.length - 1) {
             const connection_strength = (node.strength + nodes[i + 1].strength) / 2;
             svg.append('line')
               .attr('x1', node.x)
               .attr('y1', node.y)
               .attr('x2', nodes[i + 1].x)
               .attr('y2', nodes[i + 1].y)
               .attr('stroke', connection_strength > 0.5 ? '#00ff88' : '#ff4444')
               .attr('stroke-width', connection_strength * 3)
               .attr('opacity', connection_strength);
           }
         });
         
         // Draw nodes with varying health
         nodes.forEach(node => {
           svg.append('circle')
             .attr('cx', node.x)
             .attr('cy', node.y)
             .attr('r', 3 + node.strength * 4)
             .attr('fill', node.strength > 0.6 ? '#00ff88' : node.strength > 0.3 ? '#ffaa00' : '#ff4444')
             .attr('opacity', 0.8);
         });
       }
       
       function drawLinguisticVoidVisualization(svg, samples, width, height, mode) {
         if (mode === 'semantic-collapse') {
           // Words dissolving into noise
           const words = ['MEANING', 'SYMBOL', 'SIGN', 'WORD', 'CONCEPT'];
           samples.slice(0, 5).forEach((sample, i) => {
             const word = words[i];
             const x = (sample % width);
             const y = ((sample >> 8) % height);
             const dissolution = (sample % 100) / 100;
             
             svg.append('text')
               .attr('x', x)
               .attr('y', y)
               .attr('font-size', `${8 + dissolution * 6}px`)
               .attr('fill', `hsl(0, 0%, ${100 - dissolution * 80}%)`)
               .attr('opacity', 1 - dissolution * 0.7)
               .text(word.substring(0, Math.max(1, Math.floor(word.length * (1 - dissolution)))));
           });
         } else {
           // Communication static
           samples.slice(0, 100).forEach((sample, i) => {
             if (sample % 3 === 0) {
               const x = (sample % width);
               const y = ((sample >> 8) % height);
               svg.append('rect')
                 .attr('x', x)
                 .attr('y', y)
                 .attr('width', 1)
                 .attr('height', 1)
                 .attr('fill', sample % 2 === 0 ? '#ffffff' : '#000000')
                 .attr('opacity', 0.8);
             }
           });
         }
       }
       
       function drawCleanBars(svg, samples, width, height) {
         const margin = {top: 20, right: 20, bottom: 30, left: 40};
         const chartWidth = width - margin.left - margin.right;
         const chartHeight = height - margin.top - margin.bottom;
         
         const x = d3.scaleBand()
           .domain(d3.range(samples.length))
           .range([margin.left, chartWidth])
           .padding(0.1);
         
         const y = d3.scaleLinear()
           .domain([0, d3.max(samples)])
           .range([chartHeight, margin.top]);
         
         svg.selectAll('rect')
           .data(samples)
           .enter().append('rect')
           .attr('x', (d, i) => x(i))
           .attr('y', d => y(d))
           .attr('width', x.bandwidth())
           .attr('height', d => chartHeight - y(d))
           .attr('fill', '#00ff88')
           .attr('opacity', 0.8);
       }
       
       function refreshAll() {
         const sources = ['anu-qrng', 'nist-beacon', 'hotbits', 'random-org', 'atmospheric', 
                         'system-secure', 'timing-jitter', 'memory-entropy', 'network-latency'];
         sources.forEach(source => fetchEntropy(source));
       }
       
       function fetchReal() {
         refreshAll();
       }
       
       // Initialize
       setTimeout(refreshAll, 500);
     "]]])) ; Close script, body, html tags and main-page function)

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
  
  (route/not-found "Not Found"))

(def app (wrap-params app-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3002"))]
    (println "Starting Enhanced Entropy Gallery on port" port)
    (jetty/run-jetty app {:port port :join? true})))