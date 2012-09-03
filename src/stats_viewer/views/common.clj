(ns stats-viewer.views.common
  (:use [noir.core :only [defpartial]] hiccup.page))

(defmacro local-redirect [url]
  `(noir.response/redirect 
     (if-let [context# (:context (noir.request/ring-request))]
       (str context# ~url) ~url)))

(defpartial layout [& content]
            (html5
              [:head
               [:title "stats-viewer"]
               
               (include-css "/css/reset.css")
               [:script {:type "text/javascript", :src "//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"}]
               (include-js "/js/jquery.flot.min.js"
                           "/js/jquery.flot.pie.min.js"
                           "/js/jquery.flot.selection.min.js"                            
                           "/js/site.js")]
              [:body content]))
