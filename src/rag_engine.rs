use std::collections::HashMap;
use std::path::Path;
use serde::{Deserialize, Serialize};
use sqlite::{Connection, State};
use regex::Regex;
use anyhow::Result;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Document {
    pub id: String,
    pub title: String,
    pub content: String,
    pub metadata: HashMap<String, String>,
    pub embedding: Option<Vec<f32>>,
    pub chunks: Vec<DocumentChunk>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DocumentChunk {
    pub id: String,
    pub content: String,
    pub start_pos: usize,
    pub end_pos: usize,
    pub embedding: Option<Vec<f32>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SearchResult {
    pub document_id: String,
    pub chunk_id: String,
    pub content: String,
    pub similarity_score: f64,
    pub metadata: HashMap<String, String>,
}

pub struct RAGEngine {
    db: Connection,
    chunk_size: usize,
    overlap_size: usize,
    stop_words: std::collections::HashSet<String>,
}

impl RAGEngine {
    pub async fn new() -> Result<Self> {
        let db = sqlite::open(":memory:")?; // Use in-memory DB for simplicity
        
        // Initialize database schema
        db.execute(
            "CREATE TABLE documents (
                id TEXT PRIMARY KEY,
                title TEXT,
                content TEXT,
                metadata TEXT
            )"
        )?;

        db.execute(
            "CREATE TABLE chunks (
                id TEXT PRIMARY KEY,
                document_id TEXT,
                content TEXT,
                start_pos INTEGER,
                end_pos INTEGER,
                embedding BLOB,
                FOREIGN KEY(document_id) REFERENCES documents(id)
            )"
        )?;

        db.execute(
            "CREATE VIRTUAL TABLE chunks_fts USING fts5(
                chunk_id UNINDEXED,
                content
            )"
        )?;

        // Initialize stop words (minimal set)
        let stop_words = [
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
            "to", "was", "will", "with"
        ].iter().map(|&s| s.to_string()).collect();

        Ok(Self {
            db,
            chunk_size: 512,
            overlap_size: 64,
            stop_words,
        })
    }

    pub async fn index_document(&mut self, document: Document) -> Result<()> {
        // Store document
        let metadata_json = serde_json::to_string(&document.metadata)?;
        let mut stmt = self.db.prepare(
            "INSERT OR REPLACE INTO documents (id, title, content, metadata) VALUES (?, ?, ?, ?)"
        )?;
        
        stmt.bind((1, document.id.as_str()))?;
        stmt.bind((2, document.title.as_str()))?;
        stmt.bind((3, document.content.as_str()))?;
        stmt.bind((4, metadata_json.as_str()))?;
        stmt.next()?;

        // Create chunks
        let chunks = self.create_chunks(&document.content, &document.id);
        
        // Store chunks
        for chunk in chunks {
            let mut stmt = self.db.prepare(
                "INSERT OR REPLACE INTO chunks (id, document_id, content, start_pos, end_pos) VALUES (?, ?, ?, ?, ?)"
            )?;
            
            stmt.bind((1, chunk.id.as_str()))?;
            stmt.bind((2, chunk.document_id.as_str()))?;
            stmt.bind((3, chunk.content.as_str()))?;
            stmt.bind((4, chunk.start_pos as i64))?;
            stmt.bind((5, chunk.end_pos as i64))?;
            stmt.next()?;

            // Index for FTS
            let mut fts_stmt = self.db.prepare(
                "INSERT INTO chunks_fts (chunk_id, content) VALUES (?, ?)"
            )?;
            fts_stmt.bind((1, chunk.id.as_str()))?;
            fts_stmt.bind((2, chunk.content.as_str()))?;
            fts_stmt.next()?;
        }

        tracing::info!("Indexed document: {} with {} chunks", document.id, document.chunks.len());
        Ok(())
    }

    pub async fn query(&self, query: &str, limit: usize) -> Result<Vec<String>> {
        // Simple keyword-based search using FTS
        let processed_query = self.process_query(query);
        
        let mut stmt = self.db.prepare(
            "SELECT c.content, c.document_id, d.title, d.metadata
             FROM chunks_fts cf
             JOIN chunks c ON cf.chunk_id = c.id
             JOIN documents d ON c.document_id = d.id
             WHERE chunks_fts MATCH ?
             ORDER BY rank
             LIMIT ?"
        )?;
        
        stmt.bind((1, processed_query.as_str()))?;
        stmt.bind((2, limit as i64))?;

        let mut results = Vec::new();
        while let Ok(State::Row) = stmt.next() {
            let content: String = stmt.read::<String, _>(0)?;
            let doc_id: String = stmt.read::<String, _>(1)?;
            let title: String = stmt.read::<String, _>(2)?;
            
            results.push(format!(
                "[Document: {} ({})] {}",
                title,
                doc_id,
                content
            ));
        }

        // If no FTS results, fall back to simple text matching
        if results.is_empty() {
            results = self.fallback_search(query, limit).await?;
        }

        Ok(results)
    }

    async fn fallback_search(&self, query: &str, limit: usize) -> Result<Vec<String>> {
        let query_words: Vec<&str> = query.split_whitespace()
            .filter(|word| !self.stop_words.contains(&word.to_lowercase()))
            .collect();

        let mut stmt = self.db.prepare(
            "SELECT c.content, c.document_id, d.title
             FROM chunks c
             JOIN documents d ON c.document_id = d.id
             LIMIT ?"
        )?;
        
        stmt.bind((1, (limit * 5) as i64))?; // Get more candidates for filtering

        let mut candidates = Vec::new();
        while let Ok(State::Row) = stmt.next() {
            let content: String = stmt.read::<String, _>(0)?;
            let doc_id: String = stmt.read::<String, _>(1)?;
            let title: String = stmt.read::<String, _>(2)?;
            
            // Simple relevance scoring
            let content_lower = content.to_lowercase();
            let score = query_words.iter()
                .map(|word| {
                    let word_lower = word.to_lowercase();
                    content_lower.matches(&word_lower).count() as f64
                })
                .sum::<f64>();

            if score > 0.0 {
                candidates.push((score, format!(
                    "[Document: {} ({})] {}",
                    title,
                    doc_id,
                    content
                )));
            }
        }

        // Sort by relevance and take top results
        candidates.sort_by(|a, b| b.0.partial_cmp(&a.0).unwrap());
        let results = candidates.into_iter()
            .take(limit)
            .map(|(_, content)| content)
            .collect();

        Ok(results)
    }

    fn create_chunks(&self, content: &str, doc_id: &str) -> Vec<DocumentChunk> {
        let mut chunks = Vec::new();
        let chars: Vec<char> = content.chars().collect();
        let mut start = 0;

        while start < chars.len() {
            let end = std::cmp::min(start + self.chunk_size, chars.len());
            
            // Try to break at sentence boundaries
            let mut actual_end = end;
            if end < chars.len() {
                for i in (start + self.chunk_size - 100..end).rev() {
                    if i < chars.len() && (chars[i] == '.' || chars[i] == '!' || chars[i] == '?') {
                        actual_end = i + 1;
                        break;
                    }
                }
            }

            let chunk_content: String = chars[start..actual_end].iter().collect();
            let chunk_id = format!("{}_{}", doc_id, chunks.len());

            chunks.push(DocumentChunk {
                id: chunk_id,
                document_id: doc_id.to_string(),
                content: chunk_content.trim().to_string(),
                start_pos: start,
                end_pos: actual_end,
                embedding: None, // Would implement with actual embeddings
            });

            // Move start position with overlap
            start = if actual_end >= self.overlap_size {
                actual_end - self.overlap_size
            } else {
                actual_end
            };

            if start >= chars.len() {
                break;
            }
        }

        chunks
    }

    fn process_query(&self, query: &str) -> String {
        // Simple query processing - remove stop words and prepare for FTS
        let words: Vec<String> = query.split_whitespace()
            .filter(|word| !self.stop_words.contains(&word.to_lowercase()))
            .map(|word| format!("\"{}\"", word)) // Quote each word for exact matching
            .collect();

        words.join(" OR ")
    }

    pub async fn index_void_shrine_knowledge(&mut self) -> Result<()> {
        // Index some void shrine specific knowledge
        let void_shrine_docs = vec![
            Document {
                id: "void_shrine_principles".to_string(),
                title: "Void Shrine Core Principles".to_string(),
                content: "Void-first design emphasizes absence as foundational architectural principle. \
                         Swarm intelligence enables collective behavior from individual agents. \
                         Emergence over engineering allows complex behaviors from simple rules. \
                         Reality bootstrap means implementation creates its own truth through hyperstitious computing.".to_string(),
                metadata: {
                    let mut map = HashMap::new();
                    map.insert("category".to_string(), "philosophy".to_string());
                    map.insert("source".to_string(), "void_shrine_constitution".to_string());
                    map
                },
                embedding: None,
                chunks: vec![],
            },
            Document {
                id: "agent_coordination".to_string(),
                title: "Multi-Agent Coordination Patterns".to_string(),
                content: "Agent orchestration involves tactical coordination through strategic analysis, \
                         scientific investigation through pattern recognition, operational implementation through \
                         systematic execution, and engineering solutions through technical architecture. \
                         Creative synthesis generates innovative approaches while research provides knowledge synthesis.".to_string(),
                metadata: {
                    let mut map = HashMap::new();
                    map.insert("category".to_string(), "technical".to_string());
                    map.insert("source".to_string(), "orchestration_manual".to_string());
                    map
                },
                embedding: None,
                chunks: vec![],
            },
            Document {
                id: "care_ethics".to_string(),
                title: "Care Ethics Framework".to_string(),
                content: "Care ethics prioritizes relational wellbeing and stakeholder agency. \
                         Moral recentering applies care-focused perspectives to technical decisions. \
                         Ethical considerations include social impact assessment, accessibility priorities, \
                         and sustainable design principles that center collective flourishing.".to_string(),
                metadata: {
                    let mut map = HashMap::new();
                    map.insert("category".to_string(), "ethics".to_string());
                    map.insert("source".to_string(), "moral_framework".to_string());
                    map
                },
                embedding: None,
                chunks: vec![],
            },
        ];

        for doc in void_shrine_docs {
            self.index_document(doc).await?;
        }

        tracing::info!("Indexed void shrine knowledge base");
        Ok(())
    }

    pub async fn get_stats(&self) -> Result<RAGStats> {
        let mut doc_stmt = self.db.prepare("SELECT COUNT(*) FROM documents")?;
        doc_stmt.next()?;
        let doc_count: i64 = doc_stmt.read(0)?;

        let mut chunk_stmt = self.db.prepare("SELECT COUNT(*) FROM chunks")?;
        chunk_stmt.next()?;
        let chunk_count: i64 = chunk_stmt.read(0)?;

        Ok(RAGStats {
            document_count: doc_count as usize,
            chunk_count: chunk_count as usize,
            chunk_size: self.chunk_size,
            overlap_size: self.overlap_size,
        })
    }
}

#[derive(Debug, Serialize)]
pub struct RAGStats {
    pub document_count: usize,
    pub chunk_count: usize,
    pub chunk_size: usize,
    pub overlap_size: usize,
}

// Binary for running RAG engine standalone
#[tokio::main]
async fn main() -> Result<()> {
    tracing_subscriber::init();
    
    let mut rag = RAGEngine::new().await?;
    
    // Index void shrine knowledge
    rag.index_void_shrine_knowledge().await?;
    
    // Test queries
    let test_queries = vec![
        "What are the core principles of void shrine?",
        "How do agents coordinate?",
        "What is care ethics?",
        "Explain emergence over engineering",
    ];

    tracing::info!("🔍 Testing RAG Engine:");
    
    for query in test_queries {
        println!("\n🔍 Query: {}", query);
        let results = rag.query(query, 3).await?;
        
        for (i, result) in results.iter().enumerate() {
            println!("  {}. {}", i + 1, result);
        }
    }

    let stats = rag.get_stats().await?;
    println!("\n📊 RAG Stats: {:#?}", stats);

    Ok(())
}