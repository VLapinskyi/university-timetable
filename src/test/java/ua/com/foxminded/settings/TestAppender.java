package ua.com.foxminded.settings;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

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
