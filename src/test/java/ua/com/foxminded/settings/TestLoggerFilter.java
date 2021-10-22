package ua.com.foxminded.settings;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

@Component
@EnableAutoConfiguration
public class TestLoggerFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getLoggerName().contains("ua.com.foxminded")) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }

}
