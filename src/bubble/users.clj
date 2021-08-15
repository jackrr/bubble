(ns bubble.users
  (:require [bubble.views :as views]
            [next.jdbc :as sql]
            [bubble.db :refer [db]]
            [bubble.nav :refer [redirect-home-with-error]]
            [ring.util.response :refer [redirect]]))

(defn- redirect-different-user [req on-ok]
  (if (= (-> req :current-user :users/id str)
         (get-in req [:params :user-id]))
    (on-ok req)
    (redirect-home-with-error "Cannot edit other users")))

(defn edit-page [req]
  (redirect-different-user
   req
   (fn [req]
     (let [user (:current-user req)]
       (views/base-view
        [[:h1 "Update your profile"]
         [:form
          {:method "post"
           :action (str "/profiles/" (:users/id user))}
          [:div
           [:label "Username"]]
          [:input {:name "name" :value (:users/name user) :placeholder "Username"}]
          [:p (views/style :margin-top "4px") "Your username will be displayed to other members of any bubbles you participate in."]
          [:div (views/style :margin-top "16px")
           [:button "Update profile"]]]])))))

(defn update-profile [req]
  (redirect-different-user
   req
   (fn [req]
     (sql/execute-one! db ["update users set name = ? where id = ?"
                           (get-in req [:params :name])
                           (-> req
                               :params
                               :user-id
                               java.util.UUID/fromString)])
     (redirect "/"))))

(defn user->handle [user]
  (or (:users/name user) (:users/phone user)))
