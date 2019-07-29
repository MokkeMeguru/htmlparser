(ns htmlparser.core
  (:require [hiccup.core :as h]
            [hiccup-bridge.core :as hicv]
            [clojure.data.csv :as csv]
            [taoensso.nippy :as nippy]))


(defn take-csv [fname]
  (with-open [file (clojure.java.io/reader (clojure.java.io/resource fname))]
    (csv/read-csv (slurp file))))


(defn replace-funcs [raw-html]
  (-> raw-html
      (clojure.string/replace #"\\\n" "")
      (clojure.string/replace #"(?<!src)=\"(.*?)\""  "=\"\"")
      (clojure.string/replace #"[a-zA-Z]+=\"\"" "")
      (clojure.string/replace #"\s|　" "")))

(defn parse-csv [raw-csv]
  (map (fn [[idx raw-html timestamp]]
         [(Integer. (re-find #"[0-9]*" idx)) (first (hicv/html->hiccup (replace-funcs raw-html)))  timestamp]) raw-csv))

;; (spit (clojure.java.io/resource "./example.edn") (pr-str (first (parse-csv (take-csv "./example.csv")))))
;; (parse-csv (take-csv "./rev201401.csv"))

(defn save-serialized-data [read-file write-file]
  (nippy/freeze-to-file
   (clojure.java.io/resource write-file)
   (-> read-file
        take-csv
        parse-csv)))

(save-serialized-data "./rev201401.csv" "./rev201401.npy")

