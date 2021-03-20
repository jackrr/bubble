(ns bubble.db
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

(def db-url (env :database-url))

(defn bubble-count []
  (sql/query db-url ["select count(*) from bubbles"]))
