(ns combine-and-clean.core (:gen-class))

(def output-file "output/combined.txt")

(defn append
  "appends content to the output file"
  [text]
  (spit output-file text :append true))

(defn files [path]
  "returns all files (real files, not directories and such) residing under or nested under the given path"
  (filter (fn[x](.isFile x)) (file-seq (clojure.java.io/file path))))

(use '[instaparse.core :only (parser)]) ; https://github.com/Engelberg/instaparse

(defn transform [text]
  "transforms the wikipedia text for vector embedding learning"

  (defn remove-wikiextractor-headers [text]
    "removes the xml-like encapsulation of each wikipedia entry, produced by wikiextractor. this also discards the title of the entry,
    which is of no concern for creating word vectors, thus discarding any notion of separation between the wikipedia entries"
    (def wikiextractor-parser
      "a parser for the output of wikiextractor (https://github.com/attardi/wikiextractor)"
      (parser
        "
          S = Entry*
          Entry = <Header> ContentAsText <Trailer> <OptionalPadding>
          Header = '<doc' (' ' HeaderProp)* '>'
          HeaderProp = #'[^=]*' '=' '\"' #'[^\"]*' '\"' (* e.g. id=\"4030\" *)
          ContentAsText = #'(?sm).*'
          Trailer = '</doc>'
          OptionalPadding = #'\\s*'
      ")
    )
    (wikiextractor-parser text))

  (remove-wikiextractor-headers text))

(defn -main [input-path]
  "iterates the output directory of the wikiextractor tool, combining its output files into a single file while stripping away the xml wrappers" []
  (println "input:" input-path)
  (let [files (files input-path)]
    (println "About to combine wikiextractor output files from" input-path)
    (println "Found" (count files) "files to combine")
    ;(doseq [file files] (println file (parse (java.io.FileReader. file))))
    (doseq [file files] (append (transform (slurp file))))
    (println "done")))

;; TODOS:
;;
;; warn if output file already exists
;; output usage when no arg is supplied
;; split word2vec stage to separate main as per http://stackoverflow.com/a/18988264/1509695

;; Workflow engineering TODOs:
;;
;; connect under a (Drake / Overseer / Azkaban / Airflow / Luigi) workflow along with:
;; a git clone step for https://github.com/attardi/wikiextractor and for this repo
;; a run of https://github.com/attardi/wikiextractor (python WikiExtractor.py --escapedoc -o output /data/hebrew-wikipedia/hewiki-20161020-pages-articles.xml)
;; a run of this repo (lein run "../wikiextractor/output")
;; a curl and unzip step for https://dumps.wikimedia.org/hewiki/20161020/hewiki-20161020-pages-articles.xml.bz2
;; a preconditions step that checks that git, python and the unzip utility are available
;; a step for making lein available on the machine
;; a step that pulls and builds https://github.com/facebookresearch/fastText
;; the word2vec / fasttext step
