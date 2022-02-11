(ns bubble.incoming-sms
  (:require [tilakone.core :as tk :refer [_]]))

(def user-thread-state-chart
; State definitions, pure data here:
{::tk/states [{::tk/name :listening
               ::tk/transitions [{::tk/on #"\.help" ::tk/to :listening ::tk/actions [:send-help-menu]}
                                 {::tk/on #"\.invite" ::tk/to :listening ::tk/actions [:send-invite-prompt :send-confirmation]}
                                 ;;{::tk/on #"\.leave" ::tk.to :leaving ::tk/actions [:send-leave-prompt] }
                                 ]}
              {::tk/name :invited
                ::tk/transitions [{::tk/on #"\.*" ::tk/to :listening ::tk/actions [:join] }]
               
 ::tk/action! (fn [& args]
                (println args)
                ;; TODO: implement me
                )
 ::tk/matcher? (fn [value regex] (some? (re-matches regex value)))})

(tk/apply-signal user-thread-state-chart "whatever")

{:name :menu
 :states []}
{:name :invite
 :states []}
{:name :join
 : [{:}]}

(defn next-action [body bubble-id user-id]
  ;; Decision-making logic here
  {:flow :broadcast
   :body body
   :bubble-id bubble-id
   :user-id user-id
   :state 0})

(defn handle-message [body bubble-id user-id]
  (let [next (next-action body bubble-id user-id)]
    ((:fn next) (:args next))))

;; TODO: delete examples from below
(def count-ab-states
  [{::tk/name        :start
    ::tk/transitions [{::tk/on \a, ::tk/to :found-a}
                      {::tk/on _}]}
   {::tk/name        :found-a
    ::tk/transitions [{::tk/on \a}
                      {::tk/on \b, ::tk/to :start, ::tk/actions [:inc-val]}
                      {::tk/on _, ::tk/to :start}]}])

; FSM has states, a function to execute actions, and current state and value:

(def count-ab
  {::tk/states  count-ab-states
   ::tk/action! (fn [{::tk/keys [action] :as fsm}]
                  (case action
                    :inc-val (update fsm :count inc)))
   ::tk/state   :start
   :count       0})

; Lets apply same inputs to our FSM:

(->> ["abaaabc" "aaacb" "bbbcab"]
     (map (partial reduce tk/apply-signal count-ab))
     (map :count))
