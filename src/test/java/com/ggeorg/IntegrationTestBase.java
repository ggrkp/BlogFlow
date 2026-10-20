package com.ggeorg;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for tests that need the real Postgres and Redis.
 * <p>
 * The containers are started once per JVM and shared by all subclasses. Stopping them after
 * each test class would leave Spring's cached context pointing at dead ports.
 */
@SpringBootTest(properties = "jwt.secret=test-only-secret-key-that-is-at-least-256-bits-long-for-hs256-signing")
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();
    }
}
