(ns bubble.mem-store
  (:require [taoensso.carmine :as car :refer (wcar)]
            [environ.core :refer [env]]))

(def server1-conn {:pool {} :spec {:uri (env :redis-conn)}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

;; 10 minutes
(def MAGIC_LINK_SECONDS (* 60 10))

(defn store-login-code [code user-id]
  (wcar* (car/setex (str "login_codes:" code) MAGIC_LINK_SECONDS user-id)))

(defn user-id-for-login-code [code]
  (wcar* (car/get (str "login_codes:" code))))

(comment
  (wcar* (car/ping)))

;; (defn send-magic-link [email]
;;   (let [token (gen-token)]
;;     (send-link-email token)
;;     (store-login-token token "10min")))
