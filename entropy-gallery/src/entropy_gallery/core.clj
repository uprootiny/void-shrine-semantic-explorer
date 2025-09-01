(ns entropy-gallery.core
  (:require [entropy-gallery.web :as web]
            [entropy-gallery.entropy.sources :as sources]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main
  "Main entry point for the entropy gallery"
  [& args]
  (let [port (Integer/parseInt (or (first args) "3001"))]
    (println "🎨 Starting Entropy Gallery on port" port)
    (sources/initialize-entropy-sources!)
    (jetty/run-jetty web/app {:port port :join? true})))