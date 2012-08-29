(defproject stats-viewer "0.1.0-SNAPSHOT"
            :description "apache log stats viewer"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.3.0-beta3"]                                                      
                           [org.clojure/data.json "0.1.2"]                                           
                           [midje "1.4.0"]]
            :dev-dependencies [[lein-ring "0.7.3"]]
            :ring {:handler stats-viewer.server/handler}                        
            :main stats-viewer.server)

