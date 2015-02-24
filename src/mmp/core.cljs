(ns mmp.core
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [clojure.core.strint :refer [<<]])
  (:require
    [clojure.string :as string]
    [cljs.core.async :refer [<!]]
    [markdown.core :refer [md->html]]
    [rum]
    [mmp.fixtures :as fixtures]
    [mmp.tools :refer [get-json! normalize-postcode]]))

(enable-console-print!)

(def state
  (atom {:content  fixtures/blurb
         :mp       nil
         :postcode "SW1A 0AA"}))

(defn is-mp? [candidate]
  (let [identifiers (:identifiers (:person_id candidate))]
    (some #(= "uk.org.publicwhip" (:scheme %)) identifiers)))

(defn find-mp [data]
  (let [candidates (:memberships (:result data))]
    (some #(and (is-mp? %) %) candidates)))

(defn get-mp! [postcode]
  (go
    (let [postcode (normalize-postcode postcode)
          mapit-url (str "https://mapit.mysociety.org/postcode/" postcode)
          mapit-response (<! (get-json! mapit-url))
          constituency-id (:WMC (:shortcuts mapit-response))
          yournextmp-url (<< "https://yournextmp.popit.mysociety.org/api/v0.1/"
                             "posts/~{constituency-id}?embed=membership.person")
          yournextmp-response (<! (get-json! yournextmp-url))
          constituency-name (:name (:area (:result yournextmp-response)))
          mp (find-mp yournextmp-response)]
      (if mp
        (let [{:keys [email gender name party_memberships]} (:person_id mp)]
          (swap! state assoc
                 :mp {:name name
                      :email email
                      :gender (string/lower-case gender)
                      :party (:name (:2010 party_memberships))
                      :constituency constituency-name}))))))

(rum/defc header < rum/static [id]
  [:header.jumbotron
   [:h1.animated.tada "Mail Your MP"]])

(rum/defc find-component < rum/static [data]
  (let [{:keys [content postcode]} data]
    [:div.animated.bounceInDown
     [:div {:dangerouslySetInnerHTML {:__html (md->html content)}}]
     [:p "Enter your postcode"]
     [:input {:class "text-center text-uppercase"
              :type "text"
              :default-value postcode
              :on-input #(swap! state assoc :postcode (.. % -target -value))}]
     [:br]
     [:button.btn.btn-success {:on-click #(get-mp! postcode)} "Find"]]))

(rum/defc contact-component < rum/static [mp]
  (let [{:keys [constituency email gender name party]} mp
        pronoun (if (= "female" gender) "her" "his")]
    [:div#mp.animated.bounceInDown
     [:p (<< "The MP for your constituency, ~{constituency}, "
             "is ~{name} (~{party}). ")]
     (if-not (string/blank? email)
       [:div
        [:p (<< "~(string/capitalize pronoun) email address is ")
         [:a {:href (<< "mailto:~{email}")} email] "."]]
       [:p (<< "Unfortunately, however, we were unable to find "
               "~{pronoun} email address.")])
     [:br]
     [:button.btn.btn-success
      {:on-click #(swap! state assoc :mp nil)} "Start Again"]]))

(rum/defc main-component < rum/reactive
  "One component to rule them all"
  []
  (let [{:keys [mp] :as data} (rum/react state)]
    (conj [:div#rum-components.text-center]
          (header)
          [:div.container.animated.bounceInDown
           [:div.row
            [:div.col-sm-10.col-sm-offset-1
             (if-not mp
               [:div#find
                (find-component data)]
               [:div#contact
                (contact-component mp)])]]])))

(defn ^:export mount [element]
  (rum/mount (main-component) element))
