(ns stats-viewer.util.log-reader
  (:use clojure.java.io)
  (:import         
    [java.io FileInputStream BufferedReader InputStreamReader]
    java.util.Date
    java.util.Calendar
    java.text.SimpleDateFormat
    java.util.zip.GZIPInputStream
    java.io.RandomAccessFile))

;(def log-path "yogthos.net")
;(def log-path "/home/yogthos/tomcat/logs/localhost_access_log.")
(def log-path "/usr/local/apache/domlogs/yogthos/yogthos.net")


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

(defn round-ms-down-to-nearest-sec [date]
  (when date
    ( * 1000 (quot (.getTime date) 1000))))

(defn round-ms-down-to-nearest-min [date]
  (when date (* 60000 (quot (.getTime date) 60000))))

(defn to-date [s]
  (.parse (new SimpleDateFormat "dd MMM yyyy") s))

(defn format-date
  ([date] (format-date date "yyyy-MM-dd"))
  ([date fmt]
    (.format (new java.text.SimpleDateFormat fmt) date)))

(defn accesses-by-browser [browsers os]
  (count (filter (partial re-find (re-pattern os)) browsers)))

;;todo use an agent to tail the file
#_(doseq [line (line-seq rdr)]
      (let [date (round-ms-down-to-nearest-sec (:access-time (parse-line line)))] 
        (swap! logs update-in [date] (fn [x] (if x (inc x) 1)))))


(defn group-by-time [logs]
  (->> logs
    (reduce #(update-in %1 [(round-ms-down-to-nearest-sec (:access-time %2))] 
                      (fn [x] (if x (inc x) 1))) 
            (sorted-map))
    vec))


(defn group-by-os [logs]  
  (->> logs
    (group-by (fn [{:keys [browser]}]                 
                (cond
                  (nil? browser) "other"
                  (re-find #"iPhone" browser) "iPhone"
                  (re-find #"iPad" browser) "iPad"
                  (re-find #"OS X" browser) "OS X"
                  (re-find #"Linux" browser) "Linux"
                  (re-find #"Android" browser) "Android"                                                       
                  (re-find #"Windows" browser) "Windows"
                  :else "other")))
    (map (fn [[k v]] {:label k :data (count v)}))))

(defn group-by-route [logs]
  (->> logs
    (group-by (fn [{:keys [route]}] (second (.split route " "))))
    (map (fn [[k v]] {:label k :data (count v)}))))


(defn get-logs []
  (with-open [rdr (reader log-path)]    
    (let [logs (->> rdr
                 line-seq
                 (map parse-line)
                 (group-by :ip)
                 (mapcat second))]
      {:time  (group-by-time logs)
       :os    (group-by-os logs)
       :route (group-by-route logs)})))
