/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.basic;

import java.util.ArrayList;

/**
 * A Thread that executes tasks managed in a queue.
 * Tasks are guaranteed to be executed in the order they are queued.
 */
public class QueueWorker extends Thread
{
    private volatile boolean stopped;
    protected ArrayList<Runnable> queue = new ArrayList<Runnable>();
    private boolean trace;
    private volatile Runnable currentTask;
    
    public QueueWorker() {
    }

    public QueueWorker(String name) {
        super(name);
    }

    /**
     * Stops the thread after execution of the last task.
     */
    public void shutdown()
    {
        this.stopped = true;
        try {
            // FIX interrupt() can be a problem
            //    if the thread is reading or writing to a file
            ////interrupt();    // stops the thread if it is waiting

            // instead simply unblock it
            synchronized(queue) {
                queue.notify();
            }
            
            join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(trace)
            System.err.println(this + " stopped");
    }

    public interface DelayableTask extends Runnable
    {
        /**
         * Get delay in milliseconds.
         */
        int getDelay();
        /**
         * Specify a delay in milliseconds.
         */
        void setDelay(int msDelay);
    }
    
    /**
     * Queue a task. If the task is a DelayableTask, the specified delay will
     * be introduced, unless another task is queued meanwhile: in that case
     * the new task has priority over the delayed task.
     */
    public void queueTask(Runnable task)
    {
        // can happen: stop required but task queued by latest task
//        if(stopped)
//            throw new IllegalStateException("stopped");
        synchronized(queue) {
            queue.add(task);
            queue.notify();     // only one thread
        }
    }
    
    /**
     * Put again a task to the queue, but without notifying, so the task
     * will be executed after some delay.
     */
    public void requeueTask(Runnable task)
    {
//        if(stopped)
//            throw new IllegalStateException("stopped");
        synchronized(queue) {
            queue.add(task);
        }
    }
    
    public void run()
    {
        for( ; ; ) {
            Runnable nextTask = null;
            synchronized(queue) {
                if(queue.size() == 0) {
                    try {
                        if(stopped)
                            break;  // only if empty queue
                        queue.wait(500);
                    }
                    catch (InterruptedException e) { ; }
                }
                // reacquired lock on queue: is there some task?
                if(queue.size() == 0)
                    continue;
                nextTask = queue.remove(0);
            }
            // execute OUTSIDE of synchronized (possible deadlock otherwise)
            try {
                if(nextTask != null)    // useful?
                {
                    currentTask = nextTask;
                    // manage requested delay
                    int delay = 10;
                    if((nextTask instanceof DelayableTask)
                        && (delay = ((DelayableTask) nextTask).getDelay()) > 0)
                    {
                        // Wait on queue: if another task is queued meanwhile,
                        // we are interrupted, then the delayed task is
                        // AFTER the new task, which is what we want
                        synchronized(queue) {
                            try {
                                queue.wait(delay);
                            }
                            catch (InterruptedException e) {  }
                        }
                        ((DelayableTask) nextTask).setDelay(0);
                        requeueTask(nextTask);
                    }
                    else
                        nextTask.run();
                }
            }
            catch (Throwable e) {
                logError(e);                
            }
            currentTask = null;
        }
    }

    public Runnable getCurrentTask()
    {
        return currentTask;
    }
    
    /**
     * Can be redefined by subclass.
     */
    protected void logError(Throwable e)
    {
        e.printStackTrace(); 
    }
}
