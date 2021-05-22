(ns bubble.delivery
  (:require [bubble.db :refer [db]]
            [bubble.delivery.sms :as sms]
            [dotenv :refer [env]]
            [next.jdbc :as sql]))

;; Do not buy extra phone #s by default
(def BUY_SMS_METHODS (boolean (env "BUY_SMS_METHODS")))

(defn- acquire-sender [tx]
  (let [phone (if BUY_SMS_METHODS
                (sms/acquire-number) sms/from-phone)]
    (:senders/id (sql/execute-one!
                  tx
                  ["INSERT INTO senders (phone) VALUES (?)" phone]
                  {:return-keys true}))))

(defn assign-sender [tx user-id]
  ;; Only does phone # methods for now
  (println (sql/execute-one!
            tx
            [(str
              "select id from senders where id not in "
              "(select bu.sender_id from bubbles_users bu "
              "join senders s on bu.sender_id = s.id "
              "where bu.user_id = ?) "
              "limit 1")
             user-id]))
  (or (some-> (sql/execute-one!
               tx
               [(str
                 "select id from senders where id not in "
                 "(select bu.sender_id from bubbles_users bu "
                 "join senders s on bu.sender_id = s.id "
                 "where bu.user_id = ?) "
                 "limit 1")
                user-id])
              :senders/id)
      (acquire-sender tx)))

(defn send-welcome-message [bubble-id user-id]
  (let [data (sql/execute-one!
              db
              [(str "select u.phone,b.name,s.phone from bubbles_users bu "
                    "join users u on u.id = bu.user_id "
                    "join senders s on s.id = bu.sender_id "
                    "join bubbles b on b.id = bu.bubble_id "
                    "where bu.user_id = ? and bu.bubble_id = ?") user-id bubble-id])
        member-count (:count
                      (sql/execute-one!
                       db ["select count(*) from bubbles_users where bubble_id = ?" bubble-id]))]
    (sms/send-message {:to (:users/phone data)
                       :from (:senders/phone data)
                       :body (str "You have successfully joined bubble " (:bubbles/name data)
                                  ". It has " member-count " members."
                                  " Simply reply to send a message to everyone.")})))

(defn handle-inbound-sms [req]
;; TODO: add incoming handler
;; map to user/bubble
;; broadcast on bubble w/ prefix of who sent (do not send to sender)
;; if no user/bubble found -- send a default response
  )
