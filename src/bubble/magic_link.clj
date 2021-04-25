(ns bubble.magic-link
  (:require [taoensso.carmine :as car :refer (wcar)]
            [environ.core :refer [env]]))

(def server1-conn {:pool {} :spec {:uri (env :redis-conn)}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(comment
  (wcar* (car/ping)))

;; (defn send-magic-link [email]
;;   (let [token (gen-token)]
;;     (send-link-email token)
;;     (store-login-token token "10min")))
