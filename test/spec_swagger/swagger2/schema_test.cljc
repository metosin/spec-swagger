(ns spec-swagger.swagger2.schema-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [clojure.spec.alpha :as s]
    [spec-swagger.swagger2.schema :refer [transform]]
    #?(:clj
    [ring.swagger.validator :as v])))

(s/def ::integer integer?)
(s/def ::string string?)
(s/def ::set #{1 2 3})
(s/def ::keys (s/keys :req-un [::integer]))

(def exceptations
  {int?
   {:type "integer", :format "int64"}

   integer?
   {:type "integer"}

   float?
   {:type "number" :format "float"}

   double?
   {:type "number" :format "double"}

   string?
   {:type "string"}

   boolean?
   {:type "boolean"}

   nil?
   {}

   #{1 2 3}
   {:enum [1 3 2], :type "string"}

   (s/int-in 1 10)
   {:type "integer"
    :format "int64"
    :x-allOf [{:type "integer"
               :format "int64"}
              {:minimum 1
               :maximum 10}]}

   (s/keys :req-un [::integer] :opt-un [::string])
   {:type "object"
    :properties {"integer" {:type "integer"}
                 "string" {:type "string"}}
    :required ["integer"]}

   ::keys
   {:type "object",
    :properties {"integer" {:type "integer"}},
    :required ["integer"],
    :title "spec-swagger.swagger2.schema-test/keys"}

   (s/and int? pos?)
   {:type "integer"
    :format "int64",
    :x-allOf [{:type "integer"
               :format "int64"}
              {:minimum 0
               :exclusiveMinimum true}]}

   (s/or :int int? :pos pos?)
   {:type "integer"
    :format "int64",
    :x-anyOf [{:type "integer"
               :format "int64"}
              {:minimum 0
               :exclusiveMinimum true}]}

   (s/merge (s/keys :req-un [::integer])
            (s/keys :req-un [::string]))
   {:type "object",
    :properties {"integer" {:type "integer"},
                 "string" {:type "string"}},
    :required ["integer" "string"]}

   (s/every integer?)
   {:type "array", :items {:type "integer"}}

   (s/every-kv string? integer?)
   {:type "object", :additionalProperties {:type "integer"}}

   (s/coll-of string?)
   {:type "array", :items {:type "string"}}

   (s/coll-of string? :into '())
   {:type "array", :items {:type "string"}}

   (s/coll-of string? :into [])
   {:type "array", :items {:type "string"}}

   (s/coll-of string? :into #{})
   {:type "array", :items {:type "string"}, :uniqueItems true}

   (s/map-of string? integer?)
   {:type "object", :additionalProperties {:type "integer"}}

   (s/* integer?)
   {:type "array", :items {:type "integer"}}

   (s/+ integer?)
   {:type "array", :items {:type "integer"}, :minItems 1}

   (s/? integer?)
   {:type "array", :items {:type "integer"}, :minItems 0}

   (s/alt :int integer? :string string?)
   {:type "integer", :x-anyOf [{:type "integer"} {:type "string"}]}

   (s/cat :int integer? :string string?)
   {:type "array"
    :minItems 2
    :maxItems 2
    :items {:type "integer"
            :x-anyOf [{:type "integer"}
                      {:type "string"}]}}

   (s/tuple integer? string?)
   {:type "array"
    :items [{:type "integer"}]
    :x-items [{:type "integer"} {:type "string"}]
    :minItems 2
    :maxItems 2}

   (s/map-of string? clojure.core/integer?)
   {:type "object", :additionalProperties {:type "integer"}}

   (s/nilable string?)
   {:type "string", :x-nullable true}})

(deftest test-expectations
  (doseq [[spec swagger-spec] exceptations]
    (is (= swagger-spec (transform spec)))))

#?(:clj
   (do
     (defn swagger-spec [schema]
       {:swagger "2.0"
        :info {:title "" :version ""}
        :paths {"/hello" {:get
                          {:responses
                           {200 {:description ""
                                 :schema schema}}}}}})

     (deftest test-schema-validation
       (let [swagger-spec (fn [schema]
                            {:swagger "2.0"
                             :info {:title "" :version ""}
                             :paths {"/hello" {:get
                                               {:responses
                                                {200 {:description ""
                                                      :schema schema}}}}}})]
         (testing "invalid schema fails on swagger spec validation"
           (is (-> {:type "invalid"} swagger-spec v/validate)))
         (testing "all expectations pass the swagger spec validation")
         (doseq [[spec] exceptations]
           (is (= nil (-> spec transform swagger-spec v/validate))))))))
