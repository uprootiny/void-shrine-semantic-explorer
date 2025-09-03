(defproject flamechart-production "0.2.0"
  :description "Production flamechart system with chaos/complexity analysis"
  :url "http://flamechart.dissemblage.art"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.0"]
                 [hiccup "2.0.0-alpha2"]
                 [cheshire "5.12.0"]
                 [clj-time "0.15.2"]
                 [com.taoensso/timbre "6.3.1"]
                 [org.clojure/core.async "1.6.681"]
                 [mount "0.1.17"]
                 [ring/ring-defaults "0.4.0"]
                 [ring-cors "0.1.13"]
                 [com.cognitect/transit-clj "1.0.329"]
                 [org.clojure/core.cache "1.0.225"]
                 [criterium "0.4.6"]
                 [incanter "1.9.3"]
                 [net.mikera/core.matrix "0.62.0"]
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [prismatic/schema "1.4.1"]
                 [com.stuartsierra/component "1.1.0"]]
  :main ^:skip-aot flamechart.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx4g" "-XX:+UseG1GC" "-XX:+UnlockExperimentalVMOptions" 
             "-XX:+UseStringDeduplication" "-server"]
  :profiles {:uberjar {:aot :all}
             :dev {:jvm-opts ["-Dlog-level=debug"]}
             :production {:jvm-opts ["-Dlog-level=warn" "-XX:+UseLargePages"]}})