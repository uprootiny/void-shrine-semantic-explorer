(defproject entropy-gallery "0.1.0-SNAPSHOT"
  :description "Visual gallery of true randomness sources with entropy analysis"
  :url "http://entropy-gallery.dissemblage.art"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [ring/ring-devel "1.9.5"]
                 [compojure "1.6.2"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "2.4.0"]
                 [http-kit "2.5.3"]
                 [criterium "0.4.6"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]
                 [com.taoensso/timbre "6.3.1"]]
  :main ^:skip-aot entropy-gallery.plumbed-working
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]]}}
  :ring {:handler entropy-gallery.web/app}
  :plugins [[lein-ring "0.12.5"]])