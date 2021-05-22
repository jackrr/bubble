(ns bubble.handler
  (:require [bubble.bubbles :as bubbles]
            [bubble.delivery :as delivery]
            [bubble.login :as login]
            [bubble.login.session :as login.session]
            [bubble.views :as views]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.logger :as logger]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [bubble.delivery :as delivery]))

(defroutes app-routes
  ;; bubble management
  (GET "/" req (login/logged-in req bubbles/index-page))
  (POST "/newbubble" req (login/logged-in req bubbles/make-bubble))
  (GET "/bubble/:id" req (bubbles/bubble-page req))
  (GET "/bubble/:id/join" req (bubbles/join-bubble-form req))
  (GET "/bubble/:id/optin" req (login/logged-in req bubbles/optin))

  ;; thread management
  (POST "/incoming-sms" req (delivery/handle-inbound-sms req))

  ;; login stuff
  (GET "/login" req (login/form-page req))
  (POST "/logout" req (login/logged-in req login/logout))
  (POST "/login" req (login/handle-request req))
  (GET "/login-code/:code" req (login/handle-code req))
  (POST "/login-code" req (login/handle-short-code req))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (logger/wrap-with-logger)
      (wrap-cookies)))
