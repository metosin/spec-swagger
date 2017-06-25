(ns spec-swagger.swagger2.schema
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

(defn- spec-dispatch [dispatch spec children options] dispatch)
(defmulti accept-spec spec-dispatch :default ::default)

(defmethod accept-spec 'clojure.core/float? [_ _ _ _] {:type "number" :format "float"})
(defmethod accept-spec 'clojure.core/double? [_ _ _ _] {:type "number" :format "double"})

(defmethod accept-spec 'clojure.spec.alpha/or [dispatch spec children options]
  ;; :anyOf is not supported by Swagger 2.0, so we just give up. In principle,
  ;; we could do better in some special cases. For example, a reasonable schema
  ;; for (s/or ::int int? ::str string?) would be {:type ["number", "string"]}.
  {})

(defmethod accept-spec 'clojure.spec.alpha/nilable [dispatch spec children options]
  ;; Neither :oneOf nor {:type "null"} are supported by Swagger 2.0, so we just
  ;; give up.
  {})

(defmethod accept-spec ::default [dispatch spec children options]
  (json-schema/accept-spec dispatch spec children options))

(defn transform
  "Generate Swagger schema matching the given clojure.spec spec.

  Since clojure.spec is more expressive than Swagger schemas, everything that
  satisfies the spec should satisfy the resulting schema, but the converse is
  not true."
  ([spec]
    (transform spec nil))
  ([spec options]
   (visitor/visit spec accept-spec options)))
