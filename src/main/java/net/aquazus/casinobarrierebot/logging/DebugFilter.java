package net.aquazus.casinobarrierebot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import net.aquazus.casinobarrierebot.Bot;

public class DebugFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (Level.DEBUG.isGreaterOrEqual(event.getLevel()) && !Bot.debugMode) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }
}
