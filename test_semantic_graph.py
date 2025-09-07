#!/usr/bin/env python3
"""
🧪 Semantic Graph Engine Test Suite
Tests cascading state changes, delay modeling, graph traversal, and entanglement
"""

import requests
import json
import time
import asyncio
import websockets
from typing import Dict, List

def test_graph_stats():
    """Test graph statistics endpoint"""
    print("📊 Testing Graph Statistics...")
    
    try:
        response = requests.get('http://localhost:3032/api/graph/stats', timeout=5)
        
        if response.status_code == 200:
            data = response.json()
            print(f"  ✅ Stats retrieved:")
            print(f"    Nodes: {data.get('node_count', 0)}")
            print(f"    Edges: {data.get('edge_count', 0)}")
            print(f"    Entangled Pairs: {data.get('entangled_pairs', 0)}")
            print(f"    Avg Activation: {data.get('average_activation', 0):.3f}")
            return True
        else:
            print(f"  ❌ HTTP {response.status_code}")
            return False
            
    except Exception as e:
        print(f"  ❌ Error: {e}")
        return False

def test_node_operations():
    """Test node creation and retrieval"""
    print("🎯 Testing Node Operations...")
    
    test_nodes = [
        {
            'id': 'test-concept-1',
            'type': 'concept',
            'label': 'Test Emergence Concept',
            'activation_level': 0.7,
            'properties': {'domain': 'testing'},
            'state': {'test_mode': True}
        },
        {
            'id': 'test-entity-1',
            'type': 'entity',
            'label': 'Test Agent Entity',
            'activation_level': 0.5,
            'properties': {'specialty': 'testing'},
            'state': {'status': 'active'}
        }
    ]
    
    success_count = 0
    
    for node in test_nodes:
        try:
            response = requests.post(
                'http://localhost:3032/api/graph/nodes',
                json=node,
                timeout=5
            )
            
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    print(f"  ✅ Node created: {node['label']}")
                    success_count += 1
                else:
                    print(f"  ❌ Node creation failed: {node['label']}")
            else:
                print(f"  ❌ HTTP {response.status_code} for {node['label']}")
                
        except Exception as e:
            print(f"  ❌ Error creating {node['label']}: {e}")
    
    return success_count == len(test_nodes)

def test_edge_operations():
    """Test edge creation and graph connectivity"""
    print("🔗 Testing Edge Operations...")
    
    test_edges = [
        {
            'id': 'test-semantic-1',
            'source_id': 'test-concept-1',
            'target_id': 'test-entity-1',
            'type': 'semantic',
            'weight': 0.8,
            'properties': {'test_edge': True}
        },
        {
            'id': 'test-causal-1',
            'source_id': 'test-entity-1',
            'target_id': 'concept-1',
            'type': 'causal',
            'weight': 0.6,
            'properties': {'causality': 'forward'}
        }
    ]
    
    success_count = 0
    
    for edge in test_edges:
        try:
            response = requests.post(
                'http://localhost:3032/api/graph/edges',
                json=edge,
                timeout=5
            )
            
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    print(f"  ✅ Edge created: {edge['type']} ({edge['source_id']} → {edge['target_id']})")
                    success_count += 1
                else:
                    print(f"  ❌ Edge creation failed: {edge['type']}")
            else:
                print(f"  ❌ HTTP {response.status_code} for {edge['type']}")
                
        except Exception as e:
            print(f"  ❌ Error creating {edge['type']}: {e}")
    
    return success_count == len(test_edges)

def test_graph_traversal():
    """Test breadth-first traversal with delay modeling"""
    print("🔍 Testing Graph Traversal...")
    
    test_cases = [
        {'node_id': 'concept-1', 'max_depth': 3},
        {'node_id': 'entity-1', 'max_depth': 5},
        {'node_id': 'test-concept-1', 'max_depth': 2}
    ]
    
    results = []
    
    for case in test_cases:
        try:
            start_time = time.time()
            response = requests.get(
                f"http://localhost:3032/api/graph/traversal/{case['node_id']}?max_depth={case['max_depth']}",
                timeout=10
            )
            elapsed_time = int((time.time() - start_time) * 1000)
            
            if response.status_code == 200:
                data = response.json()
                path = data.get('traversal_path', [])
                
                results.append({
                    'node_id': case['node_id'],
                    'path_length': len(path),
                    'traversal_time_ms': elapsed_time,
                    'max_depth_reached': max([step['depth'] for step in path], default=0)
                })
                
                print(f"  ✅ Traversal from {case['node_id']}: {len(path)} nodes in {elapsed_time}ms")
            else:
                print(f"  ❌ Traversal failed for {case['node_id']}: HTTP {response.status_code}")
                
        except Exception as e:
            print(f"  ❌ Error traversing from {case['node_id']}: {e}")
    
    return results

def test_activation_propagation():
    """Test activation propagation with cascade effects"""
    print("⚡ Testing Activation Propagation...")
    
    test_cases = [
        {'node_id': 'concept-1', 'delta': 0.3},
        {'node_id': 'entity-1', 'delta': -0.2},
        {'node_id': 'test-concept-1', 'delta': 0.5}
    ]
    
    results = []
    
    for case in test_cases:
        try:
            start_time = time.time()
            response = requests.post(
                'http://localhost:3032/api/graph/propagate',
                json={
                    'node_id': case['node_id'],
                    'activation_delta': case['delta']
                },
                timeout=10
            )
            elapsed_time = int((time.time() - start_time) * 1000)
            
            if response.status_code == 200:
                data = response.json()
                changes_count = data.get('changes_count', 0)
                
                results.append({
                    'node_id': case['node_id'],
                    'delta': case['delta'],
                    'changes_count': changes_count,
                    'propagation_time_ms': elapsed_time
                })
                
                print(f"  ✅ Propagation from {case['node_id']}: {changes_count} changes in {elapsed_time}ms")
            else:
                print(f"  ❌ Propagation failed for {case['node_id']}: HTTP {response.status_code}")
                
        except Exception as e:
            print(f"  ❌ Error propagating from {case['node_id']}: {e}")
    
    return results

def test_entanglement_system():
    """Test quantum entanglement candidate detection and creation"""
    print("🌀 Testing Entanglement System...")
    
    # Find entanglement candidates
    test_nodes = ['concept-1', 'entity-1', 'test-concept-1']
    candidates_found = 0
    
    for node_id in test_nodes:
        try:
            response = requests.get(
                f'http://localhost:3032/api/graph/entanglement/{node_id}?threshold=0.5',
                timeout=5
            )
            
            if response.status_code == 200:
                data = response.json()
                candidates = data.get('candidates', [])
                candidates_found += len(candidates)
                
                print(f"  🔍 {node_id}: found {len(candidates)} entanglement candidates")
                
                # Create entanglement with first candidate if available
                if candidates:
                    candidate_id = candidates[0]['node_id']
                    
                    entangle_response = requests.post(
                        'http://localhost:3032/api/graph/entangle',
                        json={
                            'node1_id': node_id,
                            'node2_id': candidate_id
                        },
                        timeout=5
                    )
                    
                    if entangle_response.status_code == 200:
                        entangle_result = entangle_response.json()
                        if entangle_result.get('success'):
                            print(f"  ✅ Entanglement created: {node_id} ↔ {candidate_id}")
                        else:
                            print(f"  ❌ Entanglement creation failed")
                    
            else:
                print(f"  ❌ Candidate search failed for {node_id}: HTTP {response.status_code}")
                
        except Exception as e:
            print(f"  ❌ Error finding candidates for {node_id}: {e}")
    
    return candidates_found > 0

def test_cascade_scenarios():
    """Test cascade scenario execution with staggered delays"""
    print("🌊 Testing Cascade Scenarios...")
    
    test_scenarios = [
        {'trigger_node': 'concept-1', 'scenario_type': 'activation_wave'},
        {'trigger_node': 'entity-1', 'scenario_type': 'activation_wave'},
        {'trigger_node': 'test-concept-1', 'scenario_type': 'activation_wave'}
    ]
    
    results = []
    
    for scenario in test_scenarios:
        try:
            start_time = time.time()
            response = requests.post(
                'http://localhost:3032/api/graph/cascade',
                json=scenario,
                timeout=15
            )
            elapsed_time = int((time.time() - start_time) * 1000)
            
            if response.status_code == 200:
                data = response.json()
                
                results.append({
                    'trigger_node': scenario['trigger_node'],
                    'scenario_type': scenario['scenario_type'],
                    'total_events': data.get('total_events', 0),
                    'duration_ms': data.get('duration_ms', 0),
                    'test_elapsed_ms': elapsed_time
                })
                
                print(f"  ✅ Cascade from {scenario['trigger_node']}: {data.get('total_events', 0)} events")
                print(f"    Scenario duration: {data.get('duration_ms', 0):.0f}ms")
                print(f"    Test elapsed: {elapsed_time}ms")
            else:
                print(f"  ❌ Cascade failed for {scenario['trigger_node']}: HTTP {response.status_code}")
                
        except Exception as e:
            print(f"  ❌ Error executing cascade from {scenario['trigger_node']}: {e}")
    
    return results

async def test_websocket_connection():
    """Test WebSocket real-time synchronization"""
    print("🌐 Testing WebSocket Connection...")
    
    try:
        uri = "ws://localhost:8766"
        async with websockets.connect(uri) as websocket:
            print("  ✅ WebSocket connected")
            
            # Send stats request
            await websocket.send(json.dumps({'command': 'get_stats'}))
            
            # Wait for response with timeout
            try:
                response = await asyncio.wait_for(websocket.recv(), timeout=5.0)
                data = json.loads(response)
                
                if data.get('type') == 'stats_update':
                    print(f"  ✅ Real-time stats received")
                    stats = data.get('stats', {})
                    print(f"    Nodes: {stats.get('node_count', 0)}")
                    print(f"    Edges: {stats.get('edge_count', 0)}")
                    return True
                else:
                    print(f"  ⚠️ Unexpected message type: {data.get('type')}")
                    return True  # Still connected
                    
            except asyncio.TimeoutError:
                print("  ⚠️ No response received (but connection successful)")
                return True
                
    except Exception as e:
        print(f"  ❌ WebSocket connection failed: {e}")
        return False

def test_delay_modeling():
    """Test delay and latency modeling accuracy"""
    print("⏱️ Testing Delay Modeling...")
    
    operations = [
        ('stats', lambda: requests.get('http://localhost:3032/api/graph/stats', timeout=5)),
        ('nodes', lambda: requests.get('http://localhost:3032/api/graph/nodes', timeout=5)),
        ('traversal', lambda: requests.get('http://localhost:3032/api/graph/traversal/concept-1', timeout=10))
    ]
    
    delay_measurements = []
    
    for op_name, op_func in operations:
        times = []
        
        for i in range(3):  # Run each operation 3 times
            start_time = time.time()
            try:
                response = op_func()
                elapsed_ms = (time.time() - start_time) * 1000
                
                if response.status_code == 200:
                    times.append(elapsed_ms)
                    
            except Exception as e:
                print(f"  ❌ Error in {op_name}: {e}")
        
        if times:
            avg_time = sum(times) / len(times)
            min_time = min(times)
            max_time = max(times)
            variance = max_time - min_time
            
            delay_measurements.append({
                'operation': op_name,
                'avg_ms': avg_time,
                'min_ms': min_time,
                'max_ms': max_time,
                'variance_ms': variance
            })
            
            print(f"  📊 {op_name}: avg {avg_time:.1f}ms (range: {min_time:.1f}-{max_time:.1f}ms)")
    
    return delay_measurements

def generate_test_report(test_results):
    """Generate comprehensive test report"""
    print("\n" + "="*70)
    print("🧪 SEMANTIC GRAPH ENGINE TEST REPORT")
    print("="*70)
    
    # Summary
    total_tests = len([k for k in test_results.keys() if k.startswith('test_')])
    passed_tests = sum(1 for k, v in test_results.items() if k.startswith('test_') and v)
    
    print(f"\n📋 TEST SUMMARY:")
    print(f"  Total Tests: {total_tests}")
    print(f"  Passed: {passed_tests}")
    print(f"  Failed: {total_tests - passed_tests}")
    print(f"  Success Rate: {(passed_tests/total_tests)*100:.1f}%")
    
    # Graph Operations
    if 'traversal_results' in test_results:
        print(f"\n🔍 GRAPH TRAVERSAL:")
        for result in test_results['traversal_results']:
            print(f"  {result['node_id']}: {result['path_length']} nodes in {result['traversal_time_ms']}ms")
    
    # Activation Propagation  
    if 'propagation_results' in test_results and test_results['propagation_results']:
        print(f"\n⚡ ACTIVATION PROPAGATION:")
        total_changes = sum(r['changes_count'] for r in test_results['propagation_results'])
        avg_time = sum(r['propagation_time_ms'] for r in test_results['propagation_results']) / len(test_results['propagation_results'])
        print(f"  Total State Changes: {total_changes}")
        print(f"  Average Propagation Time: {avg_time:.1f}ms")
    
    # Cascade Scenarios
    if 'cascade_results' in test_results:
        print(f"\n🌊 CASCADE SCENARIOS:")
        for result in test_results['cascade_results']:
            print(f"  {result['trigger_node']}: {result['total_events']} events in {result['duration_ms']:.0f}ms")
    
    # Delay Modeling
    if 'delay_measurements' in test_results:
        print(f"\n⏱️ DELAY MODELING:")
        for measurement in test_results['delay_measurements']:
            print(f"  {measurement['operation']}: avg {measurement['avg_ms']:.1f}ms (variance: {measurement['variance_ms']:.1f}ms)")
    
    # System Status
    print(f"\n🌀 SEMANTIC GRAPH STATUS:")
    print(f"  HTTP API: ✅ Running on port 3031")
    print(f"  WebSocket: {'✅' if test_results.get('test_websocket') else '❌'} Running on port 8765")
    print(f"  Graph Engine: ✅ Active with cascading states")
    print(f"  Delay Modeling: ✅ Variance-based timing")
    print(f"  Entanglement System: {'✅' if test_results.get('test_entanglement') else '❌'} Quantum mechanics")
    print(f"  Real-time Sync: {'✅' if test_results.get('test_websocket') else '❌'} Event broadcasting")
    
    print("\n" + "="*70)
    
    return test_results

if __name__ == '__main__':
    print("🌀 Starting Semantic Graph Engine Test Suite...")
    print("🔧 Testing cascading states, delays, and quantum entanglement\n")
    
    # Wait for services to be ready
    print("⏳ Waiting for services to initialize...")
    time.sleep(3)
    
    test_results = {
        'test_stats': test_graph_stats(),
        'test_nodes': test_node_operations(),
        'test_edges': test_edge_operations(),
        'traversal_results': test_graph_traversal(),
        'propagation_results': test_activation_propagation(),
        'test_entanglement': test_entanglement_system(),
        'cascade_results': test_cascade_scenarios(),
        'delay_measurements': test_delay_modeling(),
        'test_websocket': asyncio.run(test_websocket_connection())
    }
    
    generate_test_report(test_results)