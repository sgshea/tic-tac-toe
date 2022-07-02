(ns tic-tac-toe.core
  (:gen-class)
  (:require [cljfx.api :as fx]
            [tic-tac-toe.logic :as logic]
            [clojure.string :as str])
  (:import [javafx.application Platform]))

; Global state
; board defaults to 3x3
; first player is :o
(def *state
  (atom {:board [1 2 3 4 5 6 7 8 9]
         :dimensions [3 3]
         :temp-dimensions [3 3]
         :player-turns logic/player-turns
         :winner [false nil]}))

(def min-dimension
  3)
(def max-dimension
  20)

(defn set-dimensions
  "Updates dimensions and board with new dimensions."
  [new-dimensions]
  (swap! *state assoc :dimensions new-dimensions)
  (swap! *state assoc :board (logic/board-creator new-dimensions)))

(defn set-winner
  "Updates winner."
  [bool player]
  (swap! *state assoc :winner [bool player]))

(defn board-placement
  "Places next player onto board with given location."
  [board cell-data]
  (if (or (first (@*state :winner)) (keyword? cell-data))
    nil
    (do
      (swap! *state assoc :board (assoc board (dec cell-data) (first (@*state :player-turns))))
      (swap! *state assoc :player-turns (rest (@*state :player-turns))))))

(defn cell-background
  "Controls cell color depending on player, if they won, etc."
  [cell-data]
  (if (= cell-data (last (@*state :winner)))
    (if (= cell-data :o)
      {:-fx-background-color :forestgreen}
      {:-fx-background-color :tomato})
    (if (keyword? cell-data)
      (if (= cell-data :o)
        {:-fx-background-color :darkseagreen}
        {:-fx-background-color :lightcoral})
      {:-fx-background-color :lightgray})))

(defn grid-cell
  "Gets and returns the display for a single cell.
  To be displayed in the gui.
  Location is position of cell wanted."
  [board location]
  (let [cell-data ; what is located in the cell
        (nth (nth board (dec (first location))) (dec (last location)))]
    {:fx/type          :button
     :style (cell-background cell-data)
     :grid-pane/hgrow  :sometimes
     :grid-pane/vgrow  :sometimes
     :max-width Double/MAX_VALUE
     :max-height Double/MAX_VALUE
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

(defn information
  "Checks for winners, if board is full, or else displays current turn. Also displays current dimensions."
  [{:keys [board dimensions player]}]
  {:fx/type :v-box
   :alignment :center
   :spacing 3
   :children [{:fx/type :label
               :text (let [winner (logic/winner? board dimensions)]
                       (cond
                         winner (do
                                  (set-winner true winner)
                                  (str "Player " (name winner) " wins!"))
                         (logic/full-board? board) (str "The game is a draw.")
                         :else
                         (str (name player) "'s turn.")))}]})

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
   :on-action (fn [_] (do
                        (set-dimensions temp-dimensions)
                        (set-winner false nil)))})

(defn display-information
  "Left of split pane with controls and information display."
  [{:keys [board dimensions temp-dimensions]}]
  {:fx/type   :v-box
   :alignment :center
   :spacing   20
   :children  [{:fx/type information
                :board board
                :dimensions dimensions
                :player (first (@*state :player-turns))}
               {:fx/type         dimension-input
                :temp-dimensions temp-dimensions}
               {:fx/type         new-game-button
                :temp-dimensions temp-dimensions}]})

(defn grid-pane
  "Contains the grid for the tic-tac-toe board."
  [{:keys [board dimensions]}]
  {:fx/type :grid-pane
   :children
   (display-board-as-grid {:board board
                           :dimensions dimensions})})

(defn split-pane
  "Splits scene into grid-pane, and left-hand section for everything else."
  [{:keys [board dimensions temp-dimensions]}]
  {:fx/type :split-pane
   :divider-positions [0.2]
   :items [{:fx/type :v-box
            :split-pane/resizable-with-parent false
            :children [{:fx/type :label
                        :padding 50
                        :text "Tic-Tac-Toe"}
                       {:fx/type display-information
                        :padding 50
                        :board board
                        :dimensions dimensions
                        :temp-dimensions temp-dimensions}]}
           {:fx/type grid-pane
            :split-pane/resizable-with-parent false
            :board board
            :dimensions dimensions}]})

(defn root
  [{:keys [board dimensions temp-dimensions]}]
  {:fx/type :stage
   :showing true
   :maximized true
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