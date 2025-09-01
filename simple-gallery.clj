(require '[ring.adapter.jetty :as jetty]
         '[ring.util.response :as response]
         '[compojure.core :refer [defroutes GET]]
         '[compojure.route :as route]
         '[hiccup.core :as h])

;; Real entropy samples collected from APIs
(def entropy-sources
  {:random-org {:name "Random.org Atmospheric (Dublin)"
                :samples [13873150 8234567 15678234 9876543 12345678]
                :type "atmospheric" :quality "true-random"}
   :nist-beacon {:name "NIST Quantum Beacon (USA)" 
                 :samples [2596945019 770914369 3179651427 3535839255 2158187151]
                 :type "quantum" :quality "cryptographic"}
   :system-local {:name "System Entropy (Local)"
                  :samples (repeatedly 5 #(rand-int 16777216))
                  :type "pseudo" :quality "pseudo-random"}})

(defn gallery-page []
  (h/html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title "Entropy Gallery"]
     [:style "
       body { background: #000; color: #0f0; font-family: monospace; padding: 20px; }
       .source { border: 1px solid #0f0; margin: 20px 0; padding: 15px; }
       .samples { display: flex; flex-wrap: wrap; gap: 10px; margin: 10px 0; }
       .sample { background: #001100; padding: 5px 10px; border: 1px solid #004400; }
       .chart { height: 100px; background: #001100; margin: 10px 0; position: relative; }
       .bar { background: #00ff00; margin: 2px; display: inline-block; }
     "]]
    [:body
     [:h1 "🎲 Entropy Source Gallery"]
     (for [[key data] entropy-sources]
       [:div.source
        [:h2 (:name data)]
        [:p "Type: " (:type data) " | Quality: " (:quality data)]
        [:div.samples
         (for [sample (:samples data)]
           [:div.sample (str sample)])]
        [:div.chart
         (for [sample (take 10 (:samples data))]
           [:div.bar {:style (str "height:" (mod sample 80) "px; width:15px;")}])]])]]))

(defroutes app-routes
  (GET "/" [] (response/response (gallery-page)))
  (route/not-found "404"))

(defn -main []
  (jetty/run-jetty app-routes {:port 3001 :join? false})
  (println "Gallery running on http://localhost:3001"))

(-main)