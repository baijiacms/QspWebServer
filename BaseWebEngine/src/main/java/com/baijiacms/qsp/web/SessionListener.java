package com.baijiacms.qsp.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);
    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId=se.getSession().getId();
        logger.info("sessionDestroyed:"+sessionId);
        UserManager.INSTANCE.deleteUserBySessionId(sessionId);
    }
}
