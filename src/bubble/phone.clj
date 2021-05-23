(ns bubble.phone)

(defn parse-phone [st]
  (let [match (re-find
               (re-matcher
                #"^\+?\s*(\d*)\-?\s*\(?\s*(\d{3})\-?\s*\)?\s*(\d{3})\-?\s*(\d{4})\s*$"
                st))
        res (if (nil? match) nil (drop 1 match))]
    (when res
      (apply str (if (= "" (first res)) (into [1] res) res)))))
