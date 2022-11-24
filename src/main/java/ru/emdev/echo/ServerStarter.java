package ru.emdev.echo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

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
        if (ssl) {
            http.addCustomizer(new SecureRequestCustomizer(false));
        }
        ConnectionFactory connectionFactory = buildConnectionFactory();
        ServerConnector connector = new ServerConnector(server, connectionFactory,
                new HttpConnectionFactory(http));
        server.addConnector(connector);
        connector.setPort(port);
        connector.setIdleTimeout(1000);

        if (ssl) {

        }

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

    private ConnectionFactory buildConnectionFactory() {
        ConnectionFactory result;
        if (ssl) {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setTrustAll(true);
            sslContextFactory.setKeyStorePath(keyfile);
            sslContextFactory.setKeyStorePassword(keypass);
            sslContextFactory.setKeyStoreType("JKS");
            sslContextFactory.setSniRequired(false);
            Properties props = System.getProperties();
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification",Boolean.TRUE.toString());
            sslContextFactory.setProtocol("TLS");
            result = new SslConnectionFactory(sslContextFactory, "http/1.1");
        } else {
            result = new HttpConnectionFactory();
        }
        return result;
    }

    private static class EchoHandler extends AbstractHandler {

        @Override
        public void handle(String target, Request jettyRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String contentType = request.getContentType();
            if (contentType != null) {
                response.setContentType(contentType);
            }
            response.setContentLength(request.getContentLength());
            try (InputStream in = request.getInputStream();
                    OutputStream out = response.getOutputStream()){
               in.transferTo(out);
               out.flush();
            }
            jettyRequest.setHandled(true);
        }
    }
}
