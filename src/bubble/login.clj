(ns bubble.login
  (:require [bubble.db.base :refer [db]]
            [bubble.login.code :as code]
            [bubble.login.session :as session]
            [bubble.sms :as sms]
            [bubble.views :as views]
            [next.jdbc :as sql]
            [ring.util.response :refer [content-type redirect response]]))

(defn login-error-redirect [error-message]
  (redirect (str "/login?" (ring.util.codec/form-encode {:error error-message}))))

(defn logged-in [req handler]
  (let [user (session/current-user req)]
    (println req (:session req) user)
    (if user
      (handler (assoc req :current-user user))
      (login-error-redirect "You must be logged in to see that"))))

(defn form-page [req]
  (let [error (get-in req [:params :error])]
    (views/base-view [[:h1 "Login"]
                      (when error [:p (str "Error: " error)])
                      [:form {:action "/login" :method "post"}
                       [:input {:name "phone" :placeholder "Phone #"}]
                       [:button {:name "submit"} "Send me a link"]]])))

(defn handle-code [{{code :code} :params}]
  (let [user-id (code/user-id-for-code code)
        session-id (when user-id (session/create-session user-id))]
    (if session-id
      (-> (redirect "/")
          (assoc :session {:session-id session-id}))
      (login-error-redirect "Invalid or expired code. Please try again."))))

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
        (code/store-code login-code (:users/id user))
        (sms/send-message
         {:to phone
          :body (str
                 "Click here to log in: "
                 (-> req :scheme name)
                 "://"
                 (get-in req [:headers "host"])
                 "/login-code/"
                 login-code)})
        ;; TODO: render page saying to check for text message w/ URL
        ;; TODO: delete this link rendering as it is not a real auth test
        (views/base-view [[:a {:href (str "/login-code/" login-code)} "(TMP) Complete login"]]))
      (login-error-redirect "Invalid phone # provided"))))

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
