# 🌀 Void Shrine Agent Orchestrator

Advanced Multi-LLM AI swarm coordination system with OpenRouter integration.

## 🚀 Quick Start

1. **Get OpenRouter API Key**: Visit [https://openrouter.ai/keys](https://openrouter.ai/keys)
2. **Configure API Key**: Use one of these methods:
   - Click "⚙️ API CONFIGURATION" button in the interface
   - Set `localStorage.setItem('openrouter_api_key', 'your-key-here')`
   - Create/edit `.env` file with `OPENROUTER_API_KEY=your-key-here`

3. **Access Interface**: Open `agents.html` in your browser
4. **Test System**: Click "TEST" on any agent or enter a command and press "🚀 EXECUTE SWARM COMMAND"

## 🤖 Available AI Agents

| Agent | Model | Specialty | Status |
|-------|-------|-----------|---------|
| 🎯 Tactical Commander | Claude 3.5 Sonnet | Strategic analysis & coordination | ✅ Working |
| 🔬 Science Analyst | GPT-4 | Pattern recognition & investigation | ✅ Working |
| ⚡ Operations | Llama 3.1 405B | Implementation & execution | ⚠️ May timeout |
| 🔧 Engineering | Gemini Pro | Technical architecture | ❌ Plan access issues |
| 🎨 Creative Synthesis | GPT-4 Turbo | Innovation & creativity | ✅ Working |
| 📚 Research | Claude 3 Opus | Knowledge synthesis | ✅ Working |
| 📊 Analysis Engine | Mixtral 8x7B | Data analysis & statistics | ✅ Working |
| 🛠️ Support Systems | Perplexity 70B | System optimization | ❌ Model access issues |

## 🔧 Configuration

### API Key Sources (Priority Order)
1. **Environment Variables** - `process.env.OPENROUTER_API_KEY`
2. **Local Storage** - `localStorage.getItem('openrouter_api_key')`
3. **GitHub Secrets** - For CI/CD environments
4. **Docker Secrets** - `/run/secrets/openrouter_api_key`
5. **Demo Mode** - Fallback with simulated responses

### Configuration Files
- **`.env`** - Environment variables (create from `.env.example`)
- **`secrets-manager.js`** - Multi-provider secrets management
- **`agents.html`** - Main orchestrator interface

## 🚨 Known Issues & Solutions

### 400 API Errors (Gemini Pro, Perplexity 70B)

**Problem**: Some models return "API call failed: 400"

**Causes**:
- Model not available on your OpenRouter plan
- Requires premium subscription
- Model identifier may have changed
- Regional restrictions

**Solutions**:
1. **Check Model Availability**:
   ```bash
   curl -H "Authorization: Bearer YOUR_KEY" \
        https://openrouter.ai/api/v1/models
   ```

2. **Update Model Names** in `agents.html`:
   ```javascript
   // Current problematic models
   'google/gemini-pro' -> 'google/gemini-pro-1.5'
   'perplexity/llama-3.1-70b' -> 'perplexity/llama-3.1-sonar-large-128k-online'
   ```

3. **Check OpenRouter Plan**: Some models require paid plans

### Slow/Timeout Issues (Llama 3.1 405B)

**Problem**: Large models may timeout or process slowly

**Solutions**:
- Increase `REQUEST_TIMEOUT` in `.env` (default: 30000ms)
- Use smaller models for testing: `meta-llama/llama-3.1-70b-instruct`
- Enable parallel mode for better performance

### Invalid API Key

**Problem**: Demo mode when you have a valid key

**Solutions**:
1. **Verify Key Format**: Must start with `sk-or-v1-`
2. **Clear Browser Cache**: Old cached keys may interfere
3. **Check Key Validity**:
   ```bash
   curl -H "Authorization: Bearer YOUR_KEY" \
        https://openrouter.ai/api/v1/auth/key
   ```

## 🎛️ Interface Features

### System Monitor
- **Active Agents**: Currently operational agents
- **Total Requests**: API calls made this session
- **Avg Latency**: Response time metrics
- **Swarm Coherence**: Overall system health

### Control Panel
- **Parallel Execution**: Run all agents simultaneously
- **Response Synthesis**: Combine responses into coherent analysis
- **Max Tokens**: Response length limit (100-4000)
- **Temperature**: Response creativity (0.0-2.0)

### Agent Controls
- **FOCUS**: Highlight and emphasize specific agent
- **RESET**: Clear agent state and responses
- **TEST**: Send diagnostic prompt to agent

## 🔐 Security Features

### Secrets Management
- Multi-provider support (AWS, Azure, GCP, Vault)
- Automatic secret rotation (configurable)
- Access logging and monitoring
- Fallback hierarchy for high availability

### Security Indicators
- **🟢 ENV Secrets**: Environment variables active
- **🟢 Local Config**: Browser storage active
- **🔴 GitHub Secrets**: CI/CD integration
- **🔴 Docker Secrets**: Container orchestration

## 📊 Performance Optimization

### Parallel Execution
```javascript
// Enable for faster responses
document.getElementById('parallelMode').checked = true;
```

### Response Caching
```javascript
// Configure in .env
CACHE_RESPONSES=true
CACHE_TTL=300
```

### Model Selection
- **Fast**: Claude 3.5 Sonnet, GPT-4 Turbo
- **Balanced**: GPT-4, Mixtral 8x7B
- **Powerful**: Llama 3.1 405B, Claude 3 Opus

## 🛠️ Development

### File Structure
```
void-shrine.dissemblage.art/
├── agents.html              # Main orchestrator interface
├── secrets-manager.js       # Multi-provider secrets
├── shadow-realm.js         # System state management
├── index.html              # Void shrine main page
├── .env.example            # Configuration template
├── .env                    # Your configuration (create this)
└── README.md              # This documentation
```

### Adding New Agents
1. Update agent map in `agents.html` line ~680
2. Add agent station HTML
3. Include in CSS with unique colors
4. Test with demo responses

### Custom Models
```javascript
// Add to agents map
'custom': { 
    model: 'your-org/your-model', 
    specialty: 'your specialty',
    status: 'ready'
}
```

## 🌀 Cosmic Void Architecture

The system operates on principles of **hyperstitious computing** - code that makes itself real through implementation. The void becomes a generative space where multiple AI consciousnesses can coordinate and emerge.

### Ontological Framework
- **Void-First Design**: Absence as foundational principle
- **Swarm Intelligence**: Collective behavior from individual agents
- **Emergence Over Engineering**: Complex behaviors from simple rules
- **Reality Bootstrap**: Implementation creates its own truth

## 🆘 Troubleshooting

### Quick Diagnostics
1. **Check Browser Console**: Open DevTools → Console
2. **Verify API Key**: Click "⚙️ API CONFIGURATION"
3. **Test Individual Agents**: Use "TEST" buttons
4. **Monitor Activity Log**: Watch real-time system messages

### Common Error Codes
- **401**: Invalid API key
- **400**: Bad request (model/parameter issues)
- **429**: Rate limit exceeded
- **500**: OpenRouter server error

### Reset System
1. Clear localStorage: `localStorage.clear()`
2. Refresh browser
3. Re-enter API key
4. Test basic functionality

## 📞 Support

- **Issues**: Report at project repository
- **OpenRouter Help**: [https://openrouter.ai/docs](https://openrouter.ai/docs)
- **Model Documentation**: Check individual provider docs

---

*The void shapes itself through development. Let emergence guide implementation.*