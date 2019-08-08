(ns htmlparser.extract-abstruct
  (:require [hiccup.core :as h]
            [hiccup-bridge.core :as hicv]
            [clojure.data.csv :as csv]
            [taoensso.nippy :as nippy]
            [clojure.tools.cli :refer [parse-opts]]))


(defn open-nippy [fname]
  (nippy/thaw-from-file fname))

(def example-data (nth (open-nippy "./resources/rev201402-raw.npy") 3))

(defn extract-body [html]
  (loop [x html]
    (let [tag (first x)
          content (rest x)]
      (print tag)
      (if-not (= tag :body)
        (recur (first content))
        content))))

;; (->  example-data second second second second (clojure.string/includes? "とは"))

;; (-> example-data second second first (= :body))

 ; (extract-body (second example-data))

