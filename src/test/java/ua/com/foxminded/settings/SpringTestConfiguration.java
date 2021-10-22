package ua.com.foxminded.settings;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.Logger;

@Configuration
@ComponentScan("ua.com.foxminded")
public class SpringTestConfiguration {
    
    @Autowired
    private TestAppender testAppender;
    
    private void setTestAppender(ConfigurableApplicationContext context, TestAppender testAppender){
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(testAppender);
    }
    
}
