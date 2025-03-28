FROM python:3.9-slim

WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive

# Instala git e limpa cache
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Copia e instala apenas requirements (aproveita cache melhor)
COPY requirements.txt .
RUN python -m pip install --upgrade pip==23.3.1 \
    && pip install --no-cache-dir -r requirements.txt

# Copia o restante do código
COPY main.py .

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
