(ns entropy-gallery.processes.levy
)

;; Lévy stable distribution parameters
(defrecord LevyParams [alpha beta scale location])

(defn box-muller-transform []
  "Generate two independent standard normal random variables"
  (let [u1 (rand)
        u2 (rand)
        z0 (* (Math/sqrt (* -2 (Math/log u1))) (Math/cos (* 2 Math/PI u2)))
        z1 (* (Math/sqrt (* -2 (Math/log u1))) (Math/sin (* 2 Math/PI u2)))]
    [z0 z1]))

(defn stable-random [alpha beta]
  "Generate a sample from stable distribution using Chambers-Mallows-Stuck method"
  (let [v (* Math/PI (- (rand) 0.5))
        w (- (Math/log (rand)))
        ; Special case for Cauchy (alpha=1)
        _ (if (= alpha 1.0)
            (/ (+ (* beta (Math/tan v)) (Math/log (/ (* w (Math/cos v)) (+ v (* beta v)))))
               (* Math/PI 0.5))
            ; General case
            (let [t (/ (Math/sin (* alpha (+ v (* beta (Math/tan (* Math/PI alpha 0.5))))))
                      (Math/pow (Math/cos v) (/ 1.0 alpha)))
                  s (Math/pow (/ (Math/cos (- v (* alpha (+ v (* beta (Math/tan (* Math/PI alpha 0.5)))))))
                                w)
                             (/ (- 1 alpha) alpha))]
              (* t s)))]
    _))

(defn levy-stable-sample 
  "Generate Lévy stable distributed sample"
  ([alpha beta] (stable-random alpha beta))
  ([alpha beta scale location] 
   (+ location (* scale (stable-random alpha beta)))))

(defn levy-flight [n alpha beta scale start-pos]
  "Generate a Lévy flight path of n steps"
  (reductions + start-pos 
              (repeatedly n #(levy-stable-sample alpha beta scale 0))))

(defn fractional-brownian-motion [n hurst scale]
  "Generate fractional Brownian motion using Cholesky decomposition approximation"
  (let [h hurst
        ; Covariance function: K(s,t) = 0.5 * (|s|^(2H) + |t|^(2H) - |s-t|^(2H))
        covariance (fn [i j] 
                    (let [s (/ i n) t (/ j n)]
                      (* 0.5 scale scale
                         (+ (Math/pow (Math/abs s) (* 2 h))
                            (Math/pow (Math/abs t) (* 2 h))
                            (- (Math/pow (Math/abs (- s t)) (* 2 h)))))))
        ; Generate correlated Gaussian process (simplified)
        normals (repeatedly n #(first (box-muller-transform)))
        ; Apply simple correlation structure
        correlated (map-indexed 
                    (fn [i x] 
                      (* x (Math/pow (/ (+ i 1) n) (- h 0.5))))
                    normals)]
    (reductions + 0 correlated)))

(defn jump-diffusion-process [n lambda mu sigma jump-mean jump-std dt]
  "Generate jump diffusion process (Merton model)"
  (let [diffusion-part (fn [] (* sigma (Math/sqrt dt) (first (box-muller-transform))))
        jump-part (fn [] (if (< (rand) (* lambda dt))
                          (+ jump-mean (* jump-std (first (box-muller-transform))))
                          0))
        drift (* mu dt)]
    (reductions + 0 
                (repeatedly n #(+ drift (diffusion-part) (jump-part))))))

(defn stable-diffusion-kernel [entropy-samples alpha beta temperature]
  "Create a diffusion kernel from entropy using Lévy stable processes"
  (let [normalized-entropy (map #(/ % (apply max entropy-samples)) entropy-samples)
        levy-samples (map #(levy-stable-sample alpha beta (* temperature %) 0) 
                         normalized-entropy)]
    {:kernel levy-samples
     :entropy normalized-entropy
     :alpha alpha
     :beta beta
     :temperature temperature}))

(defn diffusion-step [current-state kernel step-size]
  "Single step of entropy-driven diffusion"
  (let [{:keys [kernel alpha beta temperature]} kernel
        noise (rand-nth kernel)
        drift (* -0.1 current-state) ; Simple mean-reverting drift
        diffusion (* step-size noise)]
    (+ current-state drift diffusion)))

(defn entropy-stable-diffusion [initial-state entropy-samples steps alpha beta temperature step-size]
  "Full entropy-driven stable diffusion process"
  (let [kernel (stable-diffusion-kernel entropy-samples alpha beta temperature)]
    (take steps 
          (iterate #(diffusion-step % kernel step-size) initial-state))))