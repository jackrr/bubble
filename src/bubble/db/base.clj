(ns bubble.db.base
  (:require [next.jdbc :as sql]
            [next.jdbc.date-time]
            [environ.core :refer [env]]))

(def db-conf {:dbtype "postgres"
              :dbname "bubble"
              :user "bubble"
              :password (env :db-password)
              :port 5432})

(def db (sql/get-datasource db-conf))
