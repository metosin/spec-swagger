(ns spec-swagger.spec-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec :as s]
            [spec-swagger.spec :as ss]))

(deftest swagger-test
  (testing "gen works"
    (is (= 10 (count (s/exercise ::ss/spec 10))))))
