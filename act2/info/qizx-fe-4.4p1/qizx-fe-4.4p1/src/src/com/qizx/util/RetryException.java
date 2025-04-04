/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.api.EvaluationException;

public class RetryException extends EvaluationException
{
    public RetryException(String message)
    {
        super(null, message);
    }
}
