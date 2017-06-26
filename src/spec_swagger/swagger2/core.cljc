(ns spec-swagger.swagger2.core
  (:require [clojure.walk :as walk]
            [spec-swagger.swagger2.schema :as schema]))

;;
;; extract swagger2 parameters
;;

(defmulti extract-parameter (fn [in _] in))

(defmethod extract-parameter :body [_ spec]
  (let [schema (schema/transform spec {:in :body, :type :parameter})]
    [{:in "body"
      :name ""
      :description ""
      :required true
      :schema schema}]))

(defmethod extract-parameter :default [in spec]
  (let [{:keys [properties required]} (schema/transform spec {:in in, :type :parameter})]
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
  {:schema (schema/transform v {:type :schema})})

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

(defn transform
  "Transform spec-swagger spec into a swagger2 spec object."
  ([x]
   (transform x nil))
  ([x options]
   (expand-qualified-keywords x expand options)))
