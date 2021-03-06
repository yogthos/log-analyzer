(ns stats-viewer.util.log-reader
  (:use clojure.java.io #_clj-geoip.core)
  (:import         
    [java.io File FileInputStream BufferedReader InputStreamReader]    
    java.util.Date
    java.util.Calendar
    java.text.SimpleDateFormat
    java.util.zip.GZIPInputStream
    java.io.RandomAccessFile))

(def log-path "/var/log/glassfish-access-logs/")
;(def log-path "logs/")

(defn parse-line [line]
  (merge 
    {:ip (re-find #"\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b" line) 
     :access-time (.parse (new SimpleDateFormat "dd/MMM/yyyy:HH:mm:ss zzzzz") (second (re-find #"\[(.*?)\]" line)))}
    (into {} (map vector [:route :path :browser] (re-seq #"\".*?\"" line)))))

(defn raf-seq
  [#^RandomAccessFile raf]
  (if-let [line (.readLine raf)]
    (lazy-seq (cons line (raf-seq raf)))
    (do (Thread/sleep 1000)
      (recur raf))))

(defn tail-seq [input]
  (let [raf (RandomAccessFile. input "r")]
    (.seek raf (.length raf))
    (raf-seq raf)))

;;todo use an agent to tail the file
#_(doseq [line (line-seq rdr)]
      (let [date (round-ms-down-to-nearest-sec (:access-time (parse-line line)))] 
        (swap! logs update-in [date] (fn [x] (if x (inc x) 1)))))

(defn round-ms-down-to-nearest-sec [date]
  (when date
    ( * 1000 (quot (.getTime date) 1000))))

(defn to-date [s]
  (.parse (new SimpleDateFormat "dd MMM yyyy") s))

(defn format-date
  ([date] (format-date date "yyyy-MM-dd"))
  ([date fmt]
    (.format (new java.text.SimpleDateFormat fmt) date)))

(defn group-by-time [logs]
  (->> logs
    (reduce #(update-in %1 [(round-ms-down-to-nearest-sec (:access-time %2))] 
                      (fn [x] (if x (inc x) 1))) 
            (sorted-map))
    vec))


(defn group-by-browser [logs]
  (->> logs
    (group-by (fn [{:keys [browser]}]                 
                (cond
                  (nil? browser) "other"
                  (re-find #"Firefox" browser) "Firefox"
                  (re-find #"WebKit" browser) "WebKit"
                  (re-find #"MSIE" browser) "IE"    
                  :else "other")))
    (map (fn [[k v]] {:label k :data (count v)}))))

(defn group-by-os [logs]  
  (->> logs
    (group-by (fn [{:keys [browser]}]                 
                (cond
                  (nil? browser) "other"
                  (re-find #"iPhone" browser) "iPhone"
                  (re-find #"iPad" browser) "iPad"
                  (re-find #"Android" browser) "Android"
                  (re-find #"OS X" browser) "OS X"
                  (re-find #"Linux" browser) "Linux"                                                                         
                  (re-find #"Windows" browser) "Windows"
                  :else "other")))
    (map (fn [[k v]] {:label k :data (count v)}))))

(defn group-by-route [logs]
  (map 
    (fn [[k v]] {:label k :data (count v)})
    (dissoc        
      (group-by (fn [{:keys [route]}] 
                  (let [route (second (.split route " "))]
                    (if (= "/" route) route
                      (if-let [route (re-find #"/blog/\d+" route)]
                        route "other")))) 
                logs)
      "other")))

;(def ip-country-memo (memoize ip-country))

#_(defn group-by-country [logs]
(->> logs
  (map #(ip-country-memo (:ip %)))
  (group-by :country)
  (map (fn [[k v]] {:label k :data (count v)}))))

(defn list-files [] 
  (->> log-path
    (new File)
    (.listFiles)
    (filter #(.startsWith (.getName %) "server_access_log") )
    (sort-by (memfn lastModified))
    (map #(str log-path (.getName %)))))

(defn parse-files [files]
  (mapcat
    #(with-open [rdr (reader %)] 
       (doall (->> rdr line-seq (map parse-line))))
    files))

(defn get-logs [n]
  (let [logs        (parse-files (take-last n (list-files)))
        unique-logs (->> logs (group-by :ip) (map #(first (second %))))
        by-time (group-by-time unique-logs)
        all-by-time (group-by-time logs)] 
    {:uniquehits  (reduce + (map second by-time))
     :allhits  (reduce + (map second all-by-time))
     :alltime     all-by-time    
     :time        by-time
     :os          (group-by-os unique-logs)
     :browser     (group-by-browser unique-logs)
     :route       (group-by-route unique-logs)
     ;:country (group-by-country logs)
     }))


