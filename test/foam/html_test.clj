(ns foam.html-test
  (:require [clojure.test :refer :all]
            [foam.core :as foam]
            [foam.dom :as dom]
            [foam.html :as html]))

(deftest parsing-tags
  (are [expr expected] (= expected (html/normalize-element expr))
       [:div] ["div" {} nil]

       [:div#foo] ["div" {:id "foo"} nil]

       [:.bar] ["div" {:class "bar"} nil]

       [:#foo] ["div" {:id "foo"} nil]

       [:div.bar] ["div" {:class "bar"} nil]

       [:div#foo.bar] ["div" {:id "foo"
                              :class "bar"} nil]

       [:div.bar {:display "none"} "hello"] ["div" {:class "bar"
                                                    :display "none"} ["hello"]]

       [:div.bar {:class "foo"} "hello"] ["div" {:class "bar foo"} ["hello"]]

       [:div#foo.bar.baz] ["div" {:id "foo"
                                  :class "bar baz"} nil]

       [:#foo.bar] ["div" {:id "foo"
                           :class "bar"} nil]

       [:.bar#foo] ["div" {:id "foo"
                           :class "bar"} nil]

       [:#foo.bar.baz] ["div" {:id "foo"
                               :class "bar baz"} nil]))

(deftest html-return-types
  (let [ret (html/html
             [:h1 "Hello World"])]
    (is (satisfies? foam/ReactDOMRender ret))
    (is (-> ret :tag (= "h1")))))

(deftest content-are-text-nodes
  (let [ret (html/html
             [:h1 "Hello World"])]
    (is (satisfies? foam/ReactDOMRender ret))
    (is (->> ret :children first (#(satisfies? foam/ReactDOMRender %))))))

(defn simple-component [app owner opts]
  (reify
    foam/IRender
    (render [this]
      (dom/h1 nil "Hello World"))))

(defn nested-component [app owner opts]
  (reify
    foam/IRender
    (render [this]
      (html/html
       [:div
        (foam/build simple-component app {})]))))

(deftest handles-om-components
  (let [state (atom {})
        cursor (foam/root-cursor state)
        com (foam/build nested-component cursor {})]
    (is (string? (dom/render-to-string com)))))

(defn inner-html-and-classname-component [app owner opts]
  (reify
    foam/IRender
      (render [this]
        (dom/p {:className "some-class" 
                :dangerouslySetInnerHTML {
                :__html "<span>Some inline text</span> and more text"}}))))

(deftest inner-html-and-classname
  (let [state (atom {})
        cursor (foam/root-cursor state)
        com (foam/build inner-html-and-classname-component cursor {})
        com-string (dom/render-to-string com)]
    (is (= com-string "<p class=\"some-class\" data-reactid=\".0\" data-react-checksum=\"577707518\"><span>Some inline text</span> and more text</p>"))))