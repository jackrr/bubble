(ns bubble.db
  (:require [next.jdbc :as sql]
            [next.jdbc.date-time]
            [environ.core :refer [env]]))

(def db-conf {:dbtype "postgres"
              :dbname "bubble"
              :user "bubble"
              :password (env :db-password)
              :host (or (env :db-host) "127.0.0.1")
              :port 5432})

(def db (sql/get-datasource db-conf))
