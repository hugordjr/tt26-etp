# ADR 0005: Processamento de Eventos

## Status
Aceito

## Contexto
O sistema recebe eventos assincronos via webhook (ENTRY, PARKED, EXIT) que precisam ser processados.

## Decisao
Processar eventos de forma sincrona no controller do webhook, com validacoes e regras de negocio no service.

## Justificativa
- Simplicidade para o escopo do projeto
- Garantia de processamento imediato
- Facilita tratamento de erros
- Nao requer infraestrutura adicional (message broker)

## Consequencias
- Positivas:
  - Implementacao simples
  - Sem dependencias externas
  - Facil de debugar
- Negativas:
  - Se o processamento demorar, pode causar timeout no webhook
  - Nao escala bem para alto volume (nao e requisito atual)
