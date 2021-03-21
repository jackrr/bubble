(ns bubble.db
  (:require [next.jdbc :as sql]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

(def db-conf {:dbtype "postgres"
              :dbname "bubble"
              :user "bubble"
              :password (env :db-password)
              :port 5432})

(def ds (sql/get-datasource db-conf))

(defn bubble-count []
  (:count (sql/execute-one! ds ["select count(*) from bubbles"])))
