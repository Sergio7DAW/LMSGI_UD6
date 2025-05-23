/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
 
package com.qizx.api.util.logging;

/**
 * Interface used to report errors, warnings and information messages.
 */
public interface Log
{
    public static final int LOG_INHERITED = 0;
    public static final int LOG_FATAL = 1;
    public static final int LOG_ERROR = 10;
    public static final int LOG_WARNING = 20;
    public static final int LOG_INFO = 30;
    public static final int LOG_DEBUG = 40;

    Level FATAL = new Level(LOG_FATAL, "FATAL");
    Level ERROR = new Level(LOG_ERROR, "ERROR");
    Level WARNING = new Level(LOG_WARNING, "WARNING");
    Level INFO = new Level(LOG_INFO, "INFO");
    Level DEBUG = new Level(LOG_DEBUG, "DEBUG");
    Level INHERITED = new Level(LOG_INHERITED, "INHERITED");
    
    /**
     * Returns the current message level.
     */
    Level getLevel();
    
    /**
     * Sets the current message level.
     * @param level
     */
    void setLevel(Level level);

    /**
     * The current actual level as an integer value.
     */
    int getWorkLevel();
    
    /**
     * Identifier of the Log within its context.
     */
    String getName();
    
    /**
     * Tests whether the 'error' level is enabled.
     */
    boolean allowsError();
    
    /**
     * Log an error message.
     */
    void error(String message);
    
    /**
     * Log an error message.
     * @param message
     */
    void error(String message, Throwable error);

    /**
     * Returns the number of errors since the latest reset.
     */
    int getErrorCount();

    void setErrorCount(int count);
    
    /**
     * Tests whether the 'warning' level is enabled.
     */
    boolean allowsWarning();
    
    /**
     * Log a warning message.
     */
    void warning(String message);
    
    /**
     * Log an warn message.
     * @param message
     */
    void warning(String message, Throwable error);

    int getWarningCount();
    
    void setWarningCount(int count);

    /**
     * Tests whether the INFO level is enabled.
     */
    boolean allowsInfo();
    
    /**
     * Log an info message if the INFO level is enabled.
     */
    void info(String message);
    
    /**
     * Log an warn message.
     * @param message
     */
    void info(String message, Throwable error);
   
    
    /**
     * Tests whether the 'debug' level is enabled.
     */
    boolean allowsDebug();
    
    /**
     * Log a debug message if the DEBUG level is enabled.
     */
    void debug(String message);
    
    /**
     * Log an warn message.
     * @param message
     */
    void debug(String message, Throwable error);
    
    /**
     * Represents a level of logging (ERROR, WARNING etc.)
     */
    public class Level
    {
        public int level;
        public String name;
        
        public Level(int level, String name)
        {
            this.level = level;
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
    }
}
