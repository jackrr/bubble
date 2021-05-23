(ns bubble.delivery
  (:require [bubble.db :refer [db]]
            [bubble.delivery.sms :as sms]
            [bubble.phone :as phone]
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
  (let [params (:params req)
        sender-phone (phone/parse-phone (:To params))
        user-phone (phone/parse-phone (:From params))
        msg (:Body params)
        data (sql/execute-one!
              db
              [(str "select u.id,u.phone,bu.bubble_id "
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
                  (println "IN MAP")
                  (println bubble-user-sender)
                  (sms/send-message {:to (:users/phone bubble-user-sender)
                                     :from (:senders/phone bubble-user-sender)
                                     ;; TODO: send username once we have available
                                     :body (str msg " - From " (:users/phone data))}))
                recipients))
          (sms/empty-response)))
      (sms/message-response
       "We're sorry, we don't recognize you. Please sign in and check your bubble memberships."))))
