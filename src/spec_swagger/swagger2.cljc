(ns spec-swagger.swagger2)

(defn transform
  "Produces swagger2 object from `:spec-swagger.spec/swagger` data.
   Optional second argument is a options map, supporting"
  ([data]
   (transform data nil))
  ([data options]
    (throw (ex-info "not implemented" {}))))
