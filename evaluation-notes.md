Não temos um prazo definido. Damos preferência à qualidade do exercício. Vou deixar te algumas notas para teres em conta na realização do mesmo:



Estrutura e organização



Código bem organizado, com separação clara de responsabilidades entre camadas (controllers, serviços, repositórios, DTOs).

Operações CRUD completas e coerentes para as entidades pedidas.



Persistência



Uso adequado de JPA e gestão de migrações da base de dados (por exemplo, Flyway ou Liquibase).

Validação dos pedidos de entrada.



Segurança e configuração



Autenticação implementada de forma adequada (por exemplo, JWT).

Credenciais e configurações sensíveis fora do código.

Separação entre ambientes (desenvolvimento e produção).



Qualidade da API



Documentação da API (Swagger/OpenAPI).

Tratamento centralizado e consistente de erros.



Pontos a que deve dar especial atenção



Semântica das operações de atualização: defina e documente claramente se uma atualização substitui o recurso por inteiro (PUT) ou o altera parcialmente (PATCH). Garanta que quem usa a API não perde dados associados sem querer, por exemplo ao omitir uma lista de entidades filhas.

Consistência entre endpoints: se a mesma entidade puder ser alterada por mais do que um endpoint (por exemplo, diretamente ou através da entidade-pai), o comportamento deve ser o mesmo. Em particular, a identidade dos registos deve ser preservada e não podem surgir registos novos de forma inesperada.

Regras de integridade: pense nas regras de negócio (por exemplo, unicidade de identificadores ou números) e garanta que são aplicadas de forma coerente na API, na lógica de negócio e na base de dados.

Justificação das decisões: um README curto com as principais decisões técnicas e os pressupostos assumidos é muito valorizado.

