package dev.aenco.mydash.platform.fabric;

import dev.aenco.mydash.core.MyDashCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

final class MyDashLogAppender extends AbstractAppender implements AutoCloseable {
    private static final String APPENDER_NAME = "myDashConsoleCapture";

    private final MyDashCore core;
    private LoggerContext context;

    private MyDashLogAppender(MyDashCore core) {
        super(
            APPENDER_NAME,
            (Filter) null,
            (Layout<? extends Serializable>) PatternLayout.createDefaultLayout(),
            true,
            Property.EMPTY_ARRAY
        );
        this.core = core;
    }

    static MyDashLogAppender install(MyDashCore core) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        LoggerConfig root = configuration.getRootLogger();

        MyDashLogAppender appender = new MyDashLogAppender(core);
        appender.context = context;
        appender.start();

        root.addAppender(appender, null, null);
        context.updateLoggers();

        return appender;
    }

    @Override
    public void append(LogEvent event) {
        if (event == null) return;

        String message = event.getMessage() == null
            ? ""
            : event.getMessage().getFormattedMessage();

        Throwable thrown = event.getThrown();
        if (thrown != null) {
            StringWriter buffer = new StringWriter();
            thrown.printStackTrace(new PrintWriter(buffer));
            message = message + "\n" + buffer.toString();
        }

        core.publishConsoleLine(
            event.getLevel() == null ? "INFO" : event.getLevel().name(),
            event.getLoggerName() == null ? "Minecraft" : event.getLoggerName(),
            message
        );
    }

    @Override
    public void close() {
        LoggerContext activeContext = context;
        context = null;

        if (activeContext != null) {
            Configuration configuration = activeContext.getConfiguration();
            configuration.getRootLogger().removeAppender(APPENDER_NAME);
            activeContext.updateLoggers();
        }

        stop();
    }
}
