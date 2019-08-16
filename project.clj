(defproject htmlparser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 [hiccup "2.0.0-alpha2"]
                 [org.clojure/data.csv "0.1.4"]
                 [hiccup-bridge "1.0.1"]
                 [com.taoensso/nippy "2.14.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.xerial/sqlite-jdbc "3.28.0"]
                 [org.clojure/data.json "0.2.6"]]
  :repl-options {:init-ns htmlparser.core}
  :main htmlparser.core)
