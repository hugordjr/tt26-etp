# Database Migrations

Este diretório contém as migrations do banco de dados usando Flyway.

## Estrutura das Migrations

As migrations são executadas na ordem numérica:

1. **V1__create_garages_table.sql** - Cria a tabela `garages`
2. **V2__create_sectors_table.sql** - Cria a tabela `sectors` com índice único em `code`
3. **V3__create_spots_table.sql** - Cria a tabela `spots` com foreign key para `sectors`
4. **V4__create_vehicles_table.sql** - Cria a tabela `vehicles` com foreign keys para `sectors` e `spots`
5. **V5__create_revenues_table.sql** - Cria a tabela `revenues` com foreign key para `sectors`

## Convenções

- Nomenclatura: `V{version}__{description}.sql`
- Versões devem ser sequenciais e únicas
- Descrições devem ser descritivas e em snake_case
- Todas as tabelas usam `utf8mb4` charset e `utf8mb4_unicode_ci` collation
- Foreign keys usam `ON DELETE CASCADE`

## Índices Criados

### sectors
- `idx_sectors_code` - Índice único no campo `code`

### spots
- `idx_spots_sector_id` - Índice no campo `sector_id`
- `idx_spots_occupied` - Índice no campo `occupied` (para busca de vagas disponíveis)

### vehicles
- `idx_vehicles_license_plate` - Índice no campo `license_plate`
- `idx_vehicles_sector_id` - Índice no campo `sector_id`
- `idx_vehicles_spot_id` - Índice no campo `spot_id`
- `idx_vehicles_status` - Índice no campo `status`

### revenues
- `idx_revenues_sector_id` - Índice no campo `sector_id`
- `idx_revenues_date` - Índice no campo `date`
- `idx_revenues_sector_date` - Índice composto em `sector_id` e `date` (otimiza consultas de receita)

## Execução

As migrations são executadas automaticamente na inicialização da aplicação via Flyway.

Para executar manualmente:
```bash
mvn flyway:migrate
```

Para verificar o status:
```bash
mvn flyway:info
```
