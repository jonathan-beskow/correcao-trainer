from fastapi import FastAPI, Request
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModel
import torch

app = FastAPI()

# Carrega o modelo CodeBERT (base)
tokenizer = AutoTokenizer.from_pretrained("microsoft/codebert-base")
model = AutoModel.from_pretrained("microsoft/codebert-base")

class EmbeddingRequest(BaseModel):
    codigo: str
    tipo: str

@app.post("/embed")
async def gerar_embedding(req: EmbeddingRequest):
    entrada = f"{req.tipo}: {req.codigo}"
    tokens = tokenizer(entrada, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**tokens)
    # Usa a média dos embeddings da última camada como representação vetorial
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().tolist()
    return {"embedding": embedding}
