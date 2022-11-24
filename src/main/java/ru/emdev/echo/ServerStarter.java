package ru.emdev.echo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventListener;

public class ServerStarter {

    private final int port;
    private final QueuedThreadPool pool;

    private final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

    public ServerStarter(int port, int maxThreads) {
        this.port = port;
        this.pool = new QueuedThreadPool(maxThreads);
    }

    public void start() {
        Server server = new Server(pool);
        ServerConnector connector = new ServerConnector(server);
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
        }
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
