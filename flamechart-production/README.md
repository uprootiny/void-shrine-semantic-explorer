# Flamechart Production - Advanced Chaos & Complexity Analysis

A production-ready flamechart system with chaos theory and complexity analysis for the void shrine ecosystem.

## Features

### 🔥 Advanced Profiling
- **Smart Sampling**: Configurable sampling rates (default 1%) with adaptive adjustment
- **Circuit Breaker**: Automatic protection against profiling overhead
- **Async Processing**: Non-blocking sample collection and analysis
- **Stack Optimization**: Configurable stack depth with internal filtering

### 🧮 Chaos Theory Analysis
- **Lyapunov Exponents**: Chaos sensitivity measurement (>0 indicates chaotic behavior)
- **Shannon Entropy**: Information content and randomness quantification
- **Fractal Dimension**: Geometric complexity via box-counting method
- **Lempel-Ziv Complexity**: Algorithmic complexity estimation
- **Autocorrelation**: Multi-lag temporal dependency analysis

### 📊 Mathematical Rigor
- **Real-time Statistics**: Live computation of chaos metrics
- **Cross-correlation**: System metric interdependency analysis
- **Time Series Analysis**: Windowed autocorrelation at multiple lags
- **Information Theory**: Entropy calculations with configurable binning
- **Nonlinear Dynamics**: Simplified Lyapunov exponent estimation

### 🎯 Production Ready
- **Configuration Management**: Schema-validated config with defaults
- **Performance Monitoring**: Monitor the monitoring system overhead
- **Memory Management**: Bounded collections with automatic cleanup  
- **Error Handling**: Circuit breakers and graceful degradation
- **Logging**: Structured logging with configurable levels

### 🌐 Advanced Visualization
- **Interactive Flamechart**: D3.js-powered performance visualization
- **Chaos Dashboards**: Real-time complexity metric displays
- **Correlation Matrices**: Cross-system dependency visualization
- **Entropy Meters**: Radial entropy visualization
- **Export Capabilities**: JSON data export for further analysis

## Architecture

```
flamechart-production/
├── project.clj                    # Leiningen configuration with optimized JVM settings
├── src/flamechart/
│   ├── config.clj                 # Schema-validated configuration management
│   ├── sampling.clj               # Smart sampling with adaptive rate adjustment
│   ├── profiler.clj               # Enhanced profiler with circuit breaker
│   ├── analysis.clj               # Chaos theory and complexity analysis
│   ├── server.clj                 # Async web server with advanced visualizations
│   └── core.clj                   # Main entry point with workload simulation
├── resources/
│   └── flamechart.js             # Advanced D3.js visualizations
└── README.md                     # This file
```

## Configuration

The system uses a validated configuration schema:

```clojure
{:profiling {:enabled true
             :sampling-rate 0.01        ; 1% sampling
             :max-samples 10000
             :stack-depth-limit 20
             :overhead-threshold-ns 1000000} ; 1ms max overhead
 
 :monitoring {:collection-interval-ms 1000
              :retention-samples 3600    ; 1 hour retention
              :circuit-breaker {:failure-threshold 5
                               :timeout-ms 30000}}
 
 :server {:port 3000
          :async-timeout-ms 5000
          :max-concurrent-requests 100}
 
 :analysis {:window-size 100
            :correlation-lags [1 5 10 20 50]
            :entropy-bins 32
            :chaos-dimensions 3}}
```

## Mathematical Foundations

### Chaos Theory Metrics

1. **Lyapunov Exponent (λ)**
   - Measures sensitivity to initial conditions
   - λ > 0: Chaotic behavior
   - λ = 0: Neutral stability  
   - λ < 0: Stable behavior

2. **Shannon Entropy (H)**
   ```
   H = -Σ p(x) log₂ p(x)
   ```
   - Quantifies information content
   - Higher values indicate more randomness

3. **Fractal Dimension (D)**
   - Box-counting method approximation
   - D > 1: Complex, fractal-like structure
   - D ≈ 1: Simple, linear behavior

4. **Lempel-Ziv Complexity (C)**
   - Algorithmic complexity measure
   - Higher values indicate more complex patterns
   - Normalized compression-based estimation

### Performance Optimizations

- **ThreadLocal Sampling**: Efficient per-thread random decision making
- **Atomic Counters**: Lock-free statistics collection
- **Dropping Buffers**: Backpressure protection in async channels
- **Circuit Breakers**: Automatic overhead protection
- **Transit Serialization**: Fast JSON-compatible data exchange

## Running

```bash
cd flamechart-production
lein run [port]

# Or with custom JVM settings
lein run -Dlog-level=debug 3000
```

### Production Deployment

```bash
lein uberjar
java -jar target/flamechart-production-0.2.0-standalone.jar
```

### Integration with Void Shrine

The flamechart system is designed to integrate with the void shrine ecosystem:

- **Cross-references**: Link to entropy gallery at http://localhost:3002
- **Unified Aesthetics**: Void shrine visual design language
- **API Compatibility**: RESTful APIs for system integration
- **Data Export**: JSON format compatible with void shrine analysis

## API Endpoints

- `GET /` - Interactive flamechart dashboard
- `GET /api/profiling-data` - Function performance metrics
- `GET /api/chaos-analysis` - Real-time chaos theory analysis
- `GET /api/sampling-stats` - Sampling statistics and circuit breaker status
- `POST /api/toggle-sampling` - Enable/disable profiling
- `POST /api/reset-circuit` - Reset circuit breaker

## Monitoring

The system monitors itself:

- **Sampling Overhead**: Tracks profiling performance impact
- **Circuit Breaker Status**: Automatic protection against excessive overhead
- **Memory Usage**: Bounded data structures with cleanup
- **Correlation Analysis**: Cross-system metric dependencies

## Advanced Usage

### Custom Analysis

Export chaos analysis data for deeper mathematical investigation:

```javascript
// Browser console
exportData(); // Downloads chaos analysis JSON
```

### Configuration Tuning

Adjust sampling rate based on performance requirements:

```clojure
(config/update-config! {:profiling {:sampling-rate 0.005}}) ; 0.5% sampling
```

### Integration Example

```clojure
;; In your void shrine code
(require '[flamechart.profiler :refer [profile-sampled]])

(defn complex-void-manifestation []
  (profile-sampled "void-manifestation"
    ;; Your void shrine logic here
    (generate-ontological-chaos)))
```

This production system provides mathematical rigor for analyzing the chaotic beauty of the void shrine's computational manifestations.