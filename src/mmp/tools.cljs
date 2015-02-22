(ns mmp.tools
  (:require 
    [goog.net.XhrIo :as xhr]
    [clojure.string :as string]
    [clojure.walk :refer [keywordize-keys]]
    [cljs.core.async :refer [chan put!]]
    [cognitect.transit :as transit]))

(defn- get-response-text [response]
  (.getResponseText (.-target response)))

(defn- parse-json [json]
  (keywordize-keys (transit/read (transit/reader :json) json)))

(defn get-json!
  "Fetch and parse JSON data from the given URL"
  [url]
  (let [channel (chan)]
    (xhr/send url #(put! channel (parse-json (get-response-text %))))
    channel))

(defn normalize-postcode [postcode]
  (-> (string/upper-case postcode)
      (string/trim)
      (string/split #" ")
      (string/join)))
