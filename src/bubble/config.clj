(ns bubble.config
  (:require [dotenv :refer [env]]))

(def base-url
  (or (env "HOST")
      "http://localhost:3000"))
