(ns bubble.login.session
  (:require [bubble.db.base :refer [db]]
            [next.jdbc :as sql]
            [ring.middleware.session.cookie :as ring-cookie]))

;; 1 hour
(def SESSION_AGE_MILLISECONDS (* 60 60 1000))
(def SESSION_KEY "session-id")

(defn- expire-at []
  (java.util.Date. (+ SESSION_AGE_MILLISECONDS (System/currentTimeMillis))))

(defn- session-id-from-req [{:keys [cookies]}]
  (some-> cookies
          (get-in [SESSION_KEY :value])
          java.util.UUID/fromString))

(defn- delete-expired-sessions! []
  (sql/execute! db ["delete from sessions where expires_at < ?" (java.util.Date.)]))

(defn- create-session [user-id]
  (delete-expired-sessions!)
  (:sessions/id
   (sql/execute-one!
    db
    ["insert into sessions (user_id, expires_at) values (?,?)"
     user-id (expire-at)]
    {:return-keys true})))

(defn- extend-session [req]
  (when-let [session-id (session-id-from-req req)]
    (sql/execute-one!
     db
     ["update sessions set expires_at = ? where id = ?"
      (expire-at)
      (session-id-from-req req)])
    true))

(defn- user-from-req [req]
  (when-let [session-id (session-id-from-req req)]
    (sql/execute-one!
     db
     ["select * from sessions s left join users u on u.id = s.user_id where s.id = ?"
      session-id])))

(defn current-user
  ([req]
   (user-from-req req))
  ([req {:keys [extend]}]
   (and
    (extend-session req)
    (user-from-req req))))

(defn create-session-cookie [user-id]
  {SESSION_KEY {:value (create-session user-id)
                :secure true
                :http-only true
                :same-site :strict
                :max-age SESSION_AGE_MILLISECONDS}})
