(ns vector-generation.vectors-3d
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

;;;
;;; Source code for Random Vector Generation article.
;;; http://nbeloglazov.com/2017/04/09/random-vector-generation.html
;;;

; Number of vectors to generate in visualization
(def n 2000)

(def frame-rate 10)


;;;
;;; rand-3d functions described in the article.
;;;

(defn rand-3d-v1 []
  (let [x (dec (rand 2))
        y (dec (rand 2))
        z (dec (rand 2))
        len (Math/sqrt (+ (* x x) (* y y) (* z z)))]
    [(/ x len) (/ y len) (/ z len)]))

(defn rand-3d-v2 []
  (let [x (dec (rand 2))
        y (dec (rand 2))
        sign (rand-nth [-1 1])
        z (* sign (Math/sqrt (- 1 (* x x) (* y y))))]
    [x y z]))

(defn rand-3d-v3 []
  (let [phi (rand (* 2 Math/PI))
        theta (- (rand Math/PI) (/ Math/PI 2))]
    [(* (Math/cos theta) (Math/cos phi))
     (* (Math/cos theta) (Math/sin phi))
     (Math/sin theta)]))


;;;
;;; Visualization.
;;;

(defn generate-vectors []
  (repeatedly n rand-3d-v3))

(defn setup []
  (q/frame-rate frame-rate))

(defn draw []
  (q/background 240)

  ; setup camera to look at [0, 0, 0]
  (q/camera  (/ (q/width) 1.5) (/ (q/height) 1.5)
             (/ (/ (q/height) 2.0) (q/tan (/ (* q/PI 60.0) 360.0)))
             0 0 0
             1 1 0)

  (let [coef-1 0.7
        coef-2 0.95
        half-w (/ (q/width) 2)]

    ; draw axis
    (q/stroke-weight 2)
    (q/stroke 255 0 0)
    (q/line (- half-w) 0 0 half-w 0 0)
    (q/stroke 0 255 0)
    (q/line 0 (- half-w) 0 0 half-w 0 )
    (q/stroke 0 0 255)
    (q/line 0 0 (- half-w) 0 0 half-w)
    (q/stroke 0)
    (q/stroke-weight 1)

    ; draw vectors
    (doseq [[x y z] (generate-vectors)]
      (q/line (* x coef-1 half-w)
              (* y coef-1 half-w)
              (* z coef-1 half-w)
              (* x coef-2 half-w)
              (* y coef-2 half-w)
              (* z coef-2 half-w)))))

(q/defsketch visualization
  :title "Random 3D vectors."
  :host "host"
  :size [300 300]
  :setup setup
  :draw draw
  :renderer :p3d
  :features [:keep-on-top])
