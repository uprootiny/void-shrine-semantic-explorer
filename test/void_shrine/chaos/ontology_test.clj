(ns void-shrine.chaos.ontology-test
  (:require [clojure.test :refer [deftest is testing]]
            [void-shrine.chaos.ontology :as ontology]))

(deftest test-void-tree-structure
  (testing "Void tree has expected structure"
    (is (contains? ontology/void-tree :void))
    (let [void-branch (:void ontology/void-tree)]
      (is (contains? void-branch :primordial-absence))
      (is (contains? void-branch :entropic-cascade))
      (is (contains? void-branch :nihil-philosophy)))))

(deftest test-traverse-void
  (testing "Void traversal returns valid paths"
    (let [path (ontology/traverse-void 12345 3)]
      (is (vector? path))
      (is (every? keyword? path))
      (is (<= (count path) 3)))))

(deftest test-void-manifestation
  (testing "Void manifestation generation"
    (let [manifestation (ontology/void-manifestation 42)]
      (is (contains? manifestation :path))
      (is (contains? manifestation :manifestation))
      (is (contains? manifestation :seed))
      (is (contains? manifestation :timestamp))
      (is (= 42 (:seed manifestation))))))

(deftest test-chaos-transform
  (testing "Chaos transformation processes data correctly"
    (let [data [10 20 30]
          entropy 100
          result (ontology/chaos-transform data entropy)]
      (is (contains? result :original))
      (is (contains? result :transformed))
      (is (contains? result :void-path))
      (is (contains? result :entropy))
      (is (= data (:original result)))
      (is (= entropy (:entropy result))))))

(deftest test-generate-void-poetry
  (testing "Void poetry generation"
    (let [manifestation {:manifestation {:realm :void
                                        :domain :primordial-absence
                                        :aspect :pre-being}}
          poetry (ontology/generate-void-poetry manifestation)]
      (is (string? poetry))
      (is (re-find #"void" poetry))
      (is (re-find #"primordial-absence" poetry)))))