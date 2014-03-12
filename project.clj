(defproject flic "0.0.1"
  :description "A simple feature toggle library"
  :url "http://yobriefca.se/flic"
  :license { :name "Eclipse Public License"
             :url "http://www.eclipse.org/legal/epl-v10.html" }
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [biscuit "1.0.0"]]
  :plugins [[lein-midje "3.0.0"]]
  :profiles  { :dev  { :dependencies  [[midje "1.5.1"]] } })
