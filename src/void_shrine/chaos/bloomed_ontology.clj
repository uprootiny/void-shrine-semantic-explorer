(ns void-shrine.chaos.bloomed-ontology
  (:require [clojure.walk :as walk]
            [clojure.string :as str]))

(def infinite-void-tree
  {:void
   {:primordial-absence
    {:pre-being
     {:never-was
      {:unborn-possibility [:rejected-timeline :canceled-future :aborted-potential :stillborn-dream :unmade-choice]
       :anti-genesis [:reverse-creation :uncreation-force :genesis-negation :birth-prevention :origin-denial]
       :zero-point [:absolute-null :mathematical-void :emptyset-reality :coordinate-absence :reference-negation]}
      :erased-memory
      {:forgotten-past [:lost-history :deleted-records :amnesia-total :memory-void :recollection-death]
       :unremembered-events [:phantom-moments :ghostly-occurrences :trace-erasure :impression-void :echo-silence]
       :obliterated-identity [:self-deletion :ego-erasure :name-forgetting :recognition-loss :being-anonymity]}
      :collapsed-beginning
      {:imploded-start [:inward-collapse :genesis-implosion :origin-crush :beginning-fold :start-compression]
       :inverted-creation [:backwards-making :reverse-formation :undoing-genesis :creation-inversion :making-unmade]
       :nullified-origin [:source-void :root-negation :ground-absence :foundation-empty :basis-null]}}
     
     :negative-space
     {:hollow-forms
      {:shadow-matter [:dark-substance :anti-material :void-mass :negative-density :absence-weight]
       :absent-presence [:here-not-here :present-absence :being-unbeing :existence-void :reality-gap]
       :ghost-geometry [:phantom-shapes :void-angles :empty-dimensions :absent-lines :null-curves]}
      :inverse-reality
      {:mirror-void [:reflection-absence :image-negation :opposite-nothing :reverse-empty :backwards-null]
       :backwards-time [:temporal-reversal :chronos-inversion :time-negation :duration-backwards :moment-reverse]
       :inside-out-space [:spatial-inversion :dimension-flip :geometry-inside :space-reversed :location-inverted]}
      :gap-between
      {:interstitial-nothing [:between-void :gap-essence :interval-empty :spacing-null :separation-absence]
       :liminal-absence [:threshold-void :boundary-empty :edge-nothing :margin-null :border-absence]
       :threshold-vacuum [:doorway-void :passage-empty :transition-null :crossing-absence :portal-nothing]}}
     
     :pure-negation
     {:absolute-no
      {:total-denial [:complete-rejection :absolute-refusal :ultimate-negation :perfect-no :final-denial]
       :universal-rejection [:cosmos-refuses :reality-denies :existence-rejects :being-says-no :all-refuses]
       :complete-refusal [:total-unwillingness :absolute-decline :perfect-rejection :ultimate-refusal :final-no]}
      :cancellation
      {:self-erasure [:auto-deletion :self-negation :being-cancel :existence-revoke :reality-delete]
       :mutual-annihilation [:bilateral-destruction :reciprocal-void :dual-erasure :paired-negation :twin-destruction]
       :recursive-deletion [:loop-erasure :spiral-negation :cyclical-void :repetitive-destruction :iterative-annihilation]}
      :undoing
      {:retroactive-nonexistence [:backwards-never :past-erasure :history-undone :timeline-deleted :was-never]
       :causal-collapse [:cause-void :effect-negation :chain-break :sequence-shatter :link-destruction]
       :timeline-unraveling [:chronos-disintegration :history-dissolution :past-crumble :future-scatter :present-fragment]}}}
   
   :entropic-cascade
   {:heat-death
    {:maximum-entropy
     {:information-dissolution [:data-decay :pattern-loss :order-destruction :structure-collapse :organization-death]
      :pattern-decay [:design-rot :form-dissolution :shape-deterioration :configuration-breakdown :arrangement-decay]
      :structure-loss [:framework-collapse :architecture-dissolution :support-failure :foundation-crumble :skeleton-disintegration]}
     :cold-forever
     {:infinite-stillness [:eternal-motionless :perpetual-static :endless-immobile :permanent-frozen :continuous-halt]
      :frozen-eternity [:time-ice :moment-crystallized :duration-solid :temporal-frost :chronos-frozen]
      :motionless-end [:final-still :ultimate-halt :terminal-pause :conclusive-stop :definitive-freeze]}
     :final-equilibrium
     {:perfect-balance [:ideal-stasis :complete-equilibrium :absolute-stability :total-balance :ultimate-poise]
      :dead-symmetry [:lifeless-proportion :sterile-harmony :vacant-balance :empty-symmetry :null-proportion]
      :static-forever [:eternal-unchanging :perpetual-sameness :endless-identical :continuous-static :permanent-fixed]}}
    
    :dissolution-patterns
    {:gradual-fade
     {:slow-dimming [:gentle-darkening :soft-diminishment :quiet-reduction :mild-lessening :tender-decrease]
      :gentle-erosion [:kind-wearing :soft-deterioration :mild-weathering :tender-dissolution :gentle-decay]
      :patient-decay [:waiting-rot :slow-decomposition :gradual-breakdown :measured-deterioration :unhurried-dissolution]}
     :sudden-collapse
     {:instant-void [:immediate-nothing :sudden-absence :abrupt-emptiness :sharp-null :quick-vacuum]
      :catastrophic-end [:disaster-termination :calamity-finish :tragedy-conclusion :crisis-finale :doom-ending]
      :flash-termination [:lightning-end :instant-stop :split-second-finale :moment-termination :blink-conclusion]}
     :oscillating-death
     {:dying-cycles [:mortal-repetition :fatal-rhythm :deadly-pattern :terminal-loop :ending-cycle]
      :recursive-end [:looping-termination :circular-conclusion :spiral-finale :repetitive-ending :cyclical-death]
      :repeating-finale [:recurring-end :iterative-conclusion :duplicate-termination :multiple-finale :echoing-ending]}}
    
    :chaos-mathematics
    {:strange-attractors
     {:void-spirals [:emptiness-helixes :null-coils :absence-twists :vacuum-corkscrews :nothing-whirls]
      :nihil-fractals [:meaningless-patterns :purposeless-geometries :senseless-shapes :pointless-forms :absurd-structures]
      :death-curves [:mortality-lines :terminal-arcs :ending-trajectories :final-paths :concluding-traces]}
     :infinite-regression
     {:endless-falling [:perpetual-descent :eternal-dropping :continuous-plummet :unending-tumble :permanent-fall]
      :bottomless-descent [:fathomless-drop :measureless-fall :boundless-plunge :limitless-descent :infinite-sink]
      :forever-inward [:eternal-interior :perpetual-inside :endless-internal :continuous-within :permanent-inner]}
     :broken-symmetries
     {:shattered-order [:fractured-arrangement :splintered-organization :crushed-system :destroyed-pattern :demolished-structure]
      :corrupted-patterns [:tainted-designs :polluted-forms :infected-shapes :contaminated-structures :poisoned-arrangements]
      :failed-structure [:collapsed-framework :broken-architecture :ruined-foundation :damaged-skeleton :compromised-support]}}}
   
   :nihil-philosophy
   {:meaning-absence
    {:purpose-void
     {:pointless-existence [:senseless-being :meaningless-presence :absurd-reality :futile-life :useless-being]
      :arbitrary-being [:random-existence :chance-presence :accidental-reality :coincidental-being :haphazard-existence]
      :random-presence [:chaotic-being :unpredictable-existence :erratic-reality :irregular-presence :unstable-being]}
     :value-vacuum
     {:worthless-all [:valueless-everything :meaningless-total :insignificant-complete :trivial-absolute :unimportant-universal]
      :empty-significance [:hollow-meaning :vacant-importance :null-relevance :absent-consequence :void-impact]
      :hollow-import [:empty-weight :vacant-gravity :null-substance :absent-depth :void-density]}
     :truth-negation
     {:all-lies [:everything-false :universal-deception :total-untruth :complete-fabrication :absolute-falsehood]
      :nothing-real [:all-illusion :everything-fake :universal-pretense :total-simulation :complete-mirage]
      :false-foundation [:untrue-basis :deceptive-ground :illusory-root :fake-source :pretend-origin]}}
    
    :existential-horror
    {:cosmic-indifference
     {:universe-doesnt-care [:cosmos-apathy :reality-disinterest :existence-unconcern :being-indifference :space-carelessness]
      :cold-stars [:frozen-suns :icy-lights :frigid-beacons :chilled-fires :arctic-flames]
      :silent-gods [:mute-deities :voiceless-divinity :speechless-powers :wordless-creators :soundless-eternals]}
     :personal-insignificance
     {:dust-mote [:particle-nothing :speck-meaningless :grain-irrelevant :fragment-worthless :bit-insignificant]
      :forgotten-already [:pre-erased :never-remembered :instant-amnesia :immediate-oblivion :rapid-disappearance]
      :never-mattered [:always-irrelevant :eternally-meaningless :perpetually-insignificant :continuously-worthless :permanently-trivial]}
     :time-terror
     {:infinite-before [:endless-past :eternal-yesterday :boundless-history :measureless-bygone :limitless-previous]
      :eternal-after [:endless-future :infinite-tomorrow :boundless-ahead :measureless-coming :limitless-next]
      :momentary-now [:fleeting-present :brief-instant :transient-current :temporary-immediate :passing-now]}}
    
    :anti-teleology
    {:no-direction
     {:aimless-drift [:purposeless-float :meaningless-wander :senseless-roam :pointless-meander :directionless-flow]
      :random-walk [:chance-steps :arbitrary-path :accidental-journey :coincidental-route :haphazard-progression]
      :purposeless-motion [:meaningless-movement :senseless-travel :pointless-progress :absurd-advancement :futile-locomotion]}
     :no-progress
     {:circular-nothing [:round-void :cyclical-null :orbital-empty :rotational-absence :spinning-vacuum]
      :futile-advance [:useless-progress :meaningless-forward :senseless-ahead :pointless-improvement :absurd-development]
      :illusory-development [:fake-progress :pretend-advancement :false-improvement :deceptive-growth :misleading-evolution]}
     :no-culmination
     {:never-arriving [:eternally-approaching :perpetually-nearing :continuously-coming :endlessly-reaching :permanently-almost]
      :endless-journey [:infinite-travel :boundless-voyage :measureless-expedition :limitless-trek :eternal-passage]
      :destination-void [:goal-empty :target-null :endpoint-absent :finish-void :completion-nothing]}}}
   
   :shadow-realms
   {:dark-mirrors
    {:inverse-self
     {:anti-you [:opposite-being :contrary-existence :reverse-identity :backwards-self :negated-you]
      :shadow-twin [:dark-double :void-duplicate :null-sibling :absent-pair :empty-match]
      :negative-identity [:minus-self :subtracted-being :reduced-existence :diminished-identity :lessened-you]}
     :reversed-world
     {:backwards-reality [:inverted-existence :opposite-universe :contrary-cosmos :reverse-realm :flipped-reality]
      :flipped-universe [:turned-cosmos :rotated-reality :inverted-existence :reversed-space :backwards-dimension]
      :mirror-dimension [:reflected-realm :echoed-reality :duplicated-space :copied-existence :repeated-universe]}
     :corrupted-reflection
     {:twisted-image [:distorted-reflection :warped-mirror :bent-echo :curved-duplicate :deformed-copy]
      :broken-mirror [:shattered-reflection :fractured-image :cracked-duplicate :splintered-echo :fragmented-copy]
      :distorted-echo [:warped-reverberation :twisted-response :bent-reply :curved-answer :deformed-return]}}
    
    :liminal-zones
    {:between-states
     {:neither-nor [:not-this-not-that :absent-both :void-either :null-choice :empty-option]
      :both-and-not [:simultaneous-absence :concurrent-void :parallel-nothing :dual-empty :twin-null]
      :suspended-between [:hanging-middle :floating-center :hovering-midst :dangling-between :drifting-among]}
     :threshold-spaces
     {:door-to-nothing [:portal-void :entrance-empty :gateway-null :opening-absent :passage-nothing]
      :bridge-over-void [:span-across-empty :crossing-over-null :passage-through-absent :journey-via-nothing :travel-across-void]
      :gate-without-destination [:entrance-nowhere :portal-to-nothing :doorway-to-void :opening-to-empty :passage-to-null]}
     :transition-voids
     {:becoming-nothing [:transforming-void :changing-null :evolving-empty :developing-absent :growing-nothing]
      :unbecoming-something [:losing-form :dissolving-shape :fading-structure :disappearing-substance :evaporating-matter]
      :transforming-into-absence [:morphing-void :shifting-null :converting-empty :transmuting-absent :metamorphosing-nothing]}}
    
    :forgotten-dimensions
    {:lost-axes
     {:disappeared-direction [:vanished-vector :erased-orientation :deleted-bearing :removed-heading :eliminated-course]
      :erased-coordinate [:deleted-position :removed-location :eliminated-point :obliterated-place :annihilated-spot]
      :forgotten-vector [:lost-direction :misplaced-orientation :abandoned-bearing :discarded-heading :neglected-course]}
     :collapsed-spaces
     {:folded-reality [:creased-existence :bent-space :curved-dimension :twisted-realm :warped-reality]
      :compressed-universe [:squeezed-cosmos :crushed-reality :compacted-existence :condensed-space :pressed-dimension]
      :imploded-dimension [:inward-collapsed-space :self-destructed-realm :internally-crushed-dimension :auto-compressed-reality :self-folded-existence]}
     :hidden-voids
     {:secret-nothing [:concealed-void :hidden-empty :masked-null :disguised-absent :camouflaged-nothing]
      :concealed-absence [:hidden-void :secret-empty :masked-null :disguised-nothing :camouflaged-absent]
      :masked-empty [:disguised-void :concealed-null :hidden-absent :secret-nothing :camouflaged-empty]}}}
   
   :temporal-dissolution
   {:time-death
    {:chronos-end
     {:clock-stop [:timepiece-halt :chronometer-freeze :timer-cease :watch-still :hourglass-pause]
      :calendar-blank [:date-empty :schedule-void :appointment-null :event-absent :meeting-nothing]
      :history-erased [:past-deleted :bygone-removed :yesterday-obliterated :previous-eliminated :former-annihilated]}
     :moment-collapse
     {:now-never [:present-absent :current-void :immediate-null :instant-empty :moment-nothing]
      :present-absent [:now-void :current-null :immediate-empty :instant-nothing :moment-absent]
      :instant-eternal [:moment-forever :split-second-infinity :flash-eternity :blink-endless :pulse-permanent]}
     :duration-void
     {:lengthless-time [:measure-absent-duration :span-void-period :extent-null-interval :stretch-empty-time :range-nothing-duration]
      :pointless-duration [:meaningless-span :senseless-period :absurd-interval :futile-stretch :useless-range]
      :empty-interval [:void-gap :null-space :absent-pause :nothing-break :empty-intermission]}}
    
    :memory-erasure
    {:forgotten-past
     {:never-happened [:un-occurred :non-events :anti-history :reverse-occurrence :negative-happening]
      :unremembered-events [:lost-occurrences :misplaced-happenings :abandoned-incidents :discarded-episodes :forgotten-circumstances]
      :lost-history [:misplaced-past :abandoned-bygone :discarded-yesterday :forgotten-previous :neglected-former]}
     :amnesia-universal
     {:collective-forgetting [:group-amnesia :mass-memory-loss :shared-oblivion :communal-forgetting :social-amnesia]
      :species-amnesia [:human-forgetting :mankind-memory-loss :civilization-oblivion :cultural-amnesia :evolutionary-forgetting]
      :cosmic-memory-loss [:universe-forgetting :reality-amnesia :existence-memory-void :being-oblivion :cosmos-forgetting]}
     :record-destruction
     {:burned-archives [:incinerated-records :cremated-documents :charred-files :scorched-papers :flamed-histories]
      :deleted-data [:erased-information :removed-files :eliminated-records :obliterated-databases :annihilated-archives]
      :erased-evidence [:deleted-proof :removed-traces :eliminated-signs :obliterated-markers :annihilated-indicators]}}
    
    :future-cancellation
    {:tomorrow-never
     {:prevented-future [:blocked-tomorrow :obstructed-ahead :hindered-coming :impeded-next :stopped-forward]
      :blocked-potential [:obstructed-possibility :hindered-capability :impeded-capacity :prevented-ability :stopped-power]
      :closed-possibility [:shut-potential :sealed-opportunity :locked-chance :barred-prospect :blocked-opening]}
     :prophecy-void
     {:no-prediction [:absent-forecast :void-prophecy :null-prognostication :empty-prediction :nothing-foreseen]
      :unknown-forever [:eternal-mystery :perpetual-secret :continuous-enigma :permanent-unknown :endless-hidden]
      :unseeable-ahead [:invisible-future :hidden-tomorrow :concealed-coming :masked-next :disguised-forward]}
     :hope-negation
     {:expectation-death [:anticipation-murder :prospect-killing :outlook-assassination :future-slaughter :tomorrow-execution]
      :dream-dissolution [:vision-melting :aspiration-dissolving :ambition-evaporating :goal-vanishing :aim-disappearing]
      :wish-nullification [:desire-voiding :want-canceling :longing-negating :craving-eliminating :yearning-obliterating]}}}
   
   :consciousness-unraveling
   {:mind-dissolution
    {:thought-decay
     {:idea-rot [:concept-putrefaction :notion-decomposition :thought-corruption :mental-decay :cognitive-deterioration]
      :concept-corruption [:idea-contamination :notion-pollution :thought-infection :mental-taint :cognitive-defilement]
      :mental-entropy [:mind-chaos :cognitive-disorder :intellectual-randomness :psychological-confusion :consciousness-turbulence]}
     :awareness-fade
     {:dimming-consciousness [:darkening-awareness :shadow-mind :twilight-cognition :dusk-perception :evening-consciousness]
      :attention-scatter [:focus-fragment :concentration-disperse :mindfulness-shatter :awareness-splinter :consciousness-spread]
      :focus-loss [:concentration-disappearance :attention-vanishing :mindfulness-evaporation :awareness-dissolution :consciousness-fading]}
     :identity-erosion
     {:self-forgetting [:ego-amnesia :identity-oblivion :selfhood-memory-loss :personality-forgetting :character-amnesia]
      :i-dissolution [:self-melting :ego-evaporation :identity-vaporization :selfhood-sublimation :personality-dissolution]
      :ego-death [:self-murder :identity-suicide :selfhood-assassination :personality-execution :character-termination]}}
    
    :perception-void
    {:sense-negation
     {:blind-deaf-numb [:sightless-soundless-feelingless :vision-hearing-touch-void :sight-sound-sensation-absent :eyes-ears-skin-dead :see-hear-feel-nothing]
      :input-absence [:stimulus-void :signal-null :data-absent :information-empty :sensation-nothing]
      :signal-lost [:transmission-failed :communication-broken :message-dropped :data-corrupt :information-garbled]}
     :reality-doubt
     {:nothing-certain [:everything-questionable :all-doubtful :universal-uncertainty :total-skepticism :complete-doubt]
      :all-illusion [:everything-fake :universal-deception :total-mirage :complete-fantasy :absolute-delusion]
      :experience-false [:sensation-lie :perception-deception :feeling-fraud :awareness-fake :consciousness-pretense]}
     :interpretation-failure
     {:meaning-lost [:significance-vanished :import-disappeared :relevance-gone :consequence-absent :impact-void]
      :understanding-impossible [:comprehension-blocked :grasp-prevented :insight-denied :clarity-forbidden :knowledge-impossible]
      :comprehension-void [:understanding-empty :grasp-null :insight-absent :clarity-nothing :knowledge-void]}}
    
    :will-annihilation
    {:desire-death
     {:want-nothing [:need-absent :crave-void :wish-null :long-empty :yearn-nothing]
      :need-absence [:requirement-void :necessity-null :demand-absent :want-empty :desire-nothing]
      :craving-void [:hunger-null :thirst-absent :appetite-empty :urge-nothing :drive-void]}
     :agency-loss
     {:cannot-choose [:choice-impossible :decision-blocked :selection-denied :option-forbidden :alternative-prevented]
      :no-control [:power-absent :influence-void :command-null :authority-empty :dominion-nothing]
      :puppet-strings-cut [:marionette-freed :doll-uncontrolled :figure-autonomous :toy-independent :model-self-directed]}
     :intention-vacuum
     {:purpose-gone [:aim-vanished :goal-disappeared :objective-lost :target-absent :end-void]
      :goal-absent [:target-void :aim-null :objective-empty :purpose-nothing :end-absent]
      :aim-lost [:direction-misplaced :bearing-forgotten :heading-abandoned :course-discarded :path-neglected]}}}
   
   :material-negation
   {:matter-void
    {:particle-absence
     {:empty-quantum [:void-subatomic :null-elementary :absent-fundamental :nothing-basic :vacuum-primary]
      :vacant-field [:empty-domain :null-realm :absent-territory :nothing-space :void-region]
      :null-wave [:absent-oscillation :void-vibration :empty-frequency :nothing-amplitude :null-wavelength]}
     :energy-zero
     {:power-drained [:force-exhausted :strength-depleted :vigor-spent :potency-consumed :might-emptied]
      :force-absent [:power-void :energy-null :strength-empty :vigor-nothing :potency-absent]
      :potential-none [:possibility-zero :capability-null :capacity-absent :ability-void :power-empty]}
     :mass-negation
     {:weight-nothing [:heaviness-absent :gravity-void :mass-null :density-empty :substance-nothing]
      :substance-gone [:matter-vanished :material-disappeared :stuff-evaporated :essence-dissolved :content-vaporized]
      :density-zero [:thickness-null :concentration-absent :compactness-void :solidity-empty :firmness-nothing]}}
    
    :form-dissolution
    {:shape-loss
     {:boundary-blur [:edge-soften :outline-fade :perimeter-dissolve :border-melt :limit-evaporate]
      :edge-fade [:rim-diminish :margin-dissolve :brink-evaporate :verge-vanish :periphery-disappear]
      :contour-gone [:outline-vanished :silhouette-disappeared :profile-evaporated :form-dissolved :shape-vaporized]}
     :structure-collapse
     {:framework-fail [:skeleton-break :foundation-crumble :support-buckle :architecture-fall :infrastructure-crash]
      :architecture-crumble [:building-collapse :construction-fall :edifice-tumble :structure-topple :framework-crash]
      :pattern-break [:design-shatter :arrangement-fragment :organization-splinter :order-crack :system-fracture]}
     :organization-entropy
     {:order-decay [:arrangement-rot :organization-deteriorate :system-decompose :structure-putrefy :pattern-corrupt]
      :system-scatter [:organization-disperse :arrangement-spread :order-fragment :structure-splinter :pattern-shatter]
      :arrangement-random [:organization-chaotic :order-confused :structure-disordered :pattern-scrambled :system-mixed]}}
    
    :substance-absence
    {:essential-void
     {:core-empty [:heart-hollow :center-vacant :nucleus-void :middle-null :interior-absent]
      :heart-hollow [:core-void :center-empty :middle-null :nucleus-absent :interior-nothing]
      :center-absent [:middle-void :core-null :heart-empty :nucleus-nothing :interior-absent]}
     :quality-negation
     {:property-lost [:attribute-vanished :characteristic-disappeared :trait-evaporated :feature-dissolved :aspect-vaporized]
      :attribute-gone [:property-vanished :quality-disappeared :characteristic-evaporated :trait-dissolved :feature-vaporized]
      :character-void [:nature-empty :essence-null :personality-absent :identity-nothing :being-void]}
     :being-erasure
     {:existence-revoked [:reality-canceled :being-nullified :presence-voided :actuality-eliminated :entity-obliterated]
      :presence-cancelled [:being-revoked :existence-nullified :reality-voided :actuality-eliminated :entity-obliterated]
      :reality-denied [:existence-refused :being-rejected :presence-declined :actuality-spurned :entity-dismissed]}}}
   
   :language-unmaking
   {:word-death
    {:meaning-drain
     {:semantic-void [:significance-empty :sense-null :import-absent :relevance-nothing :consequence-void]
      :definition-lost [:meaning-vanished :sense-disappeared :significance-evaporated :import-dissolved :relevance-vaporized]
      :sense-absent [:meaning-void :significance-null :import-empty :relevance-nothing :consequence-absent]}
     :symbol-corruption
     {:sign-broken [:mark-shattered :symbol-fractured :token-cracked :emblem-split :icon-damaged]
      :letter-meaningless [:character-senseless :glyph-absurd :symbol-pointless :mark-futile :sign-useless]
      :glyph-empty [:character-void :letter-null :symbol-absent :mark-nothing :sign-empty]}
     :name-erasure
     {:unnamed-all [:everything-nameless :universal-anonymous :total-untitled :complete-unlabeled :absolute-undesignated]
      :label-lost [:tag-vanished :title-disappeared :name-evaporated :designation-dissolved :identifier-vaporized]
      :title-forgotten [:name-abandoned :label-discarded :designation-neglected :identifier-overlooked :tag-ignored]}}
    
    :communication-void
    {:message-lost
     {:signal-noise [:transmission-static :communication-interference :message-distortion :data-corruption :information-scramble]
      :transmission-failed [:message-dropped :signal-lost :communication-broken :data-corrupt :information-garbled]
      :reception-null [:receiving-void :intake-empty :absorption-absent :acceptance-nothing :acquisition-null]}
     :understanding-gap
     {:comprehension-impossible [:grasp-blocked :insight-prevented :clarity-denied :knowledge-forbidden :awareness-impossible]
      :translation-failed [:conversion-broken :interpretation-corrupted :rendering-damaged :transformation-flawed :adaptation-failed]
      :interpretation-void [:understanding-empty :comprehension-null :grasp-absent :insight-nothing :clarity-void]}
     :connection-severed
     {:link-broken [:bond-snapped :tie-cut :connection-severed :relationship-fractured :association-destroyed]
      :bridge-burned [:span-destroyed :crossing-eliminated :passage-obliterated :connection-annihilated :link-incinerated]
      :channel-closed [:pathway-blocked :route-sealed :avenue-shut :conduit-stopped :passage-barred]}}
    
    :silence-absolute
    {:sound-absence
     {:perfect-quiet [:ideal-silence :complete-hush :total-stillness :absolute-calm :ultimate-peace]
      :total-mute [:complete-silent :absolute-soundless :perfect-noiseless :ultimate-quiet :ideal-hushed]
      :complete-hush [:total-silence :absolute-quiet :perfect-still :ultimate-calm :ideal-peaceful]}
     :voice-lost
     {:cannot-speak [:speech-impossible :voice-blocked :words-denied :expression-prevented :utterance-forbidden]
      :no-expression [:communication-absent :articulation-void :verbalization-null :vocalization-empty :pronunciation-nothing]
      :utterance-impossible [:speech-blocked :voice-prevented :words-denied :expression-forbidden :articulation-impossible]}
     :echo-death
     {:reverb-none [:reverberation-absent :reflection-void :bounce-null :return-empty :response-nothing]
      :response-absent [:reply-void :answer-null :reaction-empty :feedback-nothing :comeback-absent]
      :call-unanswered [:shout-ignored :cry-unheard :summons-rejected :invitation-declined :appeal-dismissed]}}}
   
   :relational-void
   {:connection-absence
    {:isolation-total
     {:alone-forever [:eternally-solitary :perpetually-isolated :continuously-separate :permanently-apart :endlessly-detached]
      :cut-off-complete [:totally-severed :absolutely-disconnected :perfectly-isolated :utterly-separated :completely-detached]
      :separated-absolute [:perfectly-apart :utterly-detached :completely-isolated :totally-disconnected :absolutely-severed]}
     :bond-broken
     {:link-severed [:connection-cut :tie-broken :relationship-fractured :association-destroyed :partnership-shattered]
      :tie-cut [:bond-severed :link-broken :connection-snapped :relationship-chopped :association-sliced]
      :relationship-null [:connection-void :bond-empty :tie-absent :link-nothing :association-null]}
     :network-collapse
     {:web-torn [:net-ripped :mesh-shredded :grid-destroyed :matrix-shattered :system-demolished]
      :grid-failed [:network-collapsed :system-crashed :structure-failed :framework-broken :infrastructure-destroyed]
      :system-disconnected [:network-unplugged :grid-offline :web-severed :matrix-isolated :structure-separated]}}
    
    :love-negation
    {:affection-void
     {:feeling-absent [:emotion-void :sentiment-null :sensation-empty :passion-nothing :ardor-absent]
      :care-none [:concern-absent :regard-void :consideration-null :attention-empty :interest-nothing]
      :warmth-gone [:heat-vanished :temperature-dropped :glow-extinguished :fire-died :flame-extinguished]}
     :attachment-severed
     {:bond-broken [:connection-snapped :link-fractured :tie-severed :relationship-destroyed :association-shattered]
      :connection-cut [:bond-sliced :link-chopped :tie-severed :relationship-cut :association-divided]
      :tie-released [:bond-freed :connection-loosened :link-untied :relationship-liberated :association-unbound]}
     :intimacy-impossible
     {:distance-infinite [:separation-boundless :gap-measureless :space-endless :interval-limitless :span-eternal]
      :closeness-denied [:nearness-refused :proximity-rejected :intimacy-declined :togetherness-spurned :unity-dismissed]
      :touch-forbidden [:contact-banned :connection-prohibited :meeting-disallowed :joining-forbidden :union-banned]}}
    
    :community-dissolution
    {:group-scatter
     {:collective-broken [:assembly-shattered :gathering-destroyed :congregation-demolished :crowd-dispersed :mob-fragmented]
      :unity-shattered [:oneness-destroyed :wholeness-broken :completeness-fractured :solidarity-demolished :togetherness-shattered]
      :together-never [:united-impossible :joined-forbidden :connected-denied :linked-prevented :bonded-blocked]}
     :society-collapse
     {:structure-gone [:organization-vanished :system-disappeared :framework-evaporated :institution-dissolved :establishment-vaporized]
      :order-lost [:arrangement-misplaced :organization-forgotten :system-abandoned :structure-discarded :pattern-neglected]
      :civilization-end [:culture-terminated :society-concluded :community-finished :humanity-ended :civilization-stopped]}
     :species-alone
     {:last-one [:final-individual :ultimate-survivor :concluding-being :terminal-entity :ending-creature]
      :only-survivor [:sole-remaining :single-left :lone-enduring :solitary-lasting :individual-persisting]
      :solitary-forever [:eternally-alone :perpetually-isolated :continuously-separate :permanently-detached :endlessly-solitary]}}}
   
   :sacred-profaned
   {:divine-absence
    {:god-dead
     {:deity-gone [:divinity-vanished :godhead-disappeared :supreme-evaporated :almighty-dissolved :eternal-vaporized]
      :creator-absent [:maker-void :architect-null :designer-empty :builder-nothing :constructor-absent]
      :supreme-void [:ultimate-empty :highest-null :greatest-absent :paramount-nothing :foremost-void]}
     :heaven-empty
     {:paradise-lost [:eden-vanished :bliss-disappeared :utopia-evaporated :nirvana-dissolved :elysium-vaporized]
      :afterlife-null [:beyond-void :hereafter-empty :eternity-null :immortality-absent :perpetuity-nothing]
      :eternity-vacant [:forever-empty :infinity-void :endless-null :perpetual-absent :continuous-nothing]}
     :prayer-unheard
     {:call-ignored [:plea-dismissed :request-rejected :appeal-spurned :petition-declined :supplication-refused]
      :plea-wasted [:appeal-futile :request-useless :petition-pointless :supplication-meaningless :prayer-senseless]
      :worship-pointless [:adoration-meaningless :reverence-senseless :devotion-absurd :veneration-futile :praise-useless]}}
    
    :ritual-meaningless
    {:ceremony-empty
     {:rite-hollow [:ritual-vacant :ceremony-void :observance-null :service-empty :celebration-nothing]
      :practice-pointless [:custom-meaningless :tradition-senseless :habit-absurd :routine-futile :procedure-useless]
      :tradition-dead [:custom-lifeless :practice-deceased :ritual-expired :ceremony-perished :observance-defunct]}
     :symbol-powerless
     {:sign-impotent [:mark-weak :symbol-feeble :token-frail :emblem-ineffective :icon-useless]
      :token-worthless [:symbol-valueless :sign-meaningless :mark-insignificant :emblem-trivial :icon-unimportant]
      :icon-empty [:symbol-void :sign-null :mark-absent :token-nothing :emblem-empty]}
     :magic-failed
     {:spell-broken [:incantation-shattered :enchantment-fractured :charm-cracked :hex-destroyed :curse-demolished]
      :power-gone [:force-vanished :energy-disappeared :strength-evaporated :might-dissolved :potency-vaporized]
      :enchantment-null [:magic-void :spell-empty :charm-absent :incantation-nothing :sorcery-null]}}
    
    :faith-destroyed
    {:belief-shattered
     {:trust-broken [:confidence-fractured :faith-cracked :belief-destroyed :conviction-shattered :certainty-demolished]
      :confidence-lost [:assurance-vanished :certainty-disappeared :conviction-evaporated :faith-dissolved :trust-vaporized]
      :conviction-gone [:belief-vanished :faith-disappeared :certainty-evaporated :confidence-dissolved :trust-vaporized]}
     :hope-murdered
     {:expectation-dead [:anticipation-killed :prospect-slaughtered :outlook-assassinated :future-murdered :tomorrow-executed]
      :optimism-killed [:positivity-murdered :cheerfulness-slaughtered :brightness-assassinated :enthusiasm-executed :joy-terminated]
      :future-dark [:tomorrow-black :ahead-shadow :coming-night :next-darkness :forward-gloom]}
     :transcendence-denied
     {:stuck-here [:trapped-present :imprisoned-now :confined-here :locked-current :chained-immediate]
      :no-escape [:exit-blocked :freedom-denied :liberation-impossible :release-forbidden :deliverance-prevented]
      :material-prison [:physical-cage :corporeal-jail :bodily-confinement :flesh-trap :matter-bondage]}}}
   
   :beauty-corrupted
   {:aesthetic-void
    {:ugliness-universal
     {:all-repulsive [:everything-disgusting :universal-revolting :total-nauseating :complete-sickening :absolute-repugnant]
      :beauty-absent [:loveliness-void :attractiveness-null :charm-empty :grace-nothing :elegance-absent]
      :harmony-lost [:balance-misplaced :proportion-forgotten :symmetry-abandoned :order-discarded :unity-neglected]}
     :art-meaningless
     {:creation-pointless [:making-senseless :forming-absurd :shaping-futile :crafting-useless :building-meaningless]
      :expression-empty [:communication-void :articulation-null :manifestation-absent :demonstration-nothing :showing-empty]
      :form-without-content [:shape-substance-less :structure-meaning-void :design-significance-null :pattern-import-absent :arrangement-sense-nothing]}
     :sublime-negated
     {:wonder-dead [:awe-killed :amazement-murdered :astonishment-slaughtered :marvel-assassinated :miracle-executed]
      :awe-impossible [:wonder-blocked :amazement-prevented :astonishment-denied :reverence-forbidden :veneration-impossible]
      :majesty-gone [:grandeur-vanished :magnificence-disappeared :splendor-evaporated :glory-dissolved :nobility-vaporized]}}
    
    :pleasure-absent
    {:joy-impossible
     {:happiness-denied [:contentment-refused :satisfaction-rejected :delight-declined :bliss-spurned :euphoria-dismissed]
      :delight-dead [:pleasure-killed :enjoyment-murdered :satisfaction-slaughtered :contentment-assassinated :happiness-executed]
      :satisfaction-null [:contentment-void :fulfillment-empty :gratification-absent :pleasure-nothing :enjoyment-null]}
     :sensation-numb
     {:feeling-gone [:sensation-vanished :perception-disappeared :awareness-evaporated :consciousness-dissolved :experience-vaporized]
      :touch-dead [:contact-lifeless :connection-deceased :meeting-expired :joining-perished :union-defunct]
      :experience-flat [:sensation-level :feeling-even :perception-smooth :awareness-uniform :consciousness-constant]}
     :ecstasy-forbidden
     {:bliss-blocked [:rapture-obstructed :euphoria-hindered :ecstasy-prevented :transport-stopped :transcendence-barred]
      :rapture-denied [:bliss-refused :ecstasy-rejected :euphoria-declined :transport-spurned :transcendence-dismissed]
      :transport-impossible [:transcendence-blocked :elevation-prevented :ascension-denied :rising-forbidden :lifting-impossible]}}
    
    :harmony-broken
    {:discord-eternal
     {:clash-forever [:conflict-eternal :battle-perpetual :struggle-continuous :fight-permanent :war-endless]
      :conflict-permanent [:discord-eternal :strife-perpetual :tension-continuous :opposition-permanent :antagonism-endless]
      :tension-unresolved [:stress-permanent :strain-eternal :pressure-perpetual :force-continuous :pull-endless]}
     :balance-lost
     {:symmetry-broken [:proportion-shattered :balance-fractured :harmony-cracked :order-destroyed :unity-demolished]
      :proportion-wrong [:balance-incorrect :symmetry-false :harmony-mistaken :order-erroneous :unity-flawed]
      :equilibrium-gone [:balance-vanished :stability-disappeared :steadiness-evaporated :poise-dissolved :composure-vaporized]}
     :rhythm-disrupted
     {:beat-broken [:pulse-shattered :rhythm-fractured :tempo-cracked :meter-destroyed :cadence-demolished]
      :flow-interrupted [:stream-broken :current-severed :movement-stopped :progression-halted :advance-blocked]
      :pattern-chaotic [:design-confused :arrangement-disordered :organization-scrambled :structure-mixed :system-jumbled]}}}
   
   :knowledge-unlearned
   {:wisdom-void
    {:understanding-lost
     {:comprehension-failed [:grasp-unsuccessful :insight-defeated :clarity-thwarted :knowledge-frustrated :awareness-blocked]
      :insight-absent [:understanding-void :comprehension-null :grasp-empty :clarity-nothing :knowledge-absent]
      :clarity-gone [:understanding-vanished :comprehension-disappeared :insight-evaporated :knowledge-dissolved :awareness-vaporized]}
     :truth-unknowable
     {:reality-hidden [:truth-concealed :fact-masked :actuality-disguised :verity-camouflaged :certainty-obscured]
      :facts-obscured [:truth-clouded :reality-dimmed :actuality-shadowed :verity-darkened :certainty-veiled]
      :certainty-impossible [:sureness-blocked :confidence-prevented :assurance-denied :conviction-forbidden :belief-impossible]}
     :learning-futile
     {:knowledge-flees [:information-escapes :data-runs :facts-retreat :truth-withdraws :wisdom-departs]
      :education-pointless [:teaching-meaningless :instruction-senseless :training-absurd :schooling-futile :learning-useless]
      :growth-denied [:development-refused :progress-rejected :advancement-declined :improvement-spurned :evolution-dismissed]}}
    
    :information-decay
    {:data-corruption
     {:bits-flipped [:digits-inverted :binary-reversed :code-twisted :information-turned :data-switched]
      :signal-degraded [:transmission-deteriorated :communication-decayed :message-corrupted :data-spoiled :information-tainted]
      :message-scrambled [:communication-mixed :transmission-confused :signal-jumbled :data-disordered :information-chaotic]}
     :archive-destruction
     {:records-burned [:documents-incinerated :files-cremated :papers-charred :data-scorched :information-flamed]
      :history-lost [:past-misplaced :bygone-forgotten :yesterday-abandoned :previous-discarded :former-neglected]
      :memory-erased [:recollection-deleted :remembrance-removed :recall-eliminated :retention-obliterated :storage-annihilated]}
     :pattern-loss
     {:structure-forgotten [:organization-abandoned :arrangement-discarded :order-neglected :system-overlooked :pattern-ignored]
      :order-dissolved [:arrangement-melted :organization-evaporated :structure-vaporized :pattern-sublimated :system-dissipated]
      :system-chaos [:organization-confused :structure-disordered :arrangement-scrambled :pattern-mixed :order-jumbled]}}
    
    :mystery-absolute
    {:unknown-forever
     {:never-revealed [:eternally-hidden :perpetually-secret :continuously-concealed :permanently-obscured :endlessly-veiled]
      :always-hidden [:eternally-concealed :perpetually-obscured :continuously-secret :permanently-veiled :endlessly-disguised]
      :eternally-secret [:perpetually-hidden :continuously-concealed :permanently-obscured :endlessly-veiled :forever-disguised]}
     :question-unanswered
     {:riddle-permanent [:puzzle-eternal :enigma-perpetual :mystery-continuous :conundrum-permanent :paradox-endless]
      :puzzle-unsolved [:riddle-unresolved :enigma-unclarified :mystery-unexplained :conundrum-unanswered :paradox-unaddressed]
      :enigma-eternal [:mystery-perpetual :riddle-continuous :puzzle-permanent :conundrum-endless :paradox-eternal]}
     :explanation-absent
     {:reason-none [:cause-absent :purpose-void :motive-null :justification-empty :rationale-nothing]
      :cause-unknown [:reason-hidden :origin-secret :source-mysterious :root-concealed :beginning-obscured]
      :why-never [:reason-impossible :cause-unknowable :purpose-hidden :motive-secret :justification-mysterious]}}}}})