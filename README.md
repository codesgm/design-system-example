# Sistema de Importação Massiva + Dashboards Internos

Sistema interno para ingestão massiva de lançamentos financeiros via CSV e consulta em dashboards com filtros, paginação e agregações.

[Diagrama da arquitetura](https://drive.google.com/file/d/1DYv6b3m6kHFHYPB3iY6jOBy-pxL15tjX/view?usp=sharing)

---

## Considerações antes da arquitetura

### Requisitos e suposições

Os requisitos do enunciado são intencionalmente abertos. Por não haver definições rígidas de volume, frequência e perfil de uso, a arquitetura foi desenhada de forma pragmática para o cenário atual, mas com diagramas e decisões que representam evoluções futuras — permitindo melhorar a stack de leitura ou de escrita conforme a necessidade real se apresente.
[As evoluções de arquitetura podem ser vistas aqui.](https://drive.google.com/file/d/1DYv6b3m6kHFHYPB3iY6jOBy-pxL15tjX/view?usp=sharing)
### Escolha do banco de dados

Banco SQL (PostgreSQL) pela natureza financeira dos dados, onde **consistência transacional (ACID)** é inegociável. Materialized views não foram utilizadas no momento por não serem necessárias ainda — e caso fossem, a decisão dependeria muito dos requisitos reais: o custo de reprocessar as views é alto e precisa ser justificado. Se a demanda de leitura crescer, o caminho seria um banco específico para leitura (como ClickHouse). Se a frequência de leitura for baixa, os dados agregados poderiam ser atualizados com menor frequência, pesando na consistência eventual.

### Idempotência de arquivo

Através do binário do arquivo é gerado um hash SHA-256 que é salvo no banco, impedindo o reprocessamento do mesmo arquivo — mesmo que renomeado.

### O que senti falta no enunciado

Senti falta da **história do cliente**. Sem entender o fluxo real de uso, a arquitetura é montada com base em especulações. Antes de projetar recursos e infraestrutura, precisaria de respostas para:

- **Quem faz as importações?** É uma equipe financeira interna? Um sistema automatizado? Quantas pessoas?
- **Quando acontecem?** Há horários de pico? É diário, semanal, sob demanda? Concentrado no fechamento mensal?
- **Qual o volume real?** 100k linhas por arquivo é muito diferente de 10M. Quantos arquivos por dia?  Qual prazo temos para processar? 
- **Quem acompanha os dashboards?** São as mesmas pessoas que importam? Quantos usuários simultâneos? 
- **Qual a frequência de atualização aceitável?** Os dados precisam aparecer em tempo real após a importação, ou um delay de minutos/horas é aceitável?
- **Qual o comportamento esperado com erros?** O usuário corrige e reimporta?
- **Há integração com outros sistemas?** Os dados vêm de ERPs, bancos, conciliação?

Sem essas respostas, as decisões de escala (quantidade de workers, tamanho do pool de conexões, necessidade de cache, separação de banco de leitura) são estimativas que precisariam ser validadas com dados reais de uso.

---

## Como rodar

Pré-requisitos: Docker e Docker Compose.

```bash
cd docker
cp .env.example .env  (pode remover o .example que irá funcionar)
docker compose up -d --build
```

| Serviço | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Swagger (API docs) | http://localhost:8080/swagger-ui.html |

### Planilhas para teste

- [1M de linhas (~85MB)](https://drive.google.com/file/d/1HbyMT0b9_LPKdrlTEFwuXEHpN--fNHbZ/view?usp=sharing)
- [100k de linhas (~8MB)](https://drive.google.com/file/d/1fFEJnnFRDnou4PR_-3cy86Rb6Arr9FmE/view?usp=sharing)

---

## Stack

| Componente | Tecnologia | Justificativa |
|---|---|---|
| Backend | Java 21, Spring Boot 4.0.6 | ACID, ecossistema maduro, performance com batch inserts |
| Banco | PostgreSQL 16 | `ON CONFLICT` para idempotência, materialized views |
| Fila | AWS SQS (LocalStack local) | Desacopla upload do processamento, retry nativo |
| Storage | AWS S3 (LocalStack local) | Arquivos brutos para auditoria e reprocessamento |
| Frontend | React + Vite + Recharts | SPA leve, Recharts para dashboards futuros |
| Migrations | Flyway | Versionamento de schema, rollback controlado |

---

## Arquitetura Geral

```
Usuário → Frontend (React)
              │
              ▼
         API Backend (Spring Boot)
              │
         ┌────┴────┐
         ▼         ▼
       S3        SQS ──→ Worker (3 threads paralelas)
    (arquivo)  (fila)         │
                              ▼
                         PostgreSQL
                    ┌─────────┼─────────┐
                    │         │         │
               staging   transactions  financial_accounts
              (temporária) (definitiva)   (saldos)
```

---

## Pipeline de Ingestão

### Fluxo completo

1. **Upload**: usuário envia CSV via frontend → backend recebe multipart → calcula SHA-256 → conta linhas → salva no S3 → cria registro `imports` (PENDING) → envia mensagem SQS
2. **Processamento**: worker consome SQS → faz claim do import → download streaming do S3 → lê CSV em chunks de 10k linhas → valida cada linha → batch insert no staging (`ON CONFLICT DO NOTHING`)
3. **Validação batch**: resolve `financial_account_id` via JOIN com `financial_accounts` → registra erros referenciais
4. **Promoção**: move dados validados do staging para `transactions` → atualiza saldos → limpa staging
5. **Conclusão**: status COMPLETED (ou FAILED se >10% de erros)

### Estados da importação

```
PENDING → PROCESSING → VALIDATING → COMPLETED
               │              │
               └→ FAILED ←────┘
```

### Formato do CSV

8 colunas, separador vírgula, header obrigatório, datas em `dd/MM/yyyy`:

```csv
DESCRICAO,CONTA BANCARIA,DATA COMPETENCIA,DATA VENCIMENTO,DATA LANCAMENTO,VALOR TOTAL,NSU,TIPO
Despesa 1,Bradesco,28/04/2026,30/04/2026,10/04/2026,12390,N123LK,Receita
```

- **TIPO**: `Receita` ou `Despesa`
- **NSU**: chave de unicidade por empresa — duplicatas são ignoradas
- **CONTA BANCARIA**: deve corresponder a uma conta cadastrada (Bradesco, Itau, Banco do Brasil, Santander, Caixa, Nubank, Inter, Safra)

### Limite de arquivo: 400MB

---

## Modelagem de Dados

### Entidades

| Tabela | Descrição |
|---|---|
| `companies` | Tenant — toda query filtra por `company_id` |
| `financial_accounts` | Contas bancárias por empresa, com saldo pré-calculado |
| `imports` | Registro de cada importação (status, progresso, timestamps) |
| `staging_transactions` | Tabela temporária sem FK — recebe dados brutos do CSV |
| `transactions` | Tabela definitiva com FK — dados validados e promovidos |
| `import_errors` | Erros por linha (formato, validação, referência) |
| `mv_balance_by_account` | Materialized view — agregação mensal por conta |

### Decisões de modelagem

- **Staging + Promoção**: dados entram sem FK no staging, são validados em batch, e só depois movidos para a tabela definitiva. Dashboards nunca veem dados parciais.
- **Idempotência em dois níveis**: hash SHA-256 do arquivo (impede reprocessamento) + NSU por empresa (impede duplicidade de linha).
- **Saldo pré-calculado**: `financial_accounts.balance` é atualizado após cada importação. Consulta de saldo é um simples SELECT, sem SUM.

---

## Consistência e Concorrência

- **Arquivo duplicado**: constraint `UNIQUE(company_id, file_hash)` — mesmo arquivo renomeado é detectado pelo hash do conteúdo
- **Linha duplicada**: `ON CONFLICT (company_id, nsu) DO NOTHING` — NSU repetido é ignorado silenciosamente
- **Isolamento**: dashboards leem apenas da tabela `transactions` (definitiva) — nunca veem dados do staging
- **Optimistic locking**: campo `version` na tabela `imports` com `@Version` do JPA
- **Transações por chunk**: cada chunk de 10k linhas é uma transação independente — rollback máximo de 10k linhas, não do arquivo inteiro

---

## APIs

### Importação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/imports` | Upload CSV (multipart + companyId) → 202 Accepted |
| GET | `/api/imports?companyId=&status=` | Lista importações (paginado) |
| GET | `/api/imports/{id}` | Detalhe (status, progresso, contadores) |
| GET | `/api/imports/{id}/errors` | Erros da importação (paginado) |
| DELETE | `/api/imports/reset?companyId=` | Limpa todos os dados (apenas para testes) |

### Transações

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/transactions` | Listagem com filtros: companyId, financialAccountId, from, to, nsu, tipo, search (paginado) |

### Dashboards

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/dashboards/balances?companyId=` | Saldo atual por conta |
| GET | `/api/dashboards/monthly-summary?companyId=&year=` | Totais mensais (materialized view) |

### Métricas

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/metrics` | Pressão de escrita, conexões, operações do banco, status das importações |

Autenticação: HTTP Basic (`admin` / `admin`). Swagger UI acessível sem autenticação.

---

## Observabilidade

### Métricas em tempo real (aba Métricas no frontend)

- **Pressão de escrita** (score 0-100): escritas ativas, lock waits, queries lentas, uso de conexões
- **Banco**: conexões, tamanho, cache hit ratio, inserts/updates/deletes
- **Tabelas**: linhas, tamanho em disco, operações por tabela
- **Importações**: pendentes, processando, concluídas, falhas

### Logs

Logs estruturados no container: `docker logs -f massivo-app`

---

## Trade-offs

| Decisão | Escolha | Alternativa | Justificativa |
|---|---|---|---|
| Banco único | PostgreSQL para escrita e leitura | CQRS com banco analítico separado | Volume atual não justifica dois bancos |
| Staging + promoção | Tabela temporária sem FK | FK direto na ingestão | FK por linha em milhões de inserts reduz throughput em ~40-60% |
| Chunks de 10k | Commit por chunk | Transação única por arquivo | Rollback de 1M linhas é catastrófico; chunks permitem retomada |
| Processamento assíncrono | Fila SQS + workers | Processamento síncrono no upload | Arquivo de 1M linhas levaria minutos — HTTP timeout |
| Materialized view | MV para agregação mensal | Query direta | Compensa quando muitos usuários consultam o mesmo dashboard |
| Índices vs MV | Índices para filtros dinâmicos, MV para agregações fixas | Índices pesados em tudo | MV isola custo de agregação do custo de escrita |

---

## Gaps identificados

Detalhados em [`arquitetura.md`](arquitetura.md), seção 8:

1. **Reprocessamento e rollback** de importações com dados incorretos
2. **Backpressure** contra sobrecarga (rate limiting, circuit breaker)
3. **Disaster recovery** (WAL archiving, PITR)
4. **Multi-tenancy** com Row-Level Security
5. **Testes de carga** pré-produção
6. **Conformidade regulatória** (SOX, BACEN, LGPD)

---

## Estrutura do projeto

```
├── docker/
│   ├── docker-compose.yml    # PostgreSQL + LocalStack + Backend + Frontend
│   ├── .env.example
│   └── .env
├── demo/                     # Backend Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/massivo/
│       ├── common/           # Exceções, Security, AWS (S3, SQS)
│       └── system/
│           ├── company/
│           ├── financialaccount/
│           ├── importjob/    # Controller, Service, Consumer, Executor, Processors
│           ├── transaction/
│           ├── dashboard/
│           └── metrics/
├── frontend/                 # React + Vite
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/pages/            # Imports, Dashboard, Metrics
├── script/
│   ├── s3-init.sh
│   └── sqs-init.sh
├── arquitetura.md            # Documento de arquitetura completo
├── requisitos.md             # Enunciado do teste
└── README.md
```
