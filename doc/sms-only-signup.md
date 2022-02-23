# SMS-only Signup

## Goals

- P1 -- Be able to join a bubble without going to website
- P1 -- Set username without going to website
- P1 -- Invite someone to join a bubble without going to website
- P2 -- Create a bubble without going to website

## UX

### In-thread help macro

- Update welcome message to contain additional instructions: "Send 'help' to get a lowdown of anything you can do'"

1. Send '.help' to bubble
1. Message DOES NOT forward to all memebers
1. Receive SMS of instructions of available actions in bubble (invite only for now)

### Inviting

#### In-thread invite macro

1. Send ".Invite: 1111111111" in bubble
1. Message DOES NOT forward to all members
1. GOTO "Joining" flow for person with # 1111111111
1. Send confirmation of invite to all members: "Jack invited a new member"

#### Rejected: Main-menu option

1. Send "help" (or anything) to "god" number
1. "God" number replies with menu (for now shows invite instructions) 
1. Send "Invite: 1111111111" to "god" number
1. GOTO "Joining" flow for person with # 1111111111
1. Send confirmation of invite to sender from step 1

### Joining

1. Receive message from bubble number to join bubble prompting for username
1. Replies with username
1. --> Existing welcome experience

Note: All of the above occurs on the bubble x sender phone #

## Implementation

- FSM system for incoming messages
- Each flow is a state machine (help, invite, broadcast?, join)

### Finding next state:

```
inputs: message, bubble_user

;; Active state on a machine gets priority
ongoing_flow = find_pre_existing_flow(bubble_user)

if ongoing_flow exists
  next_state = ongoing_flow.get_next_state(message)
end

;; If no pre-exising flow, or message did not qualify for pre-existing flow,
;; proceed checking all flows to see if message qualifies for one
for each flow and while !next_state
  next_state = flow.get_next_state(message)
end
```

### Detection of transition matching

```
inputs: message, flow, state

next_qualifier_fns = get_transition_qualifiers(state)
qualifier_fn = find in next_qualifier_fns |fn| where fn.call(message) is true
next_state = state for qualifier_fn

;; invite qualifier example
(defn invite_qualifier_fn [msg]
  (regex-match /^invite:\s{phone-regex}/i)

;; join.clj
(defn next-state [current-state message]
;; returns next state message would transition to or nil if none 
)

;; invite.clj
(defn next-state [current-state message]
;; returns next state message would transition to or nil if none 
)
```

### Processing transitions

Every state transition has procedural logic tied to it.

So, overall flow looks like:

1. Find enrollment (bubble_user) for sender/receiver phones
1. Find next state transition for bubble_user X message (See "Finding next state", above)
1. Execute procedural logic tied to state transition

## Plan

1. Create FSM system w/ entrypoint APIs
1. Create each machine for flows:
   - Help
   - Invite
   - Broadcast
   - Join
1. State persistence layer
