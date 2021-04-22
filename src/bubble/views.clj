(ns bubble.views
  (:use [hiccup core page]))

(defn index-page [count]
  (html5
    [:head
      [:title "Hello World"]
      (include-css "/style.css")]
    [:body
     [:h1 "Hello World"]
     [:h2 (str "There are " count " bubbles in the database.")]
     [:form {:action "/newbubble" :method "post"}
      [:input {:placeholder "Name of Bubble" :name "bubblename"}]
      [:input {:placeholder "Email or Phone Number" :name "participantaddress"}]
      [:button () "blow bubble"]]
     [:h3 "test"]
     ]))
