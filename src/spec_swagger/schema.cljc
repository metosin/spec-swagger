(ns spec-swagger.schema
  "Generate a Swagger schema from a clojure.spec spec.

  If you want to generate a JSON Schema, look at spec-tools.json-schema. Swagger
  schemas are based on JSON Schema, but it lacks some of JSON Schema's features
  while having some that are not found in JSON Schema."
  (:require [spec-tools.json-schema :as json-schema]
            [spec-tools.visitor :as visitor]))

(defn- unwrap
  "Unwrap [x] to x. Asserts that coll has exactly one element."
  [coll]
  {:pre [(= 1 (count coll))]}
  (first coll))

(defn- spec-dispatch [dispatch spec children] dispatch)
(defmulti accept-spec spec-dispatch :default ::default)

(defmethod accept-spec 'clojure.core/float? [_ _ _] {:type "number" :format "float"})
(defmethod accept-spec 'clojure.core/double? [_ _ _] {:type "number" :format "double"})

(defmethod accept-spec 'clojure.spec/or [dispatch spec children]
  ;; :anyOf is not supported by Swagger 2.0, so we just give up. In principle,
  ;; we could do better in some special cases. For example, a reasonable schema
  ;; for (s/or ::int int? ::str string?) would be {:type ["number", "string"]}.
  {})

(defmethod accept-spec 'clojure.spec/nilable [dispatch spec children]
  ;; Neither :oneOf nor {:type "null"} are supported by Swagger 2.0, so we just
  ;; give up.
  {})

(defmethod accept-spec ::default [dispatch spec children]
  (json-schema/accept-spec dispatch spec children))

(defn swagger-schema
  "Generate Swagger schema matching the given clojure.spec spec.

  Since clojure.spec is more expressive than Swagger schemas, everything that
  satisfies the spec should satisfy the resulting schema, but the converse is
  not true."
  [spec]
  (visitor/visit spec accept-spec))
