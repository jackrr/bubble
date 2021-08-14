(ns bubble.views
  (:use [hiccup core page]))

(defn style [& info]
  {:style (.trim (apply str (map #(let [[kwd val] %]
                                    (str (name kwd) ":" val "; "))
                                 (apply hash-map info))))})

(defn base-view [children]
  (html5
   [:head
    [:title "Hello World"]
    (include-css "/style.css")]
   (into [:body] children)))
