(ns tutorial-client.simulated.worker
  (:require [tutorial-client.worker.init :as init]
            [tutorial-client.simulated.services :as services]))

(init/init! services/->MockServices services/services-fn)
