# ADR 0004: Modelo de Dados

## Status
Aceito

## Contexto
Precisa-se definir como armazenar informacoes de garagem, setores, vagas, veiculos e receitas.

## Decisao
Criar as seguintes entidades:
- Garage: Configuracao geral da garagem
- Sector: Setor da garagem (divisao logica)
- Spot: Vaga individual (com coordenadas)
- Vehicle: Veiculo estacionado (placa, entrada, saida, vaga ocupada)
- Revenue: Registro de receita por setor e data

## Justificativa
- Modelo normalizado evita redundancia
- Facilita consultas de receita por setor e data
- Permite rastreamento completo de veiculos
- Suporta regras de lotacao por setor

## Consequencias
- Positivas:
  - Dados organizados e consultaveis
  - Suporta relatorios e analises
- Negativas:
  - Multiplas tabelas para gerenciar
  - Necessidade de joins em algumas consultas
