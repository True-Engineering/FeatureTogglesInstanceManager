package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.trueengineering.featureflag.manager.infrastructure.db.DBConfiguration

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [DBConfiguration::class],
    initializers = [JpaBaseTest.PostgreSQLContainerInitializer::class]
)
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class JpaBaseTest {

    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:10")
            .withPassword("pass")
            .withUsername("user")
            .withDatabaseName("ff-db-test")

        init {
            postgres.start()
        }
    }

    class PostgreSQLContainerInitializer :
        ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgres.jdbcUrl,
                "spring.datasource.username=" + postgres.username,
                "spring.datasource.password=" + postgres.password
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}