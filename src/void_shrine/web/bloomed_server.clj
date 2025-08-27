(ns void-shrine.web.bloomed-server
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as response]
            [clojure.core.async :as async :refer [go go-loop <! >! chan timeout]]
            [clojure.data.json :as json]
            [void-shrine.chaos.bloomed-traversal :as traversal]
            [void-shrine.chaos.enhanced-manifestations :as manifestations]
            [void-shrine.chaos.bloomed-ontology :as ontology]
            [void-shrine.web.bloomed-ui :as ui]))

;;; Enhanced state with bloomed ontology
(def bloomed-chaos-state 
  (atom {:entropy-values []
         :void-manifestations []
         :fractal-paths []
         :void-constellations []
         :manifestation-symphonies []
         :dimensional-mappings []
         :void-weather-systems []
         :chaos-metrics {:total-entropy 0
                        :void-depth 0
                        :dissolution-rate 0
                        :ontology-nodes (traversal/count-total-nodes ontology/infinite-void-tree)
                        :fractal-dimensions 0
                        :constellation-density 0}
         :timestamp (System/currentTimeMillis)}))

;;; Enhanced entropy generation
(defn generate-quantum-entropy []
  (repeatedly 20 #(rand-int 256)))

(defn generate-fractal-burst [seed]
  (traversal/fractal-void-descent seed 8))

(defn generate-void-constellation [entropy]
  (traversal/void-constellation entropy 16))

(defn generate-dimensional-mapping [entropy]
  (traversal/multidimensional-void-mapping entropy 12))

(defn generate-manifestation-symphony [entropy]
  (manifestations/void-manifestation-symphony entropy))

(defn generate-void-weather [entropy]
  (traversal/void-weather-system entropy))

;;; Enhanced UI rendering
(defn bloomed-main-page []
  (ui/bloomed-main-page @bloomed-chaos-state))

;;; Enhanced API endpoints
(defroutes bloomed-app-routes
  ;; Main bloomed dashboard
  (GET "/" []
    (response/response (bloomed-main-page)))

  ;; Enhanced chaos bloom
  (POST "/api/bloom-chaos" []
    (let [new-entropy (generate-quantum-entropy)
          total-entropy (reduce + new-entropy)
          fractal-paths (generate-fractal-burst total-entropy)
          constellation (generate-void-constellation total-entropy)
          symphony (generate-manifestation-symphony total-entropy)
          weather (generate-void-weather total-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update :fractal-paths conj fractal-paths)
                  (update :void-constellations conj constellation)
                  (update :manifestation-symphonies conj symphony)
                  (update :void-weather-systems conj weather)
                  (update-in [:chaos-metrics :total-entropy] + total-entropy)
                  (update-in [:chaos-metrics :void-depth] + (count fractal-paths))
                  (update-in [:chaos-metrics :fractal-dimensions] inc)
                  (update-in [:chaos-metrics :constellation-density] + (count constellation))))
      (response/response 
       (json/write-str {:status :success 
                       :action :chaos-bloomed
                       :nodes-generated (count fractal-paths)
                       :constellation-stars (count constellation)}))))

  ;; Fractal descent trigger
  (POST "/api/fractal-descent" []
    (let [base-entropy (or (first (:entropy-values @bloomed-chaos-state)) (rand-int 1000000))
          fractal-paths (generate-fractal-burst base-entropy)
          dimensional-mapping (generate-dimensional-mapping base-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :fractal-paths conj fractal-paths)
                  (update :dimensional-mappings conj dimensional-mapping)
                  (update-in [:chaos-metrics :fractal-dimensions] inc)
                  (update-in [:chaos-metrics :void-depth] + (count fractal-paths))))
      (response/response
       (json/write-str {:status :success
                       :action :fractal-descent-initiated
                       :depth (count fractal-paths)
                       :dimensions (count (:dimensions dimensional-mapping))}))))

  ;; Void constellation generation
  (POST "/api/void-constellation" []
    (let [base-entropy (or (first (:entropy-values @bloomed-chaos-state)) (rand-int 1000000))
          constellation (generate-void-constellation base-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :void-constellations conj constellation)
                  (update-in [:chaos-metrics :constellation-density] + (count constellation))))
      (response/response
       (json/write-str {:status :success
                       :action :void-constellation-manifested
                       :stars (count constellation)}))))

  ;; Manifestation symphony creation
  (POST "/api/manifestation-symphony" []
    (let [base-entropy (or (first (:entropy-values @bloomed-chaos-state)) (rand-int 1000000))
          symphony (generate-manifestation-symphony base-entropy)
          weather (generate-void-weather base-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :manifestation-symphonies conj symphony)
                  (update :void-weather-systems conj weather)
                  (assoc-in [:chaos-metrics :last-symphony] 
                           (get-in symphony [:identity :void-name]))))
      (response/response
       (json/write-str {:status :success
                       :action :manifestation-symphony-composed
                       :entity-name (get-in symphony [:identity :void-name])
                       :classification (get-in symphony [:identity :classification])
                       :power-level (get-in symphony [:identity :power-level])}))))

  ;; Bloomed state endpoint
  (GET "/api/bloomed-state" []
    (response/response (json/write-str @bloomed-chaos-state)))

  ;; Ontology statistics
  (GET "/api/ontology-stats" []
    (let [total-nodes (traversal/count-total-nodes manifestations/infinite-void-tree)
          all-paths (take 100 (traversal/collect-all-paths manifestations/infinite-void-tree))
          depth-analysis (group-by count all-paths)]
      (response/response
       (json/write-str {:total-nodes total-nodes
                       :sample-paths (count all-paths)
                       :depth-distribution (into {} (map (fn [[k v]] [k (count v)]) depth-analysis))
                       :deepest-path-length (apply max (map count all-paths))}))))

  ;; Random void path exploration
  (GET "/api/explore-void/:entropy" [entropy]
    (let [entropy-value (Integer/parseInt entropy)
          path (traversal/entropy-guided-traversal entropy-value 8)
          constellation (traversal/void-constellation entropy-value 6)
          weather (traversal/void-weather-system entropy-value)]
      (response/response
       (json/write-str {:exploration-path path
                       :related-constellation constellation
                       :void-weather weather}))))

  ;; Semantic clustering
  (GET "/api/semantic-clusters/:entropy/:clusters" [entropy clusters]
    (let [entropy-value (Integer/parseInt entropy)
          cluster-count (Integer/parseInt clusters)
          clusters (traversal/void-semantic-clustering entropy-value cluster-count)]
      (response/response
       (json/write-str {:clusters clusters
                       :cluster-count (count clusters)}))))

  ;; Legacy endpoints for compatibility
  (POST "/api/chaos" []
    (let [new-entropy (generate-quantum-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update-in [:chaos-metrics :total-entropy] + (reduce + new-entropy))))
      (response/response (json/write-str {:status :success :action :chaos-triggered}))))

  (POST "/api/entropy" []
    (let [new-entropy (generate-quantum-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update-in [:chaos-metrics :total-entropy] + (reduce + new-entropy))))
      (response/response (json/write-str {:status :success :action :entropy-harvested}))))

  (POST "/api/void" []
    (let [new-entropy (repeatedly 50 #(rand-int 256))
          symphony (generate-manifestation-symphony (reduce + new-entropy))]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :entropy-values into new-entropy)
                  (update :manifestation-symphonies conj symphony)
                  (update-in [:chaos-metrics :void-depth] + 10)
                  (assoc :void-status :deep-entry)))
      (response/response (json/write-str {:status :success :action :void-entered}))))

  (GET "/api/state" []
    (response/response (json/write-str @bloomed-chaos-state)))

  (route/not-found "404 - Lost in the Infinite Void"))

(def bloomed-app (wrap-content-type bloomed-app-routes))

(defn start-bloomed-chaos-processes!
  "Start enhanced background chaos processing"
  []
  ;; Continuous quantum entropy generation
  (go-loop []
    (<! (timeout 3000))
    (let [quantum-entropy (rand-int 256)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :entropy-values conj quantum-entropy)
                  (update-in [:chaos-metrics :total-entropy] + quantum-entropy)
                  (update :entropy-values (fn [vals] (take 500 vals))))))
    (recur))
  
  ;; Periodic fractal generation
  (go-loop []
    (<! (timeout 15000))
    (when (> (count (:entropy-values @bloomed-chaos-state)) 5)
      (let [base-entropy (reduce + (take 5 (:entropy-values @bloomed-chaos-state)))
            fractal-paths (generate-fractal-burst base-entropy)]
        (swap! bloomed-chaos-state 
               #(-> %
                    (update :fractal-paths conj fractal-paths)
                    (update :fractal-paths (fn [fps] (take 20 fps)))
                    (update-in [:chaos-metrics :fractal-dimensions] inc)))))
    (recur))
  
  ;; Constellation evolution
  (go-loop []
    (<! (timeout 10000))
    (when (> (count (:entropy-values @bloomed-chaos-state)) 3)
      (let [constellation-entropy (bit-xor (first (:entropy-values @bloomed-chaos-state))
                                          (System/currentTimeMillis))
            constellation (generate-void-constellation constellation-entropy)]
        (swap! bloomed-chaos-state
               #(-> %
                    (update :void-constellations conj constellation)
                    (update :void-constellations (fn [consts] (take 10 consts)))
                    (update-in [:chaos-metrics :constellation-density] + (count constellation))))))
    (recur))
  
  ;; Manifestation symphony generation
  (go-loop []
    (<! (timeout 20000))
    (when (> (count (:entropy-values @bloomed-chaos-state)) 10)
      (let [symphony-entropy (hash (str (:entropy-values @bloomed-chaos-state) (System/currentTimeMillis)))
            symphony (generate-manifestation-symphony symphony-entropy)
            weather (generate-void-weather symphony-entropy)]
        (swap! bloomed-chaos-state
               #(-> %
                    (update :manifestation-symphonies conj symphony)
                    (update :void-weather-systems conj weather)
                    (update :manifestation-symphonies (fn [syms] (take 5 syms)))
                    (update :void-weather-systems (fn [weathers] (take 3 weathers)))))))
    (recur))
  
  ;; Dimensional flux updates
  (go-loop []
    (<! (timeout 8000))
    (let [flux-entropy (mod (System/currentTimeMillis) 1000000)
          dimensional-mapping (generate-dimensional-mapping flux-entropy)]
      (swap! bloomed-chaos-state
             #(-> %
                  (update :dimensional-mappings conj dimensional-mapping)
                  (update :dimensional-mappings (fn [dims] (take 3 dims))))))
    (recur)))

(defn start-bloomed-server!
  "Start the enhanced bloomed chaos server"
  [port]
  (start-bloomed-chaos-processes!)
  (println "🌀∞ Bloomed Void Shrine awakening on port" port)
  (println "🔥∞ Infinite ontology manifesting...")
  (println "💀∞ Fractal chaos patterns emerging...")
  (println "⚡∞" (traversal/count-total-nodes ontology/infinite-void-tree) "void nodes available for traversal...")
  (jetty/run-jetty bloomed-app {:port port :join? false}))

(defn -main
  "Main entry point for bloomed system"
  [& args]
  (let [port (Integer/parseInt (or (first args) "3000"))]
    (start-bloomed-server! port)))