(ns bubble.handler
  (:require [bubble.bubbles :as bubbles]
            [bubble.views :as views]
            [bubble.login :as login]
            [bubble.login.session :as login.session]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.logger :as logger]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer [wrap-session]]))

(defroutes app-routes
  (GET "/" req (login/logged-in req bubbles/index-page))
  (POST "/newbubble" req (login/logged-in req bubbles/make-bubble))
  (GET "/login" req (login/form-page req))
  (POST "/login" req (login/handle-request req))
  (GET "/login-code/:code" req (login/handle-code req))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (logger/wrap-with-logger)
      (wrap-session login.session/config)))
