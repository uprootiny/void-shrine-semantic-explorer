(ns void-shrine.web.server-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mock]
            [void-shrine.web.minimal-server :as server]
            [clojure.data.json :as json]))

(def test-state (atom {:entropy-values [42 84 126]
                      :void-manifestations []
                      :chaos-metrics {:total-entropy 252
                                      :void-depth 3
                                      :dissolution-rate 0.05}
                      :timestamp (System/currentTimeMillis)}))

(deftest test-main-page
  (testing "Main page returns HTML"
    (let [response (server/main-page)]
      (is (string? response))
      (is (re-find #"<!DOCTYPE html>" response))
      (is (re-find #"Void Shrine" response)))))

(deftest test-chaos-api-endpoint
  (testing "POST /api/chaos returns success"
    (let [request (mock/request :post "/api/chaos")
          response (server/app-routes request)]
      (is (= 200 (:status response)))
      (let [body (json/read-str (:body response) :key-fn keyword)]
        (is (= "success" (:status body)))
        (is (= "chaos-triggered" (:action body)))))))

(deftest test-entropy-api-endpoint
  (testing "POST /api/entropy returns success"
    (let [request (mock/request :post "/api/entropy")
          response (server/app-routes request)]
      (is (= 200 (:status response)))
      (let [body (json/read-str (:body response) :key-fn keyword)]
        (is (= "success" (:status body)))
        (is (= "entropy-harvested" (:action body)))))))

(deftest test-void-api-endpoint
  (testing "POST /api/void returns success"
    (let [request (mock/request :post "/api/void")
          response (server/app-routes request)]
      (is (= 200 (:status response)))
      (let [body (json/read-str (:body response) :key-fn keyword)]
        (is (= "success" (:status body)))
        (is (= "void-entered" (:action body)))))))

(deftest test-entropy-to-color
  (testing "Entropy to color conversion"
    (let [color (server/entropy-to-color 0xFF0000)]
      (is (string? color))
      (is (re-find #"rgb\(" color)))))

(deftest test-generate-entropy
  (testing "Entropy generation produces valid values"
    (let [entropy (server/generate-entropy)]
      (is (sequential? entropy))
      (is (= 10 (count entropy)))
      (is (every? #(and (>= % 0) (<= % 255)) entropy)))))