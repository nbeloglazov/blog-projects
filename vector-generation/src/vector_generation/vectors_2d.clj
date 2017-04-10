(ns vector-generation.vectors-2d
  (:require [quil.core :as q  :include-macros true]
            [quil.middleware :as m]))

;;;
;;; Source code for Random Vector Generation article.
;;; http://nbeloglazov.com/2017/04/09/random-vector-generation.html
;;;


; Number of vectors to generate in visualization
(def n 1000)

(def frame-rate 10)

;;;
;;; rand-2d functions described in the article.
;;;

(defn rand-2d-v1 []
  (let [x (dec (rand 2))
        y (dec (rand 2))
        len (Math/sqrt (+ (* x x) (* y y)))]
    [(/ x len) (/ y len)]))

(defn rand-2d-v2 []
  (let [x (dec (rand 2))
        sign (rand-nth [-1 1])
        y (* sign (Math/sqrt (- 1 (* x x))))]
    [x y]))

(defn rand-2d-v3 []
  (let [phi (rand (* 2 Math/PI))]
    [(Math/cos phi) (Math/sin phi)]))


;;;
;;; Visualization.
;;;

(defn generate-vectors []
  (repeatedly n rand-2d-v3))

(defn setup []
  (q/frame-rate frame-rate))

(defn draw []
  (q/background 240)
  (q/fill 0)
  (let [sk-w (q/width)
        sk-h (q/height)
        half-w (/ (q/width) 2)
        coef-1 0.6
        coef-2 0.95]
    (q/with-translation [half-w (/ sk-h 2)]
      (doseq [[x y] (generate-vectors)]
        (q/line (* x coef-1 half-w) (* y coef-1 half-w)
                (* x coef-2 half-w) (* y coef-2 half-w))))))

(q/defsketch visualization
  :title "Random 2D vectors."
  :host "host"
  :size [300 300]
  :setup setup
  :draw draw
  :features [:keep-on-top])
