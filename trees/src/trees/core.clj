(ns trees.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def initial-angle q/HALF-PI)
(def grow-speed 1)
(def length-coef [0.7 1])
(def angle-coefs [[(- q/QUARTER-PI) 0]
                  [0 q/QUARTER-PI]])
(def branch-prob 0.9)
(def initial-depth 14)
(def transform-delay 200)
(def transform-length 200)

(defn branch-width [length]
  (q/map-range length
               5 (/ (q/height) 7)
               0.1 2))

(defn first-branch [width height]
  (let [length (/ height 7)]
    {:parent [0 0]
     :begin [0 0]
     :end [0 length]
     :length length
     :angle initial-angle
     :time 0
     :end-time (/ length grow-speed)
     :width 4}))

(defn rand-in-range [[a b]]
  (+ a (rand (- b a))))

(defn pol->cart [base angle radius]
  (map + base
       [(-> angle q/cos (* radius))
        (-> angle q/sin (* radius))]))

(defn branch-off-prob [root-length length]
  (q/map-range length
               10 root-length
               0.9 1))

(defn branch-off [root branch]
  (let [prob (branch-off-prob (:length root) (:length branch))
        max-dist (- (q/height) (:length root))]
    (for [angle-coef (filter (fn [_] (< (rand) prob))
                             (shuffle angle-coefs))
          :let [length (* (:length branch)
                          (rand-in-range length-coef))
                angle (+ (:angle branch)
                         (rand-in-range angle-coef))
                end (pol->cart (:end branch) angle length)
                time (+ (:end-time branch) (rand-int 10))]
          :when (and (< (* (q/width) -0.5) (first end) (* (q/width) 0.5))
                     (< 0 (second end) (q/height))
                     (< (apply q/dist (concat (:end root) end)) max-dist)
                     (> length 5))]
      {:parent (:begin branch)
       :begin (:end branch)
       :end end
       :length length
       :angle angle
       :time time
       :end-time (+ time (/ length grow-speed))
       :width (branch-width length)})))

(defn current-end-point [branch time]
  (if (> time (:end-time branch))
    (:end branch)
    (let [delta (- time (:time branch))
          cur-length (min (:length branch)
                          (* delta grow-speed))]
      (pol->cart (:begin branch) (:angle branch)
                 cur-length))))

(defn generate-branches [branch n]
  (->> [branch]
       (iterate #(mapcat (partial branch-off branch) %))
       (take n)
       (apply concat)
       (doall)))

(defn generate-state [old-state]
  (let [branches (generate-branches (first-branch (q/width) (q/height))
                                    (:depth old-state))
        end-time (apply max (map :end-time branches))]
    (-> old-state
        (update-in [:main-color] #(- 255 %))
        (update-in [:secondary-color] #(- 255 %))
        (assoc
         :branches (vec branches)
         :time 0
         :end-time end-time
         :buffer (q/create-graphics (q/width) (q/height) :java2d)))))

(defn setup []
  (generate-state
   {:depth initial-depth
    :main-color 255
    :secondary-color 0
    :show-stats? false}))

(defn update-state [state]
  (if (> (:time state) (+ (:end-time state)
                          transform-delay
                          transform-length))
    (-> state
        generate-state)
    (update-in state [:time] inc)))

(defn draw-branch [branch time]
  (when (< (:time branch) time (:end-time branch))
    (q/stroke-weight (:width branch))
    (q/line (:begin branch) (current-end-point branch time))))

(defn draw-tree [state]
  (q/push-matrix)
  (q/rotate (- q/PI))
  (q/translate (/ (q/width) -2) (- (q/height)))
  (q/stroke (:main-color state))
  (doseq [branch (:branches state)]
    (draw-branch branch (:time state)))
  (q/pop-matrix))

(defn draw-stats [state]
  (q/fill (:main-color state))
  (q/stroke (:main-color state))
  (q/text (str "lvls: " (:depth state)
               " branches: " (count (:branches state))) 10 10)
  (q/text (str "fps: " (q/current-frame-rate)) (- (q/width) 100) 10))

(defn draw-state [state]
  (q/with-graphics (:buffer state)
    (draw-tree state))
  (let [step (-> (- (:time state) (:end-time state) transform-delay)
                 (/ (double transform-length))
                 (q/constrain 0 1))
        background (q/lerp (:secondary-color state) (:main-color state) step)]
    (q/background background))
  (q/image (:buffer state) 0 0)
  (when (:show-stats? state)
    (draw-stats state)))

(defn key-pressed [state event]
  (case (:key event)
    :r (-> (generate-state state)
           (update-in [:time] dec))
    :up (update-in state [:depth] inc)
    :down (update-in state [:depth] #(max 1 (dec %)))
    :i (update-in state [:show-stats?] not)
    :s (do
         (q/save-frame "tree-####.png")
         state)
    state))

(q/defsketch tree
  :size [1000 700]
  :setup setup
  :update update-state
  :draw draw-state
  :key-pressed key-pressed
  :renderer :p2d
  :features [:resizable]
  :middleware [;m/pause-on-error
               m/fun-mode])
