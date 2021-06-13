(ns bubble.delivery.sms
  (:require [bubble.config :as config]
            [clj-http.client :as client]
            [clojure.data.xml :as xml]
            [dotenv :refer [env]]
            [next.jdbc :as sql]
            [ring.util.response :refer [content-type response]]))

(def from-phone (env "TWILIO_PHONE_NUMBER"))
(def auth-token (env "TWILIO_AUTH_TOKEN"))
(def account-sid (env "TWILIO_ACCOUNT_SID"))
(def api-base (str "https://api.twilio.com/2010-04-01/Accounts/" account-sid))
(def incoming-sms-url (str config/base-url "/incoming-sms"))

(defn- req-payload [opts]
  (assoc opts :basic-auth [account-sid auth-token]))

(defn- available-phone-number []
  (->
   (client/get (str api-base "/AvailablePhoneNumbers/US/Local.json")
               (req-payload {:query-params {"PageSize" 1
                                            "capabilities" {"sms" true
                                                            "mms" true}}
                             :as :json}))
   (get-in [:body :available_phone_numbers])
   first
   :phone_number))

(defn send-message [{:keys [to from body]}]
  (client/post (str api-base "/Messages.json")
               (req-payload {:form-params {:From (str "+" (or from from-phone))
                                           :To (str "+" to)
                                           :Body body}})))

(defn acquire-number []
  (let [phone-number (available-phone-number)]
    (client/post (str api-base "/IncomingPhoneNumbers.json")
                 (req-payload {:form-params {:PhoneNumber phone-number
                                             :SmsUrl incoming-sms-url}}))
    phone-number))

(defn- xml-response [xml-obj]
  (-> xml-obj
      xml/emit-str
      response
      (content-type "text/xml")))

(defn empty-response []
  (xml-response (xml/element :Response)))

(defn message-response [msg]
  (xml-response (xml/element :Response
                             (xml/element :Message
                                          (xml/element :Body {} msg)))))

(comment
  (send-message {:to "" :body "Hello there!"})
  account-sid

  (-> env keys sort)
  (env2/env "TWILIO_PHONE_NUMBER"))
