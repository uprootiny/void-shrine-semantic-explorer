(ns entropy-gallery.entropy.live-sources
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [taoensso.timbre :as log])
  (:import [java.util.concurrent ConcurrentHashMap]))

;; Cache for entropy data with TTL
(defonce ^ConcurrentHashMap entropy-cache (ConcurrentHashMap.))
(def cache-ttl-ms (* 30 1000)) ; 30 seconds

(defn cache-key [source]
  (str "entropy-" (name source)))

(defn cache-get [source]
  (let [key (cache-key source)
        cached (.get entropy-cache key)]
    (when (and cached 
               (< (- (System/currentTimeMillis) (:timestamp cached)) cache-ttl-ms))
      (:data cached))))

(defn cache-put [source data]
  (.put entropy-cache (cache-key source) 
        {:data data :timestamp (System/currentTimeMillis)}))

;; Live entropy sources
(defn fetch-random-org []
  "Fetch true atmospheric randomness from Random.org"
  (try
    (let [response (http/get "https://www.random.org/integers/"
                            {:query-params {:num 10 :min 1 :max 16777215 :col 1 :base 10 :format "plain"}
                             :timeout 5000})
          numbers (->> (:body response)
                      clojure.string/split-lines
                      (map #(Integer/parseInt (clojure.string/trim %)))
                      (filter pos?))]
      {:name "Random.org Atmospheric Noise (Dublin)"
       :samples numbers
       :quality "true-random"
       :source-type "atmospheric"
       :url "https://www.random.org/"
       :last-updated (java.time.Instant/now)})
    (catch Exception e
      (log/warn "Failed to fetch Random.org data:" (.getMessage e))
      nil)))

(defn fetch-nist-beacon []
  "Fetch quantum randomness from NIST Beacon"
  (try
    (let [response (http/get "https://beacon.nist.gov/beacon/2.0/pulse/last"
                            {:accept :json :timeout 5000})
          data (json/parse-string (:body response) true)
          output-value (:outputValue (:pulse data))
          ;; Convert hex string to integers
          hex-chunks (partition 8 output-value)
          numbers (map #(Integer/parseUnsignedInt (apply str %) 16) 
                      (take 8 hex-chunks))]
      {:name "NIST Quantum Beacon (USA)"
       :samples numbers
       :quality "cryptographic"
       :source-type "quantum"
       :url "https://beacon.nist.gov/"
       :last-updated (java.time.Instant/now)})
    (catch Exception e
      (log/warn "Failed to fetch NIST Beacon data:" (.getMessage e))
      nil)))

(defn fetch-system-entropy []
  "Generate system-based entropy"
  {:name "System Entropy (Local)"
   :samples (repeatedly 20 #(rand-int 16777216))
   :quality "pseudo-random"
   :source-type "system"
   :url "local://system"
   :last-updated (java.time.Instant/now)})

(defn fetch-market-volatility []
  "Generate market-inspired entropy (simulated for now)"
  (let [base-price 100.0
        volatility 0.02
        samples (take 10 (reductions 
                          (fn [price _] 
                            (let [change (* volatility (- (rand) 0.5) 2)]
                              (* price (+ 1 change))))
                          base-price
                          (range)))]
    {:name "Financial Market Volatility"
     :samples (map #(int (* % 10000)) samples)
     :quality "economic-chaos"
     :source-type "market"
     :url "api://market"
     :last-updated (java.time.Instant/now)}))

(defn fetch-entropy-source [source-key]
  "Fetch entropy from live source with caching"
  (if-let [cached (cache-get source-key)]
    cached
    (let [data (case source-key
                 :random-org (fetch-random-org)
                 :nist-beacon (fetch-nist-beacon)
                 :system-local (fetch-system-entropy)
                 :market-data (fetch-market-volatility)
                 :hotbits (fetch-system-entropy) ; Fallback for now
                 nil)]
      (when data
        (cache-put source-key data))
      data)))