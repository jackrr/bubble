(ns bubble.incoming-sms-test
  (:require [bubble.incoming-sms :refer [next-action]]
            [clojure.test :refer [deftest is testing]]))

(deftest next-action-test
  (testing "no special handler"
    (let [result (next-action "Hello Bubble" "1234" "1234")]
      (is (= (:flow result) :broadcast))
      (is (= (:state result) 0))
      (is (= (:body result) "Hello Bubble"))))

  (testing "'.help' triggers menu"
    (let [result (next-action ".help" "1234" "1234")]
      (is (= (:flow result) :menu)))))
