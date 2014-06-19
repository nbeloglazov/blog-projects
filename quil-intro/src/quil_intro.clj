(ns quil-intro
  (:require [quil.core :as q]))

;;; parametric functions in the order they were shown in the post

(defn spiral [t]
  [(* t (q/sin t))
   (* t (q/cos t))])

(defn flower [t]
  (let [r (* 200 (q/sin t) (q/cos t))]
    [(* r (q/sin (* t 0.2)))
     (* r (q/cos (* t 0.2)))]))

(defn water-drop [t]
  (let [r (* 1.5 t (q/cos t) (q/sin t))]
    [(* r (q/sin t))
     (* r (q/tan t))]))

(defn leaf [t]
  (let [r (* 1.5 t (q/cos t) (q/sin t))]
    [(* r (q/cos t))
     (* r (q/tan t))]))

(defn f [t]
  (let [r (* 3 t (q/cos t) (q/sin t))]
    [(* r (q/cos (* 2 t)))
     (* r (q/sin t))]))

;;; end of parametric functions


(defn draw-plot [f from to step]
  (doseq [two-points (->> (range from to step)
                          (map f)
                          (partition 2 1))]
    (apply q/line two-points)))

; this is 'draw' from first half of the post
; draws static plot
(defn draw-static []
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    (draw-plot f 0 100 0.01)))

(defn setup []
  (q/frame-rate 60)
  (q/background 255)
  (q/color-mode :hsb 10 1 1))

; this is 'draw' from second half of the post
(defn draw-animation []
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    (let [t (/ (q/frame-count) 10)]
      ; set color for line
      (q/stroke (mod t 10) 1 1)
      (q/line (flower t) (flower (+ t 0.1))))))

(q/defsketch trigonometry
  :size [300 300]
  :setup setup
  :draw draw-animation)
