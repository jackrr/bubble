(ns bubble.crypto
  (:require
   [digest :refer [sha1]]))

;; TODO: login code should be 6-digit number, check datastore for uniqueness
(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn gen-login-code []
  (-> (rand-str 6)
      (str (System/currentTimeMillis))
      sha1))

(comment
  (rand-str 10)
  (gen-login-code)
  (print ""))
