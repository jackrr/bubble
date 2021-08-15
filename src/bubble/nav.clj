(ns bubble.nav
  (:require [ring.util.response :refer [redirect]]))

(defn redirect-home-with-error [msg]
  (redirect (str "/?" (ring.util.codec/form-encode
                       {:error msg}))))
