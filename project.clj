(defproject metosin/spec-swagger "0.1.0-SNAPSHOT"
  :description "Tools for writing Swagger with clojure.spec"
  :url "https://github.com/metosin/spec-swagger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[metosin/spec-tools "0.3.0-SNAPSHOT"]]
  :codeina {:target "gh-pages/doc"
            :src-uri "https://github.com/metosin/spec-swagger/blob/master/"
            :src-uri-prefix "#L"}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]
                             [funcool/codeina "0.5.0"]
                             [lein-doo "0.1.7"]
                             [lein-cljsbuild "1.1.6"]]
                   :jvm-opts ^:replace ["-server"]
                   ;:global-vars {*warn-on-reflection* true}
                   :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                                  [org.clojure/clojurescript "1.9.562"]
                                  [criterium "0.4.4"]
                                  [org.clojure/test.check "0.9.0"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [com.gfredericks/test.chuck "0.2.7"]
                                  [metosin/ring-swagger "0.23.0"]
                                  [metosin/scjsv "0.4.0"]]}
             :perf {:jvm-opts ^:replace ["-server"]}}
  :aliases {"all" ["with-profile" "dev"]
            "perf" ["with-profile" "default,dev,perf"]
            "test-clj" ["all" "do" ["test"] ["check"]]
            "test-phantom" ["doo" "phantom" "test"]
            "test-node" ["doo" "node" "node-test"]}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/out/test.js"
                                   :output-dir "target/out"
                                   :main spec-swagger.doo-runner
                                   :optimizations :none}}
                       ;; Node.js requires :target :nodejs, hence the separate
                       ;; build configuration.
                       {:id "node-test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/node_out/test.js"
                                   :output-dir "target/node_out"
                                   :main spec-swagger.doo-runner
                                   :optimizations :none
                                   :target :nodejs}}]})
