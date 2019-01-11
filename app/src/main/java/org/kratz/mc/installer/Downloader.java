/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.installer;

/**
 * Game components download handler.
 */
public interface Downloader {
    
    /**
     * Execute download in parallel thread.
     */
    public void start();

    /**
     * Get components download handler parallel thread status.
     * @return Value of {@code true} when parallel downloading thread is running or {@code false} otherwise.
     */
    public boolean isRunning();

}
