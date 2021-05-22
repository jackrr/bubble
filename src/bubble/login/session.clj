(ns bubble.login.session
  (:require [bubble.db :refer [db]]
            [dotenv :refer [env]]
            [lock-key.core :refer [decrypt-from-base64 encrypt-as-base64]]
            [next.jdbc :as sql]
            [ring.middleware.session.cookie :as ring-cookie]))

;; 1 hour
(def SESSION_AGE_MILLISECONDS (* 60 60 1000))
(def SESSION_KEY "session-id")
(def SECRET_KEY (or (env "AUTH_SECRET") "super secret password"))

(defn- expire-at []
  (java.util.Date. (+ SESSION_AGE_MILLISECONDS (System/currentTimeMillis))))

(defn- decrypt [token]
  (try
    (decrypt-from-base64 token SECRET_KEY)
    (catch javax.crypto.IllegalBlockSizeException e
      (do
        (println (str "Failed to decrypt session cookie: " (.getMessage e)))
        nil))))

(defn- session-id-from-req [{:keys [cookies]}]
  (some-> cookies
          (get-in [SESSION_KEY :value])
          decrypt
          java.util.UUID/fromString))

(defn- delete-expired-sessions! []
  (sql/execute! db ["delete from sessions where expires_at < ?" (java.util.Date.)]))

(defn- create-session [user-id]
  (delete-expired-sessions!)
  (->
   (sql/execute-one!
    db
    ["insert into sessions (user_id, expires_at) values (?,?)"
     user-id (expire-at)]
    {:return-keys true})
   :sessions/id
   str
   (encrypt-as-base64 SECRET_KEY)))

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

(defn log-out-user [req]
  (when-let [session-id (session-id-from-req req)]
    (sql/execute-one!
     db
     ["delete from sessions s where s.id = ?"
      session-id])))

(defn current-user
  ([req]
   (user-from-req req))
  ([req {:keys [extend]}]
   (and
    (extend-session req)
    (user-from-req req))))

;; TODO: encrypt this payload, ensure it actually expires
(defn create-session-cookie [user-id]
  {SESSION_KEY {:value (create-session user-id)
                :secure true
                :http-only true
                :same-site :strict
                :max-age SESSION_AGE_MILLISECONDS}})
