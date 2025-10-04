package com.example.nasa.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class HibernateConfig {

    @Value("${db.driver}")
    private String dbDriver;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${hibernate.hbm2ddl.auto}")
    private String hibernateHbm2ddlAuto;

    /**
     * Cấu hình DataSource sử dụng HikariCP Connection Pool
     * HikariCP là connection pool nhanh nhất và được khuyến nghị
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(dbDriver);
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);

        // HikariCP Pool Configuration
        config.setMaximumPoolSize(10);          // Số connection tối đa trong pool
        config.setMinimumIdle(5);               // Số connection tối thiểu luôn sẵn sàng
        config.setConnectionTimeout(30000);     // Timeout khi lấy connection (30s)
        config.setIdleTimeout(600000);          // Timeout cho idle connection (10 phút)
        config.setMaxLifetime(1800000);         // Thời gian sống tối đa của connection (30 phút)
        config.setAutoCommit(true);             // Auto commit cho non-transactional queries
        config.setPoolName("NASA-APOD-HikariCP");

        return new HikariDataSource(config);
    }

    /**
     * Cấu hình Hibernate SessionFactory
     * SessionFactory là factory để tạo ra Hibernate Session
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());

        // Scan package chứa các Entity classes
        sessionFactory.setPackagesToScan("com.example.nasa.model");

        // Set Hibernate properties
        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory;
    }

    /**
     * Cấu hình Transaction Manager
     * Quản lý transactions cho Hibernate
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    /**
     * Hibernate Properties
     * Các cấu hình chi tiết cho Hibernate
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();

        // Database Dialect
        properties.put("hibernate.dialect", hibernateDialect);

        // Show SQL queries in console
        properties.put("hibernate.show_sql", hibernateShowSql);

        // Format SQL queries for readability
        properties.put("hibernate.format_sql", "true");

        // Schema generation strategy
        properties.put("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);

        // Current session context
        // Spring quản lý session thông qua transaction
        properties.put("hibernate.current_session_context_class",
                "org.springframework.orm.hibernate5.SpringSessionContext");

        // Enable lazy loading outside transaction
        // Cho phép lazy loading ngay cả khi không có transaction active
        properties.put("hibernate.enable_lazy_load_no_trans", "true");

        // Use second-level cache (optional - tắt mặc định)
        properties.put("hibernate.cache.use_second_level_cache", "false");

        // Use query cache (optional - tắt mặc định)
        properties.put("hibernate.cache.use_query_cache", "false");

        // JDBC batch size for better performance
        properties.put("hibernate.jdbc.batch_size", "20");

        // Order inserts and updates for better batch processing
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");

        // Use JDBC metadata defaults
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");

        return properties;
    }
}
