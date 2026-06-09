# Exemplos para Postman

Importe o arquivo `Gestao-Acoes.postman_collection.json` no Postman.

## Configuração

- Aplicação: `http://localhost:8080`
- Header para requisições com body: `Content-Type: application/json`
- Enums devem ser escritos em maiúsculas:
  - Mercado: `BR` ou `US`
  - Moeda: `BRL` ou `USD`
  - Operação: `COMPRA` ou `VENDA`
- Valores decimais no JSON usam ponto: `32.50`

A coleção possui a variável `{{baseUrl}}` e salva automaticamente os IDs criados.

## Ordem recomendada

1. Testar API.
2. Cadastrar ou carregar uma corretora.
3. Cadastrar uma ação brasileira e/ou americana.
4. Criar um usuário.
5. Depositar saldo na moeda da ação.
6. Registrar uma compra em `/operacoes`.
7. Consultar carteira, posição e saldos.
8. Para vender, usar o ID da compra no campo `compraOrigemId`.
9. Executar exclusões por último.

## Corretoras

### Cadastrar

`POST {{baseUrl}}/corretoras`

```json
{
  "cnpj": "00000000000191",
  "cep": "01310100",
  "numero": "122",
  "complemento": "Sala 12"
}
```

O CNPJ precisa ter 14 dígitos, o CEP 8 dígitos e o CNPJ não pode estar cadastrado.

### Consultar

```text
GET {{baseUrl}}/corretoras
GET {{baseUrl}}/corretoras/{{corretoraId}}
GET {{baseUrl}}/corretoras/cnpj/{{cnpj}}
GET {{baseUrl}}/corretoras/{{corretoraId}}/validacao
GET {{baseUrl}}/corretoras/padrao
```

### Cadastrar lista padrão

```text
POST {{baseUrl}}/corretoras/padrao
```

### Excluir

```text
DELETE {{baseUrl}}/corretoras/{{corretoraId}}
```

## Ações

### Cadastrar ação brasileira

`POST {{baseUrl}}/acoes`

```json
{
  "ticker": "PETR4",
  "mercado": "BR",
  "corretoraId": 1
}
```

### Cadastrar ação americana

```json
{
  "ticker": "AAPL",
  "mercado": "US",
  "corretoraId": 1
}
```

### Consultar e atualizar

```text
GET {{baseUrl}}/acoes
GET {{baseUrl}}/acoes/{{acaoIdBR}}
GET {{baseUrl}}/acoes/ticker/{{tickerBR}}
PUT {{baseUrl}}/acoes/{{acaoIdBR}}/atualizar-cotacao
GET {{baseUrl}}/acoes/{{acaoIdBR}}/historico
GET {{baseUrl}}/acoes/brapi/list?search=PETR&limit=10&page=1
DELETE {{baseUrl}}/acoes/{{acaoIdBR}}
```

O `PUT` de atualização não possui body. A exclusão retorna `409` quando há operações vinculadas.

## Usuários

### Criar

`POST {{baseUrl}}/usuarios`

```json
{
  "nome": "Usuário Postman"
}
```

### Consultar e excluir

```text
GET {{baseUrl}}/usuarios
GET {{baseUrl}}/usuarios/{{usuarioId}}
DELETE {{baseUrl}}/usuarios/{{usuarioId}}
```

A exclusão também remove operações e lançamentos financeiros do usuário.

## Financeiro

### Depositar reais

`POST {{baseUrl}}/financeiro/deposito/{{usuarioId}}`

```json
{
  "valor": 10000.00,
  "moeda": "BRL"
}
```

### Depositar dólares

```json
{
  "valor": 5000.00,
  "moeda": "USD"
}
```

### Consultar os dois saldos

```text
GET {{baseUrl}}/financeiro/saldo/{{usuarioId}}
```

## Operações da carteira

### Comprar

`POST {{baseUrl}}/operacoes`

```json
{
  "acaoId": 1,
  "usuarioId": 1,
  "tipo": "COMPRA",
  "quantidade": 10,
  "precoUnitario": 32.50
}
```

A moeda debitada é definida pela ação. É necessário ter saldo suficiente em `BRL` ou `USD`.

### Vender

```json
{
  "acaoId": 1,
  "usuarioId": 1,
  "tipo": "VENDA",
  "quantidade": 10,
  "precoUnitario": 41.22,
  "compraOrigemId": 85
}
```

`compraOrigemId` deve ser o `id` retornado pela operação de `COMPRA` em `/operacoes`.

### Consultar

```text
GET {{baseUrl}}/operacoes
GET {{baseUrl}}/operacoes/posicao/{{acaoIdBR}}
GET {{baseUrl}}/operacoes/carteira/{{usuarioId}}
```

## Compra legada

`POST {{baseUrl}}/compras`

```json
{
  "acaoId": 1,
  "quantidade": 5
}
```

Essa rota é separada: grava na tabela `compras`, mas não movimenta saldo nem carteira do usuário.

## Documentação da API

```text
GET {{baseUrl}}/v3/api-docs
GET {{baseUrl}}/swagger-ui/index.html
```

