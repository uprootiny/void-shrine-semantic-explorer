(ns void-shrine.test-runner
  (:require [clojure.test :as test]
            [void-shrine.entropy.harvester-test]
            [void-shrine.chaos.ontology-test]
            [void-shrine.web.server-test]))

(defn run-all-tests []
  (test/run-all-tests #"void-shrine\..*-test"))

(defn -main [& args]
  (let [results (run-all-tests)]
    (if (pos? (+ (:error results) (:fail results)))
      (System/exit 1)
      (System/exit 0))))