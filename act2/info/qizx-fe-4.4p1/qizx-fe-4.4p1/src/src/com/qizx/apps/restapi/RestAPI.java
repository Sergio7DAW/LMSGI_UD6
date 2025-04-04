/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.restapi;

import com.qizx.api.QName;
import com.qizx.xdm.IQName;

public interface RestAPI
{
    String FORMAT_ITEMS = "items"; // wrapped items
    String FORMAT_XML = "xml"; // no wrapping, XML serialisation
    String FORMAT_HTML = "html"; // no wrapping, XHTML serialisation
    
    String COUNTING_EXACT = "exact";
    String COUNTING_ESTIMATE = "estimated";
    String COUNTING_NONE = "none";
    
    String PROFILE = "profile";
    
    String EXPERT_LEVEL = "expert";
    
    QName NAME = IQName.get("name");
    QName TYPE_ATTR = IQName.get("type");
    QName PROPERTY = IQName.get("property");
    QName T_COUNT = IQName.get("total-count");
    QName E_COUNT = IQName.get("estimated-count");
    QName PROFILING = IQName.get("profiling");

    String[] CONFIGURATION_FIELDS = {
        "Name", "Category", "Level", "Type", "Value", "DefaultValue", "Description"
    };
    String[] RUNNING_QUERIES_FIELDS = {
        "Id", "User", "Elapsed", "Source"
    };
    String[] STATS_FIELDS = {
        "Id", "Type", "Value", "Family", "Description"
    };
    String[] TASKS_FIELDS = {
        "Type", "Database", "StartTime", "FinishTime", "Duration", "Progress"
    };
    String[] INFO_FIELDS = {
        "Name", "Value"
    };

}
