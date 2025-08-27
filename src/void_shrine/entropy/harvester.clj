(ns void-shrine.entropy.harvester
  (:require [clojure.core.async :as async :refer [go go-loop <! >! chan timeout]]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]))

(def entropy-sources
  [{:name "quantum-anu"
    :url "https://qrng.anu.edu.au/API/jsonI.php"
    :params {:length 1024 :type "uint8"}
    :parse-fn (fn [resp] (get-in resp ["data"]))}
   
   {:name "random-org"
    :url "https://www.random.org/integers/"
    :params {:num 100 :min 0 :max 255 :col 1 :base 10 :format "plain" :rnd "new"}
    :parse-fn (fn [resp] (map #(Integer/parseInt %) (str/split-lines resp)))}
   
   {:name "hotbits"
    :url "https://www.fourmilab.ch/cgi-bin/Hotbits.api"
    :params {:nbytes 128 :fmt "json"}
    :parse-fn (fn [resp] (get resp "data"))}])

(defn fetch-entropy
  "Fetch entropy from a specific source"
  [{:keys [url params parse-fn] :as source}]
  (try
    (let [response (http/get url {:query-params params
                                   :as :auto
                                   :socket-timeout 5000
                                   :connection-timeout 5000})]
      {:source (:name source)
       :timestamp (System/currentTimeMillis)
       :data (parse-fn (:body response))
       :status :success})
    (catch Exception e
      {:source (:name source)
       :timestamp (System/currentTimeMillis)
       :status :error
       :error (.getMessage e)})))

(defn entropy-stream
  "Create an async channel that continuously harvests entropy"
  []
  (let [out-chan (chan 100)]
    (go-loop []
      (doseq [source entropy-sources]
        (go
          (let [entropy (fetch-entropy source)]
            (when (= :success (:status entropy))
              (>! out-chan entropy))))
        (<! (timeout 3000)))
      (recur))
    out-chan))

(defn mix-entropy
  "Mix multiple entropy sources using XOR and rotation"
  [sources]
  (let [all-data (mapcat :data sources)
        mixed (reduce (fn [acc [idx val]]
                        (bit-xor acc
                                 (bit-or (bit-shift-left val (mod idx 8))
                                         (bit-shift-right val (- 8 (mod idx 8))))))
                      0
                      (map-indexed vector all-data))]
    {:mixed-value mixed
     :sources (map :source sources)
     :timestamp (System/currentTimeMillis)}))

(defn chaos-seed
  "Generate a chaos seed from mixed entropy"
  [entropy-values]
  (let [mixed (mix-entropy entropy-values)]
    (assoc mixed
           :seed (hash (str (:mixed-value mixed) 
                            (:timestamp mixed)
                            (rand))))))