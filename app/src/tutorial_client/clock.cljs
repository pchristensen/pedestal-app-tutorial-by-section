(ns tutorial-client.clock
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.platform :as platform]))

(defn increment-game-clock [queue]
  (p/put-message queue {msg/type :inc msg/topic [:clock]})
  (platform/create-timeout 2000 (fn [] (increment-game-clock queue))))
