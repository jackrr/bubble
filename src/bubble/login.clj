(ns bubble.login
  (:require [bubble.db.base :refer [db]]
            [bubble.login.code :as code]
            [bubble.views :as views]
            [next.jdbc :as sql]
            [ring.util.response :refer [redirect]]))

;; TODO: Handle error query param
(defn form-page []
  (views/base-view [[:h1 "Login"]
                    [:form {:action "/login" :method "post"}
                     [:input {:name "phone" :placeholder "Phone #"}]
                     [:button {:name "submit"} "Send me a link"]]]))

(defn find-or-create-user! [{:keys [phone]}]
  (sql/with-transaction [tx db]
    (let [user (sql/execute-one! tx ["select * from users where phone = ?" phone])]
      (or user
          (sql/execute-one! tx ["insert into users (phone) values (?)" phone] {:return-keys true})))))

(defn parse-phone [st]
  (let [match (re-find
               (re-matcher
                #"^\+?\s*(\d*)\-?\s*\(?\s*(\d{3})\-?\s*\)?\s*(\d{3})\-?\s*(\d{4})\s*$"
                st))
        res (if (nil? match) nil (drop 1 match))]
    (when res
      (apply str (if (= "" (first res)) (into [1] res) res)))))

(defn handle-request [req]
  (let [{:keys [params]} req
        phone (-> params
                  :phone
                  parse-phone)]
    (if phone
      (let [user (find-or-create-user! {:phone phone})
            login-code (code/gen-code)]
        (code/store-code login-code (:id user))
    ;; TODO: send SMS of URL w/ code to phone
    ;; TODO: render page saying to check for text message w/ URL
        )
      (redirect (str "/login?" (ring.util.codec/form-encode {:error "Invalid phone # provided"}))))))

(comment
  (count nil)
  (str ["1" "210" "863"])
  (parse-phone "(210) 8632322")
  (re-find
   (re-matcher
    #"^\+?\s*(\d*)\-?\s*\(?\s*(\d{3})\-?\s*\)?\s*(\d{3})\-?\s*(\d{4})\s*$"
    "55555555555"))
  (drop 1 (re-find
           (re-matcher
            #"^\+?\s*(\d*)\-?\s*\(?\s*(\d{3})\-?\s*\)?\s*(\d{3})\-?\s*(\d{4})\s*$"
            "1 2108632322")))
  (print ""))
