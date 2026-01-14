# ADR 0003: Estrutura de Camadas

## Status
Aceito

## Contexto
Precisa-se definir a arquitetura em camadas do sistema para organizar o codigo seguindo principios SOLID.

## Decisao
Utilizar arquitetura em camadas com separacao clara:
- Controller: Endpoints REST e Webhook
- Service: Logica de negocio
- Repository: Acesso a dados
- Entity/Model: Entidades JPA e DTOs
- Config: Configuracoes e beans

## Justificativa
- Separacao clara de responsabilidades
- Facilita testes unitarios com mocks
- Segue padroes Spring Boot
- Permite evolucao independente de cada camada

## Consequencias
- Positivas:
  - Codigo organizado e manutenivel
  - Testes mais simples
  - Reutilizacao de codigo
- Negativas:
  - Mais arquivos para gerenciar
  - Pode ser over-engineering para projetos muito pequenos
