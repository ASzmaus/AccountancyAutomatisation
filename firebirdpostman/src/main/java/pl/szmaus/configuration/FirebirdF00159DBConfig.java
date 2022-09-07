package pl.szmaus.configuration;

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
import pl.szmaus.firebirdf00152.entity.FiRegisterVat;
import pl.szmaus.firebirdf00152.entity.FiRegisterVatOther;
import pl.szmaus.firebirdf00152.entity.KRDocumentBook;
import pl.szmaus.firebirdf00152.entity.R3AccountDocument;
import pl.szmaus.firebirdf00159.entity.FiRegisterVat159;
import pl.szmaus.firebirdf00159.entity.FiRegisterVatOther159;
import pl.szmaus.firebirdf00159.entity.KRDocumentBook159;
import pl.szmaus.firebirdf00159.entity.R3AccountDocument159;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Log4j2
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "firebirdf00159EntityManager",
        transactionManagerRef = "firebirdf00159TransactionManager",
        basePackages = "pl.szmaus.firebirdf00159.repository"
)
public class FirebirdF00159DBConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.firebirdf00159.datasource")
    public DataSource firebirdf00159DataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean(name = "firebirdf00159EntityManager")
    public LocalContainerEntityManagerFactoryBean firebirdf00159EntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(firebirdf00159DataSource())
                .packages(FiRegisterVat159.class, FiRegisterVatOther159.class, KRDocumentBook159.class, R3AccountDocument159.class)
                .persistenceUnit("firebirdf00159PU")
                .build();
    }

    @Bean(name = "firebirdf00159TransactionManager")
    public PlatformTransactionManager firebirdf00159TransactionManager(@Qualifier("firebirdf00159EntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}