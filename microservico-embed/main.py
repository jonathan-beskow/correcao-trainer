from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModel
import torch
import os
from dotenv import load_dotenv

# Carregar variáveis de ambiente (se necessário)
load_dotenv()

# Inicializando o aplicativo FastAPI
app = FastAPI()

# Carregar o modelo de tokenização e o modelo de embeddings
tokenizer = AutoTokenizer.from_pretrained("microsoft/codebert-base")
model = AutoModel.from_pretrained("microsoft/codebert-base")

# Modelo de dados para a requisição
class EmbeddingRequest(BaseModel):
    codigo: str
    tipo: str

# Endpoint para verificar se o Python está funcionando
@app.get("/check")
def check_connection():
    return {"status": "OK", "message": "Python server is up and running!"}

# Endpoint para gerar embeddings
@app.post("/embed")
async def gerar_embedding(req: EmbeddingRequest):
    # Preparando a entrada para o modelo
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)

    # Gerando o embedding com o modelo
    with torch.no_grad():  # Não calcular os gradientes
        outputs = model(**tokens)
    
    # Calculando o embedding médio (ao longo dos tokens)
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().tolist()

    # Retornando o embedding gerado
    return {"embedding": embedding}
