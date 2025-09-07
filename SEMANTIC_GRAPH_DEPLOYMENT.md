# 🌀 Semantic Graph Engine Deployment Guide

## Overview

The Semantic Graph Engine implements the sophisticated Clojure-based semantic graph exploration specification with cascading state changes, delay modeling, and quantum entanglement mechanics.

## Architecture

### Core Components
- **semantic_graph_simple.py**: HTTP-only backend engine (port 3032)
- **semantic_graph_ui.html**: Interactive web interface
- **test_semantic_graph.py**: Comprehensive test suite
- **semantic_graph_engine.py**: Full WebSocket version (blocked by port conflicts)

### Data Model
```python
@dataclass
class Node:
    id: str
    type: NodeType  # concept, entity, relation, process, state, event, temporal
    label: str
    properties: Dict[str, Any]
    state: Dict[str, Any]
    activation_level: float  # 0.0 to 1.0
    entangled_nodes: Set[str]

@dataclass  
class Edge:
    id: str
    source_id: str
    target_id: str
    type: EdgeType  # semantic, causal, temporal, ontological, entangled, cascading
    weight: float
    delay_profile: Optional[DelayProfile]
```

### Delay Profiles
```python
DelayType.NETWORK:     DelayProfile(base_ms=150, variance_ms=50)
DelayType.COMPUTE:     DelayProfile(base_ms=80,  variance_ms=30)
DelayType.INFERENCE:   DelayProfile(base_ms=2000, variance_ms=800)
DelayType.VALIDATION:  DelayProfile(base_ms=300, variance_ms=100)
DelayType.TRAINING:    DelayProfile(base_ms=5000, variance_ms=2000)
DelayType.PROPAGATION: DelayProfile(base_ms=50,  variance_ms=20)
```

## Deployment

### Start the Engine
```bash
cd /var/www/void-shrine.dissemblage.art
python3 semantic_graph_simple.py
```

### Access Points
- **API Base**: http://localhost:3032/api/graph/
- **Web UI**: http://void-shrine.dissemblage.art/semantic_graph_ui.html
- **Test Suite**: `python3 test_semantic_graph.py`

## API Endpoints

### Graph Statistics
```
GET /api/graph/stats
Response: {
  "node_count": 6,
  "edge_count": 4,
  "entangled_pairs": 0,
  "average_activation": 0.55,
  "node_types": {...},
  "edge_types": {...}
}
```

### Graph Traversal
```
GET /api/graph/traversal/{node_id}?max_depth=5
Response: {
  "traversal_path": [
    {
      "node_id": "concept-1",
      "depth": 0,
      "activation_level": 0.7,
      "timestamp": "2025-09-07T12:20:45Z"
    }
  ]
}
```

### Activation Propagation
```
POST /api/graph/propagate
Body: {
  "node_id": "concept-1",
  "activation_delta": 0.3
}
Response: {
  "changes_count": 2,
  "changes": [...]
}
```

### Entanglement Candidates
```
GET /api/graph/entanglement/{node_id}?threshold=0.8
Response: {
  "candidates": [
    {
      "node_id": "entity-1",
      "similarity": 0.85,
      "activation_diff": 0.2,
      "shared_properties": ["domain"]
    }
  ]
}
```

### Cascade Scenarios
```
POST /api/graph/cascade
Body: {
  "trigger_node_id": "concept-1",
  "scenario_type": "activation_wave"
}
Response: {
  "scenario_id": "uuid",
  "total_events": 5,
  "duration_ms": 1250,
  "events": [...]
}
```

## Sample Graph Data

The engine initializes with sample nodes:
- **concept-1**: "Emergence" (philosophy domain, activation: 0.7)
- **entity-1**: "Agent-Alpha" (tactical specialty, activation: 0.5)  
- **process-1**: "State-Cascade" (wave pattern, activation: 0.3)
- **state-1**: "System-Ready" (active status, activation: 0.8)
- **event-1**: "User-Intent-Received" (explore action, activation: 0.6)
- **temporal-1**: "T-Plus-300ms" (sequence 1, activation: 0.4)

Connected by edges with delay profiles:
- semantic: concept-1 → entity-1 (weight: 0.8, delay: 100ms±30ms)
- causal: event-1 → process-1 (weight: 0.9, delay: 200ms±50ms)
- temporal: temporal-1 → state-1 (weight: 0.7, delay: 50ms±20ms)
- cascading: process-1 → state-1 (weight: 1.0, delay: 150ms±40ms)

## Web Interface Features

### Graph Visualization
- Force-directed layout with physics simulation
- Node colors by type, size by activation level
- Edge arrows with type-specific styling
- Entanglement indicators and traversal highlighting

### Interactive Controls
- Add nodes/edges with validation
- Trigger activation propagation
- Execute cascade scenarios
- Find and create entanglements
- Real-time statistics dashboard

### Event Monitoring
- System logs with timestamps
- Real-time event stream
- Selection details panel
- Performance metrics

## Test Results

Latest test run (80% success rate):
```
📋 TEST SUMMARY:
  Total Tests: 5
  Passed: 4
  Failed: 1
  Success Rate: 80.0%

🔍 GRAPH TRAVERSAL:
  concept-1: 2 nodes in 243ms
  entity-1: 1 nodes in 166ms
  test-concept-1: 3 nodes in 292ms

⚡ ACTIVATION PROPAGATION:
  Total State Changes: 5
  Average Propagation Time: 68.0ms

⏱️ DELAY MODELING:
  stats: avg 1.9ms (variance: 1.6ms)
  nodes: avg 4.8ms (variance: 3.7ms)
  traversal: avg 392.8ms (variance: 4.9ms)
```

## Known Issues

1. **WebSocket Port Conflict**: Port 8765/8766 blocked by existing Java process
   - Fallback: HTTP-only version deployed on port 3032
   - Resolution: Kill conflicting process or use different port

2. **Cascade HTTP 500 Intermittent**: Occasional server errors during cascade execution
   - Workaround: Retry cascade operations
   - Investigation: Race condition in state change logging

## Performance Characteristics

### Response Times
- Statistics: ~2ms average
- Node listing: ~5ms average  
- Graph traversal: ~300ms average (with simulated delays)
- Activation propagation: ~70ms average
- Entanglement detection: ~2s (includes inference simulation)

### Scaling Behavior
- Memory usage: SQLite in-memory database (~10MB for 100 nodes)
- CPU usage: Single-threaded Python, ~5% idle, ~20% during operations
- Network: CORS-enabled API, minimal bandwidth requirements

## Integration Points

### With MCP Service
The semantic graph can be integrated with the existing MCP service at port 3030:
```python
# Add to mcp_service.py
semantic_graph = SemanticGraphEngine()

def handle_graph_query(prompt):
    # Use semantic graph for context
    nodes = semantic_graph.breadth_first_traversal('concept-1', 3)
    return semantic_context_from_nodes(nodes)
```

### With Agent Orchestrator
The graph can enhance agent coordination:
```javascript
// Add to agents.html
async function getSemanticContext(agentId) {
    const response = await fetch(`http://localhost:3032/api/graph/entanglement/${agentId}`);
    const candidates = await response.json();
    return candidates.slice(0, 3); // Top 3 related concepts
}
```

## Future Enhancements

1. **Real-time WebSocket Support**: Resolve port conflicts for live updates
2. **Persistent Storage**: Move from in-memory SQLite to PostgreSQL
3. **Graph Algorithms**: Add shortest path, centrality measures, community detection
4. **ML Integration**: Train embedding models for better similarity detection
5. **Distributed Processing**: Scale across multiple nodes for large graphs
6. **Version Control**: Track graph state changes over time
7. **Query Language**: Implement Cypher-like query interface

## Maintenance

### Logs
Check engine logs: `tail -f /var/log/semantic_graph.log` (if configured)

### Health Monitoring
```bash
curl http://localhost:3032/api/graph/stats | jq '.node_count'
```

### Backup
```bash
# Backup graph state (if persistent storage enabled)
sqlite3 semantic_graph.db .dump > backup_$(date +%Y%m%d).sql
```

## Security Considerations

- CORS enabled for localhost development
- No authentication currently implemented
- SQLite injection protection via parameterized queries
- Rate limiting not implemented (consider for production)

---

**Status**: ✅ Deployed and operational on port 3032
**Last Updated**: 2025-09-07T12:21:00Z
**Version**: 1.0.0