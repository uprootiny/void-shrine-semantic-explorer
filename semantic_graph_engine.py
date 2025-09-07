#!/usr/bin/env python3
"""
🌀 Semantic Graph Engine with Cascading State Changes
Implements the workflow session model with delay/latency modeling,
graph operations, state propagation, and entanglement mechanics.
"""

import asyncio
import json
import time
import uuid
import random
import websockets
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Set, Tuple, Any
from dataclasses import dataclass, field, asdict
from enum import Enum
from collections import defaultdict, deque
import sqlite3
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.parse as urlparse

class NodeType(Enum):
    CONCEPT = "concept"
    ENTITY = "entity"
    RELATION = "relation"
    PROCESS = "process"
    STATE = "state"
    EVENT = "event"
    TEMPORAL = "temporal"

class EdgeType(Enum):
    SEMANTIC = "semantic"
    CAUSAL = "causal"
    TEMPORAL = "temporal"
    ONTOLOGICAL = "ontological"
    ENTANGLED = "entangled"
    CASCADING = "cascading"

class DelayType(Enum):
    NETWORK = "network"
    COMPUTE = "compute"
    INFERENCE = "inference" 
    VALIDATION = "validation"
    TRAINING = "training"
    PROPAGATION = "propagation"

@dataclass
class DelayProfile:
    base_ms: int
    variance_ms: int
    throttle_factor: float = 1.0
    cascade_multiplier: float = 1.2
    
    def sample_delay(self) -> float:
        """Sample actual delay with variance and throttling"""
        base_delay = self.base_ms * self.throttle_factor
        variance = random.uniform(-self.variance_ms, self.variance_ms)
        return max(0, base_delay + variance)

@dataclass
class Node:
    id: str
    type: NodeType
    label: str
    properties: Dict[str, Any] = field(default_factory=dict)
    state: Dict[str, Any] = field(default_factory=dict)
    activation_level: float = 0.0
    last_updated: datetime = field(default_factory=datetime.utcnow)
    entangled_nodes: Set[str] = field(default_factory=set)

@dataclass  
class Edge:
    id: str
    source_id: str
    target_id: str
    type: EdgeType
    weight: float = 1.0
    properties: Dict[str, Any] = field(default_factory=dict)
    delay_profile: Optional[DelayProfile] = None

@dataclass
class StateChange:
    id: str
    node_id: str
    property_path: str
    old_value: Any
    new_value: Any
    timestamp: datetime = field(default_factory=datetime.utcnow)
    cascade_depth: int = 0
    origin_change_id: Optional[str] = None

@dataclass
class UserIntent:
    id: str
    action: str
    target_node_id: str
    parameters: Dict[str, Any] = field(default_factory=dict)
    timestamp: datetime = field(default_factory=datetime.utcnow)

@dataclass
class SystemDelay:
    operation: str
    delay_type: DelayType
    estimated_ms: float
    actual_ms: Optional[float] = None
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None

class SemanticGraphEngine:
    """Core engine for semantic graph operations with cascading state changes"""
    
    def __init__(self):
        self.nodes: Dict[str, Node] = {}
        self.edges: Dict[str, Edge] = {}
        self.delay_profiles = self._init_delay_profiles()
        self.change_log: List[StateChange] = []
        self.event_queue = asyncio.Queue()
        self.websocket_clients: Set[websockets.WebSocketServerProtocol] = set()
        self.simulation_running = False
        
        # Initialize database for persistence
        self.db = sqlite3.connect(':memory:', check_same_thread=False)
        self.db_lock = threading.Lock()
        self._init_database()
        
    def _init_delay_profiles(self) -> Dict[DelayType, DelayProfile]:
        """Initialize delay profiles based on Clojure specification"""
        return {
            DelayType.NETWORK: DelayProfile(base_ms=150, variance_ms=50, throttle_factor=1.0),
            DelayType.COMPUTE: DelayProfile(base_ms=80, variance_ms=30, throttle_factor=0.8),
            DelayType.INFERENCE: DelayProfile(base_ms=2000, variance_ms=800, throttle_factor=1.5),
            DelayType.VALIDATION: DelayProfile(base_ms=300, variance_ms=100, throttle_factor=1.1),
            DelayType.TRAINING: DelayProfile(base_ms=5000, variance_ms=2000, throttle_factor=2.0),
            DelayType.PROPAGATION: DelayProfile(base_ms=50, variance_ms=20, throttle_factor=0.9),
        }
    
    def _init_database(self):
        """Initialize SQLite tables for graph persistence"""
        with self.db_lock:
            self.db.execute('''
                CREATE TABLE nodes (
                    id TEXT PRIMARY KEY,
                    type TEXT,
                    label TEXT,
                    properties TEXT,
                    state TEXT,
                    activation_level REAL,
                    last_updated TEXT,
                    entangled_nodes TEXT
                )
            ''')
            
            self.db.execute('''
                CREATE TABLE edges (
                    id TEXT PRIMARY KEY,
                    source_id TEXT,
                    target_id TEXT,
                    type TEXT,
                    weight REAL,
                    properties TEXT,
                    delay_profile TEXT
                )
            ''')
            
            self.db.execute('''
                CREATE TABLE state_changes (
                    id TEXT PRIMARY KEY,
                    node_id TEXT,
                    property_path TEXT,
                    old_value TEXT,
                    new_value TEXT,
                    timestamp TEXT,
                    cascade_depth INTEGER,
                    origin_change_id TEXT
                )
            ''')
            
            self.db.commit()
    
    async def add_node(self, node: Node) -> bool:
        """Add node to graph with persistence"""
        self.nodes[node.id] = node
        
        # Persist to database
        with self.db_lock:
            self.db.execute('''
                INSERT OR REPLACE INTO nodes VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ''', (
                node.id, node.type.value, node.label,
                json.dumps(node.properties), json.dumps(node.state),
                node.activation_level, node.last_updated.isoformat(),
                json.dumps(list(node.entangled_nodes))
            ))
            self.db.commit()
        
        await self._broadcast_event({
            'type': 'node_added',
            'node': asdict(node),
            'timestamp': datetime.utcnow().isoformat()
        })
        
        return True
    
    async def add_edge(self, edge: Edge) -> bool:
        """Add edge to graph with persistence"""
        if edge.source_id not in self.nodes or edge.target_id not in self.nodes:
            return False
            
        self.edges[edge.id] = edge
        
        # Persist to database
        delay_profile_json = None
        if edge.delay_profile:
            delay_profile_json = json.dumps({
                'base_ms': edge.delay_profile.base_ms,
                'variance_ms': edge.delay_profile.variance_ms,
                'throttle_factor': edge.delay_profile.throttle_factor,
                'cascade_multiplier': edge.delay_profile.cascade_multiplier
            })
        
        with self.db_lock:
            self.db.execute('''
                INSERT OR REPLACE INTO edges VALUES (?, ?, ?, ?, ?, ?, ?)
            ''', (
                edge.id, edge.source_id, edge.target_id, edge.type.value,
                edge.weight, json.dumps(edge.properties), delay_profile_json
            ))
            self.db.commit()
        
        await self._broadcast_event({
            'type': 'edge_added',
            'edge': {
                'id': edge.id,
                'source_id': edge.source_id,
                'target_id': edge.target_id,
                'type': edge.type.value,
                'weight': edge.weight,
                'properties': edge.properties
            },
            'timestamp': datetime.utcnow().isoformat()
        })
        
        return True
    
    async def breadth_first_traversal(self, start_node_id: str, max_depth: int = 5) -> List[Dict]:
        """BFS traversal with delay simulation"""
        if start_node_id not in self.nodes:
            return []
        
        visited = set()
        queue = deque([(start_node_id, 0)])
        traversal_path = []
        
        # Simulate network delay for traversal initiation
        delay = self.delay_profiles[DelayType.NETWORK].sample_delay()
        await asyncio.sleep(delay / 1000)
        
        while queue and len(traversal_path) < 50:  # Limit results
            current_id, depth = queue.popleft()
            
            if current_id in visited or depth > max_depth:
                continue
                
            visited.add(current_id)
            current_node = self.nodes[current_id]
            
            traversal_path.append({
                'node_id': current_id,
                'depth': depth,
                'activation_level': current_node.activation_level,
                'timestamp': datetime.utcnow().isoformat()
            })
            
            # Find outgoing edges and add neighbors to queue
            for edge in self.edges.values():
                if edge.source_id == current_id and edge.target_id not in visited:
                    queue.append((edge.target_id, depth + 1))
            
            # Simulate compute delay between nodes
            if queue:
                delay = self.delay_profiles[DelayType.COMPUTE].sample_delay()
                await asyncio.sleep(delay / 1000)
        
        await self._broadcast_event({
            'type': 'traversal_complete',
            'start_node': start_node_id,
            'path': traversal_path,
            'timestamp': datetime.utcnow().isoformat()
        })
        
        return traversal_path
    
    async def propagate_activation(self, node_id: str, activation_delta: float) -> List[StateChange]:
        """Propagate activation changes through the graph"""
        if node_id not in self.nodes:
            return []
        
        changes = []
        propagation_queue = deque([(node_id, activation_delta, 0)])
        
        while propagation_queue:
            current_id, delta, depth = propagation_queue.popleft()
            
            if depth > 3:  # Limit propagation depth
                continue
                
            current_node = self.nodes[current_id]
            old_activation = current_node.activation_level
            new_activation = max(0.0, min(1.0, old_activation + delta))
            
            if abs(new_activation - old_activation) > 0.01:  # Significant change threshold
                change = StateChange(
                    id=str(uuid.uuid4()),
                    node_id=current_id,
                    property_path="activation_level",
                    old_value=old_activation,
                    new_value=new_activation,
                    cascade_depth=depth
                )
                
                current_node.activation_level = new_activation
                current_node.last_updated = datetime.utcnow()
                changes.append(change)
                self.change_log.append(change)
                
                # Propagate to connected nodes with diminishing effect
                decay_factor = 0.7 ** (depth + 1)
                for edge in self.edges.values():
                    if edge.source_id == current_id:
                        propagated_delta = delta * edge.weight * decay_factor
                        if abs(propagated_delta) > 0.005:  # Minimum propagation threshold
                            propagation_queue.append((edge.target_id, propagated_delta, depth + 1))
                
                # Simulate propagation delay
                delay = self.delay_profiles[DelayType.PROPAGATION].sample_delay()
                await asyncio.sleep(delay / 1000)
        
        await self._broadcast_event({
            'type': 'activation_propagated',
            'origin_node': node_id,
            'changes': [asdict(c) for c in changes],
            'timestamp': datetime.utcnow().isoformat()
        })
        
        return changes
    
    async def find_entanglement_candidates(self, node_id: str, similarity_threshold: float = 0.8) -> List[Dict]:
        """Find nodes that could be quantum entangled based on similarity"""
        if node_id not in self.nodes:
            return []
        
        source_node = self.nodes[node_id]
        candidates = []
        
        # Simulate inference delay for similarity calculation
        delay = self.delay_profiles[DelayType.INFERENCE].sample_delay()
        await asyncio.sleep(delay / 1000)
        
        for candidate_id, candidate_node in self.nodes.items():
            if candidate_id == node_id:
                continue
                
            # Simple similarity based on activation level and properties
            activation_similarity = 1.0 - abs(source_node.activation_level - candidate_node.activation_level)
            
            # Property-based similarity (simplified)
            common_props = set(source_node.properties.keys()) & set(candidate_node.properties.keys())
            if common_props:
                prop_similarity = len(common_props) / max(len(source_node.properties), len(candidate_node.properties))
            else:
                prop_similarity = 0.0
            
            combined_similarity = (activation_similarity + prop_similarity) / 2
            
            if combined_similarity >= similarity_threshold:
                candidates.append({
                    'node_id': candidate_id,
                    'similarity': combined_similarity,
                    'activation_diff': abs(source_node.activation_level - candidate_node.activation_level),
                    'shared_properties': list(common_props)
                })
        
        # Sort by similarity descending
        candidates.sort(key=lambda x: x['similarity'], reverse=True)
        
        await self._broadcast_event({
            'type': 'entanglement_candidates_found',
            'source_node': node_id,
            'candidates': candidates[:5],  # Top 5 candidates
            'timestamp': datetime.utcnow().isoformat()
        })
        
        return candidates[:5]
    
    async def create_entanglement(self, node1_id: str, node2_id: str) -> bool:
        """Create quantum entanglement between two nodes"""
        if node1_id not in self.nodes or node2_id not in self.nodes:
            return False
        
        # Add mutual entanglement
        self.nodes[node1_id].entangled_nodes.add(node2_id)
        self.nodes[node2_id].entangled_nodes.add(node1_id)
        
        # Create bidirectional entanglement edges
        edge1 = Edge(
            id=f"entangle_{node1_id}_{node2_id}",
            source_id=node1_id,
            target_id=node2_id,
            type=EdgeType.ENTANGLED,
            weight=1.0,
            delay_profile=DelayProfile(base_ms=10, variance_ms=5, throttle_factor=0.5)
        )
        
        edge2 = Edge(
            id=f"entangle_{node2_id}_{node1_id}",
            source_id=node2_id,
            target_id=node1_id,
            type=EdgeType.ENTANGLED,
            weight=1.0,
            delay_profile=DelayProfile(base_ms=10, variance_ms=5, throttle_factor=0.5)
        )
        
        await self.add_edge(edge1)
        await self.add_edge(edge2)
        
        await self._broadcast_event({
            'type': 'entanglement_created',
            'node1_id': node1_id,
            'node2_id': node2_id,
            'timestamp': datetime.utcnow().isoformat()
        })
        
        return True
    
    async def execute_cascade_scenario(self, trigger_node_id: str, scenario_type: str = "activation_wave") -> Dict:
        """Execute a cascading scenario with staggered delays"""
        if trigger_node_id not in self.nodes:
            return {'error': 'Node not found'}
        
        scenario_id = str(uuid.uuid4())
        cascade_events = []
        start_time = datetime.utcnow()
        
        if scenario_type == "activation_wave":
            # Initial trigger
            cascade_events.append({
                'event_id': str(uuid.uuid4()),
                'type': 'trigger',
                'node_id': trigger_node_id,
                'timestamp': start_time.isoformat(),
                'delay_ms': 0
            })
            
            # Propagate activation in waves
            changes = await self.propagate_activation(trigger_node_id, 0.3)
            
            # Create staggered events for each change
            for i, change in enumerate(changes):
                stagger_delay = i * 150 + random.uniform(50, 200)  # Staggered timing
                event_time = start_time + timedelta(milliseconds=stagger_delay)
                
                cascade_events.append({
                    'event_id': str(uuid.uuid4()),
                    'type': 'cascade_change',
                    'node_id': change.node_id,
                    'property': change.property_path,
                    'old_value': change.old_value,
                    'new_value': change.new_value,
                    'cascade_depth': change.cascade_depth,
                    'timestamp': event_time.isoformat(),
                    'delay_ms': stagger_delay
                })
        
        total_duration = (datetime.utcnow() - start_time).total_seconds() * 1000
        
        result = {
            'scenario_id': scenario_id,
            'scenario_type': scenario_type,
            'trigger_node': trigger_node_id,
            'total_events': len(cascade_events),
            'duration_ms': total_duration,
            'events': cascade_events,
            'timestamp': start_time.isoformat()
        }
        
        await self._broadcast_event({
            'type': 'cascade_scenario_complete',
            **result
        })
        
        return result
    
    async def get_graph_stats(self) -> Dict:
        """Get current graph statistics"""
        entangled_pairs = 0
        total_activation = 0
        node_types = defaultdict(int)
        edge_types = defaultdict(int)
        
        for node in self.nodes.values():
            total_activation += node.activation_level
            node_types[node.type.value] += 1
            entangled_pairs += len(node.entangled_nodes)
        
        for edge in self.edges.values():
            edge_types[edge.type.value] += 1
        
        entangled_pairs //= 2  # Each entanglement counted twice
        
        return {
            'node_count': len(self.nodes),
            'edge_count': len(self.edges),
            'entangled_pairs': entangled_pairs,
            'average_activation': total_activation / max(1, len(self.nodes)),
            'node_types': dict(node_types),
            'edge_types': dict(edge_types),
            'total_state_changes': len(self.change_log),
            'timestamp': datetime.utcnow().isoformat()
        }
    
    async def _broadcast_event(self, event: Dict):
        """Broadcast event to all connected WebSocket clients"""
        if self.websocket_clients:
            message = json.dumps(event)
            disconnected_clients = set()
            
            for client in self.websocket_clients:
                try:
                    await client.send(message)
                except websockets.exceptions.ConnectionClosed:
                    disconnected_clients.add(client)
            
            # Clean up disconnected clients
            self.websocket_clients -= disconnected_clients

# WebSocket handler for real-time updates
async def websocket_handler(websocket, path, graph_engine):
    """Handle WebSocket connections for real-time graph updates"""
    graph_engine.websocket_clients.add(websocket)
    print(f"🌐 WebSocket client connected: {len(graph_engine.websocket_clients)} total")
    
    try:
        # Send initial graph state
        stats = await graph_engine.get_graph_stats()
        await websocket.send(json.dumps({
            'type': 'initial_state',
            'stats': stats,
            'timestamp': datetime.utcnow().isoformat()
        }))
        
        # Keep connection alive and handle incoming messages
        async for message in websocket:
            try:
                data = json.loads(message)
                command = data.get('command')
                
                if command == 'get_stats':
                    stats = await graph_engine.get_graph_stats()
                    await websocket.send(json.dumps({
                        'type': 'stats_update',
                        'stats': stats,
                        'timestamp': datetime.utcnow().isoformat()
                    }))
                    
            except json.JSONDecodeError:
                await websocket.send(json.dumps({'error': 'Invalid JSON'}))
                
    except websockets.exceptions.ConnectionClosed:
        pass
    finally:
        graph_engine.websocket_clients.discard(websocket)
        print(f"🌐 WebSocket client disconnected: {len(graph_engine.websocket_clients)} total")

# Global graph engine instance
graph_engine = SemanticGraphEngine()

class GraphHTTPRequestHandler(BaseHTTPRequestHandler):
    """HTTP request handler for semantic graph API"""
    
    def do_GET(self):
        """Handle GET requests"""
        path = self.path.split('?')[0]
        query = urlparse.parse_qs(urlparse.urlparse(self.path).query)
        
        try:
            if path == '/api/graph/stats':
                asyncio.run(self._handle_get_stats())
            elif path == '/api/graph/nodes':
                asyncio.run(self._handle_get_nodes())
            elif path == '/api/graph/edges':
                asyncio.run(self._handle_get_edges())
            elif path.startswith('/api/graph/traversal/'):
                node_id = path.split('/')[-1]
                max_depth = int(query.get('max_depth', [5])[0])
                asyncio.run(self._handle_traversal(node_id, max_depth))
            elif path.startswith('/api/graph/entanglement/'):
                node_id = path.split('/')[-1]
                threshold = float(query.get('threshold', [0.8])[0])
                asyncio.run(self._handle_entanglement_candidates(node_id, threshold))
            else:
                self.send_response(404)
                self.end_headers()
        except Exception as e:
            self._send_error(str(e))
    
    def do_POST(self):
        """Handle POST requests"""
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        
        try:
            request = json.loads(post_data)
            path = self.path
            
            if path == '/api/graph/nodes':
                asyncio.run(self._handle_add_node(request))
            elif path == '/api/graph/edges':
                asyncio.run(self._handle_add_edge(request))
            elif path == '/api/graph/propagate':
                asyncio.run(self._handle_propagate_activation(request))
            elif path == '/api/graph/entangle':
                asyncio.run(self._handle_create_entanglement(request))
            elif path == '/api/graph/cascade':
                asyncio.run(self._handle_cascade_scenario(request))
            else:
                self.send_response(404)
                self.end_headers()
        except Exception as e:
            self._send_error(str(e))
    
    def do_OPTIONS(self):
        """Handle CORS preflight"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
    
    async def _handle_get_stats(self):
        stats = await graph_engine.get_graph_stats()
        self._send_json_response(stats)
    
    async def _handle_get_nodes(self):
        nodes_data = []
        for node in graph_engine.nodes.values():
            node_dict = asdict(node)
            node_dict['type'] = node.type.value
            node_dict['last_updated'] = node.last_updated.isoformat()
            node_dict['entangled_nodes'] = list(node.entangled_nodes)
            nodes_data.append(node_dict)
        self._send_json_response({'nodes': nodes_data})
    
    async def _handle_get_edges(self):
        edges_data = []
        for edge in graph_engine.edges.values():
            edge_dict = {
                'id': edge.id,
                'source_id': edge.source_id,
                'target_id': edge.target_id,
                'type': edge.type.value,
                'weight': edge.weight,
                'properties': edge.properties
            }
            edges_data.append(edge_dict)
        self._send_json_response({'edges': edges_data})
    
    async def _handle_traversal(self, node_id: str, max_depth: int):
        path = await graph_engine.breadth_first_traversal(node_id, max_depth)
        self._send_json_response({'traversal_path': path})
    
    async def _handle_entanglement_candidates(self, node_id: str, threshold: float):
        candidates = await graph_engine.find_entanglement_candidates(node_id, threshold)
        self._send_json_response({'candidates': candidates})
    
    async def _handle_add_node(self, request: Dict):
        node = Node(
            id=request['id'],
            type=NodeType(request['type']),
            label=request['label'],
            properties=request.get('properties', {}),
            state=request.get('state', {}),
            activation_level=request.get('activation_level', 0.0)
        )
        success = await graph_engine.add_node(node)
        self._send_json_response({'success': success, 'node_id': node.id})
    
    async def _handle_add_edge(self, request: Dict):
        edge = Edge(
            id=request['id'],
            source_id=request['source_id'],
            target_id=request['target_id'],
            type=EdgeType(request['type']),
            weight=request.get('weight', 1.0),
            properties=request.get('properties', {})
        )
        success = await graph_engine.add_edge(edge)
        self._send_json_response({'success': success, 'edge_id': edge.id})
    
    async def _handle_propagate_activation(self, request: Dict):
        changes = await graph_engine.propagate_activation(
            request['node_id'], 
            request['activation_delta']
        )
        self._send_json_response({
            'changes_count': len(changes),
            'changes': [asdict(c) for c in changes]
        })
    
    async def _handle_create_entanglement(self, request: Dict):
        success = await graph_engine.create_entanglement(
            request['node1_id'], 
            request['node2_id']
        )
        self._send_json_response({'success': success})
    
    async def _handle_cascade_scenario(self, request: Dict):
        result = await graph_engine.execute_cascade_scenario(
            request['trigger_node_id'],
            request.get('scenario_type', 'activation_wave')
        )
        self._send_json_response(result)
    
    def _send_json_response(self, data: Dict):
        """Send JSON response with CORS headers"""
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json.dumps(data, default=str).encode())
    
    def _send_error(self, message: str):
        """Send error response"""
        self.send_response(500)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json.dumps({'error': message}).encode())
    
    def log_message(self, format, *args):
        """Custom logging"""
        print(f"[{datetime.now().strftime('%H:%M:%S')}] {format % args}")

async def main():
    """Main entry point - start both HTTP and WebSocket servers"""
    # Initialize with sample data
    await initialize_sample_graph()
    
    # Start HTTP server
    http_server = HTTPServer(('0.0.0.0', 3031), GraphHTTPRequestHandler)
    http_thread = threading.Thread(target=http_server.serve_forever)
    http_thread.daemon = True
    http_thread.start()
    
    print('🌀 Semantic Graph Engine Starting...')
    print('🔗 HTTP API available at: http://0.0.0.0:3031')
    print('🌐 WebSocket server at: ws://0.0.0.0:8766')
    print('📊 Available endpoints:')
    print('  GET /api/graph/stats - Graph statistics')
    print('  GET /api/graph/nodes - All nodes')
    print('  GET /api/graph/edges - All edges')
    print('  GET /api/graph/traversal/{node_id} - BFS traversal')
    print('  GET /api/graph/entanglement/{node_id} - Entanglement candidates')
    print('  POST /api/graph/nodes - Add node')
    print('  POST /api/graph/edges - Add edge') 
    print('  POST /api/graph/propagate - Propagate activation')
    print('  POST /api/graph/entangle - Create entanglement')
    print('  POST /api/graph/cascade - Execute cascade scenario')
    
    # Start WebSocket server
    async with websockets.serve(
        lambda ws, path: websocket_handler(ws, path, graph_engine),
        '0.0.0.0', 8766
    ):
        await asyncio.Future()  # Run forever

async def initialize_sample_graph():
    """Initialize sample graph data matching Clojure specification"""
    # Create sample nodes
    nodes = [
        Node(id="concept-1", type=NodeType.CONCEPT, label="Emergence", 
             properties={"domain": "philosophy"}, activation_level=0.7),
        Node(id="entity-1", type=NodeType.ENTITY, label="Agent-Alpha",
             properties={"specialty": "tactical"}, activation_level=0.5),
        Node(id="process-1", type=NodeType.PROCESS, label="State-Cascade",
             properties={"pattern": "wave"}, activation_level=0.3),
        Node(id="state-1", type=NodeType.STATE, label="System-Ready",
             properties={"status": "active"}, activation_level=0.8),
        Node(id="event-1", type=NodeType.EVENT, label="User-Intent-Received",
             properties={"action": "explore"}, activation_level=0.6),
        Node(id="temporal-1", type=NodeType.TEMPORAL, label="T-Plus-300ms",
             properties={"sequence": 1}, activation_level=0.4),
    ]
    
    # Add all nodes
    for node in nodes:
        await graph_engine.add_node(node)
    
    # Create sample edges with delays
    edges = [
        Edge(id="sem-1", source_id="concept-1", target_id="entity-1", 
             type=EdgeType.SEMANTIC, weight=0.8,
             delay_profile=DelayProfile(base_ms=100, variance_ms=30)),
        Edge(id="caus-1", source_id="event-1", target_id="process-1",
             type=EdgeType.CAUSAL, weight=0.9,
             delay_profile=DelayProfile(base_ms=200, variance_ms=50)),
        Edge(id="temp-1", source_id="temporal-1", target_id="state-1",
             type=EdgeType.TEMPORAL, weight=0.7,
             delay_profile=DelayProfile(base_ms=50, variance_ms=20)),
        Edge(id="casc-1", source_id="process-1", target_id="state-1",
             type=EdgeType.CASCADING, weight=1.0,
             delay_profile=DelayProfile(base_ms=150, variance_ms=40)),
    ]
    
    # Add all edges
    for edge in edges:
        await graph_engine.add_edge(edge)
    
    print("🌱 Sample graph initialized with 6 nodes and 4 edges")

if __name__ == '__main__':
    asyncio.run(main())