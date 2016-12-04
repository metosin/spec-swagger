(require 'cljs.closure)

(cljs.closure/build
  ; Includes :source-paths and :test-paths already
  "test"
  {:main "spec-swagger.runner"
   :output-to "target/generated/js/out/tests.js"
   :source-map true
   :output-dir "target/generated/js/out"
   :optimizations :none
   :target :nodejs})

(shutdown-agents)
