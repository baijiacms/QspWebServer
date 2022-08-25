package com.baijiacms.qsp.web;

import com.baijiacms.qsp.handler.EngineHandler;
import com.baijiacms.qsp.web.dto.User;
import com.baijiacms.qsp.web.dto.UserId;

import java.util.*;

public class UserManager {
    public static final UserManager INSTANCE=new UserManager();
    private Map<String, UserId> userIdMap=new Hashtable<>();
    private UserManager()
    {
        for(int i=0;i<100;i++)
        {
            UserId userId=new UserId();
            userId.setUse(false);
            userId.setUserId(i);
            userIdMap.put(USER_STR+i,userId);
        }
    }
    Map<String, User> userMamager=new Hashtable<>();
    Map<String, UserId> sessionMamager=new Hashtable<>();
    private String USER_STR="USER_";
    public synchronized  UserId getUserIdBySessionId(String sessionId)
    {
        UserId userId=sessionMamager.get(sessionId);
        return userId;
    }
    public synchronized  User getOrNewUserBySessionId(String sessionId, EngineHandler engineHandler)
    {
        UserId userId=sessionMamager.get(sessionId);
        if(userId==null)
        {
            userId=synchronizedUser(-1);
            if(userId!=null) {
                sessionMamager.put(sessionId, userId);
            }else
            {
                throw new RuntimeException("Max user online");
            }
        }
        return getUser(userId.getUserId(),engineHandler);
    }
    public synchronized  void deleteUserBySessionId(String sessionId)
    {
        UserId userId=sessionMamager.get(sessionId);
        if(userId!=null) {
            User user= userMamager.get(USER_STR+userId.getUserId());
            if(user!=null)
            {
                if( user.getHtmlHandler()!=null&&user.getHtmlHandler().getmPlayerEngine()!=null&&user.getHtmlHandler().getmPlayerEngine().getGameStatus()!=null) {
                    user.getHtmlHandler().getmPlayerEngine().getGameStatus().isStart = false;
                }
            }
            userId.setUse(false);
            if(userId.getUserId()>=9) {
                synchronizedUser(userId.getUserId());
            }
        }
        sessionMamager.remove(sessionId);
    }
    private synchronized UserId synchronizedUser(int userId)
    {

            if(userId<0) {
                for(int i=0;i<100;i++) {
                    UserId user =userIdMap.get(USER_STR+i);
                    if (user.isUse() == false) {
                        user.setUse(true);
                        return user;
                    }
                }
            }else
            {
                UserId oldUser =userIdMap.get(USER_STR+userId);
                if(oldUser!=null)
                {
                    oldUser.setUse(false);
                    return null;
                }
            }

        return null;
    }
    public  User getUser(int userId)
    {
        User user= userMamager.get(USER_STR+userId);
        return user;
    }
    private synchronized  User getUser(int userId,EngineHandler engineHandler)
    {
        User user= userMamager.get(USER_STR+userId);
        if(user==null)
        {
            user=new User(userId,engineHandler);
            userMamager.put(USER_STR+userId,user);
        }
        return user;
    }
}
