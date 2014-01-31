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

(defn remove-bubbles [renderer _ _]
  (.removeBubble (game renderer)))

(defn set-score [renderer [_ path _ v] _]
  (.setScore (game renderer) (last path) v))

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

(defn add-login-template [renderer [_ path :as delta] input-queue]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (:login-page templates)]
    (dom/append! (dom/by-id parent) (html {:id id}))))

(defn add-submit-login-handler [_ [_ path transform-name messages] input-queue]
  (events/collect-and-send :click "login-button" input-queue transform-name messages
                           {"login-name" :value}))

(defn remove-submit-login-event [_ _ _]
  (events/remove-click-event "login-button"))

(defn add-wait-template [renderer [_ path :as delta] input-queue]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:wait-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id}))))

(defn add-waiting-player [renderer [_ path :as delta] input-queue]
  (let [parent (render/new-id! renderer (vec (butlast path)) "players")
        id (render/new-id! renderer path)
        html (:player templates)]
    (dom/append! (dom/by-id parent) (html {:id id :player-name (last path)}))))

(defn render-config []
  [[:node-create  [:main] add-template]
   [:node-destroy [:main] destroy-game]
   [:node-create [:main :counters :*] add-player]
   [:value [:main :counters :*] set-score]
   [:value [:main :player-order :*] set-player-order]
   [:value [:main :add-bubbles] add-bubbles]
   [:value [:main :remove-bubbles] remove-bubbles]
   [:value [:pedestal :debug :*] set-stat]
   [:value [:main :*] set-stat]
   [:transform-enable [:main :my-counter] add-handler]
   [:node-create  [:login] add-login-template]
   [:node-destroy [:login] h/default-destroy]
   [:transform-enable  [:login :name] add-submit-login-handler]
   [:transform-disable [:login :name] remove-submit-login-event]
   [:node-create  [:wait] add-wait-template]
   [:node-destroy [:wait] h/default-destroy]
   [:transform-enable  [:wait :start] (h/add-send-on-click "start-button")]
   [:transform-disable [:wait :start] (h/remove-send-on-click "start-button")]
   [:node-create  [:wait :counters :*] add-waiting-player]
   [:node-destroy [:wait :counters :*] h/default-destroy]])
