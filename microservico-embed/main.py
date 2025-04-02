from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from transformers import AutoTokenizer, AutoModel
import torch
import requests
import os
from dotenv import load_dotenv

# Carregar variáveis de ambiente
load_dotenv()
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")

app = FastAPI()

print("🔐 Verificando chave da API OpenRouter...")
if OPENROUTER_API_KEY:
    print("✅ Chave carregada com sucesso (oculta por segurança)")
else:
    print("❌ Nenhuma chave foi carregada. Verifique o .env e a configuração do Docker.")


# Evento de inicialização para testar a chave da API
@app.on_event("startup")
async def verificar_login_openrouter():
    print("🔐 Verificando chave da API OpenRouter...")
    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": "openai/gpt-3.5-turbo",
        "messages": [
            {"role": "user", "content": "Olá, você está funcionando?"}
        ]
    }

    try:
        response = requests.post("https://openrouter.ai/api/v1/chat/completions", headers=headers, json=payload)
        response.raise_for_status()
        print("✅ Conexão com a OpenRouter bem-sucedida! API key válida.")
    except Exception as e:
        print(f"❌ Erro ao conectar com OpenRouter. Verifique sua chave. Detalhes: {str(e)}")

# Modelo para gerar embeddings (mantido local)
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
    embedding = outputs.last_hidden_state.mean(dim=1).squeeze().tolist()
    return {"embedding": embedding}


# Dados para correção com IA (via OpenRouter)
class ExemploCorrecao(BaseModel):
    codigo_original: str
    codigo_corrigido: str

class CorrecaoRequest(BaseModel):
    tipo: str
    codigo_alvo: str
    exemplos: List[ExemploCorrecao]

@app.post("/gerar-correcao")
async def gerar_correcao(req: CorrecaoRequest):
    prompt = f"""
Você é um assistente especializado em segurança de software. Dada uma vulnerabilidade do tipo {req.tipo},
e um exemplo de código vulnerável com sua respectiva correção, você deve corrigir um novo código que contém a mesma falha.

Exemplo:
Código vulnerável:
{req.exemplos[0].codigo_original}

Código corrigido:
{req.exemplos[0].codigo_corrigido}

Agora corrija o seguinte código:
{req.codigo_alvo}

Código corrigido:
"""

    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "openai/gpt-3.5-turbo",
        "messages": [
            {"role": "system", "content": "Você é um especialista em correção de código seguro."},
            {"role": "user", "content": prompt}
        ]
    }

    try:
        response = requests.post("https://openrouter.ai/api/v1/chat/completions", headers=headers, json=payload)
        response.raise_for_status()
        content = response.json()["choices"][0]["message"]["content"].strip()

        if not content:
            return {"codigoCorrigido": "A IA não retornou nenhuma sugestão. Verifique o prompt ou os exemplos."}

        return {"codigoCorrigido": content}

    except Exception as e:
        return {"codigoCorrigido": None, "erro": str(e)}
