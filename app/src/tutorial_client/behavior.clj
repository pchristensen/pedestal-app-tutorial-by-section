(ns ^:shared tutorial-client.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app :as app]
              [io.pedestal.app.dataflow :as dataflow]))

(defn inc-transform [old-value _]
  ((fnil inc 0) old-value))

(defn swap-transform [_ message]
  (:value message))

(defn publish-counter [{:keys [count name]}]
  [{msg/type :swap msg/topic [:other-counters name] :value count}])

(defn total-count [_ nums]
  (apply + nums))

(defn maximum [old-value nums]
  (apply max (or old-value 0) nums))

(defn average-count [_ {:keys [total nums]}]
  (/ total (count nums)))

(defn cumulative-average [debug key x]
  (let [k (last key)
        i (inc (or (::avg-count debug) 0))
        avg (or (::avg-raw debug) 0)
        new-avg (+ avg (/ (- x avg) i))]
    (assoc debug
      ::avg-count i
      ::avg-raw new-avg
      (keyword (str (name k) "-avg")) (int new-avg))))

(defn merge-counters [_ {:keys [me others login-name]}]
  (assoc others login-name me))

(defn sort-players [_ players]
  (into {} (map-indexed (fn [i [k v]] [k i])
                        (reverse
                         (sort-by second (map (fn [[k v]] [k v]) players))))))

(defn add-bubbles [_ {:keys [clock players]}]
  {:clock clock :count (count players)})

(defn remove-bubbles [rb other-counters]
  (assoc rb :total (apply + other-counters)))

(defn add-points [old-value message]
  (if-let [points (int (:points message))]
    (+ old-value points)
    old-value))

(defn high-score [scores {:keys [player score]}]
  (let [s ((fnil conj []) scores {:player player :score score})]
    (reverse (sort-by :score s))))

(defn start-game [inputs]
  (let [active (dataflow/old-and-new inputs [:active-game])
        login (dataflow/old-and-new inputs [:login :name])]
    (when (or (and (:new login) (not (:old active)) (:new active))
              (and (:new active) (not (:old login)) (:new login)))
      [^:input {msg/topic msg/app-model msg/type :set-focus :name :game}])))

(defn init-main [_]
  [[:transform-enable [:main :my-counter]
    :add-points [{msg/topic [:my-counter] (msg/param :points) {}}]]])

(defn init-login [_]
  [{:login
    {:name
     {:transforms
      {:login [{msg/type :swap msg/topic [:login :name] (msg/param :value) {}}
               {msg/type :set-focus msg/topic msg/app-model :name :wait}]}}}}])

(defn init-wait [_]
  (let [start-game {msg/type :swap msg/topic [:active-game] :value true}]
    [{:wait
      {:start
       {:transforms
        {:start-game [{msg/topic msg/effect :payload start-game}
                      start-game]}}}}]))

(def example-app
  {:version 2
   :transform [[:inc  [:*] inc-transform]
               [:swap [:**] swap-transform]
               [:add-points [:my-counter] add-points]
               [:debug [:pedestal :**] swap-transform]]
   :debug true
   :effect #{[{[:my-counter] :count [:login :name] :name} publish-counter :map]}
   :derive #{[#{[:pedestal :debug :dataflow-time]} [:pedestal :debug :dataflow-time-max] maximum :vals]
             [#{[:pedestal :debug :dataflow-time]} [:pedestal :debug] cumulative-average :map-seq]
             [{[:my-counter] :me [:other-counters] :others [:login :name] :login-name} [:counters] merge-counters :map]
             [#{[:counters]} [:player-order] sort-players :single-val]
             [#{[:counters :*]} [:total-count] total-count :vals]
             [#{[:counters :*]} [:max-count] maximum :vals]
             [{[:counters :*] :nums [:total-count] :total} [:average-count] average-count :map]
             [{[:clock] :clock [:counters] :players} [:add-bubbles] add-bubbles :map]
             [#{[:other-counters :*]} [:remove-bubbles] remove-bubbles :vals]}
   :emit [{:init init-login}
          [#{[:login :*]} (app/default-emitter [])]
          {:init init-main}
          {:init init-wait}
          {:in #{[:counters :*]} :fn (app/default-emitter [:wait]) :mode :always}
          [#{[:total-count]
             [:max-count]
             [:average-count]} (app/default-emitter [:main])]
          {:in #{[:counters :*]} :fn (app/default-emitter [:main]) :mode :always}
          [#{[:add-bubbles]} (app/default-emitter [:main])]
          [#{[:add-bubbles] [:remove-bubbles]} (app/default-emitter [:main])]
          [#{[:player-order :*]} (app/default-emitter [:main])]
          [#{[:pedestal :debug :dataflow-time]
             [:pedestal :debug :dataflow-time-max]
             [:pedestal :debug :dataflow-time-avg]} (app/default-emitter [])]]
   :focus {:login [[:login]]
           :wait  [[:wait]]
           :game  [[:main] [:pedestal]]
           :default :login}
   :continue #{[#{[:login :name] [:active-game]} start-game]}})
