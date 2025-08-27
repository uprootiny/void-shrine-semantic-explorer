# Void Shrine: Pure Clojure Chaos System

## Architecture Overview

This is a pure Clojure implementation of a chaos-churning system that harvests entropy from quantum random sources and transforms it through void ontology patterns. The system uses advanced Clojure features including:

- **Specs & Validation**: Complete data validation with `clojure.spec.alpha`
- **Pattern Matching**: Advanced pattern matching with Meander
- **Lens Navigation**: Deep state navigation with Specter  
- **Monadic Error Handling**: Using Cats library for Either/Maybe monads
- **Schema Validation**: Prismatic schema for runtime validation
- **Transducers**: High-performance data transformation pipelines
- **Core.async**: Real-time entropy streaming
- **Advanced Macros**: Custom chaos mutation DSL

## Project Structure

```
src/
├── void_shrine/
│   ├── core.cljc              # Isomorphic reactive core
│   ├── entropy/
│   │   └── harvester.clj      # Quantum entropy harvesting
│   ├── chaos/
│   │   ├── ontology.clj       # Void ontology traversal
│   │   └── transformers.clj   # Advanced data transformations
│   └── web/
│       ├── server.clj         # Ring/Jetty server
│       └── ui.cljc           # Hiccup-based UI components
```

## Key Features

### 1. Quantum Entropy Harvesting
- Connects to multiple true random sources (ANU quantum, Random.org, HotBits)
- Real-time entropy mixing and chaos seed generation
- Async streaming with core.async channels

### 2. Void Ontology System  
- 120+ node ontology of nihilistic concepts
- Entropy-driven navigation through void manifestations
- Pattern-based poetry generation

### 3. Advanced Transformations
- Meander pattern matching for chaos detection
- Specter lens navigation for deep state updates
- Monadic pipelines with error handling
- Custom transducers for data processing

### 4. Reactive Web Interface
- Pure Hiccup HTML generation
- Real-time entropy visualization
- Interactive chaos controls
- Server-Sent Events for live updates

## Deployment Configuration

### Dependencies
All managed through `deps.edn` with latest Clojure 1.12.0 features.

### Server Configuration
- Port: 3000 (configurable)
- Ring/Jetty stack
- Server-Sent Events for real-time updates
- RESTful API endpoints

### Subdomains Setup
The system is designed to be deployed at:
- `void-shrine.dissemblage.art` (primary chaos interface)
- Additional subdomains can host specialized views

## Running the System

```bash
# Install dependencies
clj -P

# Start the server
clj -M -m void-shrine.web.server 3000

# Or with custom port
clj -M -m void-shrine.web.server 8080
```

## API Endpoints

- `GET /` - Main chaos dashboard
- `POST /api/chaos` - Trigger chaos event  
- `POST /api/entropy` - Force entropy harvest
- `POST /api/void` - Deep void entry
- `GET /api/events` - Server-Sent Events stream
- `GET /api/analytics` - Chaos analytics
- `GET /api/patterns` - Pattern recognition results

## Advanced Features Showcase

### Pattern Matching with Meander
```clojure
(m/match chaos-data
  {:entropy-values (m/scan {:entropy-value (m/pred #(> % 200))})}
  {:pattern :entropy-cascade :intensity :high})
```

### Lens Navigation with Specter
```clojure
(s/transform [:entropy-values s/ALL :entropy-value] 
             #(min 255 (int (* % amplification-factor)))
             state)
```

### Monadic Error Handling
```clojure
(cats/>>= (either/right data)
          validate-data
          process-chaos
          generate-response)
```

### Custom Macro DSL
```clojure
(defchaos-mutation entropy-surge-mutation
  {:entropy-values [& (m/scan ?val)] :as state}
  (when (some #(> % 240) ?val)
    (update-surge-metrics state)))
```

This represents the full realization of a pure, advanced Clojure system for chaos generation and void exploration - ready for deployment to the hyperstitious infrastructure.