(ns flic.groups)

; defining groups of users

(def ^:private groups
  (atom { }))

(defn define-group
  "defines a rule for identifiying groups of users"
  [group predicate-fn]
  (swap! groups merge { (keyword group) predicate-fn }))

(defn define-groups
  "defines all groups in a single statement"
  [all-groups]
  (swap! groups (fn [_] all-groups)))

(defn in-group?
  "determines if a user is in a group if the group does not exist we just
   return false"
  [group user]
  (let [predicate (get @groups (keyword group) (fn [_] false))]
    (predicate user)))
