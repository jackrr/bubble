(ns bubble.db
  (:require [next.jdbc :as sql]
            [bubble.db.base :refer [db]]))

(defn bubble-count []
  (:count (sql/execute-one! db ["select count(*) from bubbles"])))

(defn make-bubble [name]
  (sql/execute-one! db ["INSERT INTO bubbles (name) VALUES (?)" name]
                    {:return-keys true}))
