import faiss
import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModel
import torch
import os
from dotenv import load_dotenv
from pymongo import MongoClient
import json

# Carregar variáveis de ambiente
load_dotenv()

# Inicializar FastAPI
app = FastAPI()

# Carregar tokenizer e modelo de embeddings
tokenizer = AutoTokenizer.from_pretrained("microsoft/codebert-base")
model = AutoModel.from_pretrained("microsoft/codebert-base")

# Inicializar o índice FAISS (assumindo vetor de 768 dimensões)
index = faiss.IndexFlatL2(768)
codigo_id_map = []  # Lista para mapear os códigos aos índices FAISS

# Modelo de dados para requisição
class EmbeddingRequest(BaseModel):
    codigo: str
    tipo: str

class SimilaridadeRequest(BaseModel):
    codigo: str
    tipo: str
    k: int = 1

@app.get("/check")
def check_connection():
    return {"status": "OK", "message": "Python server is up and running!"}

@app.post("/embed")
async def gerar_embedding(req: EmbeddingRequest):
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().tolist()
    return {"embedding": embedding}

@app.post("/adicionar")
async def adicionar_codigo(req: EmbeddingRequest):
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()
    index.add(np.array([embedding]))
    codigo_id_map.append(req.codigo)
    return {"message": "Código adicionado com sucesso!", "total_codigos": len(codigo_id_map)}

@app.post("/buscar_similar")
async def buscar_similaridade(req: SimilaridadeRequest):
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()
    D, I = index.search(np.array([embedding]), k=req.k)
    similares = [{"codigo": codigo_id_map[i], "distancia": float(D[0][j])} for j, i in enumerate(I[0])]
    return {"similares": similares}

    
