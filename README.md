# Introduction

On [clojars](https://clojars.org/flic)

Flic is a lightweight [feature toggle library](http://martinfowler.com/bliki/FeatureToggle.html) for Clojure based loosely on [rolllout](https://github.com/FetLife/rollout).  Useful for slowly releasing features to users or helping facilitate some A/B testing.

It offers pluggable support for different storage mediums (in memory, redis, sql etc)

It can be used to control features around

- specific users
- groups of users (defined by a predicate)
- percentage of users

The effects are componded.  So if you deactivate a specific user but they are part of a specific group or match against the baseline percentage of activated users they will still see the feature.  To this end - this is NOT an access control mechanism for restricting access.  Well its not intended to be anyway. YMMV.  

# Usage

Get it into your `project.clj` dependencies

```clojure
[flic "0.0.1"]
```

Use it.

```clojure
(require [flic.core :as flic])
```

From here we need to initialise a backing store that will be responsible for getting and setting features. The simplest one for now is the in memory store that uses an atom to persist the feature state.  We can set this by requiring the `flic.store` namespace (you can write your own as well, yes please).

```clojure
(ns examples
  (require [flic.store :as store]
           [flic.core :as flic])

(flic/backing-store! (store/in-memory-store))
```

Now we can test what features each user has access to

```clojure
(if (flic/active? :my-feature user)
  (do-some-awesome-feature-thing))
```

A user is a special-ish thing in the context of flic and we talk about that shortly.  But how do we actually __activate__ users?  Read on...

## Users

You can activate and deactivate specific users.

```clojure
(flic/activate-user! :my-feature user)
```

This will activate a user for a given feature (identified by `:my-feature` here).  A user, in the current version is a data strucutre that can produce a value when asked for `:id` e.g. { :id 1 :name "James" }.  This is currently not configurable.  Should it be?

You can deactivate this user in a similar way

```clojure
(flic/deactivate-user! :my-feature user)
```

## Groups

Groups are clusters of users that you identify using a predicate.  To use these effectively you need to define the predicate for groups outside of the actual feature store.  If you fail to define a group and then activate that group the group test will always be false. So make sure you do, or make sure I find a better way to represent this.

So we start by defining a group (require the `flic.groups` namespace).

```clojure
(groups/define-group :best-people (fn [user] (= (:name user) "James")))
```

This defines a group called `:best-people` that identifies all users whose name is `James` as a member of the group.

We can also define a a bunch of groups in one go.

```clojure
(groups/define-groups { :best-people  (fn [user] (= (:name user) "James"))
                        :worst-people (fn [user] (not (= (:name user) "James"))) })
```

Now we can include these groups in our activation scenarios

```clojure
(flic/activate-group! :my-feature :best-people)
```

Remember if its not defined it just returns false for now.  I dont like this.  Help me change it please.

Deactivating a group is not suprising.

```clojure
(flic/deactivate-group! :my-feature :best-people)
```

If you want you can also check if a person belongs to a group using
`(is-in-group? group user)`

## Percentages

Finally you can activate users via percentage of user base.  This is based on the id of the user and assumes its a number.  The percentages are rolling so activating 20% then upping it to 25% means the previous 20% will still be included in the activated people so it won't be all "You're included", "You're not included" nonsense as you roll out a new feature.

```clojure
(flic/activate-percentage! :my-feature 20)

(flic/deactivate-percentage! :my-feature 20)
```

## Blanket Activation/Deactivation

You can activate and deactivate everyone.  Say your feature is golden and good to go or perhaps your feature is broken you can use the `activate-all!` and `deactivate-all!` to make you happy.

# Stores

A store is an implementation of the `FeatureStore` protocol in the `flic.store` namespace and this is what is used to persist feature configuration longer term.  Implementor of the protocol simply need the ability to `get` a feature and `set!` a feature.  The provided in-memory store is a good example of a simple implementation

## In Memory Store

```clojure
(defprotocol FeatureStore
  "defines the contract used when working with a feature storage thing"
  (get-feature  [store key]      "retrieves a feature")
  (set-feature! [store key data] "saves the current feature"))

(deftype InMemoryStore [features]
  FeatureStore
  (get-feature  [store key]      (get @features (keyword key) {}))
  (set-feature! [store key data] (swap! features #(assoc % (keyword key) data))))
```

There is also a helper provided to construct an in-memory store with or without initial state

```clojure
(in-memory-store { :my-feature { :groups #{ :all } } })
```

## License

Copyright © 2014 James Hughes

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
