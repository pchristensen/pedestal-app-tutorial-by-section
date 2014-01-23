(ns tutorial-client.services
  (:require [io.pedestal.app.protocols :as p]
           [cljs.reader :as reader]))

(defn receive-ss-event [app e]
  (let [message (reader/read-string (.-data e))]
    (p/put-message (:input app) message)))

(defrecord Services [app]
  p/Activity
  (start [this]
    (let [source (js/EventSource. "/msgs")]
      (.addEventListener source
                         "msg"
                         (fn [e] (receive-ss-event app e))
                         false)))
  (stop [this]))

(defn services-fn [message input-queue]
  (let [body (pr-str message)]
    (let [http (js/XMLHttpRequest.)]
      (.open http "POST" "/msgs" true)
      (.setRequestHeader http "Content-type" "application/edn")
      (.send http body))))
