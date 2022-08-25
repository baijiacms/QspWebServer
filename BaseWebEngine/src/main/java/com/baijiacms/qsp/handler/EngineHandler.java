package com.baijiacms.qsp.handler;

import com.baijiacms.qsp.common.QspConstants;
import com.baijiacms.qsp.template.*;
import com.baijiacms.qsp.util.ResponseUtil;
import com.baijiacms.qsp.util.StreamUtils;
import com.baijiacms.qsp.util.UrlRequestUtil;
import com.baijiacms.qsp.util.mime.MyMediaTypeFactory;
import com.baijiacms.qsp.web.UserManager;
import com.baijiacms.qsp.web.dto.User;
import com.baijiacms.qsp.web.dto.UserId;
import com.qsp.player.libqsp.NativeMethods;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.session.HashSessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class EngineHandler extends AbstractHandler {
    private static final int IS_SESSION=1;
    private static final int IS_NEW_SESSION=2;
    private static final int IS_NOT_SESSION=0;
    private static final int IS_UNKNOW=-1;
    private static final int IS_NOT_ACCESS=-2;



    private UserTemplate userTemplate;
    private ConsoleTemplate consoleTemplate;
    private ActionTemplate actionTemplate;
    private HtmlTemplate htmlTemplate;
    private IndexTemplate indexTemplate;
    private GameSelectTemplate gameSelectTemplate;
    private LoadingTemplate loadingTemplate;
    private GameSaveTemplate gameSaveTemplate;
    private HtmlHandler requestHtmlHandler;
    public EngineHandler( ) {
        initVelocityEngine();
    }

    private void initVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(Velocity.RESOURCE_LOADER, Velocity.RESOURCE_LOADER_CLASS);
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init();
        this.userTemplate = new UserTemplate(ve);
        this.actionTemplate = new ActionTemplate(ve);
        this.consoleTemplate = new ConsoleTemplate(ve);
        this.htmlTemplate = new HtmlTemplate(ve);
        this.indexTemplate = new IndexTemplate(ve);
        this.gameSelectTemplate = new GameSelectTemplate(ve);
        this.loadingTemplate = new LoadingTemplate(ve);
        this.gameSaveTemplate = new GameSaveTemplate(ve);
        requestHtmlHandler=new HtmlHandler(this);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        String urlPath = request.getRequestURL().toString();
        if (StringUtils.isEmpty(request.getParameter("actionScript")) == false) {
            urlPath = urlPath + "?actionScript=" + request.getParameter("actionScript");
        }
        int isSession=isSessionPath(target);
        if(isSession==IS_UNKNOW||isSession==IS_SESSION||isSession==IS_NEW_SESSION)
        {
            User user;
            if(isSession==IS_NEW_SESSION)
            {
                user=UserManager.INSTANCE.getOrNewUserBySessionId(request.getSession().getId(),this);
            }else
            {
                UserId userId=UserManager.INSTANCE.getUserIdBySessionId(request.getSession().getId());
                if(userId==null)
                {
                    ResponseUtil.stringWriteToResponse(response, StreamUtils.BLANK_STR);
                    ResponseUtil.setContentType(response, HtmlHandler.HTML_CONTENT_TYPE);
                    return;
                }
                user= UserManager.INSTANCE.getUser(userId.getUserId());
            }
            if(user!=null)
            {
                user.getHtmlHandler().handleSession(new URL(urlPath), target, response);
            }

        }else
        {
            if(isSession==IS_NOT_SESSION) {
                requestHtmlHandler.handleRequest(new URL(urlPath), target, response);
            }else
            {
                ResponseUtil.stringWriteToResponse(response, StreamUtils.BLANK_STR);
                ResponseUtil.setContentType(response, HtmlHandler.HTML_CONTENT_TYPE);
                return;
            }
        }

    }
    private int isSessionPath(String target)
    {

        target = target.replace(QspConstants.ENGINE_RESOURCE_PATH, "");
        if (target.startsWith("/") == false) {
            target = "/" + target;
        }
        if (StringUtils.isEmpty(target)) {
            target = "/";
        }

        switch (target) {
            case "/engine/isNeedRefresh":
                return IS_SESSION;
            case "/engine/isNeedRefreshHtml":
                return IS_SESSION;
            case "/engine/isNeedRefreshAction":
                return IS_SESSION;
            case "/engine/isNeedRefreshUser":
                return IS_SESSION;
            case "/engine/isNeedRefreshConsole":
                return IS_SESSION;
            case "/engineHtmlPage":
                return IS_SESSION;
            case "/engineUserPage":
                return IS_SESSION;
            case "/engineConsolePage":
                return IS_SESSION;
            case "/engineActionPage":
                return IS_SESSION;
            case "/engine/html":
                return IS_SESSION;
            case "/engine/user":
                return IS_SESSION;
            case "/engine/console":
                return IS_SESSION;
            case "/engine/action":
                return IS_SESSION;
            case "/engine/htmlCall":
                return IS_SESSION;
            case "/engine/userCall":
                return IS_SESSION;
            case "/engine/actionCall":
                return IS_SESSION;
            case "/engine/consoleCall":
                return IS_SESSION;
            case "/engine/openSaveWindow":
                return IS_NOT_ACCESS;
            case "/engine/closeSaveWindow":
                return IS_NOT_ACCESS;
            case "/engine/deleteGameSave":
                return IS_NOT_ACCESS;
            case "/engine/GameSave":
                return IS_NOT_ACCESS;
            case "/engine/LoadGameSave":
                return IS_NOT_ACCESS;
            case "/engine/QuickSave":
                return IS_SESSION;
            case "/engine/loadQuickSave":
                return IS_SESSION;
            case "/engine/gameIndex"://游戏主界面
                return IS_SESSION;
            case "/engine/loadGame":
                return IS_NEW_SESSION;
            case "/favicon.ico":
                return IS_NOT_SESSION;
            case "/":
            case "/engine.html"://游戏选择界面
                return IS_NOT_SESSION;
            case "/engine/loadingPage":
                return IS_NOT_SESSION;
            default:
                if (target.startsWith("/engine/lib/")) {
                    return IS_NOT_SESSION;
                }
        }
        return IS_UNKNOW;
    }

    public UserTemplate getUserTemplate() {
        return userTemplate;
    }

    public ConsoleTemplate getConsoleTemplate() {
        return consoleTemplate;
    }

    public ActionTemplate getActionTemplate() {
        return actionTemplate;
    }

    public HtmlTemplate getHtmlTemplate() {
        return htmlTemplate;
    }

    public IndexTemplate getIndexTemplate() {
        return indexTemplate;
    }

    public GameSelectTemplate getGameSelectTemplate() {
        return gameSelectTemplate;
    }

    public LoadingTemplate getLoadingTemplate() {
        return loadingTemplate;
    }

    public GameSaveTemplate getGameSaveTemplate() {
        return gameSaveTemplate;
    }


}
