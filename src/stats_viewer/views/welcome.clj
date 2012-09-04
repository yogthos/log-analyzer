(ns stats-viewer.views.welcome
  (:require [stats-viewer.views.common :as common]            
            [stats-viewer.util.log-reader :as log-reader]
            [noir.response :as response]
            [noir.request :as request])
  (:use noir.core hiccup.form)
  (:import java.util.Date))

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
    [:div.message "Unique visitors to yogthos.net from "
     (let [fmt           "MMM dd"
           today         (new Date)
           five-days-ago (doto (new Date) (.setTime (- (.getTime today) 345600000)))] 
       (str (log-reader/format-date five-days-ago fmt) " to " 
            (log-reader/format-date today fmt)))]
    [:div#total "loading..."]
    [:div#hits-by-time]           
    [:div
     [:div#hits-by-os]
     [:div#hits-by-route]]
    [:br]
    [:div#hits-by-browser]))

(defpage [:post "/get-logs"] []  
  (cache :hits (response/json (log-reader/get-logs 5))))

