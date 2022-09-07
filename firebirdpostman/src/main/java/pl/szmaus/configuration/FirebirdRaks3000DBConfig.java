package pl.szmaus.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.entity.R3Return;

@Log4j2
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "firebirdraks3000EntityManager",
        transactionManagerRef = "firebirdraks3000TransactionManager",
        basePackages = "pl.szmaus.firebirdraks3000.repository"
)
public class FirebirdRaks3000DBConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.firebirdraks3000.datasource")
    public DataSource firebirdraks3000DataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Primary
    @Bean(name = "firebirdraks3000EntityManager")
    public LocalContainerEntityManagerFactoryBean firebirdraks3000EntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(firebirdraks3000DataSource())
                .packages(Company.class, R3Return.class)
                .persistenceUnit("firebirdraks3000PU")
                .build();
    }

    @Primary
    @Bean(name = "firebirdraks3000TransactionManager")
    public PlatformTransactionManager firebirdraks3000TransactionManager(@Qualifier("firebirdraks3000EntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
