(ns tutorial-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro tutorial-client-templates
  []
  {:tutorial-client-page (dtfn (tnodes "game.html" "tutorial") #{:id})})
