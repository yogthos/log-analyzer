(ns stats-viewer.server
  (:require [noir.server :as server]
            [stats-viewer.views common welcome])
  (:gen-class))

(server/load-views-ns 'stats-viewer.views)

(defn fix-base-url [handler]
  (fn [request]    
    (with-redefs [noir.options/resolve-url 
                  (fn [url]                    
                    ;prepend context to the relative URLs
                    (if (.contains url "://") 
                      url (str (:context request) url)))]
      (handler request))))

(def handler
  (-> (server/gen-handler 
        {:mode :prod, 
         :ns 'yuggoth 
         :session-cookie-attrs {:max-age 1800000}})
    fix-base-url))

(defn -main [& m]  
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'stats-viewer})))

