(ns bubble.db.users
  (:require [next.jdbc :as sql]
            [bubble.db.base :refer [db]]))

(defn find-or-create! [{:keys [phone]}]
  (sql/with-transaction [tx db]
    (let [user (sql/execute-one! tx ["select * from users where phone = ?" phone])]
      (or user
          (sql/execute-one! tx ["insert into users (phone) values (?)" phone] {:return-keys true})))))

(comment
  (find-or-create! {:phone "12108632323"})
  (print ""))
