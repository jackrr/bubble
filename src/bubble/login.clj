(ns bubble.login
  (:require [bubble.db :as db]
            [bubble.views :as views]))

(defn form-page []
  (views/base-view [[:h1 "Login"]
                    [:form {:action "/login" :method "post"}
                     [:input {:name "phone" :placeholder "Phone #"}]
                     [:button {:name "submit"} "Send me a link"]]]))

(defn handle-request [req]
  (let [{:keys [params]} req]
    ;; TODO: find or create user by phone param
    ;; TODO: generate a login code (unique guarantees, expiry)
    ;; TODO: store login code --> user id in redis
    ;; TODO: send SMS of URL w/ code to phone
    ;; TODO: redirect to page saying to check for text message w/ URL
    (:phone params)))
