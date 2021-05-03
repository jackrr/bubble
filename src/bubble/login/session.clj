(ns bubble.login.session
  (:require [bubble.db.base :refer [db]]
            [next.jdbc :as sql]
            [ring.middleware.session.cookie :as ring-cookie]))

;; TODO: move key to env, rotate (this aint deployed anywhere, would be hackers :) )
(def config {:store (ring-cookie/cookie-store {:key "MSHCSGBAVHWTGJNA"})
             :cookie-attrs {:max-age 3600
                            :secure true}})

;; 1 hour
(def SESSION_AGE_MILLISECONDS (* 60 60 1000))

(defn delete-expired-sessions! []
  (sql/execute! db ["delete from sessions where expires_at < ?" (java.util.Date.)]))

(defn current-user [{{session-id :session-id} :session}]
  (when session-id
    (sql/execute-one!
     db
     ["select * from sessions s left join users u on u.id = s.user_id where s.id = ?"
      session-id])))

(defn create-session [user-id]
  (delete-expired-sessions!)
  (:sessions/id
   (sql/execute-one!
    db
    ["insert into sessions (user_id, expires_at) values (?,?)"
     user-id (java.util.Date. (+ SESSION_AGE_MILLISECONDS (System/currentTimeMillis)))]
    {:return-keys true})))
