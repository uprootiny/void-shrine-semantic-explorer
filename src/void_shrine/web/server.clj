(ns void-shrine.web.server
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :as response]
            [clojure.core.async :as async :refer [go go-loop <! >! chan timeout]]
            [clojure.data.json :as json]
            [void-shrine.entropy.harvester :as harvester]
            [void-shrine.chaos.ontology :as ontology]
            [void-shrine.chaos.transformers :as transformers]
            [void-shrine.web.ui :as ui]
            [void-shrine.core :as core]
            [clojure.spec.alpha :as spec]
            [cats.core :as cats]
            [cats.monad.either :as either]))

;;; Global state management
(def chaos-state (atom (core/chaos-state)))
(def entropy-channel (harvester/entropy-stream))
(def connected-clients (atom #{}))

;;; WebSocket-like real-time updates via Server-Sent Events
(defn sse-response
  "Create Server-Sent Events response"
  [data]
  {:status 200
   :headers {"Content-Type" "text/event-stream"
             "Cache-Control" "no-cache"
             "Connection" "keep-alive"
             "Access-Control-Allow-Origin" "*"}
   :body (str "data: " (json/write-str data) "\n\n")})

(defn chaos-event-stream
  "Stream chaos events to connected clients"
  []
  (let [output-chan (chan 100)]
    (go-loop []
      (when-let [entropy-data (<! entropy-channel)]
        (let [updated-state (swap! chaos-state 
                                   #(-> %
                                        (core/update-chaos-state (:mixed-value entropy-data))
                                        (update :void-manifestations conj 
                                               (ontology/void-manifestation (:seed entropy-data)))))]
          (>! output-chan {:type :chaos-update
                          :data updated-state
                          :entropy entropy-data}))
        (recur)))
    output-chan))

;;; API endpoints with advanced Clojure features
(defn api-response
  "Create standardized API response with validation"
  [data]
  (cats/>>= (either/right data)
            (fn [validated-data]
              (either/right {:status :success
                            :data validated-data
                            :timestamp (System/currentTimeMillis)}))))

(defn trigger-chaos
  "Trigger chaos event"
  []
  (go
    (let [entropy-burst (repeatedly 10 #(rand-int 256))
          chaos-seed (harvester/chaos-seed [{:data entropy-burst :source :manual-trigger}])
          manifestation (ontology/void-manifestation (:seed chaos-seed))
          enhanced-manifestation (assoc manifestation 
                                        :poetry (ontology/generate-void-poetry manifestation))]
      (swap! chaos-state
             #(-> %
                  (update :entropy-values into entropy-burst)
                  (update :void-manifestations conj enhanced-manifestation)
                  (transformers/amplify-entropy 1.2))))))

(defn harvest-entropy-endpoint
  "Force entropy harvest"
  []
  (go
    (doseq [source harvester/entropy-sources]
      (when-let [entropy (harvester/fetch-entropy source)]
        (when (= :success (:status entropy))
          (swap! chaos-state 
                 #(core/update-chaos-state % (first (:data entropy)))))))))

(defn enter-void-endpoint
  "Enter the void - deep manifestation"
  []
  (let [current-entropy (get-in @chaos-state [:entropy-values])
        deep-seed (reduce + (take 10 current-entropy))
        deep-manifestation (ontology/void-manifestation deep-seed)
        void-pattern (transformers/chaos-pattern-match @chaos-state)]
    (swap! chaos-state
           #(-> %
                (assoc :void-status :deep-entry)
                (update :void-manifestations conj 
                       (assoc deep-manifestation 
                              :depth :deep
                              :pattern void-pattern))))))

;;; Advanced routing with pattern matching
(defroutes app-routes
  ;; Main chaos dashboard
  (GET "/" []
    (let [state-snapshot @chaos-state]
      (response/response (ui/main-page state-snapshot))))

  ;; API endpoints with monadic error handling
  (POST "/api/chaos" []
    (let [result (trigger-chaos)]
      (response/response 
       (json/write-str (either/extract (api-response {:action :chaos-triggered}))))))

  (POST "/api/entropy" []
    (let [result (harvest-entropy-endpoint)]
      (response/response 
       (json/write-str (either/extract (api-response {:action :entropy-harvested}))))))

  (POST "/api/void" []
    (let [result (enter-void-endpoint)]
      (response/response 
       (json/write-str (either/extract (api-response {:action :void-entered}))))))

  ;; Real-time state endpoint
  (GET "/api/state" []
    (response/response (json/write-str @chaos-state)))

  ;; Server-Sent Events for real-time updates
  (GET "/api/events" []
    (let [event-chan (chaos-event-stream)]
      {:status 200
       :headers {"Content-Type" "text/event-stream"
                 "Cache-Control" "no-cache"
                 "Connection" "keep-alive"
                 "Access-Control-Allow-Origin" "*"}
       :body (async/pipe event-chan (chan))}))

  ;; Chaos analytics endpoint with specter transformations
  (GET "/api/analytics" []
    (let [state @chaos-state
          analytics (transformers/safe-entropy-computation 
                     (:entropy-values state))
          void-analysis (transformers/transmute-void-paths 
                         state 
                         #(take 3 %))]
      (response/response 
       (json/write-str {:analytics (cats/extract analytics)
                       :void-analysis void-analysis}))))

  ;; Pattern recognition endpoint
  (GET "/api/patterns" []
    (let [patterns (transformers/chaos-pattern-match @chaos-state)]
      (response/response (json/write-str patterns))))

  ;; Advanced chaos query with lens navigation
  (GET "/api/chaos/query/:path" [path]
    (let [query-result (transformers/chaos-get-in @chaos-state (keyword path))]
      (response/response (json/write-str {:path path :result query-result}))))

  ;; Static resources
  (route/resources "/")
  (route/not-found "404 - Lost in the Void"))

;;; Middleware stack with advanced features
(def app
  (-> app-routes
      (wrap-content-type)
      (wrap-not-modified)))

;;; Server initialization with chaos background processes
(defn start-chaos-processes!
  "Start background chaos processing"
  []
  (go-loop []
    (<! (timeout 1000))
    (when-let [entropy-data (<! entropy-channel)]
      (let [processed (transformers/chaos-pipeline @chaos-state)]
        (when (either/right? processed)
          (swap! chaos-state merge (either/extract processed)))))
    (recur))
  
  ;; Periodic void manifestations
  (go-loop []
    (<! (timeout 5000))
    (when (> (count (:entropy-values @chaos-state)) 10)
      (let [seed (reduce + (take 5 (:entropy-values @chaos-state)))
            manifestation (ontology/void-manifestation seed)]
        (swap! chaos-state 
               update :void-manifestations 
               #(take 50 (conj % manifestation)))))
    (recur))
  
  ;; Chaos state mutations
  (go-loop []
    (<! (timeout 2000))
    (swap! chaos-state 
           #(-> %
                (transformers/entropy-surge-mutation)
                (transformers/void-deepening-mutation)))
    (recur)))

(defn start-server!
  "Start the chaos server"
  [port]
  (start-chaos-processes!)
  (println (str "🌀 Void Shrine awakening on port " port))
  (println "🔥 Entropy harvesting initiated...")
  (println "💀 Chaos patterns emerging...")
  (jetty/run-jetty app {:port port :join? false}))

(defn -main
  "Main entry point"
  [& args]
  (let [port (Integer/parseInt (or (first args) "3000"))]
    (start-server! port)))