package com.baijiacms.qsp.web.dto;

import com.baijiacms.qsp.handler.EngineHandler;
import com.baijiacms.qsp.handler.HtmlHandler;

/**
 * @author cxy
 */
public class User {
    private HtmlHandler htmlHandler;
    private int userId;
    public User(int userId, EngineHandler engineHandler)
    {
        this.userId=userId;
        htmlHandler = new HtmlHandler(userId,engineHandler);
    }

    public HtmlHandler getHtmlHandler() {
        return htmlHandler;
    }

    public int getUserId() {
        return userId;
    }
}
