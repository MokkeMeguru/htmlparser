(ns htmlparser.extract-abstruct
  (:require [hiccup.core :as h]
            [hiccup-bridge.core :as hicv]
            [clojure.data.csv :as csv]
            [taoensso.nippy :as nippy]
            [clojure.tools.cli :refer [parse-opts]]))


(defn open-nippy [fname]
  (nippy/thaw-from-file fname))

(def example-data (nth (open-nippy "./resources/rev201402-raw.npy") 250))

(defn extract-body [html]
  (loop [x html]
    (let [tag (first x)
          content (rest x)]
      ;; (print tag)
      (if-not (= tag :body)
        (recur (first content))
        content))))

;;
;; (-> example-data first) ;; id
;; (-> example-data (nth 2))  ;; updated
;; (->  example-data second extract-body first)
;; (->  example-data extract-body (nth 3))
;; (->  example-data extract-body (nth 4))

(defn extract-p [element]
  (if (string? element)
    element
      (let [tag (first element)]
        (if (string? tag)
          element
          (condp = tag
            :p (vec (remove nil? (cons tag (flatten (map extract-p (rest element))))))
            :li (vec (remove nil? (cons tag (flatten (map extract-p (rest element))))))
            :br nil
            :span (map extract-p (rest element))
            (if (string? (rest element))
              (vec (cons tag (rest element)))
              (vec (cons tag
                         (map extract-p (rest element))))))))))

;; (-> example-data extract-body (nth 3) extract-p)
;; (-> example-data extract-body (nth 11) extract-p)

;; (def example-info {:id 400000 :date 201400000})
;; (def example-body [[:p "barbar"] [:h2 "title1"] [:h3 "subtitile1"] [:p [:span "hogehgoe"] [:br]] [:h2 "title2"] [:p "foo"] [:h3 "title3"] [:div]])



(defn gen-content [body]
  {:content body
   :children []})


;; (def example-parsed
;;  (let [info example-info]
;;    (loop [body (take-while #(not= (first %) :h2) example-body)
;;           reminder (drop-while #(not= (first %) :h2) example-body)
;;           tag [:meta-abstruct ""]
;;           acc []]
;;      (print body)
;;       (if (= 0 (count body))
;;         acc
;;         (recur
;;          (take-while #(not= (first %) :h2) (rest reminder)) ;; (rest (drop-while #(not= (first %) :h2) reminder))
;;          (drop-while #(not= (first %) :h2) (rest reminder)) ;; (rest (drop-while #(not= (first %) :h2) reminder))
;;          (first reminder)
;;          (conj acc
;;                (condp = (first tag)
;;                  :meta-abstruct
;;                  (merge
;;                   {:name (:artifle-name info)
;;                    :meta :meta-abstruct
;;                    :info info}
;;                   (gen-content body))
;;                  :h2
;;                  (merge
;;                   {:name (second tag)
;;                    :meta (first tag)}
;;                   (gen-content body)))))))))
;; (clojure.pprint/pprint example-parsed)

;; TODO: get article's name from csv
(defn split-by-content
  "
  body-list:  [[:p \"barbar\"] [:h2 \"title1\"] [:h3 \"subtitile1\"] [:p [:span \"hogehgoe\"] [:br]] [:h2 \"title2\"] [:p \"foo\"] [:h3 \"title3\"] [:div]] \n
   article-info -> :article-name,  :id, :date
  "
  [body-list article-info ]
  (loop [body (take-while #(not= (first %) :h2) body-list)
         reminder (drop-while #(not= (first %) :h2) body-list)
         tag [:meta-abstruct ""]
         acc []]
    (if (zero? (count body))
      acc
      (recur
       (take-while #(not= (first %) :h2) (rest reminder))
       (drop-while #(not= (first %) :h2) (rest reminder))
       (first reminder)
       (conj acc
             (condp = (first tag)
               :meta-abstruct
               (merge
                {:name (:article-name article-info)
                 :meta :meta-abstruct
                 :info article-info}
                (gen-content body))
               :h2
               (merge
                {:name (second tag)
                 :meta (first tag)}
                (gen-content body))))))))

;; (split-by-content  example-body example-info)
(-> example-data extract-p extract-body  (split-by-content example-info) (clojure.pprint/pprint (clojure.java.io/writer "example-parsed.edn")))

;; (-> example-data extract-body)



;; (example-body)
;; (split-by-content example-body)



;; (-> example-data extract-body split-by-content)
;; (-> example-data second second first (= :body))

                                        ; (extract-body (second example-data))

;; pattern
;; 1. html -> body -> div -> h2 gaiyou ...
;; 2. html -> body -> h2 gaiyou ...


;; pattern
;; 1. html -> body -> "とは" ... -> h2 gaiyou -> ... -> kanren
;;                                             -> meta-abstruct
;; 1. html -> body -> "とは" ... -> h2 kannren


;; (-> example-data second)

;; {:name ""
;;  :meta ""
;;  :content ""
;;  :children []}
