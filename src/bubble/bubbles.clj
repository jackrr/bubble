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

(defn bubble-names []
  (sql/execute! db ["select name from bubbles"]))

(defn index-page [req]
  (println (bubble-names))
  (views/base-view
   [[:h1 "Hello World"]
    ;; TODO: hide login link once redirect built
    [:a {:href "/login"} "Login"]
    [:h2 (str "There are " (db/bubble-count) " bubbles in the database.")]
    [:ul
     (map (fn [bubble]
            [:li (:bubbles/name bubble)]) (bubble-names))]
    [:form {:action "/newbubble" :method "post"}
     [:input {:placeholder "Name of Bubble" :name "bubblename"}]
     [:input {:placeholder "Email or Phone Number" :name "participantaddress"}]
     [:button () "blow bubble"]]
    [:h3 "test"]]))
