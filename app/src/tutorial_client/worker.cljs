(ns tutorial-client.worker
  (:require [tutorial-client.worker.init :as init]
            [tutorial-client.services :as services]))

(init/init! services/->Services services/services-fn)
