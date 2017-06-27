(ns spec-swagger.swagger2.spec-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as s]
            [spec-swagger.swagger2.spec :as ss]))

(deftest swagger-test
  (testing "gen works"
    (is (= 10 (count (s/exercise ::ss/spec 10))))))
