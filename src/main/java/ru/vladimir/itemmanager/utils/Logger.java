package ru.vladimir.itemmanager.utils;

import java.util.logging.Handler;
import java.util.logging.Level;

public final class Logger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("ItemManager");
    private static Level minLevel = Level.ALL;

    static {
        LOGGER.setLevel(Level.ALL);
        final Handler[] handlers = LOGGER.getHandlers();
        if (handlers != null) {
            for (final Handler handler : handlers) {
                handler.setLevel(Level.ALL);
            }
        }
    }

    private Logger() {}

    public static void setLevel(Level l) {
        minLevel = l;

        LOGGER.setLevel(l);

        final Handler[] handlers = LOGGER.getHandlers();
        if (handlers == null) return;

        for (final Handler handler : handlers) {
            handler.setLevel(Level.ALL);
        }
    }
    
    public static void debug(Object origin, String message) {
        log(Level.FINE, origin, message, null);
    }

    public static void info(Object origin, String message) {
        log(Level.INFO, origin, message, null);
    }
    
    public static void warn(Object origin, String message) {
        log(Level.WARNING, origin, message, null);
    }

    public static void warn(Object origin, String message, Throwable t) {
        log(Level.WARNING, origin, message, t);
    }

    public static void error(Object origin, String message) {
        log(Level.SEVERE, origin, message, null);
    }

    public static void error(Object origin, String message, Throwable t) {
        log(Level.SEVERE, origin, message, t);
    }

    private static void log(Level l, Object o, String m, Throwable t) {
        if (l.intValue() < minLevel.intValue()) return;

        final String oN = getOriginName(o);

        if (t == null)
            LOGGER.log(l, "(%s) %s".formatted(oN, m));
        else
            LOGGER.log(l, "(%s) %s".formatted(oN, m), t);
    }

    private static String getOriginName(Object o) {
        return switch (o) {
            case null -> "Unknown";
            case String ignored -> o.toString();
            case Class<?> clazz -> clazz.getSimpleName();
            default -> o.getClass().getSimpleName();
        };
    }
}
