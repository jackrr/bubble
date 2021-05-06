(ns bubble.views
  (:use [hiccup core page]))

(defn base-view [children]
  (html5
   [:head
    [:title "Hello World"]
    (include-css "/style.css")]
   (into [:body] children)))
