(ns bubble.login.code
  (:require [bubble.db.base :refer [db]]
            [next.jdbc :as sql]
            [digest :refer [sha1]]))

;; 10 minutes
(def MAGIC_LINK_MILLISECONDS (* 10 60 1000))

(defn delete-expired-codes! []
  (sql/execute! db ["delete from sessions where expires_at < ?" (java.util.Date.)]))

(defn store-code [code user-id]
  (sql/execute! db ["insert into login_codes (code, user_id, expires_at) values (?,?,?)"
                    code
                    user-id
                    (java.util.Date. (+ MAGIC_LINK_MILLISECONDS (System/currentTimeMillis)))]))

(defn user-id-for-code [code]
  (delete-expired-codes!)
  (when code (:login_codes/user_id
              (sql/execute-one!
               db
               ["select * from login_codes where code = ?" code]))))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn gen-code []
  (-> (rand-str 6)
      (str (System/currentTimeMillis))
      sha1))

(comment
  (rand-str 16)
  (gen-login-code)
  (print ""))
