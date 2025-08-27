(ns void-shrine.web.minimal-server
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as response]
            [clojure.core.async :as async :refer [go go-loop <! >! chan timeout]]
            [clojure.data.json :as json]
            [hiccup.core :as h]))

;;; Simplified state
(def chaos-state (atom {:entropy-values []
                        :void-manifestations []
                        :chaos-metrics {:total-entropy 0
                                        :void-depth 0
                                        :dissolution-rate 0}
                        :timestamp (System/currentTimeMillis)}))

;;; Simple entropy generation
(defn generate-entropy []
  (repeatedly 10 #(rand-int 256)))

(defn entropy-to-color [entropy]
  (let [r (bit-and entropy 0xFF)
        g (bit-and (bit-shift-right entropy 8) 0xFF)
        b (bit-and (bit-shift-right entropy 16) 0xFF)]
    (str "rgb(" r "," g "," b ")")))

;;; Simple UI
(defn main-page []
  (str
   "<!DOCTYPE html>"
   (h/html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:title "Void Shrine - Chaos Harvester"]
      [:style "
        body { 
          background: #000011; 
          color: #ff0066; 
          font-family: 'Courier New', monospace; 
          text-align: center;
          padding: 2rem;
        }
        .void-header h1 { 
          font-size: 3rem; 
          text-shadow: 0 0 20px #ff0066; 
          animation: pulse 2s infinite; 
        }
        @keyframes pulse {
          0%, 100% { text-shadow: 0 0 20px #ff0066; }
          50% { text-shadow: 0 0 40px #ff0066, 0 0 60px #ff0066; }
        }
        .entropy-values { 
          display: flex; 
          justify-content: center; 
          gap: 0.5rem; 
          margin: 2rem 0; 
          flex-wrap: wrap;
        }
        .entropy-byte { 
          padding: 0.5rem; 
          border: 2px solid #ff0066; 
          font-size: 1rem; 
          min-width: 3rem; 
          text-align: center; 
        }
        button { 
          padding: 1rem 2rem; 
          background: transparent; 
          border: 2px solid #ff0066; 
          color: #ff0066; 
          font-family: inherit; 
          font-size: 1.2rem; 
          cursor: pointer; 
          margin: 1rem;
          transition: all 0.3s;
        }
        button:hover { 
          background: #ff0066; 
          color: #000000; 
          box-shadow: 0 0 20px #ff0066; 
        }
        .metrics { 
          margin: 2rem 0; 
          font-size: 1.2rem; 
        }
        .metric { 
          margin: 1rem 0; 
        }
        .metric-value { 
          color: #00ffff; 
        }"]]
     [:body
      [:div.void-header
       [:h1 "VOID SHRINE"]
       [:div.subtitle "Harvesting Entropy from the Quantum Abyss"]]
      
      [:div.metrics
       [:div.metric
        "Total Entropy: "
        [:span.metric-value (get-in @chaos-state [:chaos-metrics :total-entropy])]]
       [:div.metric
        "Void Depth: "
        [:span.metric-value (get-in @chaos-state [:chaos-metrics :void-depth])]]
       [:div.metric
        "Active Since: "
        [:span.metric-value (java.util.Date. (:timestamp @chaos-state))]]]
      
      [:div.entropy-values
       (for [val (take 20 (:entropy-values @chaos-state))]
         [:span.entropy-byte
          {:style {:background-color (entropy-to-color val)
                   :color (if (< val 128) "#ffffff" "#000000")}}
          (format "%02X" val)])]
      
      [:div
       [:button {:onclick "fetch('/api/chaos', {method: 'POST'}); location.reload();"} 
        "Invoke Chaos"]
       [:button {:onclick "fetch('/api/entropy', {method: 'POST'}); location.reload();"} 
        "Harvest Entropy"]
       [:button {:onclick "fetch('/api/void', {method: 'POST'}); location.reload();"} 
        "Enter Void"]]]])))

;;; Routes
(defroutes app-routes
  (GET "/" []
    (response/response (main-page)))

  (POST "/api/chaos" []
    (let [new-entropy (generate-entropy)]
      (swap! chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update-in [:chaos-metrics :total-entropy] + (reduce + new-entropy))
                  (update-in [:chaos-metrics :void-depth] inc)))
      (response/response (json/write-str {:status :success :action :chaos-triggered}))))

  (POST "/api/entropy" []
    (let [new-entropy (generate-entropy)]
      (swap! chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update-in [:chaos-metrics :total-entropy] + (reduce + new-entropy))))
      (response/response (json/write-str {:status :success :action :entropy-harvested}))))

  (POST "/api/void" []
    (let [new-entropy (repeatedly 20 #(rand-int 256))]
      (swap! chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update-in [:chaos-metrics :void-depth] + 5)
                  (assoc :void-status :deep-entry)))
      (response/response (json/write-str {:status :success :action :void-entered}))))

  (GET "/api/state" []
    (response/response (json/write-str @chaos-state)))

  (route/not-found "404 - Lost in the Void"))

(def app (wrap-content-type app-routes))

(defn start-server! [port]
  ;; Background entropy generation
  (go-loop []
    (<! (timeout 5000))
    (let [new-entropy (rand-int 256)]
      (swap! chaos-state
             #(-> %
                  (update :entropy-values conj new-entropy)
                  (update-in [:chaos-metrics :total-entropy] + new-entropy)
                  (update :entropy-values (fn [vals] (take 100 vals))))))
    (recur))
  
  (println "🌀 Void Shrine awakening on port" port)
  (println "🔥 Minimal chaos system online...")
  (jetty/run-jetty app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3000"))]
    (start-server! port)))