# Docker Setup

## Pré-requisitos

- Docker
- Docker Compose

## Como executar

### 1. Iniciar MySQL e Simulador

```bash
docker-compose up -d
```

Isso irá iniciar:
- MySQL 8.0 na porta 3306
- Simulador da garagem na porta 8080

### 2. Verificar se os serviços estão rodando

```bash
docker-compose ps
```

### 3. Ver logs

```bash
# Logs de todos os serviços
docker-compose logs -f

# Logs apenas do MySQL
docker-compose logs -f mysql

# Logs apenas do simulador
docker-compose logs -f simulator
```

### 4. Parar os serviços

```bash
docker-compose down
```

### 5. Parar e remover volumes (limpar dados)

```bash
docker-compose down -v
```

## Configuração

O MySQL é configurado com:
- **Host**: localhost
- **Porta**: 3306
- **Database**: parking
- **Usuário root**: root
- **Senha root**: root
- **Usuário**: parking
- **Senha**: parking

O simulador está configurado para enviar eventos para:
- **Webhook URL**: http://host.docker.internal:3003/webhook

## Variáveis de Ambiente

Você pode criar um arquivo `.env` na raiz do projeto para sobrescrever configurações:

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=parking
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
SIMULATOR_BASE_URL=http://localhost:8080
SERVER_PORT=3003
```

## Troubleshooting

### MySQL não inicia

Verifique se a porta 3306 não está em uso:
```bash
netstat -ano | findstr :3306
```

### Simulador não conecta ao webhook

Certifique-se de que a aplicação Spring Boot está rodando na porta 3003 e acessível via `host.docker.internal`.

### Limpar tudo e recomeçar

```bash
docker-compose down -v
docker-compose up -d
```
