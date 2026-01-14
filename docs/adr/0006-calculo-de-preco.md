# ADR 0006: Calculo de Preco

## Status
Aceito

## Contexto
O sistema precisa calcular precos dinamicamente baseado em lotacao e tempo de permanencia.

## Decisao
- Aplicar ajuste de preco dinamico no momento da entrada (evento ENTRY)
- Armazenar o preco ajustado no registro do veiculo
- Calcular valor final na saida baseado no tempo de permanencia e preco ajustado

## Justificativa
- Preco dinamico e aplicado na entrada (conforme regra de negocio)
- Preco final depende do tempo de permanencia
- Armazenar preco ajustado garante consistencia mesmo se a lotacao mudar durante a permanencia

## Consequencias
- Positivas:
  - Regra de negocio clara e implementavel
  - Historico preciso de precos aplicados
- Negativas:
  - Precisa calcular lotacao no momento da entrada
