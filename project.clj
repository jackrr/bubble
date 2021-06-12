(defproject bubble "0.0.0"
  :description "Create SMS and email based listservs"
  :url "https://github.com/jackrr/bubble"
  :min-lein-version "2.0.0"
  :dependencies [[compojure "1.6.1"]
                 [clj-http "3.12.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "2.2.2"]
                 [digest "1.4.10"]
                 [lynxeyes/dotenv "1.1.0"]
                 [environ "1.2.0"]
                 [hiccup "1.0.5"]
                 [lock-key "1.5.0"]
                 [com.github.seancorfield/next.jdbc "1.1.646"]
                 [org.postgresql/postgresql "42.2.19.jre7"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-logger "1.0.1"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[lein-environ "1.2.0"]
            [lein-ring "0.12.5"]]
  :uberjar-name "bubble.jar"
  :ring {:handler bubble.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]
         :env {:db-password "local"}}})
