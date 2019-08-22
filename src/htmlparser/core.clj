(ns htmlparser.core
  (:require [hiccup.core :as h]
            [hiccup-bridge.core :as hicv]
            [clojure.data.csv :as csv]
            [taoensso.nippy :as nippy]
            [clojure.tools.cli :refer [parse-opts]]
            [htmlparser.extract-abstruct :refer [gen-parsed-data-json gen-parsed-data]])
  (:gen-class))


(defn take-csv [fname]
  (with-open [file (clojure.java.io/reader fname)]
    (csv/read-csv (slurp file))))


(defn replace-funcs [raw-html]
  (-> raw-html
      (clojure.string/replace #"\\\n" "")
      (clojure.string/replace #"(?<!src)=\"(.*?)\""  "=\"\"")
      (clojure.string/replace #"[a-zA-Z]+=\"\"" "")
      (clojure.string/replace #"\s|　" "")))

(defn parse-csv [raw-csv]
  (pmap (fn [[idx raw-html timestamp]]
          [(Integer. (re-find #"[0-9]*" idx)) (first (hicv/html->hiccup (replace-funcs raw-html)))  timestamp])
        raw-csv))

;; (spit (clojure.java.io/resource "./example.edn") (pr-str (first (parse-csv (take-csv "./example.csv")))))
;; (parse-csv (take-csv "./rev201401.csv"))

(defn save-serialized-data [read-file write-file]
  (nippy/freeze-to-file
   write-file
   (-> read-file
        take-csv
        parse-csv)))

(defn gen-file-lists [start end]
  (

   for [x (range start (inc end))]
    (if-not (= x 14)
     (for [y  (range 1 13)]
       {:root (format "rev20%02d" x) :child (format "rev20%02d%02d" x y)}
       ))))

(defn preprocess-raw-data [zips-folder]
  (doall
   (map
    #(do
       (println "[Info] progress start" %)
       (save-serialized-data
        (str zips-folder (:root %) "/" (:child %) ".csv")
        (str  "./resources/"(:child %) "-raw.npy"))
       (println "[Info ]progress end"))
    (flatten (gen-file-lists 9 13))))
  (doall (map
          #(do
             (println "[Info] progress start" %)
             (save-serialized-data
              (str zips-folder (:root %) "/" (:child %) ".csv")
              (str  "./resources/"(:child %) "-raw.npy"))
             (println "[Info ]progress end"))
          {:root "rev2014" :child "rev201401"}
          {:root "rev2014" :child "rev201402"})))

;; for cli

(def cli-options
  [["-s" "--source SOURCE" "Source  Directory"
    :default "/home/meguru/Documents/nico-dict/zips/"
    :parse-fn str]
   ["-n" "--npy-path NPY-PATH" "NPY PATH"
    :default "./resources/rev201402-raw.npy"
    :parse-fn str]
   ["-i" "--id ID" "Article ID"
    :default "6100"
    :parse-fn str]
   ["-o" "--output-path OUTPUT-PATH" "Output PATH"
    :default "example-parsed.json"
    :parse-fn str]
   ["-d" "--db-path DB-PATH" "Database  PATH"
    :default "/home/meguru/Documents/nico-dict/zips/head/headers.db"
    :parse-fn str]
   ["-h" "--help"]])

(defn help []
  (println "this is help, please see README.md"))

(defn -main
  ([& args]
   (let [argdic (parse-opts args cli-options)
         zips-folder (-> argdic :options :source)
         arguments (-> argdic :arguments first)
         id (-> argdic :options :id)
         npy-path (-> argdic :options :npy-path)
         output-path (-> argdic :options :output-path)
         db-path (-> argdic :options :db-path)]
     (case arguments
       "preprocess-raw-data" (preprocess-raw-data zips-folder)
       "gen-parsed-data" (gen-parsed-data-json npy-path id output-path db-path)
       "gen-parsed-data-json" (gen-parsed-data-json npy-path id output-path db-path)
       "show-help" (help)
       (println "example. lein run show-help")))))
