package com.curso.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@Configuration
public class SchemaCompatibilityConfig {

    @Bean
    ApplicationRunner ajustarSchemaExistente(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute(
                    "ALTER TABLE acoes ALTER COLUMN corretora_id DROP NOT NULL"
            );

            jdbcTemplate.update(
                    "UPDATE lancamentos_financeiros SET moeda = 'BRL' WHERE moeda IS NULL"
            );

            List<Map<String, Object>> lancamentosDeAcoes = jdbcTemplate.queryForList(
                    """
                    SELECT id, descricao
                    FROM lancamentos_financeiros
                    WHERE tipo IN ('COMPRA_ACAO', 'VENDA_ACAO')
                    """
            );

            for (Map<String, Object> lancamento : lancamentosDeAcoes) {
                String descricao = String.valueOf(lancamento.get("DESCRICAO"));
                String ticker = descricao
                        .replaceFirst("^Compra de ", "")
                        .replaceFirst("^Venda de ", "");

                try {
                    String moeda = jdbcTemplate.queryForObject(
                            "SELECT moeda FROM acoes WHERE ticker = ?",
                            String.class,
                            ticker
                    );
                    jdbcTemplate.update(
                            "UPDATE lancamentos_financeiros SET moeda = ? WHERE id = ?",
                            moeda,
                            lancamento.get("ID")
                    );
                } catch (EmptyResultDataAccessException ignored) {
                    // Mantém BRL para lançamentos legados sem uma ação correspondente.
                }
            }

            List<Map<String, Object>> vendasSemOrigem = jdbcTemplate.queryForList(
                    """
                    SELECT id, usuario_id, acao_id, quantidade
                    FROM operacoes
                    WHERE tipo = 'VENDA'
                      AND compra_origem_id IS NULL
                    ORDER BY data_operacao, id
                    """
            );

            for (Map<String, Object> venda : vendasSemOrigem) {
                List<Long> comprasCompativeis = jdbcTemplate.queryForList(
                        """
                        SELECT compra.id
                        FROM operacoes compra
                        WHERE compra.tipo = 'COMPRA'
                          AND compra.usuario_id = ?
                          AND compra.acao_id = ?
                          AND compra.data_operacao <= (
                              SELECT venda.data_operacao
                              FROM operacoes venda
                              WHERE venda.id = ?
                          )
                          AND compra.quantidade - COALESCE((
                              SELECT SUM(vinculada.quantidade)
                              FROM operacoes vinculada
                              WHERE vinculada.tipo = 'VENDA'
                                AND vinculada.compra_origem_id = compra.id
                          ), 0) >= ?
                        ORDER BY compra.data_operacao DESC, compra.id DESC
                        LIMIT 1
                        """,
                        Long.class,
                        venda.get("USUARIO_ID"),
                        venda.get("ACAO_ID"),
                        venda.get("ID"),
                        venda.get("QUANTIDADE")
                );

                if (!comprasCompativeis.isEmpty()) {
                    jdbcTemplate.update(
                            "UPDATE operacoes SET compra_origem_id = ? WHERE id = ?",
                            comprasCompativeis.get(0),
                            venda.get("ID")
                    );
                }
            }
        };
    }
}
