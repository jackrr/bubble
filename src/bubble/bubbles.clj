(ns bubble.bubbles
  (:require [bubble.views :as views]
            [next.jdbc :as sql]
            [bubble.config :refer [base-url]]
            [bubble.db :refer [db]]
            [bubble.delivery :as delivery]
            [bubble.login.session :as session]
            [bubble.nav :refer [redirect-home-with-error redirect-home-with-message]]
            [bubble.users :as users]
            [ring.util.response :refer [redirect]]))

(defn at-max-enrollments? [user-id]
  (< 9 (:count
        (sql/execute-one!
         db
         ["SELECT count(*) from bubbles_users where user_id = ?" user-id]))))

(defn enrolled? [bubble-id user-id]
  (= 1 (:count
        (sql/execute-one!
         db
         ["SELECT count(*) from bubbles_users where user_id = ? AND bubble_id = ?" user-id bubble-id]))))

(defn enrollment-error [bubble-id user-id]
  (cond
    (enrolled? bubble-id user-id) "Already enrolled in bubble"
    (at-max-enrollments? user-id) "Bubble membership capacity reached"))

(defn add-member [bubble-id user-id]
  (sql/with-transaction [tx db]
    (let [sender-id (delivery/assign-sender tx user-id)]
      (sql/execute-one!
       tx
       ["INSERT INTO bubbles_users (bubble_id, user_id, sender_id) VALUES (?,?,?)"
        bubble-id user-id sender-id])))
  (delivery/send-welcome-message bubble-id user-id)
  (delivery/notify-bubble-about-join bubble-id user-id))

(defn- remove-member [bubble-id user-id]
  (sql/execute-one!
    db
    ["DELETE FROM bubbles_users WHERE bubble_id = ? AND user_id = ?"
     (java.util.UUID/fromString bubble-id) user-id]))

(defn optin [req]
  (let [id (get-in req [:params :id])
        user-id (get-in req [:current-user :users/id])]
    (if-let [enrollment-error (enrollment-error (java.util.UUID/fromString id) user-id)]
      (redirect-home-with-error enrollment-error)
      (do
        (add-member (java.util.UUID/fromString id) user-id)
        (redirect (str "/bubble/" id))))))

(defn fetch-bubble-name [id]
  (sql/execute-one! db ["select name from bubbles where id = ?" (java.util.UUID/fromString id)]))

(defn count-bubble-members [bubble-id]
  (sql/execute-one!
   db
   ["select count(users) from bubbles_users bu join users on bu.user_id = users.id where bu.bubble_id = ?"
    (java.util.UUID/fromString bubble-id)]))

(defn join-bubble-form [req]
  (let [id (get-in req [:params :id])
        user (session/current-user req {:extend true})]
    (views/base-view [[:h1 "Join bubble " (get-in (fetch-bubble-name id) [:bubbles/name])]
                      [:p (let [member-count (get-in (count-bubble-members id) [:count])]
                            (if (= member-count 1)
                              "There is 1 person in the bubble."
                              (str "There are " member-count " people in the bubble.")))]
                      (when (not user) [:p "You are not logged in. You need to login or create an account in order to join this bubble."])
                      [:a {:href (str "/bubble/" id "/optin")}
                       [:button (if user
                                  "Join bubble now"
                                  "Proceed to login")]]])))

(defn bubble-info []
  (sql/execute! db ["select name, id from bubbles"]))

(defn my-bubble-info [user-id]
  (sql/execute! db ["select bubbles.name, bubbles.id from bubbles join bubbles_users bu on bu.bubble_id = bubbles.id where bu.user_id = ?" user-id]))

(defn fetch-bubble [id]
  (sql/execute-one! db ["select * from bubbles where id = ?" (java.util.UUID/fromString id)]))

(defn bubble-members [bubble-id]
  (sql/execute!
   db
   ["select * from bubbles_users bu join users on bu.user_id = users.id where bu.bubble_id = ?"
    (java.util.UUID/fromString bubble-id)]))

(defn member? [bubble-id user-id]
  (< 0 (count (sql/execute! db ["select * from bubbles_users where bubble_id = ? and user_id = ?"
                                bubble-id user-id]))))

(defn bubble-count []
  (:count (sql/execute-one! db ["select count(*) from bubbles"])))

(defn my-bubble-count [user-id]
  (:count (sql/execute-one! db ["select count(*) from bubbles_users where user_id = ?" user-id])))

(defn create-bubble [name]
  (sql/execute-one! db ["INSERT INTO bubbles (name) VALUES (?)" name]
                    {:return-keys true}))

(defn unenroll [req]
  (let [{:keys [params current-user]} req
        bubble-id (:id params)]
    (println (:id params) (:users/id current-user))
    (remove-member (:id params) (:users/id current-user))
    (redirect-home-with-message
     (str "You have been removed from bubble "
          (-> bubble-id
              fetch-bubble
              :bubbles/name)))))

(defn make-bubble [req]
  (let [{:keys [params current-user]} req
        bubble-name (get params :bubblename)
        bubble (create-bubble bubble-name)]
    (let [user-id (:users/id current-user)
          bubble-id (:bubbles/id bubble)]
      (if-let [enrollment-error (enrollment-error bubble-id  user-id)]
        (redirect-home-with-error enrollment-error)
        (do
          (add-member (:bubbles/id bubble) user-id)
          (redirect (str "bubble/" bubble-id)))))))

(defn bubble-page [req]
  (let [{:keys [params uri]} req
        param-id (get params :id)
        bubble (fetch-bubble param-id)
        bubble-id (:bubbles/id bubble)]
    (if (member? bubble-id (-> req :current-user :users/id))
      (views/base-view
       [[:h1 (:bubbles/name bubble)]
        (into [:p "To invite new members share the following link: "]
              (let [join-link (str base-url "/bubble/" param-id "/join")]
                [[:a {:href join-link} join-link]
                 " or "
                 [:a {:href (str "sms:?&" (ring.util.codec/form-encode {:body (str "Join my bubble: " join-link)}))} "Share link via SMS"]]))
        [:div
         (views/style :display "flex"
                      :gap "16px"
                      :align-items "center")
         [:a {:href "/"} "Back to home"]
         [:form {:action (str "/bubbles/" bubble-id "/unenroll") :method "post"}
          [:button "Unenroll from this bubble"]]]
        [:div
         [:h3 "Members"]
         [:ul
          (map (fn [user]
                 [:li (users/user->handle user)])
               (bubble-members param-id))]]])
      (redirect-home-with-error "Not allowed to see that."))))

(defn index-page [req]
  (let [user (:current-user req)
        user-id (:users/id user)]
    (views/base-view
     [[:h1 "Bubble Thread"]
      (when-let [error (get-in req [:params :error])]
        [:h2 (str "Error: " error)])
      (when-let [message (get-in req [:params :message])]
        [:h2 (str "Message: " message)])
      [:h2
       (str "Welcome back " (users/user->handle user) ".")
       [:a (assoc
            (views/style :font-size "16px" :padding-left "16px" :text-decoration "none")
            :href (str "profiles/" user-id "/edit")) "(edit username)"]]
      [:h3 (str "You are enrolled in " (my-bubble-count user-id) " bubbles.")]
      [:h4 "To blow a bubble, add a bubble name below. Then invite other people to enroll in that bubble."]
      [:ul
       (map (fn [bubble]
              [:li [:a {:href (str "bubble/" (:bubbles/id bubble))} (:bubbles/name bubble)]])
            (my-bubble-info user-id))]
      [:form {:action "/newbubble" :method "post"}
       [:input {:placeholder "Name of Bubble" :name "bubblename"}]
       [:span "   "]
       [:button "blow bubble"]]
      [:p]
      [:form {:action "/logout" :method "post"}
       [:button "Log out"]]])))
