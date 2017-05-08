(ns spec-swagger.swagger2.core
  (:require [clojure.walk :as walk]
            [spec-swagger.swagger2.schema :as schema]))

;;
;; extract swagger2 parameters
;;

(defmulti extract-parameter (fn [in _] in))

(defmethod extract-parameter :body [_ spec]
  (let [schema (schema/transform spec)]
    [{:in "body"
      :name ""
      :description ""
      :required true
      :schema schema}]))

;; TODO: ensure it's a s/keys
(defmethod extract-parameter :default [in spec]
  (let [{:keys [properties required]} (schema/transform spec)]
    (mapv
      (fn [[k {:keys [type]}]]
        {:in (name in)
         :name ""
         :description ""
         :type type
         :required (contains? (set required) k)})
      properties)))

;;
;; expand the spec
;;

(defmulti expand (fn [k _ _] k) :default ::extension)

(defmethod expand ::extension [k v _]
  {(keyword (str "x-" (namespace k) "/" (name k))) v})

(defmethod expand ::schema [_ v _]
  {:schema (schema/transform v)})

(defmethod expand ::parameters [_ v _]
  {:parameters (into [] (mapcat (fn [[in spec]] (extract-parameter in spec)) v))})

(defn expand-qualified-keywords [x f]
  (walk/postwalk
    (fn [x]
      (if (map? x)
        (reduce-kv
          (fn [acc k v]
            (if (qualified-keyword? k)
              (-> acc (dissoc k) (merge (f k v acc)))
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
   (transform x expand))
  ([x f]
   (expand-qualified-keywords x f)))
