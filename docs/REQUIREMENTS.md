# Requisitos do Sistema de Gerenciamento de Estacionamento

## Requisitos Funcionais

### RF01 - Inicializacao do Sistema
- O sistema deve buscar e armazenar os dados da garagem/vagas do simulador ao iniciar
- Deve processar a configuracao retornada pelo endpoint `GET /garage` do simulador
- Deve armazenar setores, vagas e precos base no banco de dados

### RF02 - Recepcao de Eventos via Webhook
- O sistema deve aceitar eventos POST no endpoint `http://localhost:3003/webhook`
- Deve processar eventos do tipo:
  - ENTRY: Entrada de veiculo na garagem
  - PARKED: Veiculo estacionado em uma vaga especifica
  - EXIT: Saida de veiculo da garagem
- Deve retornar HTTP 200 para todos os eventos recebidos

### RF03 - Gerenciamento de Entrada de Veiculos
- Ao receber evento ENTRY, o sistema deve:
  - Verificar disponibilidade de vagas no setor
  - Aplicar regras de preco dinamico baseado na lotacao
  - Marcar uma vaga como ocupada quando o veiculo for estacionado (PARKED)
  - Bloquear entrada se o estacionamento estiver 100% ocupado

### RF04 - Gerenciamento de Saida de Veiculos
- Ao receber evento EXIT, o sistema deve:
  - Calcular o tempo de permanencia
  - Aplicar regra de cobranca:
    - Primeiros 30 minutos sao gratis
    - Apos 30 minutos, cobrar tarifa fixa por hora (arredondar para cima)
    - Usar o preco base do setor (com ajuste dinamico aplicado na entrada)
  - Marcar a vaga como disponivel
  - Registrar a receita gerada

### RF05 - Regras de Preco Dinamico
- Aplicar desconto/aumento de preco na hora da entrada baseado na lotacao:
  - Lotacao < 25%: desconto de 10%
  - Lotacao 25% - 50%: preco normal (0% de ajuste)
  - Lotacao 50% - 75%: aumento de 10%
  - Lotacao 75% - 100%: aumento de 25%

### RF06 - Regra de Lotacao
- Com 100% de lotacao, fechar o setor
- So permitir novas entradas apos a saida de um veiculo ja estacionado

### RF07 - Consulta de Receita
- Endpoint `GET /revenue` deve retornar receita total por setor e data
- Deve aceitar parametros opcionais:
  - `date`: data no formato "YYYY-MM-DD"
  - `sector`: codigo do setor (ex: "A")
- Retornar valor em BRL com timestamp

## Requisitos Nao Funcionais

### RNF01 - Tecnologias
- Java 21 ou Kotlin 2.1.x
- Framework: Spring Boot
- Banco de dados: MySQL
- Versionamento: Git

### RNF02 - Performance
- O sistema deve processar eventos de webhook em tempo real
- Resposta de webhook deve ser inferior a 500ms
- Consulta de receita deve ser otimizada com indices adequados

### RNF03 - Confiabilidade
- O sistema deve tratar erros de forma adequada
- Deve validar dados de entrada
- Deve garantir consistencia de dados (transacoes)

### RNF04 - Testabilidade
- Codigo deve ser testavel com mocks
- Testes unitarios devem cobrir caminho feliz e alternativos
- Nao deve conectar diretamente ao banco em testes unitarios

### RNF05 - Arquitetura
- Seguir principios SOLID
- REST nivel 2 de maturidade
- Paginacao em respostas que retornam listas
- Clean Code na nomenclatura e estrutura

### RNF06 - Documentacao
- ADRs para documentar decisoes arquiteturais
- Codigo autoexplicativo (evitar comentarios desnecessarios)
