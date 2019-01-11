/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.utils;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * Operating system related utilities.
 */
public class OSUtils {

    /**
     * Initialize HTTP PROXY to be used for modules download.
     * @return New {@link Proxy} instance or {@code null} if no PROXY is set.
     */
    public static Proxy systemHTTPProxy() {
        String proxyStr = System.getenv("http_proxy");
        //String proxyStr = "www-proxy-ukc1.uk.oracle.com:80";
        if (proxyStr == null) {
            proxyStr = System.getenv("HTTP_PROXY");
        }
        if (proxyStr != null) {
            try {
                if (!proxyStr.startsWith("http://")) {
                    proxyStr = "http://" + proxyStr;
                }
                final URL url = new URL(proxyStr);
                Logger.log(LogLevel.FINE, "HTTP proxy: %s:%d", url.getHost(), url.getPort());
                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), url.getPort()));
            } catch (MalformedURLException ex) {
                Logger.log(LogLevel.WARNING, "Invalid URL: %s", proxyStr);
                return Proxy.NO_PROXY;
            }
        }
        Logger.log(LogLevel.FINE, "No HTTP proxy is set");
        return Proxy.NO_PROXY;
    }

    /**
     * Initialize HTTP PROXY to be used for modules download.
     * @param host HTTP proxy host.
     * @param port HTTP proxy port
     * @return New {@link Proxy} instance or {@code null} if no PROXY is set.
     */
    public static Proxy customHTTPProxy(final String host, final int port) {
        if (host != null && host.length() > 1) {
            int portVal = port >= 0 ? port : 80;
            Logger.log(LogLevel.FINE, "HTTP proxy: %s:%d", host, portVal);
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, portVal));
        }
        return Proxy.NO_PROXY;
    }

}
