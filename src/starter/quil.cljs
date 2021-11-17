(ns starter.quil
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [starter.util :as u]
            [starter.fxhash :as f]))

(defn get-viewport
  "Returns a vector of [vw vh] for the current browser window, scaled by the scale parameter.
   If true is passed to force-square the smaller dimension will be used on both axes."
  [scale force-square]
  (if force-square
    (let [dim (min (* scale (.-innerWidth js/window)) (* scale (.-innerHeight js/window)))]
      [dim dim])
    [(* scale (.-innerWidth js/window)) (* scale (.-innerHeight js/window))]))

(def dims (get-viewport 1 true))

(defn x
  "Return t% of viewport width, allows for resolution independent drawing i.e. 10px in a 100px window = (x 10) = 25px in a 250px window"
  [t]
  (let [[w _] dims] (* t (/ w 100))))

(defn y
  "Return t% of viewport height, allows for resolution independent drawing i.e. 10px in a 100px window = (x 10) = 25px in a 250px window"
  [t]
  (let [[_ h] dims] (* t (/ h 100))))

(defn setup []
  ;; EXAMPLE CODE, DO NOT HARDCODE YOUR FEATURES


  (q/frame-rate 60)

  ;; (q/color-mode :hsb)


  (let [omega (f/fx-rand)
        gamma (f/fx-rand)
        delta (f/fx-rand)]
    (f/register-features {:twitchy (< delta 0.05)
                          :dizzy (< gamma 0.05)
                          :bouncy (and (> delta 0.3) (< gamma 0.3))
                          :loopy (< (q/abs (- delta gamma)) 0.1)
                          :swirly (> gamma 0.7)})
    {:t 0
     :omega omega
     :gamma gamma
     :delta delta}))

(defn update-state [state]
  (merge state {:t (inc (:t state))}))

(defn draw-state [{:keys [t omega gamma delta]}]
  (let [t' (/ t (+ 8 (* gamma 32)))]
    ; clear screen
    (q/fill 0 0 0)
    (q/stroke-weight (+ 1 (* delta 4)))

    (when (> (q/sin (/ t' 16)) 0.98)
      (q/blend-mode :blend)
      (q/background 0 0 0 32))

    (q/blend-mode :screen)

    ;; (q/blend-mode :blend)
    ;; (q/text (str "gamma=" gamma) (x 2) (y 2))
    ;; (q/text (str "delta=" delta) (x 2) (y 4))
    ;; (q/text (str "omega=" omega) (x 2) (y 6))
    ;; (q/blend-mode :screen)

    (let [angle (* (+ (* gamma (q/sin t')) (* delta (q/cos t'))) 2)
          length (* (y 100) (q/sin (* gamma t')) (q/cos (* delta t')))
          [lx ly] [(+ (x 45) (x (* 45 (q/cos (/ t' (* 3 delta)))))) (+ (y 45) (y (* 45 (q/sin (/ t' (* 3 gamma))))))]]
      (apply q/stroke [(* 255 (+ omega (* delta (q/cos t'))))
                       (+ 63 (* 192 (q/sin (* delta t'))))
                       (+ 128 (* 128 (q/cos (* delta t'))))])
      (q/line [(+ lx (* (/ length 2) -1 (q/cos angle))) (+ ly (* (/ length 2) -1 (q/sin angle)))]
              [(+ lx (* (/ length 2) (q/cos angle))) (+ ly (* (/ length 2) (q/sin angle)))])

      (comment (let [[w h] dims]
                 (apply q/fill [(* 255 (q/sin t')) (* 255 omega) (* 255 (q/cos t'))])
                 (q/ellipse (/ w 2) (/ h 2) (x 10) (y 10))))))



    ; draw resolution independent circle
  )

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch fxhash
    :host "app"
    :size dims
    :renderer :p2d
    :setup setup
    :update update-state
    :draw draw-state
    ;; :key-pressed (u/save-image "export.png")
    :middleware [m/fun-mode]))

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (js/console.log "start"))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (run-sketch)
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
