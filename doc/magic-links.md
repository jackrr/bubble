# Magic Links

Support authentication via a short-lived URL sent to a contact we trust the user
owns.

## User flows

1. Login
  1. User lands on bubble-thread.com
  1. User fills out email or phone and clicks submit on login form
     - Form submission handler generates a code, stores in redis with expiry,
        then sends an email/SMS with URL including code
     - This also creates a new user in the DB if none exists with this
       email/phone
  1. User navigates to the URL
     - Server looks up code, if not found or expired, redirect to login with an
       error
     - If found, user is "logged in", and sees an appropriate variant of the
       homepage
1. Any URL visit
  1. User lands on a non-root URL of bubble-thread.com
  1. Server first checks if an unexpired login session exists on the request
     - If it exists, render the page according to the user found from session
     - If it does not exist, redirect to homepage
1. Logout
  1. User clicks a logout button
    - Form submission handler removes the session from the data store
  1. User is redirected to the homepage
  1. User sees the logged-out version of the homepage

## Key components

- Email delivery for receiving magic links
  - Sendgrid
  - [postal](https://github.com/drewr/postal) + SMTP (gmail, etc)
- SMS delivery for receiving magic links
  - [twilio](https://twilio.com)
- Sessions for authorization once link is followed
  - Maybe built-in with ring/compojure?
- Ephemeral data store w/ expiration
  - Magic link code <> user id storage
  - Session id <> user id storage
  - Definitely use redis
- Magic link code generation, session id generation
  - Need to prevent collisions, make brute force generation undesirable
- Redirect logic in backend
- Generally-available user object on authed requests
