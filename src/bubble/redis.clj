(ns bubble.redis
  (:require [taoensso.carmine :as car :refer (wcar)]
            [environ.core :refer [env]]))

(def server1-conn {:pool {} :spec {:uri (env :redis-conn)}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn get [key]
  (wcar* (car/get key)))

(defn set [key value]
  (wcar* (car/set key value)))

(defn setex [key seconds value]
  (wcar* (car/setex key seconds value)))

(comment
  (wcar* (car/ping)))
