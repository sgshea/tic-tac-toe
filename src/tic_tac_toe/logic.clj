(ns tic-tac-toe.logic
  (:gen-class)
  (:require
   [clojure.string :as str]))

(defn board-creator
  "Creates a board using given dimensions. The board is a single dimension vector."
  [dimensions]
  (vec
   (range 1 (inc (* (first dimensions) (last dimensions))))))

(defn create-matches-full
  "Creates lines to be checked for matches, lines go fully across."
  [board dimensions]
  (concat
   (partition-all (first dimensions) board)                                     ; rows
   (loop                                                                        ; columns
    [board board
     columns '()]
     (if
      (and (seq board)
           (= (count (take-nth (first dimensions) board)) (last dimensions)))
       (recur (rest board)
              (conj columns (take-nth (first dimensions) board)))
       columns))
   (when (= (first dimensions) (last dimensions))                               ; diagonals
     (list
      (loop [left-diagonal '()                                                   ; diagonal starting from top left
             increment 0]                                                        ; remember indexes start from 0
        (if (< increment (last dimensions))
          (recur
           (conj left-diagonal
                 (nth board
                      (+ increment (* increment (first dimensions)))))
           (inc increment))
          left-diagonal))
      (loop [right-diagonal '()                                                  ; diagonal starting from bottom left to top right
             decrement (last dimensions)]
        (if (> decrement 0)
          (recur
           (conj right-diagonal
                 (nth board
                      (- (* decrement (first dimensions)) decrement)))
           (dec decrement))
          right-diagonal))))))

(defn player-match?
  "If a line contains the same player, return player, otherwise nil."
  [line]
  (if (every? #{:x} line)
    :x
    (if (every? #{:o} line)
      :o
      nil)))

(defn winner?
  "Returns winning player if one exists, otherwise nil."
  [board dimensions]
  (first
   (filter #{:x :o} (map player-match? (create-matches-full board dimensions)))))

(defn full-board?
  "Is every cell a :o or :x?"
  [board]
  (every? #{:x :o} board))

;; Game sequence
(def player-turns
  "Alternates between players."
  (cycle [:o :x]))

;; Following methods only will work in cli version
(defn define-dimensions
  "In game-loop, user is asked to input a pair of numbers for dimensions.
  Defaults to [3 3] if any input besides the proper form with integers is given."
  []
  (let [line-input (read-line)]
    (if (str/includes? line-input ",")
      (let [input (str/split line-input #",")]
        (try
          [(. Integer parseInt (nth input 0)) (. Integer parseInt (nth input 1))]
          (catch Exception e
            [3 3])))
      [3 3])))

;; if enabled, cli board uses cat and turtles
(def animals
  true)

;; cli display
(defn display-board
  "Displays state of board."
  [board
   width]
  (let [board (map #(if (keyword? %) ; transform keywords (:o or :x) to o and x
                      (if animals
                        (if (= % :o)
                          "????"
                          "????")
                        (str " " (name %)))
                      (if (< % 10)
                        (str " " %)
                        %))
                   board)]
    (println (str " " (str/join " " (flatten (interpose "\n" (partition-all width board))))))))

(defn next-placement
  "Reads next placement from command line and converts to integer.
  Returns move if value is existing, otherwise nil."
  [board]
  (let [keyboard-input
        (try
          (. Integer parseInt (read-line))
          (catch Exception e nil))]
    (if (some #{keyboard-input} board)
      keyboard-input
      nil)))

(defn take-turn-cli
  "Tell players to make a move and handle incorrect moves."
  [player board dimensions]
  (println (str (name player) "'s turn, Select your move (enter number 1 through " (* (first dimensions) (last dimensions)) ")"))
  (loop [move (next-placement board)]
    (if move
      (assoc board (dec move) player)
      (do
        (println (str (name player) ":") "The move entered is invalid, enter another move.")
        (recur (next-placement board))))))

;; CLI version
(defn game-loop-cli
  "Iterates through player turns until winner or board is full."
  [player-turns]
  (println "Please enter dimensions of board in form 'i,j', otherwise board defaults to '3x3'.")
  (let [dimensions (define-dimensions)]
    (loop [board (board-creator dimensions)
           player-turns player-turns]
      (println dimensions)
      (let [winner (winner? board dimensions)]
        (println "Current Board:")
        (display-board board (first dimensions))
        (cond
          winner  (println "Player " (name winner) "wins!")
          (full-board? board) (println "The game is a draw.")
          :else
          (recur
           (take-turn-cli (first player-turns) board dimensions)
           (rest player-turns)))))))

;;(game-loop player-turns)
