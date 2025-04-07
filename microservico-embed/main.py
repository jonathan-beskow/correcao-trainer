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
print("Carregando modelo CodeBERT...")
tokenizer = AutoTokenizer.from_pretrained("microsoft/codebert-base")
model = AutoModel.from_pretrained("microsoft/codebert-base")
print("Modelo carregado com sucesso.")

# Inicializar o índice FAISS
index = faiss.IndexFlatL2(768)
codigo_id_map = []

# Modelos de dados
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
    print("Gerando embedding para o código enviado...")
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().tolist()
    print("Embedding gerado com sucesso.")
    return {"embedding": embedding}

@app.post("/adicionar")
async def adicionar_codigo(req: EmbeddingRequest):
    print("Adicionando código ao índice FAISS...")
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()
    index.add(np.array([embedding]))
    codigo_id_map.append(req.codigo)
    print(f"Código adicionado. Total atual: {len(codigo_id_map)}")
    return {"message": "Código adicionado com sucesso!", "total_codigos": len(codigo_id_map)}

@app.post("/buscar_similar")
async def buscar_similaridade(req: SimilaridadeRequest):
    print("Buscando código similar via FAISS...")

    if len(codigo_id_map) == 0:
        print("Nenhum código disponível no índice FAISS.")
        return {"similares": [], "mensagem": "Índice FAISS está vazio. Adicione códigos primeiro com /adicionar."}

    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()

    k = min(req.k, len(codigo_id_map))
    D, I = index.search(np.array([embedding]), k=k)

    similares = [{"codigo": codigo_id_map[i], "distancia": float(D[0][j])} for j, i in enumerate(I[0])]
    print(f"{len(similares)} similar(es) encontrado(s).")
    return {"similares": similares}

# Conexão com o MongoDB
print("Conectando ao MongoDB...")
client = MongoClient("mongodb://mongo:27017/")
db = client["testdb"]
collection = db["casosCorrigidos"]
print("Conectado ao MongoDB com sucesso.")

# Extração do dataset
print("Extraindo dados do MongoDB para o dataset...")
dataset = []
for doc in collection.find():
    input_codigo = doc.get("codigoOriginal")
    output_codigo = doc.get("codigoCorrigido")
    tipo = doc.get("tipo")
    if input_codigo and output_codigo:
        dataset.append({
            "input": input_codigo.strip(),
            "output": output_codigo.strip(),
            "tipo": tipo.strip() if tipo else ""
        })
print(f"Total de exemplos coletados: {len(dataset)}")

# Salvamento em JSONL
output_file = "/mnt/data/dataset_treinamento_codet5.json"
os.makedirs("/mnt/data", exist_ok=True)
print(f"Salvando arquivo em: {output_file}")
with open(output_file, "w", encoding="utf-8") as f:
    for exemplo in dataset:
        f.write(json.dumps(exemplo, ensure_ascii=False) + "\n")
print("Dataset salvo com sucesso.")
