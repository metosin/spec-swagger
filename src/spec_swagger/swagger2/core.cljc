(ns spec-swagger.swagger2.core
  (:require [clojure.walk :as walk]
            [spec-swagger.swagger2.schema :as json-schema]))

;;
;; extract swagger2 parameters
;;

(defmulti extract-parameter (fn [in _] in))

(defmethod extract-parameter :body [_ spec]
  (let [schema (json-schema/transform spec)]
    [{:in "body"
      :name ""
      :description ""
      :required true
      :schema schema}]))

;; TODO: ensure it's a s/keys
(defmethod extract-parameter :default [in spec]
  (let [{:keys [properties required]} (json-schema/transform spec)]
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

(defmulti expand (fn [k _] k) :default ::extension)

(defmethod expand ::extension [k v]
  {(keyword (str "x-" (namespace k) "/" (name k))) v})

(defmethod expand ::schema [_ v]
  {:schema (json-schema/transform v)})

(defmethod expand ::parameters [_ v]
  {:parameters (into [] (mapcat (fn [[in spec]] (extract-parameter in spec)) v))})

(defn expand-qualified-keywords [x f]
  (walk/postwalk
    (fn [x]
      (if (map? x)
        (reduce-kv
          (fn [acc k v]
            (if-let [m (if (qualified-keyword? k) (f k v))]
              (-> acc (dissoc k) (merge m))
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
