(ns tutorial-client.simulated.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.platform :as platform]))

(def counters (atom {"abc" 0 "xyz" 0}))

(defn increment-counter [key t input-queue]
  (p/put-message input-queue {msg/type :swap
                              msg/topic [:other-counters key]
                              :value (get (swap! counters update-in [key] inc) key)})
  (platform/create-timeout t #(increment-counter key t input-queue)))

(defn receive-messages [input-queue]
  (increment-counter "abc" 2000 input-queue)
  (increment-counter "xyz" 5000 input-queue))

(defrecord MockServices [app]
  p/Activity
  (start [this]
    (receive-messages (:input app)))
  (stop [this]))
