(ns bubble.sms
  (:require
   [clj-http.client :as client]
   [dotenv :refer [env]]))

(def from-phone (env "TWILIO_PHONE_NUMBER"))
(def auth-token (env "TWILIO_AUTH_TOKEN"))
(def account-sid (env "TWILIO_ACCOUNT_SID"))
(def api-base "https://api.twilio.com/2010-04-01")

(defn send-message [{:keys [to body]}]
  (client/post (str api-base "/Accounts/" account-sid "/Messages.json")
               {:basic-auth [account-sid auth-token]
                :form-params {:From (str "+" from-phone)
                              :To (str "+" to)
                              :Body body}}))

(comment
  (send-message {:to "" :body "Hello there!"})
  account-sid

  (-> env keys sort)
  (env2/env "TWILIO_PHONE_NUMBER"))
