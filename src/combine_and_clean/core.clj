(ns combine-and-clean.core)

(defn append
  "appends the file's content while stripping away the xml wrapper, for the given file"
  [file]
  (spit "output/combined.txt" (slurp file) :append true))

(defn files [path]
  (filter (fn[x](.isFile x)) (file-seq (clojure.java.io/file path))))

(defn go [input-path]
  "iterates the output directory of the wikiextractor tool, combining its output files into a single file while stripping away the xml wrappers" []
  (let [files (files input-path)]
    (println "About to combine wikiextractor output files from" input-path)
    (println "Found" (count files) "files to combine")
    (doseq [file files] (append file))
    (println "done")))

;; TODO: warn if output file already exists
;; TODO: output usage when no arg is supplied
