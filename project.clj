(defproject metosin/spec-swagger "0.1.0-SNAPSHOT"
  :description "Common utilities for clojure.spec"
  :url "https://github.com/metosin/spec-swagger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[metosin/spec-tools "0.1.0-SNAPSHOT"]]
  :codeina {:target "doc"
            :src-uri "http://github.com/metosin/spec-swagger/blob/master/"
            :src-uri-prefix "#L"}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]
                             [funcool/codeina "0.5.0"]]
                   :jvm-opts ^:replace ["-server"]
                   ;:global-vars {*warn-on-reflection* true}
                   :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                                  [org.clojure/clojurescript "1.9.293"]
                                  [criterium "0.4.4"]
                                  [org.clojure/test.check "0.9.0"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [com.gfredericks/test.chuck "0.2.7"]
                                  [metosin/scjsv "0.4.0"]]}
             :perf {:jvm-opts ^:replace ["-server"]}}
  :aliases {"all" ["with-profile" "dev"]
            "perf" ["with-profile" "default,dev,perf"]
            "test-clj" ["all" "do" ["test"] ["check"]]})
