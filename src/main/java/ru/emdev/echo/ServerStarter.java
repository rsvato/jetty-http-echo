package ru.emdev.echo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerStarter {

    private final int port;
    private final QueuedThreadPool pool;

    private final boolean ssl;

    private final Logger logger = LoggerFactory.getLogger(ServerStarter.class);
    private final String keyfile;
    private final String keypass;

    public ServerStarter(int port, int maxThreads) {
        this.port = port;
        this.pool = new QueuedThreadPool(maxThreads);
        this.ssl = false;
        this.keyfile = null;
        this.keypass = null;
    }

    public ServerStarter(int port, int maxThreads, String keyfile, String keypass) {
        this.port = port;
        this.pool = new QueuedThreadPool(maxThreads);
        this.ssl = true;
        this.keyfile = keyfile;
        this.keypass = keypass;
    }

    public void start() {
        Server server = new Server(pool);
        HttpConfiguration http = new HttpConfiguration();
        HttpConnectionFactory http11 = new HttpConnectionFactory(http);
        ServerConnector connector;
        if (ssl) {
            HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(http);
            ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
            ConnectionFactory connectionFactory = buildConnectionFactory(http, alpn.getProtocol());
            alpn.setDefaultProtocol(http11.getProtocol());
            connector = new ServerConnector(server, connectionFactory, alpn, h2, http11);
        } else {
            connector = new ServerConnector(server, http11);
        }
        server.addConnector(connector);
        connector.setPort(port);
        connector.setIdleTimeout(1000);
        server.setHandler(new EchoHandler());
        try {
            logger.info("Starting server on port {}", port);
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error("Error starting Jetty echo", e);
            try {
                server.stop();
            } catch (Exception ex) {
                logger.error("Error stopping server", ex);
            }
        }
    }

    private ConnectionFactory buildConnectionFactory(HttpConfiguration http, String protocol) {
        ConnectionFactory result;
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setKeyStorePath(keyfile);
        sslContextFactory.setKeyStorePassword(keypass);
        sslContextFactory.setKeyStoreType("JKS");
        sslContextFactory.setSniRequired(false);
        http.addCustomizer(new SecureRequestCustomizer(false));
        result = new SslConnectionFactory(sslContextFactory, protocol);
        return result;
    }

    private static class EchoHandler extends AbstractHandler {

        @Override
        public void handle(String target, Request jettyRequest, HttpServletRequest request,
                HttpServletResponse response) throws IOException, ServletException {
            String contentType = request.getContentType();
            if (contentType != null) {
                response.setContentType(contentType);
            }
            response.setContentLength(request.getContentLength());
            try (InputStream in = request.getInputStream();
                    OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
                out.flush();
            }
            jettyRequest.setHandled(true);
        }
    }
}
