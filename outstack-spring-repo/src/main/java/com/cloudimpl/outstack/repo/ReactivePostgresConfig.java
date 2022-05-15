package com.cloudimpl.outstack.repo;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
@EnableR2dbcRepositories("com.restrata")
public class ReactivePostgresConfig {

    private final DataSources datasourceProps;
    private Map<DataSources.DataSource,ConnectionHandler> handlers = new ConcurrentHashMap<>();


    public Supplier<Mono<Connection>> connectionFromPool(String dataSource,String tenantId)
    {
        return ()-> handlers.computeIfAbsent(datasourceProps.getDataSource(dataSource),d->new ConnectionHandler(d))
                .getConnectionPool().create();
    }

    @Getter
    public static final class ConnectionHandler
    {
        private DataSources.DataSource dataSource;
        private ConnectionPool connectionPool;

        private ConnectionHandler(DataSources.DataSource source)
        {
            this.dataSource = source;
            ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory())
                    .maxIdleTime(Duration.ofMillis(100000))
                    .maxSize(20)
                    .build();
            connectionPool = new ConnectionPool(configuration);
        }

        private ConnectionFactory connectionFactory() {
            return ConnectionFactories.get(dataSource.getConfigs().get("prefix")
                    + dataSource.getConfigs().get("username") + ":" +
                    dataSource.getConfigs().get("password")
                    + "@" + dataSource.getConfigs().get("host")
                    + ":" + dataSource.getConfigs().get("port") +
                    "/" + dataSource.getConfigs().get("database"));


        }
    }
}