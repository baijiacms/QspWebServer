package com.baijiacms.qsp;

import com.baijiacms.qsp.common.QspConstants;
import com.baijiacms.qsp.handler.EngineHandler;
import com.baijiacms.qsp.util.JarUtil;
import com.baijiacms.qsp.web.SessionListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

import java.net.Socket;

public class QspEngineServer {
    private Server server;
//    private String httpUrl;
    private int localPort;
    private boolean isStart = false;

    public QspEngineServer(int port) {
        this.localPort = port;

        QspConstants.setBaseFoler(JarUtil.getJarPath(this.getClass()));
    }

    private void updatePort(int port) {
        this.localPort = port;
        QspConstants.HTTP_PORT = port;
//        QspConstants.HTTP_LOCAL_URL = "http://127.0.0.1:" + port;
//        httpUrl = QspConstants.HTTP_LOCAL_URL;
    }

    private int checkPort() {
        int port = localPort;
        for (int i = 1; i < 100; i++) {
            try {
                new Socket("127.0.0.1", port);
                port = port + i;
            } catch (Exception e) {
                break;
            }
        }
        updatePort(port);
        return port;
    }

    public void start() throws Exception {
        if (isStart == false) {
            int port = checkPort();
            server = new Server(port);
            HashSessionIdManager idmanager = new HashSessionIdManager();

            server.setSessionIdManager(idmanager);

// Sessions are bound to a context.

            ContextHandler context = new ContextHandler("/");



// Create the SessionHandler (wrapper) to handle the sessions

            HashSessionManager manager = new HashSessionManager();
            manager.setMaxInactiveInterval(60);
            SessionHandler sessions = new SessionHandler(manager);
            sessions.addEventListener(new SessionListener());
            context.setHandler(new EngineHandler());
            sessions.setHandler(context);
// Put jettyHandler inside of SessionHandler
//            sessions.setHandler(new EngineHandler());
            server.setHandler(sessions);
            server.start();
            isStart = true;
        }
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public int getLocalPort() {
        return localPort;
    }
}
