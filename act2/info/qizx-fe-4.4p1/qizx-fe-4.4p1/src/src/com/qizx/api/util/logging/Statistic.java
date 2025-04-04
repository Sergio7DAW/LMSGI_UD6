/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.logging;

import com.qizx.util.LikePattern;

import java.util.TreeMap;

/**
 * An item of activity measurement.
 */
public class Statistic
{
    String id;
    String family;
    String type;
    String description;
    long   value;
    
    Statistic(String id, String type, String family, String description, long value)
    {
        this.id = id;
        this.type = type;
        this.family = family;
        this.value = value;
        this.description = description;
    }
    
    /**
     * Returns a unique identifier of the value.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the value type: count, hits, size (bytes), time (milliseconds),
     * hit_ratio (%)
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the family of the value: "Activity", "IO", "CachedIO", "Server"
     */
    public String getFamily()
    {
        return family;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the actual value.
     */
    public long getValue()
    {
        return value;
    }
    
    /**
     * A map of named statistics.
     * Supports a filtering/aggregation mechanism to present statistics in
     * varied ways.
     */
    public static class Map extends TreeMap<String, Statistic>
    {
        long timeStamp;
        String[] mapping;
        
        public Map() {
            timeStamp = System.currentTimeMillis();
        }
        
        /**
         * internal use.
         */
        public void cumulate(Statistics.Base stat, String name, long value)
        {
            Statistic st = map(stat.id, name, stat.family, stat.description);
            if (st != null) // not discarded
                st.value += value;
        }

        private Statistic map(String id, String name, String family, String description)
        {
            String key = id + "\t" + name;
            if (mapping != null) {
                for(int m = mapping.length - 2; m >= 0; m -= 2)
                    if(new LikePattern(mapping[m]).matches(key)) {
                        if(mapping[m + 1].length() == 0)
                            return null;    // discard

                        id = mapping[m + 1];
                        key = id + "\t" + name;
                        break;
                    }
            }
            Statistic st = get(key);
            if (st == null) {
                
                this.put(key, st = new Statistic(id, name, family, description, 0));
            }
            return st;
        }

        /**
         * Defines a mapping used when collecting statistics.<p>
         * A mapping is a list of rules of the form:
         * <pre>   pattern -> mapping</pre>
         * where patterns are stored at even indices and actions at odd indices
         */
        public void setMapping(String[] mapping)
        {
            this.mapping = mapping;
        }
        
        
    }
}

