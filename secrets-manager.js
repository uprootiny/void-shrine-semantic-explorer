// Multi-layered Secrets Management System
class SecretsManager {
    constructor() {
        this.sources = new Map([
            ['env', { priority: 1, active: false, secure: true }],
            ['github', { priority: 2, active: false, secure: true }],
            ['docker', { priority: 3, active: false, secure: true }],
            ['vault', { priority: 4, active: false, secure: true }],
            ['aws', { priority: 5, active: false, secure: true }],
            ['azure', { priority: 6, active: false, secure: true }],
            ['gcp', { priority: 7, active: false, secure: true }],
            ['local', { priority: 8, active: false, secure: false }],
            ['fallback', { priority: 9, active: true, secure: false }]
        ]);
        
        this.secrets = new Map();
        this.rotationSchedule = new Map();
        this.accessLog = [];
        
        this.initializeSources();
    }
    
    async initializeSources() {
        // Environment Variables
        try {
            if (typeof process !== 'undefined' && process.env) {
                this.sources.get('env').active = true;
                this.log('Environment variables source active');
            }
        } catch (e) {}
        
        // GitHub Actions Secrets
        try {
            if (process?.env?.GITHUB_ACTIONS) {
                this.sources.get('github').active = true;
                this.log('GitHub Actions secrets active');
            }
        } catch (e) {}
        
        // Docker Secrets
        try {
            const dockerCheck = await fetch('/run/secrets/openrouter_api_key');
            if (dockerCheck.ok) {
                this.sources.get('docker').active = true;
                this.log('Docker secrets active');
            }
        } catch (e) {}
        
        // HashiCorp Vault
        try {
            await this.checkVaultConnection();
        } catch (e) {}
        
        // AWS Secrets Manager
        try {
            await this.checkAWSSecrets();
        } catch (e) {}
        
        // Azure Key Vault
        try {
            await this.checkAzureKeyVault();
        } catch (e) {}
        
        // Google Secret Manager
        try {
            await this.checkGCPSecrets();
        } catch (e) {}
        
        // Local Storage (least secure)
        try {
            if (typeof localStorage !== 'undefined' && localStorage.getItem('openrouter_api_key')) {
                this.sources.get('local').active = true;
                this.log('Local storage secrets active (WARNING: Not secure for production)');
            }
        } catch (e) {}
        
        // Fallback hardcoded (development only)
        this.sources.get('fallback').active = true;
        this.log('Fallback secrets active (DEVELOPMENT ONLY)');
    }
    
    async getSecret(key) {
        this.logAccess(key);
        
        const activeSources = Array.from(this.sources.entries())
            .filter(([name, config]) => config.active)
            .sort(([, a], [, b]) => a.priority - b.priority);
        
        for (const [sourceName, config] of activeSources) {
            try {
                const secret = await this.getFromSource(sourceName, key);
                if (secret) {
                    this.log(`Secret '${key}' retrieved from ${sourceName}`);
                    return secret;
                }
            } catch (error) {
                this.log(`Failed to retrieve '${key}' from ${sourceName}: ${error.message}`);
            }
        }
        
        throw new Error(`Secret '${key}' not found in any source`);
    }
    
    async getFromSource(sourceName, key) {
        switch (sourceName) {
            case 'env':
                return process?.env?.[key];
                
            case 'github':
                return process?.env?.[key]; // GitHub Actions injects as env vars
                
            case 'docker':
                try {
                    const response = await fetch(`/run/secrets/${key.toLowerCase()}`);
                    return response.ok ? await response.text() : null;
                } catch (e) {
                    return null;
                }
                
            case 'vault':
                return await this.getFromVault(key);
                
            case 'aws':
                return await this.getFromAWS(key);
                
            case 'azure':
                return await this.getFromAzure(key);
                
            case 'gcp':
                return await this.getFromGCP(key);
                
            case 'local':
                return localStorage.getItem(key.toLowerCase());
                
            case 'fallback':
                return this.getFallbackSecret(key);
                
            default:
                return null;
        }
    }
    
    getFallbackSecret(key) {
        const fallbacks = {
            'OPENROUTER_API_KEY': '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',
            'OPENROUTER_API_ENDPOINT': 'https://openrouter.ai/api/v1/chat/completions'
        };
        return fallbacks[key] || null;
    }
    
    async getFromVault(key) {
        // HashiCorp Vault integration
        try {
            const vaultToken = process?.env?.VAULT_TOKEN || localStorage.getItem('vault_token');
            const vaultAddr = process?.env?.VAULT_ADDR || 'http://localhost:8200';
            
            if (!vaultToken) return null;
            
            const response = await fetch(`${vaultAddr}/v1/secret/data/agent-orchestrator`, {
                headers: {
                    'X-Vault-Token': vaultToken
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                return data.data?.data?.[key];
            }
        } catch (e) {
            this.log(`Vault error: ${e.message}`);
        }
        return null;
    }
    
    async getFromAWS(key) {
        // AWS Secrets Manager integration
        try {
            if (typeof AWS === 'undefined') return null;
            
            const secretsManager = new AWS.SecretsManager({
                region: process?.env?.AWS_REGION || 'us-east-1'
            });
            
            const result = await secretsManager.getSecretValue({
                SecretId: 'agent-orchestrator-secrets'
            }).promise();
            
            const secrets = JSON.parse(result.SecretString);
            return secrets[key];
        } catch (e) {
            this.log(`AWS Secrets Manager error: ${e.message}`);
        }
        return null;
    }
    
    async getFromAzure(key) {
        // Azure Key Vault integration
        try {
            const vaultUrl = process?.env?.AZURE_KEY_VAULT_URL;
            if (!vaultUrl) return null;
            
            // This would use Azure SDK in production
            const response = await fetch(`${vaultUrl}/secrets/${key}?api-version=7.0`, {
                headers: {
                    'Authorization': `Bearer ${process?.env?.AZURE_ACCESS_TOKEN}`
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                return data.value;
            }
        } catch (e) {
            this.log(`Azure Key Vault error: ${e.message}`);
        }
        return null;
    }
    
    async getFromGCP(key) {
        // Google Secret Manager integration
        try {
            const projectId = process?.env?.GOOGLE_CLOUD_PROJECT;
            if (!projectId) return null;
            
            // This would use Google Cloud SDK in production
            const response = await fetch(
                `https://secretmanager.googleapis.com/v1/projects/${projectId}/secrets/${key}/versions/latest:access`,
                {
                    headers: {
                        'Authorization': `Bearer ${process?.env?.GOOGLE_ACCESS_TOKEN}`
                    }
                }
            );
            
            if (response.ok) {
                const data = await response.json();
                return atob(data.payload.data);
            }
        } catch (e) {
            this.log(`GCP Secret Manager error: ${e.message}`);
        }
        return null;
    }
    
    async checkVaultConnection() {
        try {
            const vaultAddr = process?.env?.VAULT_ADDR || 'http://localhost:8200';
            const response = await fetch(`${vaultAddr}/v1/sys/health`);
            if (response.ok) {
                this.sources.get('vault').active = true;
                this.log('HashiCorp Vault connection active');
            }
        } catch (e) {}
    }
    
    async checkAWSSecrets() {
        try {
            if (process?.env?.AWS_ACCESS_KEY_ID) {
                this.sources.get('aws').active = true;
                this.log('AWS Secrets Manager available');
            }
        } catch (e) {}
    }
    
    async checkAzureKeyVault() {
        try {
            if (process?.env?.AZURE_KEY_VAULT_URL) {
                this.sources.get('azure').active = true;
                this.log('Azure Key Vault available');
            }
        } catch (e) {}
    }
    
    async checkGCPSecrets() {
        try {
            if (process?.env?.GOOGLE_CLOUD_PROJECT) {
                this.sources.get('gcp').active = true;
                this.log('GCP Secret Manager available');
            }
        } catch (e) {}
    }
    
    async rotateSecret(key) {
        this.log(`Starting secret rotation for '${key}'`);
        
        // Get new secret from primary source
        const newSecret = await this.generateNewSecret(key);
        
        // Update in all active sources
        const activeSources = Array.from(this.sources.entries())
            .filter(([name, config]) => config.active && config.secure);
        
        for (const [sourceName] of activeSources) {
            try {
                await this.updateInSource(sourceName, key, newSecret);
                this.log(`Updated '${key}' in ${sourceName}`);
            } catch (error) {
                this.log(`Failed to update '${key}' in ${sourceName}: ${error.message}`);
            }
        }
        
        this.scheduleNextRotation(key);
    }
    
    scheduleNextRotation(key, intervalMs = 24 * 60 * 60 * 1000) { // 24 hours
        clearTimeout(this.rotationSchedule.get(key));
        
        const timeoutId = setTimeout(() => {
            this.rotateSecret(key);
        }, intervalMs);
        
        this.rotationSchedule.set(key, timeoutId);
    }
    
    logAccess(key) {
        const entry = {
            key,
            timestamp: new Date().toISOString(),
            source: this.getActiveSource()
        };
        
        this.accessLog.push(entry);
        
        // Keep only last 100 entries
        if (this.accessLog.length > 100) {
            this.accessLog.shift();
        }
    }
    
    getActiveSource() {
        return Array.from(this.sources.entries())
            .filter(([name, config]) => config.active)
            .sort(([, a], [, b]) => a.priority - b.priority)[0]?.[0] || 'none';
    }
    
    getSecurityStatus() {
        const activeSources = Array.from(this.sources.entries())
            .filter(([name, config]) => config.active);
        
        const secureCount = activeSources.filter(([, config]) => config.secure).length;
        const totalCount = activeSources.length;
        
        return {
            totalSources: totalCount,
            secureSources: secureCount,
            securityScore: (secureCount / totalCount) * 100,
            primarySource: this.getActiveSource(),
            rotationActive: this.rotationSchedule.size > 0
        };
    }
    
    log(message) {
        console.log(`[SECRETS-MANAGER] ${message}`);
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SecretsManager;
}