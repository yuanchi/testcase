package com.jerrylin.microservice.util;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceUtils {
	public static HikariDataSource ds(){
		// https://github.com/brettwooldridge/HikariCP
		// https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mariadb://localhost:3306/test?allowMultiQueries=true");
		config.setUsername("root");
		config.setPassword("root");
		
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		config.addDataSourceProperty("useLocalSessionState", "true");
		config.addDataSourceProperty("useLocalTransactionState", "true");
//		config.addDataSourceProperty("rewriteBatchedStatements", "true");
		config.addDataSourceProperty("cacheResultSetMetadata", "true");
		config.addDataSourceProperty("cacheServerConfiguration", "true");
		config.addDataSourceProperty("elideSetAutoCommits", "true");
		config.addDataSourceProperty("maintainTimeStats", "false");
		
		MetricRegistry metricRegistry = new MetricRegistry();
		HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
		config.setMetricRegistry(metricRegistry);
		config.setHealthCheckRegistry(healthCheckRegistry);
		
		HikariDataSource ds = new HikariDataSource(config);
		
		return ds;
	}
}
