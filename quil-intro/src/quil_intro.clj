(ns quil-intro
  (:require [quil.core :refer :all]))

;;; parametric functions in the order they were shown in the post

(defn spiral [t]
  [(* t (sin t))
   (* t (cos t))])

(defn flower [t]
  (let [r (* 200 (sin t) (cos t))]
    [(* r (sin (* t 0.2)))
     (* r (cos (* t 0.2)))]))

(defn water-drop [t]
  (let [r (* 1.5 t (cos t) (sin t))]
    [(* r (sin t))
     (* r (tan t))]))

(defn leaf [t]
  (let [r (* 1.5 t (cos t) (sin t))]
    [(* r (cos t))
     (* r (tan t))]))

(defn f [t]
  (let [r (* 3 t (cos t) (sin t))]
    [(* r (cos (* 2 t)))
     (* r (sin t))]))

;;; end of parametric functions


(defn draw-plot [f from to step]
  (doseq [two-points (->> (range from to step)
                          (map f)
                          (partition 2 1))]
    (apply line two-points)))

; this is 'draw' from first half of the post
; draws static plot
(defn draw-static []
  (with-translation [(/ (width) 2) (/ (height) 2)]
    (draw-plot f 0 100 0.01)))

(defn setup []
  (frame-rate 60)
  (background 255)
  (color-mode :hsb 10 1 1))

; this is 'draw' from second half of the post
(defn draw-animation []
  (with-translation [(/ (width) 2) (/ (height) 2)]
    (let [t (/ (frame-count) 10)]
      ; set color for line
      (stroke (mod t 10) 1 1)
      (line (flower t) (flower (+ t 0.1))))))

(defsketch trigonometry
  :size [300 300]
  :setup setup
  :draw draw-animation)
