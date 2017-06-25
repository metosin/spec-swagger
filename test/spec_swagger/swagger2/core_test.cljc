(ns spec-swagger.swagger2.core-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [spec-swagger.swagger2.core :as swagger]
    [clojure.spec.alpha :as s]
    #?(:clj
    [ring.swagger.validator :as v])))

(s/def ::id string?)
(s/def ::name string?)
(s/def ::street string?)
(s/def ::city #{:tre :hki})
(s/def ::address (s/keys :req-un [::street ::city]))
(s/def ::user (s/keys :req-un [::id ::name ::address]))

(deftest expand-test
  (testing "::extension"
    (is (= {:x-my/thing 42}
           (swagger/transform
             {:my/thing 42}))))
  (testing "::schema"
    (is (= {:schema
            {:type "object",
             :properties {"id" {:type "string"},
                          "name" {:type "string"},
                          "address" {:type "object",
                                     :properties {"street" {:type "string"}
                                                  "city" {:enum [:tre :hki]}},
                                     :required ["street" "city"]}},
             :required ["id" "name" "address"]}}
           (swagger/transform
             {::swagger/schema ::user}))))
  (testing "::parameters"
    (is (= {:parameters [{:in "query", :name "", :description "", :type "string", :required false}
                         {:in "query", :name "", :description "", :type "string", :required false}
                         {:in "path", :name "", :description "", :type "string", :required true}]}
           (swagger/transform
             {::swagger/parameters
              {:query (s/keys :opt-un [::name ::street])
               :path (s/keys :req [::id])}})))))

(def data
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
                               :responses {200 {::swagger/schema ::user
                                                :description "Found it!"}
                                           404 {:description "Ohnoes."}}}}}})

#?(:clj
   (deftest test-schema-validation
     (is (nil? (-> data swagger/transform v/validate)))))

(comment
  (defn validate [x]
    (-> x swagger/transform v/validate))

  (./aprint
    (swagger/transform data))

  (println "------------------------------------")

  (./aprint
    (validate data)))
