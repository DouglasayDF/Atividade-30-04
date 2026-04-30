# Atividade-30-04
Entrega da atividade

Sistema de Corretoras e Ações

Este projeto é uma API REST desenvolvida em Java com Spring Boot, que gerencia corretoras e ações, integrando dados de mercado em tempo real através de APIs externas.

Tecnologias utilizadas
Java 17
Spring Boot
Spring WebFlux (WebClient)
Spring Data JPA
Hibernate
H2 / MySQL (opcional)
Lombok (opcional)

 APIs externas utilizadas
- Alpha Vantage
Endpoint: cotação de ações internacionais (EUA)
Uso: buscar preço de ações em USD

- BRAPI
Endpoint: ações brasileiras
Uso: cotação de ações em BRL

- BrasilAPI (CNPJ)
Uso: validação e enriquecimento de dados de corretoras

- ViaCEP
Uso: preenchimento automático de endereço por CEP

Padrões de projeto utilizados

Strategy Pattern (cotação BR / US)
Factory Pattern (CotacaoFactory)
DTO Pattern
Mapper Pattern
Exception Handler global

Funcionalidades
Corretoras
Criar corretora via CNPJ + CEP
Validação automática de dados externos
Listar / buscar / deletar
Ações
Cadastro de ações vinculadas à corretora
Cotação automática por mercado (BR ou US)
Atualização de preço em tempo real

Principais endpoints

Corretoras
#POST   /corretoras
#GET    /corretoras
#GET    /corretoras/{id}
#DELETE /corretoras/{id}

Ações
#POST   /acoes
#GET    /acoes
#GET    /acoes/{id}
#DELETE /acoes/{id}

Arquitetura
Camada Controller
Camada Service
Camada Repository
Integração externa via WebClient
Strategy para cotação por mercado
