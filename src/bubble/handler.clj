(ns bubble.handler
  (:require [bubble.db :as db]
            [bubble.views :as views]
            [bubble.login :as login]
            [bubble.login.session :as login.session]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.logger :as logger]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer [wrap-session]]))

(defn display-result [req]
  (let [{:keys [params uri]} req
        param-name (get params :bubblename)
        address (get params :participantaddress)
        req-type (if (= uri "/get-submit") "GET" "POST")]
    (println params)
    (db/make-bubble param-name)
    (str
     "<div>
        <h1>Hello " param-name address "!</h1>
        <p>Submitted via a " req-type " request.</p>
        <p><a href='..'>Return to main page</p>
      </div>")))

(defroutes app-routes
  ;; TODO: redirect to login if not logged in
  (GET "/" req (login/auth-wall req (views/index-page (db/bubble-count))))
  (POST "/newbubble" req (login/auth-wall req (display-result req)))
  (GET "/login" req (login/form-page req))
  (POST "/login" req (login/handle-request req))
  (GET "/login-code/:code" req (login/handle-code req))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (logger/wrap-with-logger)
      (wrap-session login.session/config)))
