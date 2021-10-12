(ns bubble.login.code
  (:require [bubble.db :refer [db]]
            [next.jdbc :as sql]
            [digest :refer [sha1]]))

;; 10 minutes
(def MAGIC_LINK_MILLISECONDS (* 10 60 1000))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn gen-short-code []
  (rand-str 6))

(defn gen-code []
  (-> (rand-str 6)
      (str (System/currentTimeMillis))
      sha1))

(defn delete-expired-codes! []
  (sql/execute! db ["delete from login_codes where expires_at < ?" (java.util.Date.)]))

(defn create-code [user-id short]
  (first (sql/execute! db ["insert into login_codes (code, short_code, short, user_id, expires_at) values (?,?,?,?,?)"
                           (gen-code)
                           (gen-short-code)
                           short
                           user-id
                           (java.util.Date. (+ MAGIC_LINK_MILLISECONDS (System/currentTimeMillis)))]
                       {:return-keys true})))

(defn user-id-for-code [{:keys [code short-code]}]
  (delete-expired-codes!)
  (when code (:login_codes/user_id
              (sql/execute-one!
               db
               (if short-code
                 ["select * from login_codes where code = ? and short_code = ? and short = true" code short-code]
                 ["select * from login_codes where code = ? and short = false" code])))))

(comment
  (rand-str 16)
  (gen-login-code)
  (print ""))
