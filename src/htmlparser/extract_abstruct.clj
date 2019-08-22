(ns htmlparser.extract-abstruct
  (:require [hiccup.core :as h]
            [hiccup-bridge.core :as hicv]
            [clojure.data.csv :as csv]
            [taoensso.nippy :as nippy]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.jdbc :as sql]
            [clojure.test :refer [is deftest run-tests]]
            [clojure.data.json :as json]))

(defn open-nippy [fname]
  (nippy/thaw-from-file fname))

(defn extract-body [html]
  "body 要素を抽出するための関数"
  (loop [x html]
    (let [tag (first x)
          content (rest x)]
      (if  (or (= tag :body) (= tag :div))
        (if (and (= 1 (count content)) (= :div (-> content first first)))
          (recur (first content))
          content)
        (recur (first content))))))

(defn extract-p
  "各要素から装飾タグなどを取り除く関数"
  [element]
  (if (string? element) element
      (let [tag (first element)
            remove-tag (fn [content]
                         (let [removed-content (map extract-p content)]
                           (if (zero? (count (remove string? removed-content)))
                             (apply str removed-content)
                             (first removed-content)))) ;; TODO bug fix
            concat-strs (fn [elem]
                          (let [content (flatten (map extract-p (rest elem)))]
                            (-> (apply str (remove nil? content))
                                (clojure.string/replace #"。。" "。")
                                (clojure.string/replace #" " ""))))]
        (if (string? tag) element
            (condp contains? tag
              #{:h2 :h3 :dt :dd} [tag (concat-strs element)]
              #{:p} (let [content (concat-strs element)]
                      (if-not (= "" content) [tag content] nil))
              #{:br} "。"
              #{:div} (remove-tag (rest element))
              #{:em :a :span :strong :sub :b :nobr} (remove-tag (rest element))
          ;; #{:h4 :h5} [:html (h/html [tag (concat-strs element)])]
              (let [content (map extract-p (rest element))]
                (vec (cons tag content))))))))

(defn format-content-element
  "table や ul 要素を処理するための関数"
  [item]
  (let [parse-li (fn [li]
                   (map
                    (fn [phrase] (if (vector? phrase) (format-content-element phrase) phrase))
                    (rest li)))
        parse-ul (fn [ul]
                   (map
                    parse-li
                    (rest ul)))]
    (cond
      (= :table (first item)) [:html (h/html item)]
      (= :ul (first item)) (vec (cons :list (parse-ul item)))
      (= :ol (first item)) (vec (cons :list (parse-ul item)))
      :default item)))

(defn format-content
  "table や ul 要素を処理するための関数と全体の処理とのパイプ"
  [content]
  (mapv
   format-content-element
   content))

(def  ^:private tag-list
  {:h1 1
   :h2 2
   :h3 3
   :h4 4
   :h5 5
   ;; :h6 6
   ;; :h7 7
   ;; :h8 8
   })

(def ^:private invert-tag-list
  (clojure.set/map-invert tag-list))

(defn ^:private small-tag?
  [element tag]
  (if (contains? tag-list (first element))
    (< (tag tag-list) ((first element) tag-list))
    false))

(defn ^:private have-more-child?
  [rbody tag]
  (not-empty (filter #(small-tag? % tag) rbody)))

(defn gen-content
  "h2 以下の内容を処理するための関数
  例えば
  <h2>概要</h2>
   <p>hoge</p>
  なら <p>hoge</p> の部分 を処理する"
  [body tag]
  (let [body (vec (remove nil? body))
        content (format-content (vec (take-while #(not= tag (first %)) body)))
        reminder (drop-while #(not= tag (first %)) body)
        gen-child (fn [rbody tag]
                    (loop [tmp (vec rbody)
                           acc []]
                      (if-not (zero? (count tmp))
                        (recur (vec (drop-while #(not= tag (first %)) (drop 1 (vec tmp))))
                               (cons
                                (merge
                                 (gen-content (format-content (vec (take-while #(not= tag (first %)) (drop 1 (vec tmp)))))
                                              (get invert-tag-list (inc (tag tag-list))))
                                 {:name (-> (take 1 tmp) first second)
                                  :meta (-> (take 1 tmp) first first)})
                                acc))
                        (vec acc))))]
    {:content content
     :children (gen-child reminder tag)}))

(defn split-by-content
  ""
  [body-list article-info]
  (loop [body (take-while #(not= (first %) :h2) body-list)
         reminder (drop-while #(not= (first %) :h2) body-list)
         tag [:meta-abstruct ""]
         acc []]
    (if (and (zero? (count body)) (zero? (count reminder)))
      acc
      (recur
       (take-while #(not= (first %) :h2) (rest reminder))
       (drop-while #(not= (first %) :h2) (rest reminder))
       (first reminder)
       (conj acc
             (condp = (first tag)
               :meta-abstruct
               (merge
                {:meta :meta-abstruct
                 :info article-info}
                (gen-content body :h3))
               :h2
               (merge
                {:name (second tag)
                 :meta (first tag)}
                (gen-content body :h3))))))))

(split-by-content example {})

(defn gen-db-spec [db-path]
  {:subprotocol "sqlite"
   :subname db-path})

(def article-categories {"a" "単語" "v" "動画" "i" "商品" "l" "生放送"})

(defn gen-parsed-data [npy-path id output-path db-path]
  (let [data (nth (open-nippy npy-path) id)
        db-spec (gen-db-spec db-path)
        info (->
              (merge
               {:updated-date (-> data (nth 2))}
               (first (sql/query db-spec ["select * from article_header where article_id = ?" (-> data first)])))
              (update :article_category #(get article-categories % "undefined")))]
    (-> data  extract-p extract-body
        (split-by-content info)
        (clojure.pprint/pprint (clojure.java.io/writer output-path)))))

(defn gen-parsed-data-json [npy-path id output-path db-path]
  (let [data (nth (open-nippy npy-path) id)
        db-spec (gen-db-spec db-path)
        info (->
              (merge
               {:updated-date (-> data (nth 2))}
               (first (sql/query db-spec ["select * from article_header where article_id = ?" (-> data first)])))
              (update :article_category #(get article-categories % "undefined")))]
    (with-open [w (clojure.java.io/writer output-path)]
      (-> data  extract-p extract-body
          (split-by-content info)
          (json/write w :escape-unicode false)))))

(gen-parsed-data-json "./resources/rev201402-raw.npy" 6100 "example-parsed.json" "/home/meguru/Documents/nico-dict/zips/head/headers.db")

;; (-> (nth (open-nippy "./resources/rev201402-raw.npy") 5000) println)

;; (gen-parsed-data "./resources/rev201402-raw.npy" 6100 "example-parsed.edn" "/home/meguru/Documents/nico-dict/zips/head/headers.db")

nil;; (nth (open-nippy "./resources/rev201402-raw.npy") 6100)

;; bug  5000

;; (def example-nest-content [[:h2 "title"] [:ul [:li "hoge"]] [:h4 "bar"] [:ul [:li "bar"]]])

;; (defn gen-nested-content [body tag]
;;   (let [
;;         body (vec (remove nil? body))
;;         biggest-tag (loop [reminder (drop 1 body)]
;;                       (if (= '() reminder)
;;                         nil
;;                         (if (contains? (set (drop-while #(not= % tag) [:h2 :h3 :h4 :h5 :h6])) (-> reminder first first))
;;                           (-> reminder first first)
;;                           (recur (rest reminder)))))]
;;     biggest-tag))

;; (gen-nested-content example-nest-content :h3)

;; (drop-while #(not= % :h3)  [:h2 :h3 :h4 :h5 :h6])
