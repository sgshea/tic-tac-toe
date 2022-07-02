(ns tic-tac-toe.core
  (:gen-class)
  (:require [cljfx.api :as fx]
            [tic-tac-toe.logic :as logic]
            [clojure.string :as str])
  (:import [javafx.application Platform]))

;; Board is stored in an atom
;; Inital state is with default board of 3x3
(def *state
  (atom {:board [1 2 3 4 5 6 7 8 9]
         :dimensions [3 3]
         :temp-dimensions [3 3]
         :player-turns logic/player-turns}))

(defn set-dimensions
  "Updates dimensions and board with new dimensions."
  [new-dimensions]
  (swap! *state assoc :dimensions new-dimensions)
  (swap! *state assoc :board (logic/board-creator new-dimensions)))

(defn board-placement
  "Places next player onto board with given location."
  [board cell-data]
  (if (keyword? cell-data)
    nil
    (do
      (swap! *state assoc :board (assoc board (dec cell-data) (first (@*state :player-turns))))
      (swap! *state assoc :player-turns (rest (@*state :player-turns))))))

(defn grid-cell
  "Gets and returns the display for a single cell.
  To be displayed in the gui.
  Location is position of cell wanted."
  [board location]
  (let [cell-data ; what is located in the cell
        (nth (nth board (dec (first location))) (dec (last location)))]
    {:fx/type          :button
     :grid-pane/hgrow  :always
     :grid-pane/vgrow  :always
     :grid-pane/row    (first location)
     :grid-pane/column (last location)
     :on-action        (fn [_] (board-placement (vec (flatten board)) cell-data))
     :text (if (keyword? cell-data)
             (str (name cell-data))
             (str cell-data))}))

(defn display-board-as-grid
  "Displays the board as a grid."
  [{:keys [board dimensions]}]
  (let [board
        (partition-all (first dimensions) board)]
    (loop
     [grid '()
      row 1
      column 1]
      (if (<= row (last dimensions))
        (recur (conj grid (grid-cell board [row column]))
               (if (< column (first dimensions))
                 row
                 (inc row))
               (if (< column (first dimensions))
                 (inc column)
                 1))
        grid))))

;; Functions for left pane, where information is displayed
;; 1. Who's turn it is at the top
;; 2. Way to change board size
;; 3. Button to start new game

(def min-dimension
  3)
(def max-dimension
  20)

(defn dimension-input
  "Place to input new width and height."
  [{:keys [temp-dimensions]}]
  {:fx/type :v-box
   :alignment :center
   :spacing 10
   :children [{:fx/type :h-box
               :alignment :center
               :spacing 5
               :children [{:fx/type :label
                           :text "Width"}
                          {:fx/type :spinner
                           :editable true
                           :value-factory {:fx/type :integer-spinner-value-factory
                                           :min min-dimension
                                           :max max-dimension
                                           :value (first temp-dimensions)}
                           :on-value-changed #(swap! *state assoc :temp-dimensions [% (last temp-dimensions)])}]}
              {:fx/type :h-box
               :alignment :center
               :spacing 5
               :children [{:fx/type :label
                           :text "Height"}
                          {:fx/type :spinner
                           :editable true
                           :value-factory {:fx/type :integer-spinner-value-factory
                                           :min min-dimension
                                           :max max-dimension
                                           :value (last temp-dimensions)}
                           :on-value-changed #(swap! *state assoc :temp-dimensions [(first temp-dimensions) %])}]}]})

(defn new-game-button
  "Button to start new game."
  [{:keys [temp-dimensions]}]
  {:fx/type :button
   :text "New Game"
   :on-action (fn [_] (set-dimensions temp-dimensions))})

(defn display-information
  "A place to display information such as who's turn it is."
  [{:keys [temp-dimensions information]}]
  {:fx/type   :v-box
   :alignment :center
   :spacing   20
   :children  [{:fx/type :label
                :text    (str information)}
               {:fx/type         dimension-input
                :temp-dimensions temp-dimensions}
               {:fx/type         new-game-button
                :temp-dimensions temp-dimensions}]})

;; Grid for tic-tac-toe board
(defn grid-pane
  [{:keys [board dimensions]}]
  {:fx/type :grid-pane
   :children
   (display-board-as-grid {:board board
                           :dimensions dimensions})})

;; Splits scene into grid-pane and a place for other text
(defn split-pane
  [{:keys [board dimensions temp-dimensions]}]
  {:fx/type :split-pane
   :divider-positions [0.5]
   :items [{:fx/type :v-box
            :split-pane/resizable-with-parent false
            :children [{:fx/type :label
                        :padding 50
                        :text "Tic-Tac-Toe"}
                       {:fx/type display-information
                        :padding 50
                        :information (str "Current Dimensions: " (str dimensions))
                        :temp-dimensions temp-dimensions}]}
           {:fx/type grid-pane
            :board board
            :dimensions dimensions}]})

(defn root
  [{:keys [board dimensions temp-dimensions]}]
  {:fx/type :stage
   :showing true
   :title "Tic-Tac-toe"
   :scene {:fx/type :scene
           :root {:fx/type split-pane
                  :pref-width 960
                  :pref-height 540
                  :board board
                  :dimensions dimensions
                  :temp-dimensions temp-dimensions}}})

(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)))

(defn -main
  [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))

;; TODO
;; Probably need to reimplement most of the cli functionality to use javafx text boxes for input etc.
;; store the board in the atom and update on clicks? then display next turn
