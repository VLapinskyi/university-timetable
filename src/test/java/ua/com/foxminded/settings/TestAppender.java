package ua.com.foxminded.settings;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

@Component
public class TestAppender extends AppenderBase<ILoggingEvent> {
    private static List<ILoggingEvent> events = new ArrayList<>();    
    
    public List<ILoggingEvent> getEvents() {
        return events;
    }

    public void cleanEventList() {
        events.clear();
    }

    @Override
    protected void append(ILoggingEvent event) {
        events.add(event);
    }
}
