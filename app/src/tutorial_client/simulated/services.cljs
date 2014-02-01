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

(defn start-game-simulation [input-queue]
  (receive-messages input-queue))

(defn add-player [name input-queue]
  (p/put-message input-queue {msg/type :swap
                              msg/topic [:other-counters name]
                              :value 0}))

(defrecord MockServices [app]
  p/Activity
  (start [this]
    (platform/create-timeout 10000 #(add-player "abc" (:input app)))
    (platform/create-timeout 15000 #(add-player "xyz" (:input app))))
  (stop [this]))

(defn services-fn [message input-queue]
  (if (and (= (msg/topic message) [:active-game]) (:value message))
    (start-game-simulation input-queue)))
