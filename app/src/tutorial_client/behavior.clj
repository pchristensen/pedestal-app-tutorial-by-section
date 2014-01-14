(ns ^:shared tutorial-client.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app :as app]))

(defn inc-transform [old-value _]
  ((fnil inc 0) old-value))

(defn swap-transform [_ message]
  (:value message))

(defn publish-counter [count]
  [{msg/type :swap msg/topic [:other-counters] :value count}])

(defn total-count [_ nums]
  (apply + nums))

(defn maximum [old-value nums]
  (apply max (or old-value 0) nums))

(defn average-count [_ {:keys [total nums]}]
  (/ total (count nums)))

(defn init-main [_]
  [[:transform-enable [:main :my-counter] :inc [{msg/topic [:my-counter]}]]])

(def example-app
  {:version 2
   :transform [[:inc  [:my-counter] inc-transform]
               [:swap [:**] swap-transform]]
   :effect #{[#{[:my-counter]} publish-counter :single-val]}
   :derive #{[#{[:my-counter] [:other-counters :*]} [:total-count] total-count :vals]
             [#{[:my-counter] [:other-counters :*]} [:max-count] maximum :vals]
             [{[:my-counter] :nums
               [:other-counters :*] :nums
               [:total-count] :total}
              [:average-count] average-count :map]}
   :emit [{:init init-main}
          [#{[:my-counter]
             [:other-counters :*]
             [:total-count]
             [:max-count]
             [:average-count]} (app/default-emitter [:main])]]})
