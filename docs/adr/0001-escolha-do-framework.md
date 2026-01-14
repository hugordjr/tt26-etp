# ADR 0001: Escolha do Framework

## Status
Aceito

## Contexto
O projeto precisa de um framework Java/Kotlin para construir uma API REST que processa eventos de webhook e gerencia dados de estacionamento.

## Decisao
Utilizar Spring Boot como framework principal.

## Justificativa
- Ecossistema maduro e amplamente utilizado
- Excelente suporte para REST APIs
- Integracao nativa com MySQL via Spring Data JPA
- Facilidade para testes com Spring Boot Test
- Documentacao extensa e comunidade ativa
- Suporte completo para Java 21 e Kotlin

## Consequencias
- Positivas:
  - Desenvolvimento mais rapido
  - Muitos recursos prontos (JPA, Web, Validation)
  - Facil integracao com outras ferramentas
- Negativas:
  - Framework mais pesado
  - Startup time maior (nao critico para este projeto)
