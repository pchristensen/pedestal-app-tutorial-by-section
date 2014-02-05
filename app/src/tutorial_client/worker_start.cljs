(ns tutorial-client.worker-start
  (:require [io.pedestal.app.util.web-workers :as ww]
            [tutorial-client.rendering :as rendering]))

(defn ^:export main []
  (ww/run-on-web-worker! "/generated-js/worker.js"
                         :render {:type :push :id "content" :config rendering/render-config}))
