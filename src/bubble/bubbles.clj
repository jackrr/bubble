(ns bubble.bubbles
  (:require [bubble.views :as views]
            [next.jdbc :as sql]
            [bubble.db :refer [db]]
            [ring.util.response :refer [redirect]]))

(defn add-member [bubble-id user-id]
  (println "HIT ADD-MEMBER")
  (sql/execute-one! db ["INSERT INTO bubbles_users (bubble_id, user_id) VALUES (?,?)" bubble-id user-id]))

(defn optin [req]
  (println "optin test")
  (let [id (get-in req [:params :id])]
    (println id)
    (let [current-user-id (get-in req [:current-user :users/id])] 
        (println  current-user-id)
        (println req)
        (add-member (java.util.UUID/fromString id) current-user-id)
        (redirect (str "/bubble/" id))))
  )
  

(defn fetch-bubble-name [id]
  (sql/execute-one! db ["select name from bubbles where id = ?" (java.util.UUID/fromString id)]))


(defn count-bubble-members [bubble-id]
  (sql/execute-one!
   db
   ["select count(users) from bubbles_users bu join users on bu.user_id = users.id where bu.bubble_id = ?"
    (java.util.UUID/fromString bubble-id)]))


(defn join-bubble-form [req]
  (let [id (get-in req [:params :id])]
    (println (count-bubble-members id))
    (views/base-view [[:h1 "Login to join bubble " (get-in (fetch-bubble-name id) [:bubbles/name])]
                        [:p "There are this many people in the bubble: " (get-in (count-bubble-members id) [:count])]
                        [:p "You can join this bubble here"]
                        [:a {:href (str "/bubble/" id "/optin")}
                          [:button {:name "submit"} "Join bubble"]]])))

(defn bubble-info []
  (sql/execute! db ["select name, id from bubbles"]))

(defn fetch-bubble [id]
  (sql/execute-one! db ["select * from bubbles where id = ?" (java.util.UUID/fromString id)]))

(defn bubble-members [bubble-id]
  (sql/execute!
   db
   ["select * from bubbles_users bu join users on bu.user_id = users.id where bu.bubble_id = ?"
    (java.util.UUID/fromString bubble-id)]))

(defn bubble-count []
  (:count (sql/execute-one! db ["select count(*) from bubbles"])))

(defn make-bubble [name]
  (sql/execute-one! db ["INSERT INTO bubbles (name) VALUES (?)" name]
                    {:return-keys true}))

(defn make-bubble [req]
  (let [{:keys [params current-user]} req
        bubble-name (get params :bubblename)
        bubble (make-bubble bubble-name)]
    (add-member (:bubbles/id bubble) (:users/id current-user))
    (redirect (str "bubble/" (:bubbles/id bubble)))))

(defn bubble-page [req]
  (let [{:keys [params uri]} req
        param-id (get params :id)
        bubble (fetch-bubble param-id)]
    (println (bubble-members param-id))
    (views/base-view
     [[:h1 (:bubbles/name bubble)]
      [:h2 (str "Join bubble here: localhost:3000/bubble/" param-id "/join")]
      (let [join-link (str "http://localhost:3000/bubble/" param-id "/join")]
        [:a {:href (str "sms:?&=" (ring.util.codec/form-encode {:body (str "Join my bubble: " join-link)}))} (str "Bubble join link: " join-link)])
        [:a {:href "/"} "Home"]
      [:ul
       (map (fn [user]
    ;; TODO: show names members once we have name capture
              [:li (:users/phone user)])
            (bubble-members param-id))]
      [:form {:action param-id :method "post"}
       [:input {:placeholder "Your phone" :name "userphone"}]
       [:button () "join bubble"]]])))

(defn index-page [req]
  (println (bubble-info))
  (views/base-view
   [[:h1 "Hello World"]
    [:form {:action "/logout" :method "post"}
     [:button "Log out"]]
    [:h2 (str "There are " (bubble-count) " bubbles in the database.")]
    ;; TODO: should only show bubbles i'm in
    [:ul
     (map (fn [bubble]
            [:li [:a {:href (str "bubble/" (:bubbles/id bubble))} (:bubbles/name bubble)]]) (bubble-info))]
    [:form {:action "/newbubble" :method "post"}
     [:input {:placeholder "Name of Bubble" :name "bubblename"}]
     [:button () "blow bubble"]]
    [:h3 "test"]]))
