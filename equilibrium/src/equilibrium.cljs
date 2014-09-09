(ns equilibrium
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

;;;
;;; Constants
;;;

(def width 500)
(def height 500)
(def initial-n 15)
(def frame 30)

(def speed 3)
(def eps 1)

;;;
;;; Setup
;;;

(defn choose-leaders
  "Randomly chooses 2 leaders for each point and assigns them to the point
  as :leaders. Algorithm chooses leaders in such way that every point is a
  leader for 2 other points."
  [points]
  (letfn [(delete-first [coll val]
            (let [[before after] (split-with #(= % val) coll)]
              (concat before (rest after))))

          (choose-leaders [cur leaders]
            (let [chosen (->> leaders
                              distinct
                              (remove #(= % cur))
                              (take 2))]
              [chosen (reduce delete-first leaders chosen)]))

          (process-point [state point]
            (let [[chosen leaders] (choose-leaders point (:leaders state))]
              (-> state
                  (update-in [:points point] assoc :leaders chosen)
                  (assoc :leaders leaders))))

          (process-all-points []
            (let [all (range (count points))
                  leaders (shuffle (concat all all))]
              (-> (reduce process-point
                          {:points points
                           :leaders leaders}
                          all)
                  :points)))

          (good? [points]
            (every? #(-> % :leaders count (= 2)) points))]
    (->> (repeatedly process-all-points)
         (filter good?)
         (first))))

(defn rand-point
  "Creates a random point with optional constant velocity."
  [velocity?]
  (let [angle (rand q/TWO-PI)
        cur-speed (rand speed)]
      {:x (rand-int width)
       :y (rand-int height)
       :color (q/color (+ 127 (rand-int 127))
                       (+ 127 (rand-int 127))
                       (+ 127 (rand-int 127)))
       :velocity (if velocity?
                   ((juxt q/cos q/sin) cur-speed)
                   [0 0])}))

(defn generate-points
  "Generates n points with leaders assigned. velocity? indicates whether
  points should have constant velocity or not."
  [n velocity?]
  (-> (repeatedly n #(rand-point velocity?))
      vec
      (choose-leaders)
      vec))

(defn setup
  "Standard quil function which sets up a sketch and returns initial state."
  [options]
  (q/frame-rate frame)
  (merge options
         {:points (generate-points initial-n (:constant-velocity? options))
          :dragging nil
          :n initial-n}))

;;;
;;; Update logic
;;;

(defn calc-move
  "Calculates the displacement vector for point p given it leaders p1 and p2."
  [p p1 p2]
  (let [a (- (:x p2) (:x p1))
        b (- (:y p2) (:y p1))
        mx (/ (+ (:x p1) (:x p2)) 2)
        my (/ (+ (:y p1) (:y p2)) 2)
        c (- (+ (* a mx) (* b my)))
        hyp (q/sqrt (+ (q/sq a) (q/sq b)))
        d (/ (+ (* a (:x p)) (* b (:y p)) c)
             hyp)
        angle (q/atan2 b a)
        angle (if (pos? d) (+ q/PI angle) angle)
        speed (min speed (Math/abs d))]
    (if (< speed eps)
      [0 0]
      [(* (q/cos angle) speed)
       (* (q/sin angle) speed)])))

(defn ensure-in-board
  "Ensures that the point is inside sketch boundaries."
  [p]
  (-> p
      (update-in [:x] q/constrain 0 width)
      (update-in [:y] q/constrain 0 height)))

(defn ensure-in-toroidal-board
  "Ensures that the point is inside sketch boundaries.
  Uses toroidal board."
  [p]
  (-> p
      (update-in [:x] #(-> % (+ width) (mod width)))
      (update-in [:y] #(-> % (+ height) (mod height)))))

(defn move
  "Moves the point towards equidistance with point leaders."
  [p state]
  (let [points (:points state)
        [l1 l2] (:leaders p)
        [dx dy] (calc-move p (points l1) (points l2))
        [vx vy] (:velocity p)
        tail (if (zero? (:tail-size state))
               []
               (->> (:tail p)
                    (take (dec (:tail-size state)))
                    (cons {:x (:x p) :y (:y p)})))
        in-board (if (:toroidal-board? state)
                   ensure-in-toroidal-board
                   ensure-in-board)]
    (-> p
        (assoc :tail tail)
        (update-in [:x] + dx vx)
        (update-in [:y] + dy vy)
        (in-board))))

(defn update-points
  "Moves each point and returns updated vector of points."
  [points state]
  (reduce
   (fn [new-points ind]
     (update-in new-points [ind] move state))
   points
   (range (count points))))

(defn update
  "Updates sketch state. If sketch is paused then the state
  returned unmodified."
  [state]
  (if (:running? state)
    (update-in state [:points] update-points state)
    state))

;;;
;;; Draw
;;;

(defn draw-point
  "Draws the point."
  [p]
  (doseq [p (cons p (:tail p))]
    (q/point (:x p) (:y p))))

(defn find-points
  "Finds all points in 10-pixel approximity of point (x,y)."
  [points x y]
  (for [ind (range (count points))
        :let [p (points ind)]
        :when (< (q/dist (:x p) (:y p) x y) 10)]
    ind))

(defn draw-equidistance
  "Highlights a point, it leaders and draws equidistance line."
  [points p]
  (let [[l1 l2] (:leaders p)
        p1 (points l1)
        p2 (points l2)
        a (* width (- (:y p1) (:y p2)))
        b (* width (- (:x p2) (:x p1)))
        mx (/ (+ (:x p1) (:x p2)) 2)
        my (/ (+ (:y p1) (:y p2)) 2)]
    (q/stroke 0 255 0)
    (draw-point p)
    (q/stroke 255 0 0)
    (draw-point p1)
    (draw-point p2)
    (q/stroke-weight 1)
    (q/line (+ mx a) (+ my b) (- mx a) (- my b))))

(defn draw-initial-text []
  (q/text-size 20)
  (q/text-align :center :center)
  (q/fill 0)
  (q/text "Select me and press Space" 0 0 (q/width) (q/height)))

(defn draw
  "Draws sketch state."
  [{:keys [points] :as state}]
  (q/background 240)
  (q/stroke-weight 10)
  (q/stroke 0)
  (doseq [p points]
    (when (:colourful? state)
      (q/stroke (:color p)))
    (draw-point p))
  (doseq [ind (find-points points (q/mouse-x) (q/mouse-y))]
    (draw-equidistance points (points ind)))
  (when-not (contains? state :running?)
    (draw-initial-text)))

;;;
;;; User interaction
;;;

(defn mouse-dragged
  "Drags the selected point (if any). Sets coordinates of the selected
  point to be equals to the current mouse position."
  [state event]
  (if-let [ind (:dragging state)]
    (update-in state [:points ind] merge (select-keys event [:x :y]))
    state))

(defn mouse-pressed
  "Selects a point for dragging. Adds the point index to the state."
  [state event]
  (let [ind (-> (:points state)
                (find-points (:x event) (:y event))
                first)]
    (assoc state :dragging ind)))

(defn regenerate-points
  "Regenerates points in the state map."
  [state]
  (assoc state :points (generate-points (:n state)
                                        (:constant-velocity? state))))

(defn change-n
  "Updates n - number of points. fn is a function using which n is updated,
  either inc or dec. Regenerates points after that."
  [state fn]
  (-> state
      (update-in [:n] #(max 3 (fn %)))
      regenerate-points))

(defn key-pressed
  "Process key event.
  r - regenerate points
  m,l - increase/decrease number of points and regenerate them
  space - pause sketch"
  [state event]
  (condp = (:key event)
    :r (regenerate-points state)
    :up (change-n state inc)
    :down (change-n state dec)
    :left (update-in state [:tail-size] #(-> % dec (max 0)))
    :right (update-in state [:tail-size] inc)
    (keyword " ") (update-in state [:running?] not)
    state))

(defn run-sketch [options]
  (q/sketch
    :host (:host options)
    :size [width height]
    :setup #(setup options)
    :draw draw
    :update update
    :mouse-pressed mouse-pressed
    :mouse-dragged mouse-dragged
    :key-pressed key-pressed
    :middleware [m/fun-mode]))

(run-sketch {:constant-velocity? true
             :colourful? true
             :tail-size 0
             :host "sketch"
             :toroidal-board? true})
