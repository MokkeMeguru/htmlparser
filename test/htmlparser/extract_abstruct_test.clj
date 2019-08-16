(ns htmlparser.extract-abstruct-test
  (:require [htmlparser.extract-abstruct :as sut]
            [clojure.test :as t]))

(t/deftest test-extract-body
  (let [html [:html
              [:body [:p "hoge"] [:h2 "bar" [:span "boo"]] [:p "foo"]  [:h2 "hy"] [:p "yah"]]]]
    (t/is
     (= (sut/extract-body html)
        (list [:p "hoge"] [:h2 "bar" [:span "boo"]] [:p "foo"]  [:h2 "hy"] [:p "yah"])))))

(t/deftest test-extract-body2
  (let [html [:html
              [:body [:div [:div [:p "hoge"] [:h2 "bar" [:span "boo"]] [:p "foo"]  [:h2 "hy"] [:p "yah"]]]]]]
    (t/is
     (= (sut/extract-body html)
        (list [:p "hoge"] [:h2 "bar" [:span "boo"]] [:p "foo"]  [:h2 "hy"] [:p "yah"])))))

(t/deftest test-extract-p
  (let [element [:body
                 [:h2 "hoge" [:span "hoo!"]][:p [:span "hoge"] "bar" [:br] [:strong [:a "foo!"]] [:b "b"] [:p [:a]]]]]
    (t/is
     (= (sut/extract-p element)
        [:body [:h2 "hogehoo!"] [:p "hogebar。foo!b"]]))))

(t/deftest test-format-content-element
  (let [ul-list [:ul [:li "hoge"] [:li "bow" [:ul [:li "bar"] [:li "foo"] ]] [:li "booo!"]]]
    (t/is
     (= (sut/format-content-element ul-list)
        [:list '("hoge") '("bow" [:list ("bar") ("foo")]) '("booo!")]))))

(t/deftest test-gen-content
  (let [body [[:p "hoge"] [:h3 "bar"] [:p "barbar"] [:h3 "ulfoo"] [:ul [:li "li1"] [:li "li2"]]]]
    (t/is
     (= (sut/gen-content body)
        {:content [[:p "hoge"]], :children [{:name "ulfoo", :meta :h3, :content [[:list '("li1") '("li2")]]} {:name "bar", :meta :h3, :content [[:p "barbar"]]}]}
        ))))


(t/run-tests)

