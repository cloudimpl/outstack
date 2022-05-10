package com.cloudimpl.outstack.repo;

import com.cloudimpl.outstack.repo.DatasourceProps;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
@EnableR2dbcRepositories("com.restrata")
public class ReactivePostgresConfig extends AbstractR2dbcConfiguration {

    private final DatasourceProps datasourceProps;
    private  ConnectionPool connectionPool;

    @PostConstruct
    private void init()
    {
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory())
                .maxIdleTime(Duration.ofMillis(100000))
                .maxSize(20)
                .build();
        connectionPool = new ConnectionPool(configuration);
    }
    /**
     * @noinspection NullableProblems
     * @return
     */
    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(datasourceProps.getPrefix() + datasourceProps.getUsername() + ":" +
                datasourceProps.getPassword() + "@" + datasourceProps.getHost() + ":" + datasourceProps.getPort() +
                "/" + datasourceProps.getDatabase());


    }

    public Supplier<Mono<Connection>> connectionFromPool(String tenantId)
    {
        return ()-> connectionPool.create();
    }
}