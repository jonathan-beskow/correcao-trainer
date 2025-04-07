import faiss
import numpy as np
from fastapi import FastAPI, Body
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

# Carregar modelo de embeddings
print("Carregando modelo CodeBERT...")
tokenizer = AutoTokenizer.from_pretrained("microsoft/codebert-base")
model = AutoModel.from_pretrained("microsoft/codebert-base")
print("Modelo carregado com sucesso.")

# Inicializar índice FAISS
index = faiss.IndexFlatL2(768)
codigo_id_map = []

# Definições de modelos de requisição
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
async def gerar_embedding(req: EmbeddingRequest = Body(...)):
    print("Gerando embedding para o código enviado...")
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().tolist()
    print("Embedding gerado com sucesso.")
    return {"embedding": embedding}

@app.post("/adicionar")
async def adicionar_codigo(req: EmbeddingRequest = Body(...)):
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
async def buscar_similaridade(req: SimilaridadeRequest = Body(...)):
    print("[DEBUG] Body recebido com sucesso")
    print(f"Tipo: {req.tipo}, K: {req.k}")
    print(f"Código (início): {req.codigo[:80]}...")

    if len(codigo_id_map) == 0:
        print("Nenhum código disponível no índice FAISS.")
        return {
            "codigoOriginal": req.codigo,
            "codigoCorrigido": "Nenhuma sugestão encontrada",
            "similaridade": 0.0
        }

    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()

    D, I = index.search(np.array([embedding]), k=req.k)
    similares = [{"codigo": codigo_id_map[i], "distancia": float(D[0][j])} for j, i in enumerate(I[0])]
    print(f"{len(similares)} similar(es) encontrado(s). Detalhes: {similares}")

    if similares and similares[0]["codigo"]:
        codigo_similar = similares[0]["codigo"]
        doc = collection.find_one({"codigoOriginal": codigo_similar})
        if doc:
            print("Documento correspondente encontrado no MongoDB.")
            return {
                "tipo": req.tipo,
                "codigoOriginal": req.codigo,
                "codigoCorrigido": doc.get("codigoCorrigido"),
                "similaridade": similares[0]["distancia"]
            }
        else:
            print("Nenhum documento correspondente encontrado no MongoDB.")

    return {
        "tipo": req.tipo,
        "codigoOriginal": req.codigo,
        "codigoCorrigido": "Nenhuma sugestão encontrada",
        "similaridade": 0.0
    }

# Conexão com MongoDB
print("Conectando ao MongoDB...")
client = MongoClient("mongodb://mongo:27017/")
db = client["testdb"]
collection = db["casosCorrigidos"]
print("Conectado ao MongoDB com sucesso.")

# Geração do dataset e carregamento no FAISS
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

output_file = "/mnt/data/dataset_treinamento_codet5.json"
os.makedirs("/mnt/data", exist_ok=True)
print(f"Salvando arquivo em: {output_file}")
with open(output_file, "w", encoding="utf-8") as f:
    for exemplo in dataset:
        f.write(json.dumps(exemplo, ensure_ascii=False) + "\n")
print("Dataset salvo com sucesso.")

# Carregar vetores no FAISS
print("Adicionando exemplos ao índice FAISS...")
for exemplo in dataset:
    entrada = f"{exemplo['tipo']}: {exemplo['input']}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()
    index.add(np.array([embedding]))
    codigo_id_map.append(exemplo['input'])
print(f"{len(codigo_id_map)} códigos adicionados ao índice FAISS.")
