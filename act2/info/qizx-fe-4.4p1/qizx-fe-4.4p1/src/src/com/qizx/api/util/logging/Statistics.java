/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.logging;

import com.qizx.util.basic.Util;

import java.util.HashMap;

/**
 * Table of all statistics related to a module (eg LibraryManager).
 */
public class Statistics
{    
    private HashMap<String, Statistics.Base> sets;
    
    public Statistics()
    {
        sets = new HashMap<String, Statistics.Base>();
    }

    public boolean perLibBreakdown()
    {
        return false;
    }
    
    public String getLibPrefix(String libraryname)
    {
        return (perLibBreakdown()? (libraryname + ":") : "");
    }

    public Statistics.Counter forCounting(String id, String family, String description)
    {
        Statistics.Base set = sets.get(id);
        if(set == null) {
            sets.put(id, set = new Statistics.Counter(id, family, description));
            
        }
        return (Statistics.Counter) set;
    }

    // count and time
    public Statistics.Activity forActivity(String id, String family, String description)
    {
        Statistics.Base set = sets.get(id);
        if(set == null) {
            sets.put(id, set = new Statistics.Activity(id, family, description));
            
        }
        return (Statistics.Activity) set;
    }

    public Statistics.DataFlow forData(String id, String family, String description)
    {
        Statistics.Base set = sets.get(id);
        if(set == null) {
            sets.put(id, set = new Statistics.DataFlow(id, family, description));
            
        }
        return (Statistics.DataFlow) set;
    }
    
    public Statistics.Cache forCache(String id, String family, String description)
    {
        Statistics.Base set = sets.get(id);
        if(set == null) {
            sets.put(id, set = new Statistics.Cache(id, family, description));
            
        }
        return (Statistics.Cache) set;
    }

    // Statistic.Map can do remapping
    public void collect(Statistic.Map target)
    {
        for(Statistics.Base set : sets.values()) {
            set.expandTo(target);
        }
    }
    
    public static final String[] ADMIN_MAPPING = {
        "index|*|key_blocks\t*",  "", //"index|key_blocks",
        "index|*|doc_blocks\t*",  "", //"index|doc_blocks",
        "index|*|node_blocks\t*",  "", //"index|node_blocks",
        "storage|data|alloc\t*",  "", 
        "storage|data|free\t*",  "", 
        "storage|tables|*\t*",  "",
        "index|phys_block|cache\tsize", "",    
        "index|*|key_blocks\thit_ratio",  "",   // cannot sum!    
        "storage|tables|*\thit_ratio",  "",   // cannot sum!    
        "update|*\t*",  "",       
        "update|commit\t*",  "update|commit",   
    };

    public static final String[] SHORT_MAPPING = {
        "index|*|key_blocks\t*",  "index|key_blocks",
        "index|*|doc_blocks\t*",  "index|doc_blocks",
        "index|*|node_blocks\t*", "index|node_blocks",
        "storage|data|alloc\t*",  "", 
        "storage|data|free\t*",  "", 
        "storage|tables|*|get_block\t*",  "storage|tables|get_block",
        "storage|tables|*|create_block\t*",  "storage|tables|create_block",
        "storage|tables|*|delete_block\t*",  "storage|tables|delete_block",
        "storage|tables|*|write_block\t*",  "storage|tables|write_block",
        "storage|tables|*|lookup\t*",  "storage|tables|lookup",
        "storage|tables|*|insert\t*",  "storage|tables|insert",
        "storage|tables|*|remove\t*",  "storage|tables|remove",
        "index|phys_block|cache\tsize", "",    
        "index|*|key_blocks\thit_ratio",  "",   // cannot sum!    
        "storage|tables|*\thit_ratio",  "",   // cannot sum!      
    };

    public static String decorate(long value, String type)
    {
        if ("time".equals(type))
            return Long.toString(value) + " ms";
        if ("size".equals(type) || "used_memory".equals(type))
            return (value / 10486 / 100.0) + " Mb";
        if ("hit_ratio".equals(type))
            return value + " %";
        return Long.toString(value);
    }

    /**
     * Most basic statistic: a counter.
     */
    public abstract static class Base
    {
        public String id;
        public String family;
        public String description;
        
        public Base(String id, String family, String description)
        {
            this.id = id;
            this.family = family;
            this.description = description;
        }
        
        public abstract void expandTo(Statistic.Map stats);
    }

    /**
     * Most basic statistic: a counter.
     */
    public static class Counter extends Base
    {
        public long count;
        
        public Counter(String id, String family, String description)
        {
            super(id, family, description);
        }
    
        public void count()
        {
            ++count;
        }
    
        public void expandTo(Statistic.Map stats)
        {
            stats.cumulate(this, "count", count);
        }
    }

    /**
     * Simple set of stats for an activity: # of occurrences, time spent
     */
    public static class Activity extends Counter
    {
        public long time;
    
        public Activity(String id, String family, String description)
        {
            super(id, family, description);
            time = -1;
        }
    
        public synchronized void addTime(long nanos)
        {
            ++count;
            time += nanos;
        }
    
        public void expandTo(Statistic.Map stats)
        {
            stats.cumulate(this, "count", count);
            stats.cumulate(this, "time", Util.nanoToMillis(time));
        }
    }

    /**
     * Simple set of stats for an activity: # of occurrences, size, time spent
     */
    public static class DataFlow extends Activity
    {
        public long size;
    
        public DataFlow(String id, String family, String description)
        {
            super(id, family, description);
            size = -1;
        }
    
        public synchronized void add(long bytes, long nanos)
        {
            ++count;
            if (size <= 0)
                size = bytes;
            else
                size += bytes;  // data
            if (time <= 0)
                time = nanos;
            else
                time += nanos;
        }
    
        public void expandTo(Statistic.Map stats)
        {
            stats.cumulate(this, "count", count);
            if (size >= 0)
                stats.cumulate(this, "size", size);
            stats.cumulate(this, "time", Util.nanoToMillis(time));
        }
    }

    /**
     * Stats for a cache.
     */
    public static class Cache extends DataFlow
    {
        public static String USED = "used_memory";

        public long misses;
        
        public Cache(String id, String family, String description)
        {
            super(id, family, description);
            size = -1;
            time = -1;
        }
    
        public void addAccess(boolean hit)
        {
            ++ count;
            if(!hit)
                ++ misses;
        }
    
        // cached IO miss:
        public void addMiss(long bytes, long nanos)
        {
            ++ misses;
            if (size <= 0)
                size = bytes;
            else
                size += bytes;  // data
            if (time <= 0)
                time = nanos;
            else
                time += nanos;
        }
    
        public void expandTo(Statistic.Map stats)
        {
            stats.cumulate(this, "hit+miss", count);
            if (size >= 0)
                stats.cumulate(this, "size", size);
            if (time >= 0)
                stats.cumulate(this, "time", Util.nanoToMillis(time));
            long ratio = (count == 0)? 0 : ((count - misses) * 100) / count;
            //if (count > 0)
            stats.cumulate(this, "hit_ratio", ratio);
        }
    }
}
