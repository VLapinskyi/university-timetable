package ua.com.foxminded.settings;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Configuration
@EnableTransactionManagement
@ComponentScan("ua.com.foxminded")
@EnableWebMvc
@EnableAspectJAutoProxy(proxyTargetClass = true)
@PropertySource("classpath:database.properties")
@PropertySource("classpath:sql-queries.properties")
public class SpringConfiguration implements WebMvcConfigurer {
    private final ApplicationContext context;

    private Environment environment;

    @Autowired
    public SpringConfiguration(ApplicationContext context, Environment environment) {
        this.context = context;
        this.environment = environment;
    }

    @Bean
    public DataSource getDataSource() throws NamingException {
        return (BasicDataSource) new JndiTemplate().lookup(environment.getProperty("jndi.datasource.name"));
    }

    @Bean
    public Validator validator() {
        return validatorFactory().getValidator();
    }
    
    @Bean
    public ValidatorFactory validatorFactory() {
        return Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory();
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(context);
        templateResolver.setPrefix("classpath:/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setOrder(0);
        return templateResolver;
    }

    @Bean
    public ClassLoaderTemplateResolver scriptTemplateResolver() {
        ClassLoaderTemplateResolver scriptTemplateResolver = new ClassLoaderTemplateResolver();
        scriptTemplateResolver.setPrefix("classpath:/views/scripts");
        scriptTemplateResolver.setOrder(1);
        return scriptTemplateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(templateResolver());
        templateEngine.addTemplateResolver(scriptTemplateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.addDialect(new Java8TimeDialect());
        return templateEngine;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() throws NamingException {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        sessionFactory.setPackagesToScan("ua.com.foxminded.domain");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }
    
    @Bean
    public PlatformTransactionManager hibernateTransactionManager() throws NamingException {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        
        hibernateProperties.put( "hibernate.dialect",
                "org.hibernate.dialect.PostgreSQL10Dialect");
        
        hibernateProperties.put("javax.persistence.validation.mode", "none");
        
        hibernateProperties.put("show_sql", "true");
        
        hibernateProperties.put("hibernate.hbm2ddl.auto", "validate");

        return hibernateProperties;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        registry.viewResolver(resolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");

    }
}
