# spec-swagger [![Build Status](https://travis-ci.org/metosin/spec-swagger.svg?branch=master)](https://travis-ci.org/metosin/spec-swagger) [![Dependencies Status](https://jarkeeper.com/metosin/spec-tools/status.svg)](https://jarkeeper.com/metosin/spec-swagger)

Small Clojure(Script) lib to transform `clojure.spec` models into  [Swagger2](http://swagger.io) and [OpenAPI3](https://www.openapis.org/) formats.

Status: Work-in-progress & **Alpha** (as spec is still alpha too).

Plan is to eventually align this lib with [ring-swagger](https://github.com/metosin/ring-swagger).

## Latest version

[![Clojars Project](http://clojars.org/metosin/spec-swagger/latest-version.svg)](http://clojars.org/metosin/spec-swagger)

## Swagger2

```clj
(require '[spec-swagger.swagger2.core :as swagger])
```

### Spec transformations

`swagger/transform` converts specs into Swagger2 JSON Schema. Transformation can be customized with the following optional options:

  * `:type` - either `:parameter` or `:schema` (default)
  * `:in` - one of: `:query`, `:header`, `:path`, `:body` or `:formData`.

**NOTE**: As `clojure.spec` is more powerfull than the Swagger2 JSON Schema, we are losing some data in the transformation. Spec-swagger tries to retain all the informatin, via vendor extensions.

```clj
(swagger/transform float?)
; {:type "number" :format "float"}

(swagger/transform (s/nilable string?) {:type :parameter})
; {:type "string", :allowEmptyValue true}

(swagger/transform (s/nilable string?))
; {:type "string", :x-nullable true}

;; sadly, we losing information here...
(swagger/transform (s/cat :int integer? :string string?))
; {:type "array"
;  :minItems 2
;  :maxItems 2
;  :items {:type "integer"
;          :x-anyOf [{:type "integer"}
;                    {:type "string"}]}}
```

### Swagger Spec generation

`swagger/swagger-spec` function takes an spec-swagger data map and transforms it into a valid [Swagger2 Spec](http://swagger.io/specification/) format. Rules:

* by default, spec-swagger data is passed through, allowing any valid swagger data to be used
* for qualified map keys, `swagger/expand` multimethod is invoked with the key, value and the map as arguments
  * dispatches on the key, defaulting to `::swagger/extension`
  * returns a map that get's merged in to original map, without the dispatched key

Predifined dispatch keys below.

### `::swagger/extension`

Transforms the the key into valid [swagger vendor extension](http://swagger.io/specification/#vendorExtensions) by prepending a `x-` to it's namespace. Value is not touched.

```clj
(swagger/swagger-spec
  {:my/thing 42})
; {:x-my/thing 42}
```

### `::swagger/schema`

Value should be a `clojure.spec.alpha/Spec` or name of a spec. Returns a map with key `:schema` and value transformed into swagger json schema format. Mostly used under [Response Object](http://swagger.io/specification/#responsesObject).

```clj
(s/def ::name string?)
(s/def ::user (s/keys :req-un [::name]))

(swagger/swagger-spec
  {:paths
   {"echo"
    {:post
     {:responses
      {200
       {::swagger/schema ::user}}}}}})
; {:paths
;  {"echo"
;   {:post
;    {:responses
;     {200
;      {:schema
;       {:type "object"
;        :properties {"name" {:type "string"}}
;        :required ["name"]}}}}}}}
```

### `::swagger/parameters`

Value should be a map with optional keys `:body`, `:query`, `:path`, `:header` and `:formData`. For all but `:body`, the value should be a `s/keys` spec (describing the ring parameters). With `:body`, the value can be any `clojure.spec.alpha/Spec` or name of a spec.

Returns a map with key `:parameters` with value of vector of swagger [Parameter Objects](http://swagger.io/specification/#parameterObject).

```clj
(swagger/swagger-spec
  {:paths
   {"echo"
    {:post
     {::swagger/parameters
      {:query (s/keys :opt-un [::name])
       :body ::user}}}}})
; {:paths
;  {"echo"
;   {:post
;    {:parameters
;     [{:in "query"
;       :name ""
;       :description ""
;       :type "string"
;       :required false}
;      {:in "body"
;       :name ""
;       :description ""
;       :required true
;       :schema {:type "object"
;                :properties {"name" {:type "string"}}
;                :required ["name"]}}]}}}}
```

### Full example

```clj
(require '[spec-swagger.swagger2.core :as swagger])
(require '[clojure.spec.alpha :as s])

(s/def ::id string?)
(s/def ::name string?)
(s/def ::street string?)
(s/def ::city #{:tre :hki})
(s/def ::address (s/keys :req-un [::street ::city]))
(s/def ::user (s/keys :req-un [::id ::name ::address]))

(swagger/swagger-spec
  {:swagger "2.0"
   :info {:version "1.0.0"
          :title "Sausages"
          :description "Sausage description"
          :termsOfService "http://helloreverb.com/terms/"
          :contact {:name "My API Team"
                    :email "foo@example.com"
                    :url "http://www.metosin.fi"}
          :license {:name "Eclipse Public License"
                    :url "http://www.eclipse.org/legal/epl-v10.html"}}
   :tags [{:name "user"
           :description "User stuff"}]
   :paths {"/api/ping" {:get {:responses {:default {:description ""}}}}
           "/user/:id" {:post {:summary "User Api"
                               :description "User Api description"
                               :tags ["user"]
                               ::kikka 42
                               ::swagger/parameters {:path (s/keys :req [::id])
                                                     :body ::user}
                               :responses {200 {::swagger/spec ::user
                                                :description "Found it!"}
                                           404 {:description "Ohnoes."}}}}}})
; {:swagger "2.0",
;  :info {:version "1.0.0",
;         :title "Sausages",
;         :description "Sausage description",
;         :termsOfService "http://helloreverb.com/terms/",
;         :contact {:name "My API Team", :email "foo@example.com", :url "http://www.metosin.fi"},
;         :license {:name "Eclipse Public License", :url "http://www.eclipse.org/legal/epl-v10.html"}},
;  :tags [{:name "user", :description "User stuff"}],
;  :paths {"/api/ping" {:get {:responses {:default {:description ""}}}},
;          "/user/:id" {:post {:summary "User Api",
;                              :description "User Api description",
;                              :tags ["user"],
;                              :responses {200 {:description "Found it!",
;                                               :schema {:type "object",
;                                                        :properties {"id" {:type "string"},
;                                                                     "name" {:type "string"},
;                                                                     "address" {:type "object",
;                                                                                :properties {"street" {:type "string"},
;                                                                                             "city" {:enum [:tre :hki]}},
;                                                                                :required ["street" "city"]}},
;                                                        :required ["id" "name" "address"]}},
;                                          404 {:description "Ohnoes."}},
;                              :x-spec-swagger.swagger2.core-test/kikka 42,
;                              :parameters [{:in "path", :name "", :description "", :type "string", :required true}
;                                           {:in "body",
;                                            :name "",
;                                            :description "",
;                                            :required true,
;                                            :schema {:type "object",
;                                                     :properties {"id" {:type "string"},
;                                                                  "name" {:type "string"},
;                                                                  "address" {:type "object",
;                                                                             :properties {"street" {:type "string"},
;                                                                                          "city" {:enum [:tre :hki]}},
;                                                                             :required ["street" "city"]}},
;                                                     :required ["id" "name" "address"]}}]}}}}
```

## OpenAPI3

**TODO**

## License

Copyright © 2016-2017 [Metosin Oy](http://www.metosin.fi)

Distributed under the Eclipse Public License, the same as Clojure.
