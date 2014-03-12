(ns flic.core
  (require [biscuit.core :as digest]
           [flic.groups :as groups]
           [flic.store :refer :all]))

(def ^:private store (atom nil))

(defn backing-store! [s]
  (swap! store (fn [_] s)))

; controlling access to features

(defn- update! [key feature action]
  (let [current (get feature key #{})]
    (assoc feature key (action current))))

(defn- add! [key feature entry]
  (update! key feature #(conj % entry)))

(defn- remove! [key feature entry]
  (update! key feature (fn [entries] (remove #(= % entry) entries))))

(defn- update-feature!
  "adds or removes a specific user to a specific feature"
  [feature-key transform]
  (let [feature (get-feature @store feature-key)
        updated (transform feature) ]
    (set-feature! @store feature-key updated)))

(defn- clear-feature!
  "removes all users groups and percentages from a feature"
  [feature-key]
  (update-feature! feature-key (fn [_] {})))

(defn activate-user!
  "adds a specific user to a specific feature"
  [feature-key user]
  (update-feature! feature-key #(add! :users % (:id user))))

(defn deactivate-user!
  "removes a specific user from a specific feature"
  [feature-key user]
  (update-feature! feature-key #(remove! :users % (:id user))))

(defn activate-group!
  "adds a specific group to a specific feature"
  [feature-key group]
  (update-feature! feature-key #(add! :groups % group)))

(defn deactivate-group!
  "removes a specific group from a specific feature"
  [feature-key group]
  (update-feature! feature-key #(remove! :groups % group)))

(defn activate-percentage!
  "activates a certain percentage of users.  As the number grows the same users
   will still be allowed in"
  [feature-key percentage]
  (update-feature! feature-key #(assoc % :percentage percentage)))

(defn deactivate-percentage!
  "deactivates users who fall under the active percentage rule"
  [feature-key]
  (activate-percentage! feature-key 0))

(defn activate-all!
  "activates a feature for everyone"
  [feature-key]
  (activate-percentage! feature-key 100))

(defn deactivate-all!
  "deactivates a feature for everyone"
  [feature-key]
  (update-feature! feature-key (fn [_] {})))

(defn- is-user-of? [feature user]
  (let [users (get feature :users #{})]
    (not (nil? (some #{(:id user)} users)))))

(defn- is-in-group-of? [feature user]
  (let [groups (get feature :groups #{})]
    (not (nil? (some #(groups/in-group? % user) groups)))))

(defn- is-in-active-percentage? [feature user]
  (let [percentage (get feature :percentage 0)]
    (< (mod (digest/crc32 (str (:id user))) 100)
       percentage)))

(defn active?
  "determines is a user is active for a feature"
  [feature-key user]
  (if-let [feature (get-feature @store feature-key)]
    (or (is-user-of? feature user)
        (is-in-group-of? feature user)
        (is-in-active-percentage? feature user))
    false))
