(ns tic-tac-toe.core
  (:gen-class)
  (:require [cljfx.api :as fx]
            [tic-tac-toe.logic :as logic])
  (:import [javafx.application Platform]))

(defn grid-cell
  "Gets and returns the display for a single cell.
  To be displayed in the gui.
  Location is position of cell wanted."
  [board width location]
  {:fx/type :label
   :grid-pane/hgrow :always
   :grid-pane/vgrow :always
   :grid-pane/row (inc (first location))
   :grid-pane/column (inc (last location))
   :text (nth board (+ (last location) (* (first location) width)))})

(defn display-board-as-grid
  "Displays the board as a grid."
  [{:keys [board dimensions]}]
  (let [board
        (map #(if (keyword? %) ; transform keywords (:o or :x) to o and x
                (str " " (name %))
                (if (< % 10)
                  (str " " %)
                  %))
             board)]
    (loop
     [grid '()
      row 0
      column 0]
      (if (< row (last dimensions))
        (recur (conj grid (grid-cell board (last dimensions) [row column]))
               (if (< column (dec (first dimensions)))
                 row
                 (inc row))
               (if (< column (dec (first dimensions)))
                 (inc column)
                 0))
        grid))))

(defn display-information
  "A place to display information such as who's turn it is."
  [{:keys [information]}]
  {:fx/type :h-box
   :alignment :center
   :spacing 20
   :children [{:fx/type :label
               :text (str information)}]})

;; Grid for tic-tac-toe board
(defn grid-pane
  [{:keys [board dimensions]}]
  {:fx/type :grid-pane
   :children
   (display-board-as-grid {:board board
                           :dimensions dimensions})})

;; Splits scene into grid-pane and a place for other text
(defn split-pane
  [{:keys [board dimensions]}]
  {:fx/type :split-pane
   :divider-positions [0.5]
   :items [{:fx/type :v-box
            :split-pane/resizable-with-parent false
            :children [{:fx/type :label
                        :padding 50
                        :text "Tic-Tac-Toe"}
                       {:fx/type display-information
                        :padding 50
                        :information "test information!"}]}
           {:fx/type grid-pane
            :board board
            :dimensions dimensions}]})

(defn root
  [{:keys [board dimensions]}]
  {:fx/type :stage
   :showing true
   :title "Tic-Tac-toe"
   :scene {:fx/type :scene
           :root {:fx/type split-pane
                  :pref-width 960
                  :pref-height 540
                  :board board
                  :dimensions dimensions}}})

;; Board is stored in an atom
;; Inital state is with default board of 3x3
(def *state
  (atom {:board [1 2 3 4 5 6 7 8 9]
         :dimensions [3 3]}))

(defn set-board
  "Updates board."
  [board]
  (swap! *state assoc :board board))

(defn set-dimensions
  "Updates dimensions and the board with given dimensions."
  [dimensions]
  (swap! *state assoc :dimensions dimensions
         :board (logic/board-creator dimensions)))

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
