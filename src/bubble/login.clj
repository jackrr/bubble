(ns bubble.login
  (:require [bubble.config :as config]
            [bubble.db :refer [db]]
            [bubble.delivery.sms :as sms]
            [bubble.login.code :as code]
            [bubble.login.session :as session]
            [bubble.phone :as phone]
            [bubble.views :as views]
            [dotenv :refer [env]]
            [next.jdbc :as sql]
            [ring.util.response :refer [content-type redirect response]]))

(defn login-error-redirect [{error-message :error-message redirect-uri :redirect-uri}]
  (redirect (str "/login?" (ring.util.codec/form-encode (cond-> {:error error-message}
                                                          redirect-uri
                                                          (assoc :redirect_uri redirect-uri))))))

(defn login-with-redirect [req]
  (redirect (str "/login?" (ring.util.codec/form-encode
                            {:info "You'll need to log in before you can proceed to that page."
                             :redirect_uri (:uri req)}))))

(defn logged-in [req handler]
  (let [user (session/current-user req {:extend true})]
    (if user
      (handler (assoc req :current-user user))
      (login-with-redirect req))))

(defn logout [req]
  (session/log-out-user req)
  (redirect "/login"))

(defn form-page [req]
  (let [error (get-in req [:params :error])
        info (get-in req [:params :info])
        redirect-uri (get-in req [:params :redirect_uri])]
    (views/base-view [[:h1 "Login"]
                      (when error [:p (str "Error: " error)])
                      (when info [:p info])
                      [:div "Welcome to *bubble thread*. We're happy you're here."]
                      [:p]
                      [:div "Bubble thread is a tool that allows members to create and collaboratively structure groups of people (bubbles!)
                      and their text communications (threads!). All communications are currently delivered via SMS, but we're planning to add email as well."]
                      [:p]
                      [:div "Add a username and phone number below to get a login link. Then can start creating bubbles and inviting people to join them."]
                      [:p]

                      [:form {:action "/login" :method "post"}
                       (when redirect-uri
                         [:input {:name "redirect_uri" :value redirect-uri :type "hidden"}])
                       [:input {:name "username" :placeholder "username"}]
                       [:p]
                       [:input {:name "phone" :placeholder "phone number"}]
                       [:p]
                       [:input {:value "short-code" :name "sms_short_code" :type "radio"}]
                       [:label {:for "short-code"} "Send me a short code"]
                       [:span (views/style :padding-right "16px")]
                       [:input {:value "link" :name "sms_short_code" :type "radio"}]
                       [:label {:for "link"} "Send me a link"]
                       [:p]
                       [:button {:name "submit"} "Send me a shortcode or a link"]]])))

(defn login-response [{user-id :user-id redirect-uri :redirect-uri}]
  (println user-id redirect-uri)
  (if user-id
    (-> (redirect (or redirect-uri "/"))
        (assoc :cookies (session/create-session-cookie user-id)))
    (login-error-redirect {:error-message "Invalid or expired code. Please try again."
                           :redirect-uri redirect-uri})))

(defn handle-short-code [{:keys [session params]}]
  (let [user-id (code/user-id-for-code {:short-code (:code params)
                                        :code (:login-nonce session)})]
    (println user-id (:redirect_uri params))
    (login-response {:user-id user-id
                     :redirect-uri (:redirect_uri params)})))

(defn handle-code [{{code :code redirect-uri :redirect_uri} :params}]
  (login-response {:user-id (code/user-id-for-code {:code code})
                   :redirect-uri redirect-uri}))

(defn find-or-create-user! [{:keys [phone name]}]
  (sql/with-transaction [tx db]
    (let [user (sql/execute-one! tx ["select * from users where phone = ?" phone])]
      (or user
          (sql/execute-one! tx ["insert into users (phone, name) values (?,?)" phone name] {:return-keys true})))))

(defn handle-request [req]
  (let [{:keys [params]} req
        name (:username params)
        phone (-> params
                  :phone
                  phone/parse-phone)
        redirect-uri (:redirect_uri params)
        use-short-code? (= "short-code" (:sms_short_code params))]
    (if phone
      (let [login-code (code/create-code (:users/id
                                          (find-or-create-user! {:phone phone :name name}))
                                         use-short-code?)]
        (sms/send-message
         {:to phone
          :body (if use-short-code?
                  (str "Your bubble thread login code is: " (:login_codes/short_code login-code))
                  (str
                   "Click here to log in: "
                   config/base-url
                   "/login-code/"
                   (:login_codes/code login-code)
                   (when redirect-uri
                     (str "?"
                          (ring.util.codec/form-encode {:redirect_uri redirect-uri})))))})
        (if use-short-code?
          (-> (response
               (views/base-view [[:h1 "Enter the code we sent to your phone to complete log in"]
                                 [:form {:action "/login-code" :method "post"}
                                  [:input {:name "code" :placeholder "Code from SMS..."}]
                                  (when redirect-uri
                                    [:input {:name "redirect_uri" :value redirect-uri :type "hidden"}])
                                  [:button {:name "submit"} "Login"]]]))
              (content-type "text/html")
              (assoc :session {:login-nonce (:login_codes/code login-code)}))
          (views/base-view [[:h1 "Please check your phone for your login link"]])))
      (login-error-redirect {:error-message "Invalid phone # provided"
                             :redirect-uri redirect-uri}))))

(comment
  (count nil)
  (str ["1" "210" "863"])
  (phone/parse-phone "(210) 8632322")
  (re-find
   (re-matcher
    #"^\+?\s*(\d*)\-?\s*\(?\s*(\d{3})\-?\s*\)?\s*(\d{3})\-?\s*(\d{4})\s*$"
    "55555555555"))
  (drop 1 (re-find
           (re-matcher
            #"^\+?\s*(\d*)\-?\s*\(?\s*(\d{3})\-?\s*\)?\s*(\d{3})\-?\s*(\d{4})\s*$"
            "1 2108632322")))
  (print ""))
