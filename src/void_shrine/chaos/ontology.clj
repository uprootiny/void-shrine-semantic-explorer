(ns void-shrine.chaos.ontology
  (:require [clojure.walk :as walk]))

(def void-tree
  {:void
   {:primordial-absence
    {:pre-being [:never-was :anti-genesis :zero-point]
     :negative-space [:hollow-forms :inverse-reality :gap-between]
     :pure-negation [:absolute-no :cancellation :undoing]}
    
    :entropic-cascade
    {:heat-death [:maximum-entropy :cold-forever :final-equilibrium]
     :dissolution-patterns [:gradual-fade :sudden-collapse :oscillating-death]
     :chaos-mathematics [:strange-attractors :infinite-regression :broken-symmetries]}
    
    :nihil-philosophy
    {:meaning-absence [:purpose-void :value-vacuum :truth-negation]
     :existential-horror [:cosmic-indifference :personal-insignificance :time-terror]
     :anti-teleology [:no-direction :no-progress :no-culmination]}
    
    :shadow-realms
    {:dark-mirrors [:inverse-self :reversed-world :corrupted-reflection]
     :liminal-zones [:between-states :threshold-spaces :transition-voids]
     :forgotten-dimensions [:lost-axes :collapsed-spaces :hidden-voids]}
    
    :temporal-dissolution
    {:time-death [:chronos-end :moment-collapse :duration-void]
     :memory-erasure [:forgotten-past :amnesia-universal :record-destruction]
     :future-cancellation [:tomorrow-never :prophecy-void :hope-negation]}
    
    :consciousness-unraveling
    {:mind-dissolution [:thought-decay :awareness-fade :identity-erosion]
     :perception-void [:sense-negation :reality-doubt :interpretation-failure]
     :will-annihilation [:desire-death :agency-loss :intention-vacuum]}
    
    :material-negation
    {:matter-void [:particle-absence :energy-zero :mass-negation]
     :form-dissolution [:shape-loss :structure-collapse :organization-entropy]
     :substance-absence [:essential-void :quality-negation :being-erasure]}
    
    :language-unmaking
    {:word-death [:meaning-drain :symbol-corruption :name-erasure]
     :communication-void [:message-lost :understanding-gap :connection-severed]
     :silence-absolute [:sound-absence :voice-lost :echo-death]}
    
    :relational-void
    {:connection-absence [:isolation-total :bond-broken :network-collapse]
     :love-negation [:affection-void :attachment-severed :intimacy-impossible]
     :community-dissolution [:group-scatter :society-collapse :species-alone]}
    
    :sacred-profaned
    {:divine-absence [:god-dead :heaven-empty :prayer-unheard]
     :ritual-meaningless [:ceremony-empty :symbol-powerless :magic-failed]
     :faith-destroyed [:belief-shattered :hope-murdered :transcendence-denied]}
    
    :beauty-corrupted
    {:aesthetic-void [:ugliness-universal :art-meaningless :sublime-negated]
     :pleasure-absent [:joy-impossible :sensation-numb :ecstasy-forbidden]
     :harmony-broken [:discord-eternal :balance-lost :rhythm-disrupted]}
    
    :knowledge-unlearned
    {:wisdom-void [:understanding-lost :truth-unknowable :learning-futile]
     :information-decay [:data-corruption :archive-destruction :pattern-loss]
     :mystery-absolute [:unknown-forever :question-unanswered :explanation-absent]}}})

(defn traverse-void
  "Navigate the void ontology based on entropy seed"
  [seed depth]
  (let [path-choices (atom [])]
    (walk/prewalk
     (fn [node]
       (when (and (map? node) (< (count @path-choices) depth))
         (let [keys (vec (keys node))
               choice (nth keys (mod seed (count keys)))]
           (swap! path-choices conj choice)))
       node)
     void-tree)
    @path-choices))

(defn void-manifestation
  "Generate a void manifestation from entropy"
  [entropy-seed]
  (let [path (traverse-void entropy-seed 5)
        depth-names ["realm" "domain" "aspect" "quality" "essence"]]
    {:path path
     :manifestation (zipmap depth-names path)
     :seed entropy-seed
     :timestamp (System/currentTimeMillis)}))

(defn chaos-transform
  "Transform data through void ontology"
  [data entropy]
  (let [void-path (traverse-void entropy 3)
        transform-fn (fn [x]
                       (let [val (mod (bit-xor x entropy) 256)
                             bits (mod entropy 8)]
                         (bit-or (bit-shift-left val bits)
                                 (bit-shift-right val (- 8 bits)))))]
    {:original data
     :transformed (map transform-fn data)
     :void-path void-path
     :entropy entropy}))

(defn generate-void-poetry
  "Generate nihilistic poetry from void paths"
  [manifestation]
  (let [{:keys [realm domain aspect]} (:manifestation manifestation)]
    (str "In the " (name realm) " of " (name domain) ",\n"
         "Where " (name aspect) " reigns eternal,\n"
         "All meaning dissolves to nothing,\n"
         "And entropy claims its throne.")))