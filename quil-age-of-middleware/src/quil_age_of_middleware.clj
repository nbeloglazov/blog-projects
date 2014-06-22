(ns quil-age-of-middleware.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

; screen size
(def sc-size 200)

;;;
;;; Code for drawing nested squares
;;;

(def squares
  (let [s 30
        ss (- sc-size s)
        rat 0.1
        middle-point (fn [[x1 y1] [x2 y2]]
                       [(q/map-range rat 0 1 x1 x2)
                        (q/map-range rat 0 1 y1 y2)])]
    (iterate (fn [[p1 p2 p3 p4]]
               (map middle-point [p1 p2 p3 p4] [p2 p3 p4 p1]))
             [[s s]
              [ss s]
              [ss ss]
              [s ss]])))

(defn draw-squares []
  (q/background 255)
  (doseq [[[x1 y1] [x2 y2] [x3 y3] [x4 y4]] (take 45 squares)]
    (q/quad x1 y1 x2 y2 x3 y3 x4 y4)))

;;;
;;; Code for drawing bouncing ball.
;;; It uses fun-mode.
;;;

(def ball-size 10)

(defn setup []
  (q/ellipse-mode :radius)
  {:ball [100 100]
   :speed [5 3]})

(defn update [{:keys [speed ball] :as state}]
  (let [d-speed [(if (<= ball-size (first ball) (- sc-size ball-size))
                   1 -1)
                 (if (<= ball-size (second ball) (- sc-size ball-size))
                   1 -1)]
        speed (map * speed d-speed)]
    (-> state
        (update-in [:ball] #(map + % speed))
        (assoc :speed speed))))

(defn draw [{[x y] :ball}]
  (q/background 255)
  (q/stroke-weight 2)
  (q/stroke 255 0 0)
  (q/fill 255)
  (q/rect 0 0 199 199)
  (q/stroke-weight 1)
  (q/fill 0 0 255)
  (q/stroke 0)
  (q/ellipse x y ball-size ball-size))

;;;
;;; Rotating middleware
;;;

(defn rotating-draw [period orig-draw]
  (let [angle (q/map-range (mod (q/frame-count) period)
                           0 period
                           0 q/TWO-PI)
        center-x (/ (q/width) 2)
        center-y (/ (q/height) 2)]
    (q/with-translation [center-x center-y]
      (q/with-rotation [angle]
        (q/with-translation [(- center-x) (- center-y)]
          (orig-draw))))))

(defn rotate-me [options]
  (let [draw (:draw options (fn []))
        period (:rotate-period options 200)]
    (assoc options
      :draw (partial rotating-draw period draw))))

(q/defsketch my-sketch
  :setup setup
  :update update
  :draw draw
  :size [sc-size sc-size]
  :middleware [rotate-me m/fun-mode]
  :rotate-period 200)
