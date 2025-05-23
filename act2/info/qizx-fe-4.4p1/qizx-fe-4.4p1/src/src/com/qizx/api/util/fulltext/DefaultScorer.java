/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.fulltext;


import com.qizx.api.Document;
import com.qizx.api.fulltext.Scorer;

/**
 * Default scorer implementation
 */
public class DefaultScorer
    implements Scorer
{
    /**
     * Name of the metadata property which can contain a weight for a Document.
     * Support of document ranking.
     */
    public static final String FT_WEIGHT = "ft-weight";
    
    /**
     * Importance of sum versus max in Or: if sumWeight
     */
    protected float sumWeight = 0.5f;
    protected int trace = 0;
    
    public float normalizeScore(float rawScore)
    {
        float result = rawScore / (0.8f + rawScore);
        if(trace > 0)
            System.err.println("== normalize " + rawScore + " => " + result);
        return result;
    }
    
   public float normWord(float inverseDocFrequency)
    {
        float normw = 1 + (float) Math.log(inverseDocFrequency);
        if(trace > 0)
            System.err.println("norm Word: idf= " + 1/inverseDocFrequency + " -> " + normw);
        return normw;
    }

    public float scoreWord(float norm, float termFrequency)
    {
        float score = norm * termFrequency;
        if(trace > 1)
            System.err.println("score Word : norm="+norm+" "+termFrequency+" -> "+score);
        return score;
    }

    
    public float normAll(float[] subWeights)
    {
        if(trace > 0) System.err.print("norm All");
        // sum of squared weights (aka S2):
        float s2 = 0;
        for (int i = 0; i < subWeights.length; ++i ) {
            s2 += subWeights[i] * subWeights[i];
            if(trace > 0) System.err.print(" " + subWeights[i]);
        }
        // normed weight is weight divided by sqrt of s2
        float normw = (float) (1 / Math.sqrt(s2));
        if(trace > 0) System.err.println(" -> "+normw); 
        return normw;
    }

    public float scoreAll(float[] scores)
    {
        if(trace > 1) System.err.print("score All"); 
        float sum = 0;
        for (int i = 0; i < scores.length; ++i ) {
            sum += scores[i];
            if(trace > 1) System.err.print(" " + scores[i]);
        }
        if(trace > 1) System.err.println(" -> "+sum); 
        return sum;
    }

    
    public float normOr(float[] subWeights)
    {
        float sum = 0, max = 0;
        for (int i = 0; i < subWeights.length; ++i ) {
            sum += subWeights[i];
            if(subWeights[i] > max)
                max = subWeights[i];
            if(trace > 0) System.err.print(" " + subWeights[i]);
        }
        float normw = 1.0f / (sumWeight * sum + (1 - sumWeight) * max);
        if(trace > 0) System.err.println("norm Or -> "+normw); 
        return normw;
    }

    public float scoreOr(float[] scores, int scoreCount)
    {
        float sum = 0, max = 0;
        for (int i = 0; i < scores.length; ++i) {
            sum += scores[i];
            if(scores[i] > max)
                max = scores[i];
            if(trace > 1) System.err.print(" " + scores[i]);
        }
        float result = (sumWeight * sum + (1 - sumWeight) * max);
        if(trace > 1) System.err.println("score Or -> "+result); 
        return result;
    }
    

    public float getDocumentWeight(Document scoredDocument)
    {
//        try {
//            Object prop = scoredDocument.getProperty(FT_WEIGHT);
//            if(prop instanceof Double)
//                return ((Double) prop).floatValue();
//            if(prop instanceof Long)
//                return ((Long) prop).floatValue();
//            return 1; // absent or bad type
//        }
//        catch (DataModelException e) {
//            return 1;
//        }
        return 1;
    }
}
