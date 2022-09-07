package pl.szmaus.configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.szmaus.firebirdf00154.entity.TypeOtherFile;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
@Log4j2
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "firebirdf00154EntityManager",
        transactionManagerRef = "firebirdf00154TransactionManager",
        basePackages = "pl.szmaus.firebirdf00154.repository"
)
public class FirebirdF00154DBConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.firebirdf00154.datasource")
    public DataSource firebirdf00154DataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean(name = "firebirdf00154EntityManager")
    public LocalContainerEntityManagerFactoryBean firebirdf00154EntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(firebirdf00154DataSource())
                .packages(SalesInvoice.class, OtherRemainingFile.class, TypeOtherFile.class)
                .persistenceUnit("firebirdf00154PU")
                .build();
    }

    @Bean(name = "firebirdf00154TransactionManager")
    public PlatformTransactionManager firebirdf00154TransactionManager(@Qualifier("firebirdf00154EntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}