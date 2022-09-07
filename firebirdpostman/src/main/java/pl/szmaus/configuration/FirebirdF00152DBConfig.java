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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Log4j2
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "firebirdf00152EntityManager",
        transactionManagerRef = "firebirdf00152TransactionManager",
        basePackages = "pl.szmaus.firebirdf00152.repository"
)
public class FirebirdF00152DBConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.firebirdf00152.datasource")
    public DataSource firebirdf00152DataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean(name = "firebirdf00152EntityManager")
    public LocalContainerEntityManagerFactoryBean firebirdf00152EntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(firebirdf00152DataSource())
                .packages(FiRegisterVat.class, FiRegisterVatOther.class, KRDocumentBook.class, R3AccountDocument.class)
                .persistenceUnit("firebirdf00152PU")
                .build();
    }

    @Bean(name = "firebirdf00152TransactionManager")
    public PlatformTransactionManager firebirdf00152TransactionManager(@Qualifier("firebirdf00152EntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}