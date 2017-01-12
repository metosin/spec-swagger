(ns spec-swagger.core
  (:require [clojure.spec :as s]
            [#?(:clj clojure.spec.gen :cljs clojure.spec.impl.gen) :as gen]
            [clojure.string :as string]
            [spec-tools.core :as st]))

(s/def ::external-docs
  (st/coll-spec
    ::external-docs
    {(st/opt :description) string?
     :url string?}))

(s/def ::security-definitions
  (st/coll-spec
    ::security-definitions
    {string? {:type (st/enum "basic" "apiKey" "oauth2")
              (st/opt :description) string?
              (st/opt :name) string?
              (st/opt :in) (st/enum "query" "header")
              (st/opt :flow) (st/enum "implicit" "password" "application" "accessCode")
              (st/opt :authorizationUrl) string?
              (st/opt :tokenUrl) string?
              (st/opt :scopes) {string? string?}}}))

(s/def ::security-requirements
  (st/coll-spec
    ::security-requirements
    {string? [string?]}))

(s/def ::header-object any?)

(s/def ::spec any?)

(def response-code (s/or :number (s/int-in 100 600) :default #{:default}))

(s/def ::operation
  (st/coll-spec
    ::operation
    {(st/opt :tags) [string?]
     (st/opt :summary) string?
     (st/opt :description) string?
     (st/opt :externalDocs) ::external-docs
     (st/opt :operationId) string?
     (st/opt :consumes) #{string?}
     (st/opt :produces) #{string?}
     (st/opt :parameters) {:query ::spec
                           :header ::spec
                           :path ::spec
                           :formData ::spec
                           :body ::spec}
     (st/opt :responses) {response-code {(st/opt :description) string?
                                         (st/opt :schema) ::spec
                                         (st/opt :headers) {string? ::header-object}
                                         (st/opt :examples) {string? any?}}}
     (st/opt :schemes) (st/set-of #{"http", "https", "ws", "wss"})
     (st/opt :deprecated) boolean?
     (st/opt :security) ::security-requirements}))

(s/def ::swagger
  (st/coll-spec
    ::swagger
    {:swagger (st/eq "2.0")
     :info {:title string?
            (st/opt :description) string?
            (st/opt :termsOfService) string?
            (st/opt :contact) {(st/opt :name) string?
                               (st/opt :url) string?
                               (st/opt :email) string?}
            (st/opt :license) {:name string?
                               (st/opt :url) string?}
            :version string?}
     (st/opt :host) string?
     (st/opt :basePath) string?
     (st/opt :schemes) (st/set-of #{"http", "https", "ws", "wss"})
     (st/opt :consumes) #{string?}
     (st/opt :produces) #{string?}
     (st/opt :paths) {string? {#{:get :put :post :delete :options :head :patch} ::operation}}
     ;(st/opt :definitions) map?
     ;(st/opt :parameters) map?
     ;(st/opt :responses) map?
     (st/opt :securityDefinitions) ::security-definitions
     (st/opt :security) ::security-requirements
     (st/opt :tags) [{:name string?
                      (st/opt :description) string?
                      (st/opt :externalDocs) ::external-docs}]
     (st/opt :externalDocs) ::external-docs}))

(comment
  (clojure.pprint/pprint
    (last (map first (s/exercise ::swagger 10)))))

