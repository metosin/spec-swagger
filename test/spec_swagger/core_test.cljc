(ns spec-swagger.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec :as s]
            [spec-swagger.core :as ss]))

(deftest swagger-test
  (testing "gen works"
    (is (= 10 (count (s/exercise ::ss/spec 10))))))
