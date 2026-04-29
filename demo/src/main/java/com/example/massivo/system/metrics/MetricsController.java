package com.example.massivo.system.metrics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final JdbcTemplate jdbc;

    public MetricsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public Map<String, Object> metrics() {
        return Map.of(
                "database", dbStats(),
                "tables", tableStats(),
                "imports", importStats(),
                "writePressure", writePressure()
        );
    }

    private Map<String, Object> dbStats() {
        var connections = jdbc.queryForMap("""
                SELECT count(*) as total,
                       count(*) FILTER (WHERE state = 'active') as active,
                       count(*) FILTER (WHERE state = 'idle') as idle
                FROM pg_stat_activity WHERE datname = current_database()
                """);

        var dbSize = jdbc.queryForObject(
                "SELECT pg_size_pretty(pg_database_size(current_database()))", String.class);

        var cacheHit = jdbc.queryForMap("""
                SELECT round(100.0 * sum(blks_hit) / nullif(sum(blks_hit + blks_read), 0), 2) as hit_ratio
                FROM pg_stat_database WHERE datname = current_database()
                """);

        var txStats = jdbc.queryForMap("""
                SELECT xact_commit as commits, xact_rollback as rollbacks,
                       tup_inserted as inserts, tup_updated as updates, tup_deleted as deletes,
                       tup_returned as rows_returned
                FROM pg_stat_database WHERE datname = current_database()
                """);

        return Map.of(
                "connections", connections,
                "size", dbSize,
                "cacheHitRatio", cacheHit.get("hit_ratio"),
                "transactions", txStats
        );
    }

    private Object tableStats() {
        return jdbc.queryForList("""
                SELECT relname as table_name,
                       n_live_tup as row_count,
                       pg_size_pretty(pg_total_relation_size(relid)) as total_size,
                       n_tup_ins as inserts,
                       n_tup_upd as updates,
                       n_tup_del as deletes
                FROM pg_stat_user_tables
                WHERE schemaname = 'public'
                  AND relname IN ('transactions', 'staging_transactions', 'imports', 'import_errors', 'financial_accounts')
                ORDER BY n_live_tup DESC
                """);
    }

    private Object importStats() {
        return jdbc.queryForMap("""
                SELECT count(*) as total,
                       count(*) FILTER (WHERE status = 'PENDING') as pending,
                       count(*) FILTER (WHERE status = 'PROCESSING') as processing,
                       count(*) FILTER (WHERE status = 'COMPLETED') as completed,
                       count(*) FILTER (WHERE status = 'FAILED') as failed
                FROM imports
                """);
    }

    private Map<String, Object> writePressure() {
        // Conexões ativas escrevendo
        var writing = jdbc.queryForObject("""
                SELECT count(*) FROM pg_stat_activity
                WHERE datname = current_database() AND state = 'active'
                  AND query ~* '^(INSERT|UPDATE|DELETE|COPY)'
                """, Integer.class);

        // Locks esperando
        var lockWaits = jdbc.queryForObject("""
                SELECT count(*) FROM pg_stat_activity
                WHERE datname = current_database() AND wait_event_type = 'Lock'
                """, Integer.class);

        // Queries lentas (>5s)
        var slowQueries = jdbc.queryForObject("""
                SELECT count(*) FROM pg_stat_activity
                WHERE datname = current_database() AND state = 'active'
                  AND now() - query_start > interval '5 seconds'
                """, Integer.class);

        // Max connections e uso
        var maxConn = jdbc.queryForObject("SHOW max_connections", Integer.class);
        var currentConn = jdbc.queryForObject(
                "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database()", Integer.class);
        int connUsagePercent = maxConn > 0 ? (currentConn * 100 / maxConn) : 0;

        // Checkpoint stats
        var checkpoints = jdbc.queryForMap("""
                SELECT checkpoints_req as forced, checkpoints_timed as scheduled,
                       buffers_checkpoint as buffers_written
                FROM pg_stat_bgwriter
                """);

        // Score: 0-100 (0=tranquilo, 100=no limite)
        // Baseado em thresholds reais para PostgreSQL com batch inserts
        int score = 0;

        // Conexões: preocupante acima de 60% do max
        if (connUsagePercent > 80) score += 40;
        else if (connUsagePercent > 60) score += 20;
        else if (connUsagePercent > 40) score += 10;

        // Lock waits: qualquer espera por lock é sinal de contenção
        if (lockWaits != null && lockWaits > 5) score += 30;
        else if (lockWaits != null && lockWaits > 2) score += 15;
        else if (lockWaits != null && lockWaits > 0) score += 5;

        // Queries lentas (>5s): em batch insert é aceitável ter algumas
        if (slowQueries != null && slowQueries > 10) score += 20;
        else if (slowQueries != null && slowQueries > 5) score += 10;

        // Escritas ativas: normal ter várias durante ingestão
        if (writing != null && writing > 20) score += 10;
        else if (writing != null && writing > 10) score += 5;

        score = Math.min(100, score);

        String verdict;
        if (score <= 20) verdict = "🟢 Tranquilo — pode aumentar escrita";
        else if (score <= 50) verdict = "🟡 Moderado — carga aceitável";
        else if (score <= 75) verdict = "🟠 Alto — cuidado ao adicionar mais escrita";
        else verdict = "🔴 Crítico — banco no limite";

        return Map.of(
                "score", score,
                "verdict", verdict,
                "activeWrites", writing != null ? writing : 0,
                "lockWaits", lockWaits != null ? lockWaits : 0,
                "slowQueries", slowQueries != null ? slowQueries : 0,
                "connectionUsagePercent", connUsagePercent,
                "maxConnections", maxConn,
                "currentConnections", currentConn,
                "checkpoints", checkpoints
        );
    }
}
