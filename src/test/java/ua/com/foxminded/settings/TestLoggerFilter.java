package ua.com.foxminded.settings;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

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
