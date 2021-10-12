(ns bubble.nav
  (:require [ring.util.response :refer [redirect]]
            [ring.util.codec :as c]))

(defn redirect-home-with-error [msg]
  (redirect (str "/?" (c/form-encode
                       {:error msg}))))
