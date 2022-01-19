(ns bubble.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bubble.handler :refer :all]))

(deftest test-app
  (testing "home not logged in"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 302))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
