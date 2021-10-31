package ua.com.foxminded.settings;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan("ua.com.foxminded")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringTestConfiguration {

}
