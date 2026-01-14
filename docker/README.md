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

A aplicação Spring Boot suporta variáveis de ambiente para sobrescrever configurações do `application.yml`.

### Configurar via variáveis de ambiente do sistema

**Windows (PowerShell):**
```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DATABASE="parking"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="root"
$env:SIMULATOR_BASE_URL="http://localhost:8080"
$env:SERVER_PORT="3003"
```

**Linux/Mac:**
```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=parking
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root
export SIMULATOR_BASE_URL=http://localhost:8080
export SERVER_PORT=3003
```

### Configurar via profiles do Spring Boot

Você também pode criar arquivos `application-{profile}.yml` (ex: `application-docker.yml`) e executar com:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

As configurações padrão estão em `application.yml` e já suportam variáveis de ambiente com valores padrão.

## Troubleshooting

### Docker Desktop não está rodando

**Erro:** `unable to get image 'mysql:8.0': error during connect: Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.51/images/mysql:8.0/json": open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.`

**Solução:**
1. Certifique-se de que o Docker Desktop está instalado e rodando
2. Inicie o Docker Desktop no Windows
3. Aguarde até que o Docker esteja completamente inicializado (ícone na bandeja do sistema)
4. Verifique se o Docker está funcionando:
   ```bash
   docker ps
   ```
5. Tente novamente:
   ```bash
   docker-compose up -d
   ```

### MySQL não inicia

Verifique se a porta 3306 não está em uso:
```bash
netstat -ano | findstr :3306
```

### Simulador não conecta ao webhook

Certifique-se de que a aplicação Spring Boot está rodando na porta 3003 e acessível via `host.docker.internal`.

### Aplicação não consegue conectar ao simulador

Se a aplicação não consegue conectar ao simulador mesmo com o container rodando:

1. **Verifique se o simulador está rodando:**
   ```bash
   docker ps | grep simulator
   docker logs parking-simulator
   ```

2. **Teste a conectividade manualmente:**
   ```bash
   curl http://localhost:8080/garage
   ```
   Se não funcionar, o simulador pode não estar respondendo corretamente.

3. **Verifique se a porta 8080 está livre:**
   ```bash
   netstat -ano | findstr :8080
   ```

4. **Reinicie o simulador:**
   ```bash
   docker-compose restart simulator
   ```

5. **Verifique os logs do simulador:**
   ```bash
   docker-compose logs -f simulator
   ```

6. **Se o problema persistir, recrie o container:**
   ```bash
   docker-compose stop simulator
   docker-compose rm -f simulator
   docker-compose up -d simulator
   ```

### Limpar tudo e recomeçar

```bash
docker-compose down -v
docker-compose up -d
```
