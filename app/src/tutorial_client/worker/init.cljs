(ns tutorial-client.worker.init
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [tutorial-client.behavior :as behavior]
            [tutorial-client.post-processing :as post]
            [tutorial-client.clock :as clock]
            [cljs.reader :as reader]
            [io.pedestal.app.render :as render]))

(defn init! [services-ctor effects-fn]
  (let [app (app/build (post/add-post-processors behavior/example-app))
        services (services-ctor app)
        render-fn (fn [deltas _]
                    (doseq [d deltas]
                      (js/postMessage (pr-str d))))
        app-model (render/consume-app-model app render-fn)]
    (app/consume-effects app effects-fn)
    (app/begin app)
    (clock/increment-game-clock (:input app))
    (js/addEventListener "message"
                         (fn [e]
                           (p/put-message (:input app)
                                          (reader/read-string (.-data e))))
                         false)
    (p/start services)))
