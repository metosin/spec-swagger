(ns spec-swagger.schema-test
  (:require
   [clojure.test :refer [deftest testing are]]
   [clojure.spec :as s]
   [spec-swagger.schema :refer [swagger-schema]]
   #?(:clj [ring.swagger.validator :as v])))

(deftest test-simple
  (are [x y] (= y (swagger-schema (s/spec x)))
    int? {:type "integer", :format "int64"}
    float? {:type "number" :format "float"}
    (s/and int? pos?)
    {:allOf [{:type "integer", :format "int64"} {:minimum 0 :exclusiveMinimum true}]}))

#?(:clj
   (do
     (defn validate [spec]
       (let [schema (swagger-schema spec)
             structure {:swagger "2.0"
                        :info {:title "" :version ""}
                        :paths {"/hello" {:get {:responses {200 {:description "" :schema schema}}}}}}]
         (v/validate structure)))

     (deftest test-schema-validation
       (are [x] (nil? (validate (s/spec x)))
         double?
         (s/or ::int int? ::string string?)
         (s/nilable int?)))))
