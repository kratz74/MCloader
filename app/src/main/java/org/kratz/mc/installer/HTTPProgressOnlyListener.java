/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.installer;

import org.kratz.mc.common.http.HTTPDownloadListener;
import org.kratz.mc.ui.loader.DownloadListener;

/**
 * Map {@link DownloadListener} events to {@link HTTPDownloadListener} events.
 * Only progress update events should be passed. Beginning and ending events are
 * handled on modules list level.
 */
public class HTTPProgressOnlyListener implements HTTPDownloadListener {
    
    /** Download progress event listener. */
    final DownloadListener listener;

    HTTPProgressOnlyListener(final DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end(boolean result) {
    }

    @Override
    public void progress(int progress) {
        listener.progress(progress);
    }

}
