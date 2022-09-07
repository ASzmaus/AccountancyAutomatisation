package pl.szmaus.configuration;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.szmaus.mssql.entity.EAccountant;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClientStatus;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Log4j2
@Configuration
@Profile({"prod","dev"})
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "mssqlEntityManager",
        transactionManagerRef = "mssqlTransactionManager",
        basePackages = "pl.szmaus.mssql.repository"
)
public class MsSqlDBConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.mssql.datasource")
    public DataSource mssqlDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean(name = "mssqlEntityManager")
    public LocalContainerEntityManagerFactoryBean mssqlEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mssqlDataSource())
                .packages(ReceivedDocumentFromClient.class,ReceivedDocumentFromClientStatus.class, EAccountant.class)
                .persistenceUnit("mssqlPU")
                .build();
    }

    @Bean(name = "mssqlTransactionManager")
    public PlatformTransactionManager mssqlTransactionManager(@Qualifier("mssqlEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }



    @Bean
    @ConfigurationProperties(prefix = "spring.mssql.datasource.liquibase")
    public LiquibaseProperties mssqlLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase mssqlLiquibase() {
        return springLiquibase(mssqlDataSource(), mssqlLiquibaseProperties());
    }

    private static SpringLiquibase springLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setLabels(properties.getLabels());
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        return liquibase;
    }
}