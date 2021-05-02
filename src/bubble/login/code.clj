(ns bubble.login.code
  (:require [bubble.redis :refer [get setex]]
            [digest :refer [sha1]]))

;; 10 minutes
(def MAGIC_LINK_SECONDS (* 60 10))

(defn store-code [code user-id]
  (setex (str "login_codes:" code) MAGIC_LINK_SECONDS user-id))

(defn user-id-for-code [code]
  (get (str "login_codes:" code)))

;; TODO: login code should be 6-digit number, check datastore for uniqueness
(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn gen-code []
  (-> (rand-str 6)
      (str (System/currentTimeMillis))
      sha1))

(comment
  (rand-str 10)
  (gen-login-code)
  (print ""))
