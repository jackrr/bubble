(ns bubble.bubbles
  (:require [bubble.views :as views]
            [next.jdbc :as sql]
            [bubble.db.base :refer [db]]
            [ring.util.response :refer [redirect]]))

(defn add-member [bubble-id user-id]
  (sql/execute-one! db ["INSERT INTO bubbles_users (bubble_id, user_id) VALUES (?,?)" bubble-id user-id]))

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
      [:a {:href "/"} "Home"]
      [:ul
       (map (fn [user]
    ;; TODO: show names members once we have name capture
              [:li (:users/phone user)])
            (bubble-members param-id))]])))

(defn index-page [req]
  (println (bubble-info))
  (views/base-view
   [[:h1 "Hello World"]
    ;; TODO: hide login link once redirect built
    [:a {:href "/login"} "Login"]
    [:h2 (str "There are " (bubble-count) " bubbles in the database.")]
    ;; TODO: should only show bubbles i'm in
    [:ul
     (map (fn [bubble]
            [:li [:a {:href (str "bubble/" (:bubbles/id bubble))} (:bubbles/name bubble)]]) (bubble-info))]
    [:form {:action "/newbubble" :method "post"}
     [:input {:placeholder "Name of Bubble" :name "bubblename"}]
     [:button () "blow bubble"]]
    [:h3 "test"]]))
