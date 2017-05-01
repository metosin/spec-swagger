(ns spec-swagger.core
  (:require [clojure.spec :as s]
            [spec-tools.data-spec :as ds]))

(s/def ::external-docs
  (ds/spec
    ::external-docs
    {(ds/opt :description) string?
     :url string?}))

(s/def ::security-definitions
  (ds/spec
    ::security-definitions
    {string? {:type (s/spec #{"basic" "apiKey" "oauth2"})
              (ds/opt :description) string?
              (ds/opt :name) string?
              (ds/opt :in) (s/spec #{"query" "header"})
              (ds/opt :flow) (s/spec #{"implicit" "password" "application" "accessCode"})
              (ds/opt :authorizationUrl) string?
              (ds/opt :tokenUrl) string?
              (ds/opt :scopes) {string? string?}}}))

(s/def ::security-requirements
  (ds/spec
    ::security-requirements
    {string? [string?]}))

(s/def ::header-object any?)

(s/def ::spec qualified-keyword?)

(s/def ::response-code (s/or :number (s/int-in 100 600) :default #{:default}))

(s/def ::response
  (ds/spec
    ::response
    {(ds/opt :description) string?
     (ds/opt :schema) ::spec
     (ds/opt :headers) {string? ::header-object}
     (ds/opt :examples) {string? any?}}))

(s/def ::operation
  (ds/spec
    ::operation
    {(ds/opt :tags) [string?]
     (ds/opt :summary) string?
     (ds/opt :description) string?
     (ds/opt :externalDocs) ::external-docs
     (ds/opt :operationId) string?
     (ds/opt :consumes) #{string?}
     (ds/opt :produces) #{string?}
     (ds/opt :parameters) {(ds/opt :query) ::spec
                           (ds/opt :header) ::spec
                           (ds/opt :path) ::spec
                           (ds/opt :formData) ::spec
                           (ds/opt :body) ::spec}
     (ds/opt :responses) (s/map-of ::response-code ::response)
     (ds/opt :schemes) (s/coll-of #{"http", "https", "ws", "wss"} :into #{})
     (ds/opt :deprecated) boolean?
     (ds/opt :security) ::security-requirements}))

(s/def ::swagger
  (ds/spec
    ::swagger
    {:swagger (s/spec #{"2.0"})
     :info {:title string?
            (ds/opt :description) string?
            (ds/opt :termsOfService) string?
            (ds/opt :contact) {(ds/opt :name) string?
                               (ds/opt :url) string?
                               (ds/opt :email) string?}
            (ds/opt :license) {:name string?
                               (ds/opt :url) string?}
            :version string?}
     (ds/opt :host) string?
     (ds/opt :basePath) string?
     (ds/opt :schemes) (s/coll-of #{"http", "https", "ws", "wss"} :into #{})
     (ds/opt :consumes) #{string?}
     (ds/opt :produces) #{string?}
     (ds/opt :paths) {string? {#{:get :put :post :delete :options :head :patch} ::operation}}
     ;(ds/opt :definitions) map?
     ;(ds/opt :parameters) map?
     ;(ds/opt :responses) map?
     (ds/opt :securityDefinitions) ::security-definitions
     (ds/opt :security) ::security-requirements
     (ds/opt :tags) [{:name string?
                      (ds/opt :description) string?
                      (ds/opt :externalDocs) ::external-docs}]
     (ds/opt :externalDocs) ::external-docs}))

(let [user {:id int?
            :name string?
            :address {:street string?
                      :zip string?}}
      new-user (dissoc user :id)]
  (s/def ::user (ds/spec ::user user))
  (s/def ::new-user (ds/spec ::user new-user)))

(def sample
  {:swagger "2.0"
   :info {:title "swagger-spec test"
          :version "1.0"}
   :paths {"/users" {:get {:parameters {:body ::user}
                           :responses {200 {:schema ::new-user}}}}}})

(assert (s/valid? ::swagger sample))

