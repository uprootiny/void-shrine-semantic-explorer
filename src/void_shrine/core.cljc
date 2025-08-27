(ns void-shrine.core
  (:require #?(:clj [clojure.core.async :as async :refer [go go-loop <! >! chan timeout]]
               :cljs [cljs.core.async :as async :refer [<! >! chan timeout]])
            [clojure.string :as str])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]])))

(defn chaos-state
  "Create reactive chaos state atom"
  []
  (atom {:entropy-values []
         :void-manifestations []
         :chaos-metrics {:total-entropy 0
                         :void-depth 0
                         :dissolution-rate 0}
         :active-streams #{}
         :timestamp (js/Date.now)}))

(defn entropy-to-color
  "Convert entropy value to RGB color"
  [entropy]
  (let [r (bit-and entropy 0xFF)
        g (bit-and (bit-shift-right entropy 8) 0xFF)
        b (bit-and (bit-shift-right entropy 16) 0xFF)]
    (str "rgb(" r "," g "," b ")")))

(defn chaos-pulse
  "Generate pulsing chaos values"
  [base-entropy time-factor]
  (let [wave (Math/sin (* time-factor 0.001))
        chaos (* base-entropy (+ 1 (* 0.5 wave)))]
    {:value chaos
     :wave wave
     :color (entropy-to-color (int chaos))}))

(defn void-spiral
  "Calculate spiral coordinates in the void"
  [entropy angle radius]
  (let [theta (* angle (/ Math/PI 180))
        decay (Math/exp (- (* 0.01 entropy)))
        x (* radius (Math/cos theta) decay)
        y (* radius (Math/sin theta) decay)]
    {:x x :y y :decay decay}))

(defn reactive-transform
  "Transform data reactively based on user interaction"
  [data interaction-point]
  (let [{:keys [x y]} interaction-point
        distance (Math/sqrt (+ (* x x) (* y y)))
        transform-factor (/ 1 (+ 1 (* 0.01 distance)))]
    (map #(* % transform-factor) data)))

(defn entropy-flow
  "Create flowing entropy visualization data"
  [entropy-stream frame-count]
  (let [flow-points (take 100 entropy-stream)]
    (map-indexed
     (fn [idx point]
       {:x (* idx 10)
        :y (* (Math/sin (+ (* idx 0.1) (* frame-count 0.01))) 50)
        :size (mod point 20)
        :opacity (/ point 255)
        :color (entropy-to-color point)})
     flow-points)))

(defn void-fractal
  "Generate void fractal patterns"
  [seed depth max-depth]
  (if (>= depth max-depth)
    [{:x 0 :y 0 :size 1}]
    (let [branches (+ 2 (mod seed 4))
          angle-step (/ 360 branches)]
      (mapcat
       (fn [i]
         (let [angle (* i angle-step)
               sub-fractal (void-fractal (+ seed i) (inc depth) max-depth)]
           (map (fn [point]
                  {:x (+ (:x point) (* 10 (Math/cos (* angle (/ Math/PI 180)))))
                   :y (+ (:y point) (* 10 (Math/sin (* angle (/ Math/PI 180)))))
                   :size (* (:size point) 0.7)})
                sub-fractal)))
       (range branches)))))

(defn chaos-symphony
  "Generate audio-like waveform from chaos"
  [entropy-values time]
  (let [frequencies (map #(+ 20 (mod % 2000)) entropy-values)
        amplitudes (map #(/ % 255) entropy-values)]
    (reduce +
            (map (fn [freq amp]
                   (* amp (Math/sin (* 2 Math/PI freq time 0.001))))
                 frequencies
                 amplitudes))))

(defn update-chaos-state
  "Update the reactive chaos state"
  [state new-entropy]
  (-> state
      (update :entropy-values #(take 1000 (conj % new-entropy)))
      (update-in [:chaos-metrics :total-entropy] + new-entropy)
      (update-in [:chaos-metrics :void-depth] inc)
      (assoc-in [:chaos-metrics :dissolution-rate]
                (/ new-entropy (inc (get-in state [:chaos-metrics :void-depth]))))
      (assoc :timestamp (js/Date.now))))