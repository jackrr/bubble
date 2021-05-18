(ns bubble.bubbles
  (:require [bubble.db :as db]
            [bubble.views :as views]
            [next.jdbc :as sql]
            [bubble.db.base :refer [db]]))

(defn make-bubble [req]
  (let [{:keys [params uri]} req
        param-name (get params :bubblename)
        address (get params :participantaddress)
        req-type (if (= uri "/get-submit") "GET" "POST")]
    (println params)
    (db/make-bubble param-name)
    (str
     "<div>
        <h1>Hello " param-name address "!</h1>
        <p>Submitted via a " req-type " request.</p>
        <p><a href='..'>Return to main page</p>
      </div>")))

(defn bubble-info []
  (sql/execute! db ["select name, id from bubbles"]))

(defn bubble-page [req]
  (let [{:keys [params uri]} req
        param-id (get params :id)]
    (println params param-id req)
    (views/base-view
     [[:h1 param-id]])))

(defn index-page [req]
  (println (bubble-info))
  (views/base-view
   [[:h1 "Hello World"]
    ;; TODO: hide login link once redirect built
    [:a {:href "/login"} "Login"]
    [:h2 (str "There are " (db/bubble-count) " bubbles in the database.")]
    [:ul
     (map (fn [bubble]
            [:li [:a {:href (str "bubble/" (:bubbles/id bubble))} (:bubbles/name bubble)]]) (bubble-info))]
    [:form {:action "/newbubble" :method "post"}
     [:input {:placeholder "Name of Bubble" :name "bubblename"}]
     [:input {:placeholder "Email or Phone Number" :name "participantaddress"}]
     [:button () "blow bubble"]]
    [:h3 "test"]]))
