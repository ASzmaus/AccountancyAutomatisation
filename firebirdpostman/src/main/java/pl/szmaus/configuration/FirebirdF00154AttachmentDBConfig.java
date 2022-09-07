package pl.szmaus.configuration;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.szmaus.firebirdf00154attachment.entity.R3DocumentFiles;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
@Log4j2
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "firebirdf00154attachmentEntityManager",
        transactionManagerRef = "firebirdf00154attachmentTransactionManager",
        basePackages = "pl.szmaus.firebirdf00154attachment.repository"
)
public class FirebirdF00154AttachmentDBConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.firebirdf00154attachment.datasource")
    public DataSource firebirdf00154attachmentDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean(name = "firebirdf00154attachmentEntityManager")
    public LocalContainerEntityManagerFactoryBean firebirdf00154attachmentEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(firebirdf00154attachmentDataSource())
                .packages(R3DocumentFiles.class)
                .persistenceUnit("firebirdf00154attachmentPU")
                .build();
    }

    @Bean(name = "firebirdf00154attachmentTransactionManager")
    public PlatformTransactionManager firebirdf00154attachmentTransactionManager(@Qualifier("firebirdf00154attachmentEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}