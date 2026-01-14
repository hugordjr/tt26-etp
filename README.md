# Sistema de Gerenciamento de Estacionamento

Sistema backend para gerenciar estacionamento: controle de vagas, entrada/saida de veiculos e calculo de receita.

## Escopo

### Objetivo

Construir um sistema backend simples para gerenciar um estacionamento: controlar vagas disponiveis, entrada/saida de veiculos e calcular receita.

As garagens tem um unico grupo de cancelas que ficam na entrada da garagem. Os setores sao divisoes logicas e nao fisicas para organizacao do contratante do pool de vagas.

### O que fazer

- Use Java 21, Kotlin 2.1.x
- Use Spring Boot 3.x
- Use MySQL
- Use git para versionamento
- Pode usar AI se quiser

### Simulador

Inicie o simulador:

```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

Busque a configuracao da garagem com `GET /garage`. O simulador enviara eventos de veiculos para seu webhook.

### Requisitos Funcionais

- Ao iniciar, busque e armazene os dados da garagem/vagas do simulador
- Implemente uma API REST:
  - `GET /revenue` — receita total por setor e data
- Aceite POSTs no webhook `http://localhost:3003/webhook` para eventos ENTRY, PARKED e EXIT

### Regras de Negocio

- Ao entrar um veiculo, marque uma vaga como ocupada
- Ao sair, marque a vaga como disponivel e calcule o valor:
  - Primeiros 30 minutos sao gratis
  - Apos 30 minutos, cobre uma tarifa fixa por hora, inclusive a primeira hora (use `basePrice` da garagem, arredonde para cima)
- Se o estacionamento estiver cheio, nao permita novas entradas ate liberar uma vaga

#### Regra de preco dinamico

1. Com lotacao menor que 25%, desconto de 10% no preco, na hora da entrada
2. Com lotacao menor ou igual a 50%, desconto de 0% no preco, na hora da entrada
3. Com lotacao menor ou igual a 75%, aumentar o preco em 10%, na hora da entrada
4. Com lotacao menor ou igual a 100%, aumentar o preco em 25%, na hora da entrada

#### Regra de lotacao

Com 100% de lotacao, fechar o setor e so permitir mais carros com a saida de um ja estacionado.

### O que sera avaliado

- Clareza e estrutura do codigo
- Uso de REST e banco de dados
- Tratamento de eventos e regras de negocio
- Tratamento basico de erros e testes

### Detalhes da API

#### Eventos do Webhook

POST para `http://localhost:3003/webhook`:

**ENTRY**
```json
{
  "license_plate": "ZUL0001",
  "entry_time": "2025-01-01T12:00:00.000Z",
  "event_type": "ENTRY"
}
```
Response: HTTP 200

**PARKED**
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```
Response: HTTP 200

**EXIT**
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2025-01-01T12:00:00.000Z",
  "event_type": "EXIT"
}
```
Response: HTTP 200

#### Configuracao da Garagem

GET `/garage`:
```json
{
  "garage": [
    {
      "sector": "A",
      "basePrice": 10.0,
      "max_capacity": 100
    }
  ],
  "spots": [
    {
      "id": 1,
      "sector": "A",
      "lat": -23.561684,
      "lng": -46.655981
    }
  ]
}
```

#### API do Projeto a ser implementada

##### Consulta de faturamento

**GET** `/revenue`

Request
```json
{
  "date": "2025-01-01",
  "sector": "A"
}
```

Response
```json
{
  "amount": 0.00,
  "currency": "BRL",
  "timestamp": "2025-01-01T12:00:00.000Z"
}
```

## Tecnologias

- Kotlin 2.1.x
- Spring Boot 3.x
- MySQL 8.x
- Java 21

## Documentacao

- Requisitos funcionais e nao funcionais em `docs/REQUIREMENTS.md`
- Architecture Decision Records em `docs/adr/`

## Como Executar

### Pre-requisitos

- Java 21
- Maven 3.8+
- Docker e Docker Compose

### Opção 1: Usando Docker Compose (Recomendado)

1. **Iniciar MySQL e Simulador via Docker Compose:**

```bash
docker-compose up -d
```

Isso inicia:
- MySQL 8.0 na porta 3306
- Simulador da garagem na porta 8080

2. **Executar a aplicação:**

```bash
mvn spring-boot:run
```

As migrations do banco de dados são executadas automaticamente na inicialização via Flyway.

### Opção 2: Configuração Manual

1. **Iniciar o Simulador:**

```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

2. **Configurar o Banco de Dados MySQL:**

Criar banco de dados MySQL e configurar em `application.yml` ou via variáveis de ambiente.

3. **Executar a Aplicação:**

```bash
mvn spring-boot:run
```

### Variáveis de Ambiente (Opcional)

A aplicação suporta variáveis de ambiente para sobrescrever configurações. Exporte as variáveis antes de executar:

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

Alternativamente, você pode criar um profile Spring Boot (`application-{profile}.yml`) para diferentes ambientes.

Para mais detalhes sobre Docker, consulte [docker/README.md](docker/README.md).

## API Endpoints

### Health Check

- GET `/health` - Verifica o status da aplicacao, banco de dados e simulador

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-01-14T19:30:00.000Z",
  "application": {
    "status": "UP",
    "message": "Aplicacao em execucao"
  },
  "database": {
    "status": "UP",
    "message": "Conexao com banco de dados OK"
  },
  "simulator": {
    "status": "UP",
    "message": "Simulador acessivel"
  }
}
```

O status geral sera `UP` apenas se todos os componentes estiverem funcionando. Caso contrario, retornara `DOWN`.

### Webhook

- POST `http://localhost:3003/webhook` - Recebe eventos do simulador

### Consulta de Receita

- GET `/revenue?date=2025-01-01&sector=A` - Consulta receita por data e setor

## Documentacao da API

A documentacao completa da API esta disponivel via Swagger UI:

- **Swagger UI**: http://localhost:3003/swagger-ui.html
- **OpenAPI JSON**: http://localhost:3003/v3/api-docs
