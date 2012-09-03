(ns stats-viewer.views.welcome
  (:require [stats-viewer.views.common :as common]            
            [stats-viewer.util.log-reader :as log-reader]
            [noir.response :as response]
            [noir.request :as request])
  (:use noir.core hiccup.form))

(def cached (atom {}))

(defmacro cache [id content]
  `(let [last-updated# (:time (get @cached ~id))         
         cur-time# (.getTime (new java.util.Date))]
     (if (or (nil? last-updated#)
             (> (- cur-time# last-updated#) 15000))
       (swap! cached assoc ~id {:time cur-time# :content ~content}))
     (:content (get @cached ~id))))

(defpage "/" req  
  (common/layout  
    (hidden-field "context" (:context (request/ring-request)))
    [:div.message "Visitors to yogthos.net for " (log-reader/format-date (new java.util.Date))]
    [:div#total "loading..."]
    [:div#hits-by-time]           
    [:div
     [:div#hits-by-os]
     [:div#hits-by-route]]))

(defpage [:post "/get-logs"] []  
  (cache :hits (response/json (log-reader/get-logs))))

