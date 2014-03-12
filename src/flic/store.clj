(ns flic.store)

; storage managment
(defprotocol FeatureStore
  "defines the contract used when working with a feature storage thing"
  (get-feature  [store key]      "retrieves a feature")
  (set-feature! [store key data] "saves the current feature"))

(deftype InMemoryStore [features]
  FeatureStore
  (get-feature  [store key]      (get @features (keyword key) {}))
  (set-feature! [store key data] (swap! features #(assoc % (keyword key) data))))

(defn in-memory-store
  "helper method for building an in memory store and can also accept some
   initial state"
  ([]              (in-memory-store {}))
  ([initial-state] (InMemoryStore. (atom initial-state))))
