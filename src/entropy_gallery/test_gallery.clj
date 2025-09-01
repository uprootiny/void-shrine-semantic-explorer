(ns entropy-gallery.test-gallery
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.core :as h]
            [cheshire.core :as cheshire]
            [clj-http.client :as http])
  (:import [java.security SecureRandom]))

;; Test entropy sources - only verified working ones
(defn fetch-random-org []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "Testing Random.org...")
      (let [resp (http/get "https://www.random.org/integers/?num=10&min=0&max=16777215&col=1&base=10&format=plain&rnd=new"
                          {:timeout 10000
                           :headers {"User-Agent" "EntropyTest/1.0"}})
            numbers (map #(Integer/parseInt (.trim %)) 
                        (filter #(not (.isEmpty (.trim %))) 
                               (clojure.string/split (:body resp) #"\n")))
            end-time (System/currentTimeMillis)]
        {:name "Random.org Test"
         :samples numbers
         :quality "true-random"
         :response-time (- end-time start-time)
         :status "success"})
      (catch Exception e
        {:name "Random.org Test"
         :samples (repeatedly 10 #(rand-int 16777216))
         :quality "fallback"
         :response-time 0
         :status "error"
         :error (.getMessage e)}))))

(defn fetch-system-entropy []
  (let [start-time (System/currentTimeMillis)]
    (try
      (println "Testing System entropy...")
      (let [secure-random (SecureRandom.)
            samples (repeatedly 10 #(.nextInt secure-random 16777216))
            end-time (System/currentTimeMillis)]
        {:name "System Entropy Test"
         :samples samples
         :quality "cryptographic-random"
         :response-time (- end-time start-time)
         :status "success"})
      (catch Exception e
        {:name "System Entropy Test"
         :samples (repeatedly 10 #(rand-int 16777216))
         :quality "fallback"
         :response-time 0
         :status "error"
         :error (.getMessage e)}))))

(defn run-all-tests []
  (println "🧪 Running comprehensive entropy source tests...")
  (let [test-results {:random-org (fetch-random-org)
                     :system-entropy (fetch-system-entropy)}]
    (println "✅ Test results:")
    (doseq [[source result] test-results]
      (println (str "  " source ": " (:status result) 
                   " (" (:response-time result) "ms)")))
    test-results))

(defn main-page []
  (h/html
   [:html
    [:head
     [:title "🧪 Entropy Test Gallery"]
     [:style "
       body { background: #0a0a0a; color: #00ff88; font-family: monospace; padding: 20px; }
       h1 { color: #00ffff; text-align: center; }
       .test-result { background: #001a1a; padding: 15px; margin: 10px 0; border-radius: 8px; }
       .success { border-left: 5px solid #00ff88; }
       .error { border-left: 5px solid #ff4444; }
       button { background: #003333; color: #00ff88; border: 1px solid #00ff88; 
               padding: 10px 20px; margin: 5px; cursor: pointer; }
       button:hover { background: #00ff88; color: #003333; }
     "]]
    [:body
     [:h1 "🧪 Working Entropy Sources Test"]
     [:div {:id "test-results"} "Click 'Run Tests' to start..."]
     [:div
      [:button {:onclick "runTests()"} "🧪 Run Tests"]
      [:button {:onclick "commitWork()"} "💾 Commit Changes"]]
     
     [:script "
       function runTests() {
         document.getElementById('test-results').innerHTML = 'Running tests...';
         fetch('/test')
           .then(r => r.json())
           .then(results => {
             let html = '';
             Object.keys(results).forEach(source => {
               const result = results[source];
               const cssClass = result.status === 'success' ? 'success' : 'error';
               html += `<div class='test-result ${cssClass}'>
                 <h3>${result.name}</h3>
                 <p>Status: ${result.status}</p>
                 <p>Response Time: ${result['response-time']}ms</p>
                 <p>Samples: ${result.samples.length}</p>
                 <p>Quality: ${result.quality}</p>
                 ${result.error ? '<p style=\"color:#ff4444\">Error: ' + result.error + '</p>' : ''}
               </div>`;
             });
             document.getElementById('test-results').innerHTML = html;
           });
       }
       
       function commitWork() {
         fetch('/commit', {method: 'POST'})
           .then(r => r.json())
           .then(result => {
             alert('Commit result: ' + JSON.stringify(result));
           });
       }
     "]]]]))

(defroutes app-routes
  (GET "/" [] 
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (main-page)})
  
  (GET "/test" []
    (let [test-results (run-all-tests)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (cheshire/generate-string test-results)}))
  
  (POST "/commit" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (cheshire/generate-string {:message "Test run completed"})})
  
  (route/not-found "Not Found"))

(def app (wrap-params app-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "3003"))]
    (println "🧪 Starting Entropy Test Gallery on port" port)
    (run-all-tests)
    (jetty/run-jetty app {:port port :join? true})))