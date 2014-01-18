(ns tutorial-client.rendering
  (:require [domina :as dom]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.render.push.handlers :as h])
  (:require-macros [tutorial-client.html-templates :as html-templates]))

(def templates (html-templates/tutorial-client-templates))

(defn render-page [renderer [_ path] transmitter]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:tutorial-client-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id :message ""}))))

(defn render-message [renderer [_ path _ new-value] transmitter]
  (templates/update-t renderer path {:message new-value}))

(defn render-config []
  [[:node-create  [:main] render-template]
   [:node-destroy [:main] h/default-destroy]])

(defn render-template [renderer [_ path] _]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:tutorial-client-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id}))))
