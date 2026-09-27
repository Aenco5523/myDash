package dev.aenco.mydash.platform.calver.neoforge;

import dev.aenco.mydash.core.MyDashCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

final class MyDashLogAppender extends AbstractAppender implements AutoCloseable {
    private static final String APPENDER_NAME = "myDashNeoForgeCalverConsoleCapture";
    private final MyDashCore core;
    private LoggerContext context;

    private MyDashLogAppender(MyDashCore core) {
        super(APPENDER_NAME, (Filter) null,
            (Layout<? extends Serializable>) PatternLayout.createDefaultLayout(), true);
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
        String message = event.getMessage() == null ? "" : event.getMessage().getFormattedMessage();
        Throwable thrown = event.getThrown();
        if (thrown != null) {
            StringWriter buffer = new StringWriter();
            thrown.printStackTrace(new PrintWriter(buffer));
            message = message + "\n" + buffer;
        }
        core.publishConsoleLine(
            event.getLevel() == null ? "INFO" : event.getLevel().name(),
            event.getLoggerName() == null ? "NeoForge" : event.getLoggerName(),
            message
        );
    }

    @Override
    public void close() {
        LoggerContext active = context;
        context = null;
        if (active != null) {
            Configuration configuration = active.getConfiguration();
            configuration.getRootLogger().removeAppender(APPENDER_NAME);
            active.updateLoggers();
        }
        stop();
    }
}
