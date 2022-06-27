(ns tic-tac-toe.core
  (:gen-class)
  (:require
   [clojure.string :as str]))

(defn board-creator
  "Creates a board using given dimensions. The board is a single dimension vector."
  [dimensions]
  (vec
   (range 1 (inc (* (first dimensions) (last dimensions))))))

(defn define-dimensions
  "Asks user for dimensions of a board, and places them in a vector.
  Defaults to [3 3] if any input besides the proper form with integers is given."
  []
  (println "Please enter dimensions of board in form 'i,j', otherwise board defaults to '3x3'.")
  (let [line-input (read-line)]
    (if (str/includes? line-input ",")
      (let [input (str/split line-input #",")]
        (try
          [(. Integer parseInt (nth input 0)) (. Integer parseInt (nth input 1))]
          (catch Exception e
            [3 3])))
      [3 3])))

(defn display-board
  "Displays state of board."
  [board
   width]
  (let [board (map #(if (keyword? %) ; transform keywords (:o or :x) to o and x
                      (str " " (str/upper-case (name %)))
                      (if (< % 10)
                        (str " " %)
                        %))
                   board)]
    (println (str " " (str/join " " (flatten (interpose "\n" (partition-all width board))))))))

;; Functionality for checking for a winner, probably only works for 3x3 board
(defn create-matches
  "Checks all of the board's lines to be checked for a winner. Takes a board as an argument."
  [board width]
  (concat
   (partition-all width board)                        ; partition board into rows
   (list
    (take-nth 3 board)                            ; first column
    (take-nth 3 (drop 1 board))
    (take-nth 3 (drop 2 board))
    (take-nth 4 board)                            ; diagonal
    (take-nth 2 (drop-last 2 (drop 2 board))))))  ; diagonal

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
  [board width]
  (first
   (filter #{:x :o} (map player-match? (create-matches board width)))))

(defn full-board?
  "Is every cell a :o or :x?"
  [board]
  (every? #{:x :o} board))

;; Game sequence
(def player-turns
  "Alternates between players."
  (cycle [:o :x]))

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

(defn take-turn
  "Tell players to make a move and handle incorrect moves."
  [player board]
  (println (str (name player) "'s turn, Select your move (enter number 1 through 9)"))
  (loop [move (next-placement board)]
    (if move
      (assoc board (dec move) player)
      (do
        (println (str (name player) ":") "The move entered is invalid, enter another move.")
        (recur (next-placement board))))))

(defn game-loop
  "Iterates through player turns until winner or board is full."
  [player-turns]
  (loop [dimensions (define-dimensions)
         board (board-creator dimensions)
         player-turns player-turns]
    (let [winner (winner? board (nth dimensions 0))]
      (println dimensions)
      (println "Current Board:")
      (display-board board (nth dimensions 0))
      (cond
        winner  (println "Player " (name winner) "wins!")
        (full-board? board) (println "The game is a draw.")
        :else
        (recur
         dimensions
         (take-turn (first player-turns) board)
         (rest player-turns))))))

(game-loop player-turns)
