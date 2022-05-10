package com.cloudimpl.outstack.repo;

public class SafeExecute {
    private boolean done = false;

    public synchronized void execute(Runnable runnable)
    {
        if(!done)
        {
            runnable.run();
            done = true;
        }
    }
}