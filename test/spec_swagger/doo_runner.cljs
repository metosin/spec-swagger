(ns spec-swagger.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            spec-swagger.swagger2.core-test
            spec-swagger.swagger2.spec-test))

(enable-console-print!)

(doo-tests
  'spec-swagger.swagger2.core-test
  'spec-swagger.swagger2.spec-test)
