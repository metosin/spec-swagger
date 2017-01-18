(ns spec-swagger.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            spec-swagger.core-test))

(enable-console-print!)

(doo-tests 'spec-swagger.core-test)
