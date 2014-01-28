(ns tutorial-client.rendering
  (:require [domina :as dom]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.render.push.handlers :as h]
            [io.pedestal.app.render.events :as events])
  (:require-macros [tutorial-client.html-templates :as html-templates]))

(def templates (html-templates/tutorial-client-templates))

(defn add-template [renderer [_ path :as delta] input-queue]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:tutorial-client-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id}))
    (let [g (js/BubbleGame. "game-board")]
      (render/set-data! renderer path g)
      (dotimes [_ 5] (.addBubble g)))))

(defn game [renderer]
  (render/get-data renderer [:main]))

(defn destroy-game [renderer [_ path :as delta] input-queue]
  (.destroy (game renderer))
  (render/drop-data! renderer path)
  (h/default-destroy renderer delta input-game))

(defn render-config []
  [[:node-create [:main] add-template]
   [:node-destroy [:main] destroy-game]])

(defn add-player [renderer [_ path] _]
  (.addPlayer (game renderer) (last path)))

(defn add-bubbles [renderer [_ path _ v] _]
  (dotimes [x (:count v)]
    (.addBubble (game renderer))))

(defn set-score [renderer [_ path _ v] _]
  (let [n (last path)
        g (game renderer)]
    (.setScore g n v)
    (when (not= n "Me")
      (.removeBubble g))))

(defn set-stat [renderer [_ path _ v] _]
  (let [s (last path)]
    (if-let [g (game renderer)]
      (.setStat g (name s) v))))

(defn set-player-order [renderer [_ path _ v] _]
  (let [n (last path)]
    (.setOrder (game renderer) n v)))

(defn add-handler [renderer [_ path transform-name messages] input-queue]
  (.addHandler (game renderer)
               (fn [p]
                 (events/send-transforms input-queue transform-name messages {:points p}))))

(defn render-page [renderer [_ path] transmitter]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:tutorial-client-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id :message ""}))))

(defn render-message [renderer [_ path _ new-value] transmitter]
  (templates/update-t renderer path {:message new-value}))

(defn render-config []
  [[:node-create [:main] add-template]
   [:node-destroy [:main] destroy-game]
   [:node-create [:main :counters :*] add-player]
   [:value [:main :counters :*] set-score]
   [:value [:main :player-order :*] set-player-order]
   [:value [:main :add-bubbles] add-bubbles]
   [:value [:pedestal :debug :*] set-stat]
   [:value [:main :*] set-stat]
   [:transform-enable [:main :my-counter] add-handler]])
