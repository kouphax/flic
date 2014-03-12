(ns flic.core-test
  (require [midje.sweet :refer :all]
           [flic.core :refer :all]
           [flic.groups :as groups]
           [flic.store :as store]))

(def user { :id 1 })

; -- GROUP TESTS ---------------------------------------------------------------
(fact
  "in-group can be used to test if a user is in a group"
  (groups/define-group :none (fn [_] false))
  (groups/define-group :all (fn [_] true))
  (groups/in-group? :all nil)   => true
  (groups/in-group? :all user)  => true
  (groups/in-group? :none nil)  => false
  (groups/in-group? :none user) => false)

(fact
  "in-group will return false when the group is not real"
  (groups/in-group? :does-not-exist user) => false)

(fact
  "we can add groups in a single go"
  (groups/define-groups { :always (fn [_] true)
                          :never  (fn [_] false) })
  (groups/in-group? :always nil) => true
  (groups/in-group? :never nil)  => false)

; -- FEATURE TESTS -------------------------------------------------------------
(fact
  "we can add and remove a user from a feature"
  (backing-store! (store/in-memory-store))
  (active? :fact-1 user) => false
  (activate-user! :fact-1 user)
  (active? :fact-1 user) => true
  (deactivate-user! :fact-1 user)
  (active? :fact-1 user) => false)

(fact
  "we can init a store with some initial data"
  (backing-store! (store/in-memory-store { :fact-1 { :users #{ 2 } } }))
  (active? :fact-1 user) => false)

(fact
  "we can add and remove groups from a feature "
  (backing-store! (store/in-memory-store))
  (groups/define-group :all (fn [_] true))
  (active? :fact-1 user) => false
  (activate-group! :fact-1 :all)
  (active? :fact-1 user) => true)

(fact
  "we can add non existing groups and itll just return false which is not nice"
  (backing-store! (store/in-memory-store))
  (activate-group! :fact-1 :nope)
  (active? :fact-1 user) => false)

(fact
  "we can activate everyone easily"
  (backing-store! (store/in-memory-store))
  (active? :fact-1 user) => false
  (activate-all! :fact-1)
  (active? :fact-1 user) => true)

(fact
  "we can add people slowly via percentage activations"
  (backing-store! (store/in-memory-store))
  (active? :fact-1 { :id 0 }) => false
  (active? :fact-1 { :id 1 }) => false
  (active? :fact-1 { :id 2 }) => false
  (active? :fact-1 { :id 3 }) => false
  (activate-percentage! :fact-1 20)
  (active? :fact-1 { :id 0 }) => true
  (active? :fact-1 { :id 1 }) => false
  (active? :fact-1 { :id 2 }) => false
  (active? :fact-1 { :id 3 }) => true
  (activate-percentage! :fact-1 100)
  (active? :fact-1 { :id 0 }) => true
  (active? :fact-1 { :id 1 }) => true
  (active? :fact-1 { :id 2 }) => true
  (active? :fact-1 { :id 3 }) => true
  (deactivate-percentage! :fact-1)
  (active? :fact-1 { :id 0 }) => false
  (active? :fact-1 { :id 1 }) => false
  (active? :fact-1 { :id 2 }) => false
  (active? :fact-1 { :id 3 }) => false)
