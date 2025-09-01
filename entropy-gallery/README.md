# Entropy Gallery - True Randomness Visualization

A structured subproject for visualizing entropy sources with integrated flamechart monitoring.

## Features

- **True Entropy Sources**: Random.org, NIST Beacon, HotBits, Market Data
- **Multiple Visualizations**: Line charts, bar charts, heatmaps, parametric plots
- **Interactive Interface**: D3.js powered charts with accordion data tables
- **Monitoring Integration**: Flamechart profiling of entropy harvesting processes
- **Modular Architecture**: Separated concerns with proper namespacing

## Structure

```
entropy-gallery/
├── project.clj                           # Leiningen project configuration
├── src/entropy_gallery/
│   ├── core.clj                          # Main entry point
│   ├── web.clj                           # Web routes and UI
│   ├── entropy/
│   │   └── sources.clj                   # Entropy source management
│   ├── visualization/
│   │   └── charts.clj                    # D3.js chart generation
│   └── monitoring/
│       └── flamechart.clj                # Performance monitoring
└── README.md                             # This file
```

## Running

```bash
cd entropy-gallery
lein run
```

Access at: http://localhost:3001

Monitoring dashboard: http://localhost:3001/monitoring

## API Endpoints

- `GET /` - Main entropy gallery interface
- `GET /api/entropy-sample?source=random-org` - Get entropy data for specific source
- `GET /monitoring` - Flamechart monitoring dashboard
- `GET /api/monitoring-data` - Raw monitoring data as JSON
- `POST /api/record-event` - Record monitoring events

## Integration

This subproject is designed to integrate back into the main void-shrine system, providing entropy visualization services and monitoring capabilities.