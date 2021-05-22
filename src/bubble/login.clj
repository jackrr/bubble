(ns bubble.login
  (:require [bubble.config :as config]
            [bubble.db :refer [db]]
            [bubble.delivery.sms :as sms]
            [bubble.login.code :as code]
            [bubble.login.session :as session]
            [bubble.views :as views]
            [dotenv :refer [env]]
            [next.jdbc :as sql]
            [ring.util.response :refer [content-type redirect response]]))

(defn login-error-redirect [error-message]
  (redirect (str "/login?" (ring.util.codec/form-encode {:error error-message}))))

(defn logged-in [req handler]
  (let [user (session/current-user req {:extend true})]
    (if user
      (handler (assoc req :current-user user))
      (login-error-redirect "You must be logged in to see that"))))

(defn logout [req]
  (session/log-out-user req)
  (redirect "/login"))

(defn form-page [req]
  (let [error (get-in req [:params :error])]
    (views/base-view [[:h1 "Login"]
                      (when error [:p (str "Error: " error)])
                      [:form {:action "/login" :method "post"}
                       [:input {:name "phone" :placeholder "Phone #"}]
                       [:input {:name "short-code" :type "checkbox"}]
                       [:label {:for "short-code"} "Send a code instead"]
                       [:button {:name "submit"} "Send me a link"]]])))

(defn login-response [user-id]
  (if user-id
    (-> (redirect "/")
        (assoc :cookies (session/create-session-cookie user-id)))
    (login-error-redirect "Invalid or expired code. Please try again.")))

(defn handle-short-code [{:keys [session params]}]
  (login-response (code/user-id-for-code {:short-code (:code params)
                                          :code (:login-nonce session)})))

(defn handle-code [{{code :code} :params}]
  (login-response (code/user-id-for-code {:code code})))

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
                  parse-phone)
        use-short-code? (boolean (:short-code params))]
    (if phone
      (let [login-code (code/create-code (:users/id
                                          (find-or-create-user! {:phone phone}))
                                         use-short-code?)]
        (sms/send-message
         {:to phone
          :body (if use-short-code?
                  (str "Your bubble thread login code is: " (:login_codes/short_code login-code))
                  (str
                   "Click here to log in: "
                   config/base-url
                   "/login-code/"
                   (:login_codes/code login-code)))})
        (if use-short-code?
          (-> (response
                 ;; TODO: extract into own page for login to work
               (views/base-view [[:h1 "Enter your code"]
                                 [:form {:action "/login-code" :method "post"}
                                  [:input {:name "code" :placeholder "Code from SMS..."}]
                                  [:button {:name "submit"} "Login"]]]))
              (content-type "text/html")
              (assoc :session {:login-nonce (:login_codes/code login-code)}))
          (views/base-view [[:h1 "Please check your phone for your login link"]])))
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
