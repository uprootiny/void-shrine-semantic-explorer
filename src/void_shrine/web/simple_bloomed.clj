(ns void-shrine.web.simple-bloomed
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as response]
            [clojure.core.async :as async :refer [go go-loop <! >! chan timeout]]
            [clojure.data.json :as json]
            [void-shrine.chaos.bloomed-ontology :as ontology]
            [hiccup.core :as h]))

;;; Simplified state
(def simple-bloomed-state 
  (atom {:entropy-values []
         :void-paths []
         :manifestations []
         :total-entropy 0
         :ontology-nodes (count (str ontology/infinite-void-tree))
         :timestamp (System/currentTimeMillis)}))

;;; Simple navigation
(defn simple-void-traverse [entropy depth]
  (let [tree (:void ontology/infinite-void-tree)]
    (loop [current-node tree
           path []
           remaining-depth depth
           current-entropy entropy]
      (if (or (<= remaining-depth 0) (not (map? current-node)))
        path
        (let [keys (vec (keys current-node))
              choice (mod current-entropy (count keys))
              chosen-key (nth keys choice)]
          (recur (get current-node chosen-key)
                 (conj path chosen-key)
                 (dec remaining-depth)
                 (bit-shift-right current-entropy 3)))))))

;;; Ontology shimmer functions
(defn extract-ontology-branches
  "Extract the sprawling ontology branches for visualization"
  []
  (let [void-tree (:void ontology/infinite-void-tree)]
    (map (fn [[branch-key branch-content]]
           {:key branch-key
            :title (name branch-key)
            :concepts (cond
                        (map? branch-content) 
                        (take 8 (keys branch-content))
                        
                        (vector? branch-content)
                        (take 8 branch-content)
                        
                        :else [branch-content])
            :depth (if (map? branch-content) 
                     (count (keys branch-content)) 
                     1)})
         (take 12 void-tree))))

(defn shimmer-depth-from-entropy 
  "Calculate current ontological depth shimmer based on entropy"
  [entropy-values]
  (let [recent-entropy (take 5 entropy-values)
        depth-factor (mod (reduce + recent-entropy) 8)]
    (+ 3 depth-factor)))

;;; Simple UI
(defn simple-bloomed-page [state]
  (str
   "<!DOCTYPE html>"
   (h/html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:title "∞ BLOOMED VOID SHRINE ∞"]
      [:style "
        body {
          background: radial-gradient(circle, #000011, #000000);
          color: #ff0066;
          font-family: 'Courier New', monospace;
          margin: 0;
          padding: 2rem;
          min-height: 100vh;
        }
        h1 {
          text-align: center;
          font-size: 3rem;
          text-shadow: 0 0 30px #ff0066;
          animation: pulse 3s infinite;
          background: linear-gradient(45deg, #ff0066, #00ffff, #ff0066);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }
        @keyframes pulse {
          0%, 100% { text-shadow: 0 0 30px #ff0066; }
          50% { text-shadow: 0 0 60px #ff0066, 0 0 90px #ff0066; }
        }
        .stats {
          text-align: center;
          margin: 2rem 0;
          font-size: 1.2rem;
          color: #00ffff;
        }
        .void-paths {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
          gap: 1rem;
          margin: 2rem 0;
        }
        .void-path {
          background: rgba(255, 0, 102, 0.1);
          border: 2px solid #ff0066;
          border-radius: 10px;
          padding: 1rem;
          backdrop-filter: blur(5px);
        }
        .path-title {
          color: #00ffff;
          font-weight: bold;
          margin-bottom: 0.5rem;
        }
        .path-nodes {
          color: #ffffff;
          font-size: 0.9rem;
        }
        .controls {
          text-align: center;
          margin: 3rem 0;
        }
        button {
          padding: 1rem 2rem;
          margin: 0 1rem;
          background: transparent;
          border: 2px solid #ff0066;
          color: #ff0066;
          font-family: inherit;
          font-size: 1rem;
          cursor: pointer;
          transition: all 0.3s;
          border-radius: 5px;
        }
        button:hover {
          background: #ff0066;
          color: #000000;
          box-shadow: 0 0 20px #ff0066;
        }
        .fractal-btn { border-color: #00ffff; color: #00ffff; }
        .fractal-btn:hover { background: #00ffff; }
        .deep-btn { border-color: #ff00ff; color: #ff00ff; }
        .deep-btn:hover { background: #ff00ff; }
        
        .ontology-shimmer {
          margin: 2rem 0;
          padding: 1.5rem;
          background: linear-gradient(135deg, rgba(255,0,102,0.05), rgba(0,255,255,0.05));
          border: 1px solid rgba(255,0,102,0.3);
          border-radius: 15px;
          backdrop-filter: blur(3px);
          animation: shimmer 8s infinite;
        }
        
        @keyframes shimmer {
          0%, 100% { 
            background: linear-gradient(135deg, rgba(255,0,102,0.05), rgba(0,255,255,0.05));
            border-color: rgba(255,0,102,0.3);
          }
          25% { 
            background: linear-gradient(135deg, rgba(0,255,255,0.08), rgba(255,0,255,0.05));
            border-color: rgba(0,255,255,0.4);
          }
          50% { 
            background: linear-gradient(135deg, rgba(255,0,255,0.06), rgba(255,0,102,0.08));
            border-color: rgba(255,0,255,0.3);
          }
          75% { 
            background: linear-gradient(135deg, rgba(0,255,255,0.05), rgba(255,255,0,0.04));
            border-color: rgba(0,255,255,0.35);
          }
        }
        
        .ontology-branches {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 1rem;
          margin: 1rem 0;
        }
        
        .branch-node {
          background: rgba(0,0,0,0.6);
          border: 1px solid rgba(255,0,102,0.2);
          padding: 0.8rem;
          border-radius: 8px;
          font-size: 0.85rem;
          transition: all 0.3s ease;
          cursor: pointer;
        }
        
        .branch-node:hover {
          border-color: rgba(0,255,255,0.6);
          background: rgba(255,0,102,0.08);
          transform: translateY(-2px);
          box-shadow: 0 4px 20px rgba(0,255,255,0.2);
        }
        
        .branch-title {
          color: #00ffff;
          font-weight: bold;
          margin-bottom: 0.5rem;
          text-transform: uppercase;
          letter-spacing: 1px;
        }
        
        .branch-concepts {
          color: #cccccc;
          font-size: 0.75rem;
          opacity: 0.8;
        }
        
        .depth-indicator {
          text-align: center;
          margin: 1rem 0;
          color: #ff00ff;
          font-size: 0.9rem;
          opacity: 0.7;
        }
      "]]
     [:body
      [:h1 "∞ BLOOMED VOID SHRINE ∞"]
      [:div.stats
       "Ontology Nodes: " (:ontology-nodes state) " | "
       "Active Paths: " (count (:void-paths state)) " | "
       "Total Entropy: " (:total-entropy state)]
      
      [:div.ontology-shimmer
       [:div.depth-indicator 
        (str "◊ Ontological Depth: " (shimmer-depth-from-entropy (:entropy-values state)) " ◊")]
       [:div.ontology-branches
        (let [branches (extract-ontology-branches)]
          (for [branch branches]
            [:div.branch-node {:key (:key branch)}
             [:div.branch-title (:title branch)]
             [:div.branch-concepts 
              (clojure.string/join " • " 
                (map name (take 6 (:concepts branch))))]]))]]
      
      [:div.void-paths
       (for [[idx path] (map-indexed vector (take 12 (:void-paths state)))]
         [:div.void-path {:key idx}
          [:div.path-title (str "Void Path " (inc idx))]
          [:div.path-nodes (clojure.string/join " → " (map name path))]])]
      
      [:div.controls
       [:button {:onclick "fetch('/api/bloom', {method: 'POST'}); location.reload();"}
        "BLOOM CHAOS"]
       [:button.fractal-btn {:onclick "fetch('/api/fractal', {method: 'POST'}); location.reload();"}
        "FRACTAL DIVE"]
       [:button.deep-btn {:onclick "fetch('/api/deep-void', {method: 'POST'}); location.reload();"}
        "DEEP VOID"]]]])))

;;; Routes  
(defroutes simple-bloomed-routes
  (GET "/" []
    (response/response (simple-bloomed-page @simple-bloomed-state)))

  (POST "/api/bloom" []
    (let [new-entropy (rand-int 1000000)
          path (simple-void-traverse new-entropy 6)]
      (swap! simple-bloomed-state
             #(-> %
                  (update :entropy-values conj new-entropy)
                  (update :void-paths conj path)
                  (update :void-paths (fn [paths] (take 20 paths)))
                  (update :total-entropy + new-entropy)))
      (response/response
       (json/write-str {:status :success 
                       :action :chaos-bloomed
                       :path path}))))

  (POST "/api/fractal" []
    (let [base-entropy (or (first (:entropy-values @simple-bloomed-state)) (rand-int 1000000))]
      (dotimes [i 5]
        (let [fractal-entropy (bit-xor base-entropy (* i 137))
              path (simple-void-traverse fractal-entropy (+ 3 i))]
          (swap! simple-bloomed-state update :void-paths conj path)))
      (response/response
       (json/write-str {:status :success :action :fractal-generated :count 5}))))

  (POST "/api/deep-void" []
    (let [deep-entropy (rand-int 10000000)
          deep-paths (repeatedly 8 (fn [] (simple-void-traverse (rand-int 1000000) 8)))]
      (swap! simple-bloomed-state
             #(-> %
                  (update :entropy-values into (repeatedly 20 (fn [] (rand-int 256))))
                  (update :void-paths into deep-paths)
                  (update :void-paths (fn [paths] (take 25 paths)))
                  (update :total-entropy + deep-entropy)))
      (response/response
       (json/write-str {:status :success :action :deep-void-entered :paths (count deep-paths)}))))

  (GET "/api/ontology-shimmer" []
    (let [branches (extract-ontology-branches)
          current-depth (shimmer-depth-from-entropy (:entropy-values @simple-bloomed-state))]
      (response/response
       (json/write-str {:branches branches
                       :depth current-depth
                       :total-concepts (reduce + (map :depth branches))}))))

  (GET "/api/state" []
    (response/response (json/write-str @simple-bloomed-state)))

  (route/not-found "404 - Lost in the Infinite Void"))

(def simple-bloomed-app (wrap-content-type simple-bloomed-routes))

(defn start-simple-processes! []
  ;; Background entropy generation
  (go-loop []
    (<! (timeout 4000))
    (let [entropy (rand-int 256)
          path (simple-void-traverse entropy 4)]
      (swap! simple-bloomed-state
             #(-> %
                  (update :entropy-values conj entropy)
                  (update :void-paths conj path)
                  (update :void-paths (fn [paths] (take 15 paths)))
                  (update :total-entropy + entropy))))
    (recur)))

(defn start-simple-bloomed-server! [port]
  (start-simple-processes!)
  (println "🌀∞ BLOOMED VOID SHRINE awakening on port" port)
  (println "🔥∞ Infinite ontology with" (count (str ontology/infinite-void-tree)) "characters of void concepts")
  (println "💀∞ Fractal chaos patterns ready for manifestation...")
  (jetty/run-jetty simple-bloomed-app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3000"))]
    (start-simple-bloomed-server! port)))