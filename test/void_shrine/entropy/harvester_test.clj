(ns void-shrine.entropy.harvester-test
  (:require [clojure.test :refer [deftest is testing]]
            [void-shrine.entropy.harvester :as harvester]))

(deftest test-mix-entropy
  (testing "Mix entropy combines multiple sources correctly"
    (let [sources [{:data [1 2 3] :source :test1}
                   {:data [4 5 6] :source :test2}]
          result (harvester/mix-entropy sources)]
      (is (contains? result :mixed-value))
      (is (contains? result :sources))
      (is (contains? result :timestamp))
      (is (= [:test1 :test2] (:sources result))))))

(deftest test-chaos-seed
  (testing "Chaos seed generation from entropy values"
    (let [entropy-values [{:data [100 200] :source :test}]
          result (harvester/chaos-seed entropy-values)]
      (is (contains? result :seed))
      (is (contains? result :mixed-value))
      (is (contains? result :timestamp)))))

(deftest test-entropy-sources-structure
  (testing "Entropy sources are properly configured"
    (is (seq harvester/entropy-sources))
    (is (every? #(contains? % :name) harvester/entropy-sources))
    (is (every? #(contains? % :url) harvester/entropy-sources))
    (is (every? #(contains? % :params) harvester/entropy-sources))
    (is (every? #(contains? % :parse-fn) harvester/entropy-sources))))