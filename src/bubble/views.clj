(ns bubble.views
  (:use [hiccup core page]))

(defn base-view [children]
  (html5
   [:head
    [:title "Hello World"]
    (include-css "/style.css")]
   (into [:body] children)))

(defn index-page [count]
  (base-view
   [[:h1 "Hello World"]
    ;; TODO: hide login link once redirect built
    [:a {:href "/login"} "Login"]
    [:h2 (str "There are " count " bubbles in the database.")]
    [:form {:action "/newbubble" :method "post"}
     [:input {:placeholder "Name of Bubble" :name "bubblename"}]
     [:input {:placeholder "Email or Phone Number" :name "participantaddress"}]
     [:button () "blow bubble"]]
    [:h3 "test"]]))
