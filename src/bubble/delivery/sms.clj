(ns bubble.delivery.sms
  (:import [com.twilio.security RequestValidator])
  (:require [bubble.config :as config]
            [bubble.db :refer [db]]
            [bubble.phone :as phone]
            [bubble.users :as users]
            [clj-http.client :as client]
            [clojure.data.xml :as xml]
            [clojure.walk :refer [stringify-keys]]
            [dotenv :refer [env]]
            [next.jdbc :as sql]
            [ring.util.request :refer [request-url]]
            [ring.util.response :refer [content-type get-header response status]]))

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
    ;; Omit leading "+" character from Twilio
    (subs phone-number 1)))

(defn- xml-response [xml-obj]
  (-> xml-obj
      xml/emit-str
      response
      (content-type "text/xml")))

(defn empty-response []
  (xml-response (xml/element :Response)))

(defn message-response [msg]
  (xml-response (xml/element :Response
                             {}
                             (xml/element :Message
                                          {}
                                          (xml/element :Body {} msg)))))

(defn handle-inbound-sms [req]
  (let [params (:params req)]
    (if (not (.validate
              (new RequestValidator auth-token)
              (if-let [host (env "HOST")]
                (str host "/incoming-sms")
                (request-url req))
              (stringify-keys params)
              (get-header req  "X-Twilio-Signature")))
      (status (response "Invalid signature") 400)
      (let [sender-phone (phone/parse-phone (:To params))
            user-phone (phone/parse-phone (:From params))
            msg (:Body params)
            data (sql/execute-one!
                  db
                  [(str "select u.id,u.name,u.phone,bu.bubble_id "
                        "from users u "
                        "join bubbles_users bu on bu.user_id = u.id "
                        "join senders s on bu.sender_id = s.id "
                        "where u.phone = ? and s.phone = ?")
                   user-phone sender-phone])]
        (if data
          (do
            (let [recipients (sql/execute!
                              db
                              [(str "select u.phone,s.phone "
                                    "from users u "
                                    "join bubbles_users bu on bu.user_id = u.id "
                                    "join senders s on bu.sender_id = s.id "
                                    "where u.id != ? and bu.bubble_id = ?")
                               (:users/id data) (:bubbles_users/bubble_id data)])]
              (doall
               (map (fn [bubble-user-sender]
                      (send-message {:to (:users/phone bubble-user-sender)
                                     :from (:senders/phone bubble-user-sender)
                                     :body (str msg " - From " (users/user->handle data))}))
                    recipients))
              (empty-response)))
          (message-response
           "We're sorry, we don't recognize you. Please sign in and check your bubble memberships."))))))

(comment
  (send-message {:to "" :body "Hello there!"})
  (:body (message-response "test message"))
  account-sid

  (-> env keys sort)
  (env2/env "TWILIO_PHONE_NUMBER"))
