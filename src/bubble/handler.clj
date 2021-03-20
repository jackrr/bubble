(ns bubble.handler
  (:require [bubble.db :as db]
            [bubble.views :as views]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] (views/index-page (db/bubble-count)))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)))
