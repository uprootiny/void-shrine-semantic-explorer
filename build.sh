#!/bin/bash

# 🌀 Void Shrine MCP & RAG Build Script

set -e

echo "🌀 Building Void Shrine MCP Service & RAG Engine..."

# Check if Rust is available
if ! command -v cargo &> /dev/null; then
    echo "❌ Rust/Cargo not found. Installing via rustup..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
    source ~/.cargo/env
fi

echo "🔧 Building Rust components..."

# Build in development mode for faster compilation
cargo build

echo "🧪 Testing RAG Engine..."
cargo run --bin rag-engine

echo "🚀 Starting MCP Server in background..."
cargo run --bin mcp-server &
MCP_PID=$!

echo "📝 MCP Server started with PID: $MCP_PID"
echo "🌐 MCP Endpoints available at:"
echo "  - POST /api/mcp (Main MCP endpoint)"
echo "  - POST /api/chaos (Chaos engineering)"
echo "  - GET /api/throttle/{agent_id} (Throttling status)"
echo "  - POST /api/scaling (Scaling adjustments)"
echo "  - POST /api/moral-recentering (Ethical recentering)"

# Save PID for cleanup
echo $MCP_PID > mcp_server.pid

echo "✅ Void Shrine MCP & RAG system deployed!"
echo "🔍 To stop: kill \$(cat mcp_server.pid)"