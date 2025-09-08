#!/usr/bin/env python3
"""
🔮 Predict-O-Matic Prediction Engine
Multi-modal prediction system with semantic graph integration and trading signals
"""

import asyncio
import json
import time
import uuid
import random
import websockets
import redis
import threading
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Any
from dataclasses import dataclass, asdict
from collections import defaultdict, deque
from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.parse as urlparse
import requests
import numpy as np

@dataclass
class MarketData:
    symbol: str
    price: float
    volume: float
    timestamp: datetime
    source: str = "simulation"

@dataclass
class Prediction:
    id: str
    symbol: str
    predicted_price: float
    confidence: float
    timeframe: str
    created_at: datetime
    features: Dict[str, float]
    model_used: str

@dataclass
class TradingSignal:
    id: str
    symbol: str
    action: str  # BUY, SELL, HOLD
    strength: float  # 0-1
    price_target: float
    stop_loss: float
    reasoning: str
    created_at: datetime

class MessageBus:
    """Redis-based message bus for inter-service communication"""
    
    def __init__(self):
        try:
            self.redis = redis.Redis(host='localhost', port=6379, decode_responses=True)
            self.redis.ping()
            self.connected = True
        except:
            print("Redis not available, using in-memory message bus")
            self.connected = False
            self.channels = defaultdict(list)
            self.subscribers = defaultdict(list)
    
    def publish(self, channel: str, message: Dict):
        """Publish message to channel"""
        message_str = json.dumps(message, default=str)
        if self.connected:
            self.redis.publish(channel, message_str)
        else:
            # In-memory fallback
            self.channels[channel].append(message)
            for callback in self.subscribers[channel]:
                callback(message)
    
    def subscribe(self, channel: str, callback):
        """Subscribe to channel with callback"""
        if self.connected:
            pubsub = self.redis.pubsub()
            pubsub.subscribe(channel)
            
            def listen():
                for message in pubsub.listen():
                    if message['type'] == 'message':
                        try:
                            data = json.loads(message['data'])
                            callback(data)
                        except json.JSONDecodeError:
                            pass
            
            thread = threading.Thread(target=listen)
            thread.daemon = True
            thread.start()
        else:
            # In-memory fallback
            self.subscribers[channel].append(callback)

class PredictionEngine:
    """Core prediction engine with multiple models and strategies"""
    
    def __init__(self):
        self.message_bus = MessageBus()
        self.market_data = deque(maxlen=1000)
        self.predictions = {}
        self.signals = {}
        self.models = {
            'trend_momentum': self.trend_momentum_model,
            'semantic_sentiment': self.semantic_sentiment_model,
            'volume_profile': self.volume_profile_model,
            'neural_ensemble': self.neural_ensemble_model
        }
        
        # Connect to existing services
        self.semantic_graph_url = 'http://localhost:3032/api/graph'
        self.mcp_service_url = 'http://localhost:3030/api'
        
        # Start background tasks
        self.start_market_simulation()
        self.start_prediction_pipeline()
        self.setup_message_handlers()
    
    def start_market_simulation(self):
        """Simulate market data for demonstration"""
        def simulate():
            symbols = ['BTC/USD', 'ETH/USD', 'SOL/USD', 'MATIC/USD', 'AVAX/USD']
            base_prices = {
                'BTC/USD': 45000,
                'ETH/USD': 2800,
                'SOL/USD': 85,
                'MATIC/USD': 0.85,
                'AVAX/USD': 28
            }
            
            while True:
                for symbol in symbols:
                    # Simulate price movement with trend and noise
                    base_price = base_prices[symbol]
                    trend = np.sin(time.time() / 3600) * 0.02  # Hourly trend
                    noise = random.gauss(0, 0.01)  # 1% volatility
                    
                    price = base_price * (1 + trend + noise)
                    volume = random.uniform(1000, 10000)
                    
                    market_data = MarketData(
                        symbol=symbol,
                        price=price,
                        volume=volume,
                        timestamp=datetime.utcnow()
                    )
                    
                    self.market_data.append(market_data)
                    self.message_bus.publish('market_data', asdict(market_data))
                
                time.sleep(5)  # Update every 5 seconds
        
        thread = threading.Thread(target=simulate)
        thread.daemon = True
        thread.start()
    
    def start_prediction_pipeline(self):
        """Start the prediction generation pipeline"""
        def predict():
            while True:
                try:
                    self.generate_predictions()
                    self.generate_trading_signals()
                    time.sleep(30)  # Generate predictions every 30 seconds
                except Exception as e:
                    print(f"Prediction pipeline error: {e}")
                    time.sleep(5)
        
        thread = threading.Thread(target=predict)
        thread.daemon = True
        thread.start()
    
    def setup_message_handlers(self):
        """Setup message bus handlers"""
        def handle_semantic_update(message):
            """Handle semantic graph updates"""
            print(f"Semantic update received: {message}")
            # Trigger sentiment-based predictions
            self.trigger_semantic_predictions()
        
        def handle_market_data(message):
            """Handle new market data"""
            # Update technical indicators
            self.update_technical_indicators(message)
        
        self.message_bus.subscribe('semantic_graph_update', handle_semantic_update)
        self.message_bus.subscribe('market_data', handle_market_data)
    
    def generate_predictions(self):
        """Generate predictions using all available models"""
        if len(self.market_data) < 10:
            return
        
        symbols = list(set(md.symbol for md in list(self.market_data)[-50:]))
        
        for symbol in symbols:
            symbol_data = [md for md in list(self.market_data)[-50:] if md.symbol == symbol]
            if len(symbol_data) < 5:
                continue
            
            # Generate predictions with each model
            for model_name, model_func in self.models.items():
                try:
                    prediction = model_func(symbol_data)
                    if prediction:
                        self.predictions[prediction.id] = prediction
                        self.message_bus.publish('prediction_generated', asdict(prediction))
                except Exception as e:
                    print(f"Model {model_name} error: {e}")
    
    def trend_momentum_model(self, data: List[MarketData]) -> Optional[Prediction]:
        """Simple trend momentum model"""
        if len(data) < 5:
            return None
        
        prices = [d.price for d in data[-5:]]
        volumes = [d.volume for d in data[-5:]]
        
        # Simple momentum calculation
        price_change = (prices[-1] - prices[0]) / prices[0]
        volume_trend = np.mean(volumes[-3:]) / np.mean(volumes[-5:-3]) if len(volumes) >= 5 else 1
        
        # Predict next price
        momentum_factor = price_change * volume_trend
        predicted_price = prices[-1] * (1 + momentum_factor * 0.1)
        
        confidence = min(abs(momentum_factor) * 2, 0.95)
        
        return Prediction(
            id=str(uuid.uuid4()),
            symbol=data[-1].symbol,
            predicted_price=predicted_price,
            confidence=confidence,
            timeframe="1h",
            created_at=datetime.utcnow(),
            features={
                'price_change': price_change,
                'volume_trend': volume_trend,
                'momentum_factor': momentum_factor
            },
            model_used='trend_momentum'
        )
    
    def semantic_sentiment_model(self, data: List[MarketData]) -> Optional[Prediction]:
        """Sentiment-based model using semantic graph"""
        try:
            # Query semantic graph for sentiment indicators
            response = requests.get(f"{self.semantic_graph_url}/stats", timeout=2)
            if response.status_code == 200:
                graph_stats = response.json()
                sentiment_score = graph_stats.get('average_activation', 0.5)
            else:
                sentiment_score = 0.5
        except:
            sentiment_score = 0.5
        
        if len(data) < 3:
            return None
        
        current_price = data[-1].price
        sentiment_multiplier = 1 + (sentiment_score - 0.5) * 0.1
        predicted_price = current_price * sentiment_multiplier
        
        return Prediction(
            id=str(uuid.uuid4()),
            symbol=data[-1].symbol,
            predicted_price=predicted_price,
            confidence=abs(sentiment_score - 0.5) * 2,
            timeframe="1h",
            created_at=datetime.utcnow(),
            features={
                'sentiment_score': sentiment_score,
                'sentiment_multiplier': sentiment_multiplier
            },
            model_used='semantic_sentiment'
        )
    
    def volume_profile_model(self, data: List[MarketData]) -> Optional[Prediction]:
        """Volume profile analysis model"""
        if len(data) < 7:
            return None
        
        volumes = [d.volume for d in data[-7:]]
        prices = [d.price for d in data[-7:]]
        
        # Volume-weighted average price
        vwap = sum(p * v for p, v in zip(prices, volumes)) / sum(volumes)
        current_price = prices[-1]
        
        # Volume momentum
        recent_volume = np.mean(volumes[-3:])
        older_volume = np.mean(volumes[-7:-3])
        volume_ratio = recent_volume / older_volume if older_volume > 0 else 1
        
        # Price prediction based on volume profile
        price_vs_vwap = (current_price - vwap) / vwap
        volume_impact = (volume_ratio - 1) * 0.05
        
        predicted_price = current_price * (1 + volume_impact - price_vs_vwap * 0.1)
        
        return Prediction(
            id=str(uuid.uuid4()),
            symbol=data[-1].symbol,
            predicted_price=predicted_price,
            confidence=min(volume_ratio * 0.3, 0.9),
            timeframe="1h",
            created_at=datetime.utcnow(),
            features={
                'vwap': vwap,
                'volume_ratio': volume_ratio,
                'price_vs_vwap': price_vs_vwap
            },
            model_used='volume_profile'
        )
    
    def neural_ensemble_model(self, data: List[MarketData]) -> Optional[Prediction]:
        """Ensemble model combining multiple signals"""
        if len(data) < 10:
            return None
        
        prices = np.array([d.price for d in data[-10:]])
        volumes = np.array([d.volume for d in data[-10:]])
        
        # Technical indicators
        sma_5 = np.mean(prices[-5:])
        sma_10 = np.mean(prices[-10:])
        rsi = self.calculate_rsi(prices, period=9)
        
        # Combine signals
        trend_signal = (sma_5 - sma_10) / sma_10
        momentum_signal = (prices[-1] - prices[-5]) / prices[-5]
        rsi_signal = (50 - rsi) / 100  # Contrarian RSI
        
        ensemble_signal = (trend_signal * 0.4 + momentum_signal * 0.4 + rsi_signal * 0.2)
        predicted_price = prices[-1] * (1 + ensemble_signal * 0.05)
        
        confidence = min(abs(ensemble_signal) * 5, 0.95)
        
        return Prediction(
            id=str(uuid.uuid4()),
            symbol=data[-1].symbol,
            predicted_price=predicted_price,
            confidence=confidence,
            timeframe="1h",
            created_at=datetime.utcnow(),
            features={
                'sma_5': float(sma_5),
                'sma_10': float(sma_10),
                'rsi': float(rsi),
                'trend_signal': float(trend_signal),
                'momentum_signal': float(momentum_signal),
                'ensemble_signal': float(ensemble_signal)
            },
            model_used='neural_ensemble'
        )
    
    def calculate_rsi(self, prices, period=14):
        """Calculate Relative Strength Index"""
        if len(prices) < period + 1:
            return 50
        
        deltas = np.diff(prices)
        gains = np.where(deltas > 0, deltas, 0)
        losses = np.where(deltas < 0, -deltas, 0)
        
        avg_gain = np.mean(gains[-period:])
        avg_loss = np.mean(losses[-period:])
        
        if avg_loss == 0:
            return 100
        
        rs = avg_gain / avg_loss
        rsi = 100 - (100 / (1 + rs))
        return rsi
    
    def generate_trading_signals(self):
        """Generate trading signals from predictions"""
        symbols = list(set(p.symbol for p in self.predictions.values()))
        
        for symbol in symbols:
            symbol_predictions = [p for p in self.predictions.values() 
                                if p.symbol == symbol and 
                                (datetime.utcnow() - p.created_at).total_seconds() < 3600]
            
            if len(symbol_predictions) < 2:
                continue
            
            # Ensemble prediction
            avg_prediction = np.mean([p.predicted_price for p in symbol_predictions])
            avg_confidence = np.mean([p.confidence for p in symbol_predictions])
            
            # Get current price
            recent_data = [md for md in list(self.market_data)[-10:] if md.symbol == symbol]
            if not recent_data:
                continue
            
            current_price = recent_data[-1].price
            price_change = (avg_prediction - current_price) / current_price
            
            # Generate signal
            if abs(price_change) > 0.02 and avg_confidence > 0.5:
                action = "BUY" if price_change > 0 else "SELL"
                strength = min(abs(price_change) * avg_confidence * 10, 1.0)
                
                # Risk management
                if action == "BUY":
                    price_target = current_price * 1.05
                    stop_loss = current_price * 0.98
                else:
                    price_target = current_price * 0.95
                    stop_loss = current_price * 1.02
                
                signal = TradingSignal(
                    id=str(uuid.uuid4()),
                    symbol=symbol,
                    action=action,
                    strength=strength,
                    price_target=price_target,
                    stop_loss=stop_loss,
                    reasoning=f"Ensemble prediction: {price_change:.1%} change, {avg_confidence:.1%} confidence",
                    created_at=datetime.utcnow()
                )
                
                self.signals[signal.id] = signal
                self.message_bus.publish('trading_signal', asdict(signal))
    
    def trigger_semantic_predictions(self):
        """Trigger predictions based on semantic graph changes"""
        try:
            # Get entanglement data from semantic graph
            response = requests.get(f"{self.semantic_graph_url}/stats", timeout=2)
            if response.status_code == 200:
                stats = response.json()
                entangled_pairs = stats.get('entangled_pairs', 0)
                
                # If high entanglement, increase prediction frequency
                if entangled_pairs > 2:
                    self.message_bus.publish('system_alert', {
                        'type': 'high_entanglement',
                        'pairs': entangled_pairs,
                        'action': 'increase_prediction_frequency'
                    })
        except Exception as e:
            print(f"Semantic trigger error: {e}")
    
    def update_technical_indicators(self, market_data):
        """Update technical indicators based on new market data"""
        # This would update various technical indicators
        pass
    
    def get_latest_predictions(self, symbol: str = None, limit: int = 10) -> List[Prediction]:
        """Get latest predictions, optionally filtered by symbol"""
        predictions = list(self.predictions.values())
        if symbol:
            predictions = [p for p in predictions if p.symbol == symbol]
        
        predictions.sort(key=lambda x: x.created_at, reverse=True)
        return predictions[:limit]
    
    def get_trading_signals(self, symbol: str = None, limit: int = 10) -> List[TradingSignal]:
        """Get trading signals, optionally filtered by symbol"""
        signals = list(self.signals.values())
        if symbol:
            signals = [s for s in signals if s.symbol == symbol]
        
        signals.sort(key=lambda x: x.created_at, reverse=True)
        return signals[:limit]
    
    def get_market_data(self, symbol: str = None, limit: int = 100) -> List[MarketData]:
        """Get market data, optionally filtered by symbol"""
        data = list(self.market_data)
        if symbol:
            data = [md for md in data if md.symbol == symbol]
        
        return data[-limit:]

# Global prediction engine instance
prediction_engine = PredictionEngine()

class PredictionHTTPHandler(BaseHTTPRequestHandler):
    """HTTP handler for prediction engine API"""
    
    def do_GET(self):
        path = self.path.split('?')[0]
        query = urlparse.parse_qs(urlparse.urlparse(self.path).query)
        
        try:
            if path == '/api/predictions':
                symbol = query.get('symbol', [None])[0]
                limit = int(query.get('limit', [10])[0])
                predictions = prediction_engine.get_latest_predictions(symbol, limit)
                self._send_json([asdict(p) for p in predictions])
                
            elif path == '/api/signals':
                symbol = query.get('symbol', [None])[0]
                limit = int(query.get('limit', [10])[0])
                signals = prediction_engine.get_trading_signals(symbol, limit)
                self._send_json([asdict(s) for s in signals])
                
            elif path == '/api/market_data':
                symbol = query.get('symbol', [None])[0]
                limit = int(query.get('limit', [100])[0])
                market_data = prediction_engine.get_market_data(symbol, limit)
                self._send_json([asdict(md) for md in market_data])
                
            elif path == '/api/status':
                status = {
                    'predictions_count': len(prediction_engine.predictions),
                    'signals_count': len(prediction_engine.signals),
                    'market_data_count': len(prediction_engine.market_data),
                    'models': list(prediction_engine.models.keys()),
                    'message_bus_connected': prediction_engine.message_bus.connected,
                    'timestamp': datetime.utcnow().isoformat()
                }
                self._send_json(status)
                
            else:
                self.send_response(404)
                self.end_headers()
                
        except Exception as e:
            self._send_error(str(e))
    
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        
        try:
            request = json.loads(post_data)
            path = self.path
            
            if path == '/api/trigger_prediction':
                symbol = request.get('symbol')
                if symbol:
                    # Force prediction for specific symbol
                    symbol_data = prediction_engine.get_market_data(symbol, 20)
                    if symbol_data:
                        predictions = []
                        for model_name, model_func in prediction_engine.models.items():
                            pred = model_func(symbol_data)
                            if pred:
                                prediction_engine.predictions[pred.id] = pred
                                predictions.append(asdict(pred))
                        self._send_json({'predictions': predictions})
                    else:
                        self._send_error('No market data available for symbol')
                else:
                    self._send_error('Symbol required')
                    
            elif path == '/api/message':
                channel = request.get('channel')
                message = request.get('message')
                if channel and message:
                    prediction_engine.message_bus.publish(channel, message)
                    self._send_json({'status': 'published'})
                else:
                    self._send_error('Channel and message required')
                    
            else:
                self.send_response(404)
                self.end_headers()
                
        except Exception as e:
            self._send_error(str(e))
    
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
    
    def _send_json(self, data):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json.dumps(data, default=str).encode())
    
    def _send_error(self, message):
        self.send_response(500)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json.dumps({'error': message}).encode())
    
    def log_message(self, format, *args):
        print(f"[PREDICT-API {datetime.now().strftime('%H:%M:%S')}] {format % args}")

if __name__ == '__main__':
    server = HTTPServer(('0.0.0.0', 3033), PredictionHTTPHandler)
    
    print('🔮 Predict-O-Matic Prediction Engine Starting...')
    print('📡 HTTP API available at: http://0.0.0.0:3033')
    print('📊 Available endpoints:')
    print('  GET /api/predictions - Get latest predictions')
    print('  GET /api/signals - Get trading signals')  
    print('  GET /api/market_data - Get market data')
    print('  GET /api/status - Engine status')
    print('  POST /api/trigger_prediction - Force prediction')
    print('  POST /api/message - Publish message')
    print('🚀 Message bus active for inter-service communication')
    print('🧠 Models: trend_momentum, semantic_sentiment, volume_profile, neural_ensemble')
    
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print('\n⏹️ Shutting down prediction engine...')
        server.shutdown()