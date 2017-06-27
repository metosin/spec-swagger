(ns spec-swagger.swagger2.core
  (:require [clojure.walk :as walk]
            [clojure.spec.alpha :as s]
            [spec-tools.json-schema :as json-schema]
            [spec-tools.visitor :as visitor]
            [spec-tools.core :as st]))

;;
;; conversion
;;

(defn- spec-dispatch [dispatch _ _ _] dispatch)
(defmulti accept-spec spec-dispatch :default ::default)

(defmethod accept-spec 'clojure.core/float? [_ _ _ _] {:type "number" :format "float"})
(defmethod accept-spec 'clojure.core/double? [_ _ _ _] {:type "number" :format "double"})
(defmethod accept-spec 'clojure.core/nil? [_ _ _ _] {})

;; anyOf is not supported
(defmethod accept-spec 'clojure.spec.alpha/or [_ _ children _]
  (assoc
    (first children)
    :x-anyOf children))

;; allOf is not supported
(defmethod accept-spec 'clojure.spec.alpha/and [_ _ children _]
  (assoc
    (first children)
    :x-allOf children))

;; anyOf is not supported
(defmethod accept-spec 'clojure.spec.alpha/alt [_ _ children _]
  (assoc
    (first children)
    :x-anyOf children))

;; anyOf is not supported
(defmethod accept-spec 'clojure.spec.alpha/cat [_ _ children _]
  {:type "array"
   :items (assoc
            (first children)
            :x-anyOf children)})

;; heterogeneous lists not supported
(defmethod accept-spec 'clojure.spec.alpha/tuple [_ _ children _]
  {:type "array"
   :items [(first children)]
   :x-items children})

;; FIXME: resolve a real type, now - strings.
(defmethod accept-spec ::visitor/set [_ _ children _]
  ;; enums must be homogeneous in swagger2, thus we need a type
  {:enum children :type "string"})

(defmethod accept-spec 'clojure.spec.alpha/nilable [_ _ children {:keys [type in]}]
  (if (and (= type :parameter) (not= in :body))
    (assoc (visitor/unwrap children) :allowEmptyValue true)
    (assoc (visitor/unwrap children) :x-nullable true)))

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

;;
;; utils
;;

(defn nilable-spec? [spec]
  (boolean
    (some-> spec
            s/form
            seq
            first
            #{'clojure.spec.alpha/nilable
              'cljs.spec.alpha/nilable})))

;;
;; extract swagger2 parameters
;;

(defmulti extract-parameter (fn [in _] in))

(defmethod extract-parameter :body [_ spec]
  (let [schema (transform spec {:in :body, :type :parameter})
        nilable? (nilable-spec? spec)]
    [{:in "body"
      :name (or (-> spec
                    st/spec-name
                    visitor/namespaced-name) "")
      :description ""
      :required (not nilable?)
      :schema schema}]))

(defmethod extract-parameter :default [in spec]
  (let [{:keys [properties required]} (transform spec {:in in, :type :parameter})]
    (mapv
      (fn [[k {:keys [type] :as schema}]]
        (merge
          {:in (name in)
           :name k
           :description ""
           :type type
           :required (contains? (set required) k)}
          schema))
      properties)))

;;
;; expand the spec
;;

(defmulti expand (fn [k _ _ _] k) :default ::extension)

(defmethod expand ::extension [k v _ _]
  {(keyword (str "x-" (namespace k) "/" (name k))) v})

(defmethod expand ::schema [_ v _ _]
  {:schema (transform v {:type :schema})})

(defmethod expand ::parameters [_ v _ _]
  {:parameters (into [] (mapcat (fn [[in spec]] (extract-parameter in spec)) v))})

(defn expand-qualified-keywords [x f options]
  (walk/postwalk
    (fn [x]
      (if (map? x)
        (reduce-kv
          (fn [acc k v]
            (if (qualified-keyword? k)
              (-> acc (dissoc k) (merge (f k v acc options)))
              acc))
          x
          x)
        x))
    x))

;;
;; public api
;;

(defn swagger-spec
  "Transforms a spec-swagger spec into a swagger2 spec."
  ([x]
   (swagger-spec x nil))
  ([x options]
   (expand-qualified-keywords x expand options)))
