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
- MySQL 8.x
- Docker (para o simulador)

### Iniciar o Simulador

```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

### Configurar o Banco de Dados

Criar banco de dados MySQL e configurar em `application.yml`.

### Executar a Aplicacao

```bash
./gradlew bootRun
```

## API Endpoints

### Webhook

- POST `http://localhost:3003/webhook` - Recebe eventos do simulador

### Consulta de Receita

- GET `/revenue?date=2025-01-01&sector=A` - Consulta receita por data e setor
