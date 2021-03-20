(ns bubble.views
  (:use [hiccup core page]))

(defn index-page [count]
  (html5
    [:head
      [:title "Hello World"]
      (include-css "/style.css")]
    [:body
     [:h1 "Hello World"]
     [:h2 (str "There are " count " bubbles in the database.")]]))
