# ADR 0007: Arquitetura do Sistema

## Status
Aceito

## Contexto
Precisa-se definir a arquitetura geral do sistema de gerenciamento de estacionamento, considerando os requisitos funcionais e nao funcionais, tecnologias definidas (Java 21, Spring Boot 3.x, MySQL 8.x) e principios SOLID.

## Decisao
Adotar **Arquitetura em Camadas com elementos de Domain-Driven Design (DDD Lite)**, organizada por dominios de negocio.

### Estrutura de Pacotes

```
src/main/java/com/estapar/parking/
├── ParkingApplication.java
├── config/                     # Configuracoes e beans
├── domain/                     # Entidades e regras de dominio
│   ├── entity/
│   ├── enums/
│   └── exception/
├── application/                # Casos de uso e servicos
│   ├── service/
│   └── dto/
│       ├── request/
│       └── response/
├── infrastructure/             # Acesso a dados e integracoes
│   ├── repository/
│   └── client/
└── api/                        # Controllers REST
```

### Camadas e Responsabilidades

| Camada | Responsabilidade |
|--------|------------------|
| **api** | Controllers REST, recebimento de requisicoes HTTP |
| **application** | Servicos com logica de negocio, DTOs de request/response |
| **domain** | Entidades JPA, enums, excecoes de dominio |
| **infrastructure** | Repositories JPA, clientes HTTP externos |
| **config** | Configuracoes Spring, inicializacao de dados |

### Componentes Principais

- **WebhookController**: Recebe eventos ENTRY, PARKED, EXIT
- **RevenueController**: Endpoint GET /revenue
- **GarageService**: Inicializacao e estado da garagem
- **VehicleService**: Logica de entrada, estacionamento e saida
- **PricingService**: Calculo de preco dinamico baseado em lotacao
- **RevenueService**: Consulta de faturamento por setor e data

### Padroes Aplicados

- **Repository Pattern**: Abstracao do acesso a dados via Spring Data JPA
- **Service Layer**: Encapsulamento da logica de negocio
- **DTO Pattern**: Separacao entre representacao da API e modelo de dominio
- **Dependency Injection**: Inversao de controle via Spring IoC

## Justificativa

1. **Adequada ao tamanho do projeto**: Nao e over-engineering para o escopo definido
2. **Testabilidade**: Cada camada e testavel isoladamente com mocks
3. **Padrao Spring Boot**: Aproveita convencoes e facilidades do framework
4. **Separacao de responsabilidades**: Facilita manutencao e evolucao
5. **Conformidade com requisitos**: Atende SOLID, Clean Code e REST nivel 2

### Alternativas Consideradas e Rejeitadas

| Arquitetura | Motivo da Rejeicao |
|-------------|-------------------|
| Microsservicos | Over-engineering - projeto e pequeno e coeso |
| CQRS/Event Sourcing | Complexidade desnecessaria para o volume esperado |
| Clean Architecture completa | Muitas camadas para o tamanho do projeto |
| Hexagonal Architecture | Adiciona complexidade sem beneficio proporcional |

## Consequencias

### Positivas
- Codigo organizado e navegavel
- Testes unitarios simples com mocks
- Familiar para desenvolvedores Spring Boot
- Facil adicionar novos endpoints ou regras de negocio
- Baixa curva de aprendizado

### Negativas
- Mais pacotes/arquivos para gerenciar comparado a estrutura flat
- Pode haver tentacao de criar dependencias circulares entre camadas (mitigado por revisao de codigo)

## Stack Tecnologica

- Java 21 (LTS)
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8.x
- Maven ou Gradle
- JUnit 5 + Mockito para testes
