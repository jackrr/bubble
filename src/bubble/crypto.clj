(ns bubble.crypto
  (:require
   [digest :refer [sha1]]))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn gen-login-code []
  (-> (rand-str 6)
      (str (System/currentTimeMillis))
      sha1))

(comment
  (gen-login-code)
  (print ""))
