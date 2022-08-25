package com.baijiacms.qsp.player.thread;

import com.baijiacms.qsp.common.FolderLoader;
import com.baijiacms.qsp.dto.GameObject;
import com.baijiacms.qsp.dto.QspListItem;
import com.baijiacms.qsp.dto.RefreshRequest;
import com.baijiacms.qsp.player.GameInterface;
import com.baijiacms.qsp.player.service.HtmlProcessor;
import com.baijiacms.qsp.util.StreamUtils;
import com.baijiacms.qsp.util.StringUtil;
import com.baijiacms.qsp.util.Uri;
import com.baijiacms.qsp.vo.GameVo;
import com.qsp.player.libqsp.NativeMethods;
import com.qsp.player.libqsp.util.DevUtils;
import com.qsp.player.libqsp.LibQspProxyImpl;
import com.qsp.player.libqsp.dto.ActionData;
import com.qsp.player.libqsp.dto.ErrorData;
import com.qsp.player.libqsp.dto.ObjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class LibQspThread extends Thread {
    private ThreadObject libQspThreadObject = new ThreadObject();

    private static final Logger logger = LoggerFactory.getLogger(LibQspThread.class);

    private String threadName;
    private final DevUtils devUtils;

    private LibQspProxyImpl libQspProxyImpl;
    public int userId;
    public LibQspThread(int userId, LibQspProxyImpl libQspProxyImpl) {
        super("ThreadName@" + userId);
        logger.info("newUserId="+userId);
        this.userId=userId;
        this.threadName = "ThreadName@" + userId;
        this.libQspProxyImpl = libQspProxyImpl;
        devUtils = new DevUtils();
        this.libQspProxyImpl.getNativeMethods().CallBacksInit(userId);
    }

    @Override
    public void run() {
        while (true) {
            if (libQspThreadObject.seekCount < 0) {
                libQspThreadObject.seekCount = 0;
            }
            if (libQspThreadObject.seekCount == 0) {
                libQspThreadObject.threadRun = false;

                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                    try {
//                        libQspThreadObject.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
            } else {
                libQspThreadObject.threadRun = true;
            }

            if (libQspThreadObject.threadRun) {
                libQspThreadObject.seekCount = libQspThreadObject.seekCount - 1;
                try {
                    toDo();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void threadRun() {
        libQspThreadObject.seekCount = libQspThreadObject.seekCount + 1;
        libQspThreadObject.threadRun = true;

    }

    private void toDo() {
//        synchronized (libQspThreadObject) {
        switch (libQspThreadObject.method) {
            case ThreadConstants.QSP_FILE_TO_TEXT:
                qspFileToText();
                break;
            case ThreadConstants.TO_GEM_FILE:
                toGemFile();
                break;
            case ThreadConstants.QSP_SELECT_MENU_ITEM:
                qspSelectMenuItem();
                break;
            case ThreadConstants.LOAD_GAME_STATE:
                loadGameState();
                break;
            case ThreadConstants.QSP_EXEC_COUNTER:
                qspExecCounter();
                break;
            case ThreadConstants.QSP_EXEC_STRING:
                qspExecString();
                break;
            case ThreadConstants.QSP_SET_INPUT_STR_TEXT:
                qspSetInputStrText();
                break;
            case ThreadConstants.QSP_SET_SEL_OBJECT_INDEX:
                qspSetSelObjectIndex();
                break;
            case ThreadConstants.QSP_SET_SEL_ACTION_INDEX:
                qspSetSelActionIndex();
                break;
            case ThreadConstants.QSP_EXECUTE_SEL_ACTION_CODE:
                qspExecuteSelActionCode();
                break;
            case ThreadConstants.QSP_RESTART_GAME:
                qspRestartGame();
                break;
            case ThreadConstants.QSP_START:
                qspStart();
                break;
            case ThreadConstants.LOAD_GAME_WORLD:
                loadGameWorld();
                break;
            case ThreadConstants.QSP_SAVE_GAME_AS_DATA:
                qspSaveGameAsData();
                break;
            default:
                break;

        }
//        }
    }

    private GameVo gameVo;
    private String toFile;
    private int result;
    private Uri uri;
    private GameInterface gameInterface;
    private String code;
    private String input;
    private int index;
    private GameObject gameObject;

    public void qspSaveGameAsData(GameInterface gameInterface, Uri uri) {
        this.uri = uri;
        this.gameInterface = gameInterface;

        if (threadName.equals(Thread.currentThread().getName())) {

            qspSaveGameAsData();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_SAVE_GAME_AS_DATA;
                threadRun();
            }
        }
    }

    private void qspSaveGameAsData() {

        byte[] gameData = this.libQspProxyImpl.getNativeMethods().QSPSaveGameAsData(false,this.userId);
        if (gameData == null) {
            logger.error("not found save date userid:"+userId);
            return;
        }

        try (OutputStream out = StreamUtils.openOutputStream(uri)) {
            out.write(gameData);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Failed to save the game state", ex);
        }
    }

    public void loadGameWorld(GameObject gameObject, GameInterface gameInterface) {

        this.gameObject = gameObject;
        this.gameInterface = gameInterface;

        if (threadName.equals(Thread.currentThread().getName())) {

            loadGameWorld();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.LOAD_GAME_WORLD;
                threadRun();
            }
        }
    }

    private void loadGameWorld() {
        byte[] gameData;
        GameVo gameVo = FolderLoader.getFolderMap().get(gameObject.gameId);
        if (gameVo.getIsDevProject() == 1) {
//            devUtils.toGemFile(gameVo.getGameDevFolder(),gameVo.getGameQproj(),"D:/1.qsp");
            gameData = this.devUtils.getGemDate(gameVo.getGameDevFolder(), gameVo.getGameQproj());
        } else {
            try (FileInputStream in = new FileInputStream(gameObject.gameFile)) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    StreamUtils.copy(in, out);
                    gameData = out.toByteArray();
                }
            } catch (IOException ex) {
                logger.error("Failed to load the game world", ex);
                return;
            }
        }
        String fileName = gameObject.gameFile.getAbsolutePath();
        if (!this.libQspProxyImpl.getNativeMethods().QSPLoadGameWorldFromData(gameData, gameData.length, fileName,this.userId)) {

            showLastQspError(gameInterface);
            return;
        }
        this.libQspProxyImpl.getGameStatus().gameStartTime = System.currentTimeMillis();
        this.libQspProxyImpl.getGameStatus().lastMsCountCallTime = 0;
        qspRestartGame(gameInterface);


        return;
    }

    public void qspStart(GameInterface gameInterface) {

        this.gameInterface = gameInterface;

        if (threadName.equals(Thread.currentThread().getName())) {

            qspStart();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_START;
                threadRun();
            }
        }
    }
    private Set<Integer> qspInitSet= Collections.synchronizedSet(new HashSet<>());
    private void qspStart() {
        if(qspInitSet.add(this.userId)) {
            this.libQspProxyImpl.getNativeMethods().QSPInit(this.userId);
        }
//        this.libQspProxyImpl.getNativeMethods().QSPDeInit();
    }

    public void qspRestartGame(GameInterface gameInterface) {

        this.gameInterface = gameInterface;

        if (threadName.equals(Thread.currentThread().getName())) {

            qspRestartGame();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_RESTART_GAME;
                threadRun();
            }
        }
    }

    private void qspRestartGame() {
        if (!this.libQspProxyImpl.getNativeMethods().QSPRestartGame(true,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspExecuteSelActionCode(int index, GameInterface gameInterface) {

        this.gameInterface = gameInterface;
        this.index = index;
        if (threadName.equals(Thread.currentThread().getName())) {

            qspExecuteSelActionCode();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_EXECUTE_SEL_ACTION_CODE;
                threadRun();
            }
        }
    }

    private void qspExecuteSelActionCode() {
        if (!this.libQspProxyImpl.getNativeMethods().QSPSetSelActionIndex(index, false,this.userId)) {
            showLastQspError(gameInterface);
        }
        if (!this.libQspProxyImpl.getNativeMethods().QSPExecuteSelActionCode(true,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspSetSelActionIndex(int index, GameInterface gameInterface) {

        this.gameInterface = gameInterface;
        this.index = index;

        if (threadName.equals(Thread.currentThread().getName())) {

            qspSetSelActionIndex();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_SET_SEL_ACTION_INDEX;
                threadRun();
            }
        }
    }

    private void qspSetSelActionIndex() {
        if (!this.libQspProxyImpl.getNativeMethods().QSPSetSelActionIndex(index, false,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspSetSelObjectIndex(int index, GameInterface gameInterface) {

        this.gameInterface = gameInterface;
        this.index = index;
        if (threadName.equals(Thread.currentThread().getName())) {

            qspSetSelObjectIndex();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_SET_SEL_OBJECT_INDEX;
                threadRun();
            }
        }
    }

    private void qspSetSelObjectIndex() {
        if (!this.libQspProxyImpl.getNativeMethods().QSPSetSelObjectIndex(index, true,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspSetInputStrText(String input, GameInterface gameInterface) {

        this.gameInterface = gameInterface;
        this.input = input;

        if (threadName.equals(Thread.currentThread().getName())) {

            qspSetInputStrText();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_SET_INPUT_STR_TEXT;
                threadRun();
            }
        }
    }

    private void qspSetInputStrText() {
        this.libQspProxyImpl.getNativeMethods().QSPSetInputStrText(input,this.userId);

        if (!this.libQspProxyImpl.getNativeMethods().QSPExecUserInput(true,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspExecString(String code, GameInterface gameInterface) {
        this.gameInterface = gameInterface;
        this.code = code;
        if (threadName.equals(Thread.currentThread().getName())) {

            qspExecString();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_EXEC_STRING;
                threadRun();
            }
        }
    }

    private void qspExecString() {
        logger.debug("exec:" + code);
        if (!this.libQspProxyImpl.getNativeMethods().QSPExecString(code, true,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspExecCounter(GameInterface gameInterface) {
        this.gameInterface = gameInterface;

        if (threadName.equals(Thread.currentThread().getName())) {

            qspExecCounter();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_EXEC_COUNTER;
                threadRun();
            }
        }
    }

    private void qspExecCounter() {
        if (!this.libQspProxyImpl.getNativeMethods().QSPExecCounter(true,this.userId)) {
            showLastQspError(this.gameInterface);
        }
    }

    public void loadGameState(final Uri uri, GameInterface gameInterface) {

        this.uri = uri;
        this.gameInterface = gameInterface;

        if (threadName.equals(Thread.currentThread().getName())) {

            loadGameState();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.LOAD_GAME_STATE;
                threadRun();
            }
        }
    }

    private void loadGameState() {

        logger.info("command:loadGameState");
        final byte[] gameData;

        try (InputStream in = StreamUtils.openInputStream(uri)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                StreamUtils.copy(in, out);
                gameData = out.toByteArray();
            }
        } catch (IOException ex) {
            logger.error("Failed to load game state", ex);
            return;
        }

        if (!this.libQspProxyImpl.getNativeMethods().QSPOpenSavedGameFromData(gameData, gameData.length, true,this.userId)) {
            showLastQspError(gameInterface);
        }
    }

    public void qspSelectMenuItem(int result) {
        this.result = result;
        if (threadName.equals(Thread.currentThread().getName())) {

            qspSelectMenuItem();
        } else {
            synchronized (libQspThreadObject) {
                libQspThreadObject.method = ThreadConstants.QSP_SELECT_MENU_ITEM;
                threadRun();
            }
        }
    }

    private void qspSelectMenuItem() {
        this.libQspProxyImpl.getNativeMethods().QSPSelectMenuItem(result,this.userId);
    }

    public void qspFileToText(GameVo gameVo, String toFile) {
        this.gameVo = gameVo;
        this.toFile = toFile;
//        if(threadName.equals(Thread.currentThread().getName()))
//        {

        qspFileToText();
//        }else {
//            synchronized (libQspThreadObject) {
//                libQspThreadObject.method = ThreadConstants.qspFileToText;
//                libQspThreadObject.seekCount = libQspThreadObject.seekCount + 1;
//                threadGoto();
//            }
//        }
    }

    private void qspFileToText() {
        if (gameVo.getIsDevProject() == 0) {
            this.devUtils.qspFileToText(gameVo.getGameFile(), toFile, gameVo.getQspPassword());
        }
    }

    public void toGemFile(GameVo gameVo, String toFile) {
        this.gameVo = gameVo;
        this.toFile = toFile;
//        if(threadName.equals(Thread.currentThread().getName()))
//        {

        toGemFile();
//        }else {
//            synchronized (libQspThreadObject) {
//                libQspThreadObject.method = ThreadConstants.toGemFile;
//                libQspThreadObject.seekCount = libQspThreadObject.seekCount + 1;
//                threadGoto();
//            }
//        }
    }

    private void toGemFile() {
        if (gameVo.getIsDevProject() == 1) {
            devUtils.toGemFile(gameVo.getGameDevFolder(), gameVo.getGameQproj(), toFile);
        }
    }

    private void showLastQspError(GameInterface gameInterface) {
        ErrorData errorData = (ErrorData) this.libQspProxyImpl.getNativeMethods().QSPGetLastErrorData(this.userId);
        String locName = StringUtil.getStringOrEmpty(errorData.locName);
        String desc = StringUtil.getStringOrEmpty(this.libQspProxyImpl.getNativeMethods().QSPGetErrorDesc(errorData.errorNum,this.userId));

        final String message = String.format(
                Locale.getDefault(),
                "Location: %s\nAction: %d\nLine: %d\nError number: %d\nuserId: %d\nDescription: %s",
                locName,
                errorData.index,
                errorData.line,
                errorData.errorNum,
                this.userId,
                desc);

        logger.error(message);
        GameInterface inter = gameInterface;
        if (inter != null) {
            gameInterface.showError(message);
        }
    }

    public RefreshRequest getRefreshInterfaceRequest(HtmlProcessor htmlProcessor) {
//        synchronized (libQspThreadObject) {
        RefreshRequest request = new RefreshRequest();

        if (this.libQspProxyImpl.getNativeMethods().QSPIsMainDescChanged(this.userId)) {
            gameObject.mainDesc = this.libQspProxyImpl.getNativeMethods().QSPGetMainDesc(this.userId);
            request.mainDescChanged = true;
        }
        if (this.libQspProxyImpl.getNativeMethods().QSPIsActionsChanged(this.userId)) {
            gameObject.actions = getActions(htmlProcessor);
            request.actionsChanged = true;
        }
        if (this.libQspProxyImpl.getNativeMethods().QSPIsObjectsChanged(this.userId)) {
            gameObject.objects = getObjects(htmlProcessor);
            request.objectsChanged = true;
        }
        if (this.libQspProxyImpl.getNativeMethods().QSPIsVarsDescChanged(this.userId)) {
            gameObject.varsDesc = this.libQspProxyImpl.getNativeMethods().QSPGetVarsDesc(this.userId);
            request.varsDescChanged = true;
        }
        return request;
//        }
    }


    private ArrayList<QspListItem> getActions(HtmlProcessor htmlProcessor) {
        ArrayList<QspListItem> actions = new ArrayList<>();

        int count = this.libQspProxyImpl.getNativeMethods().QSPGetActionsCount(this.userId);
        for (int i = 0; i < count; ++i) {
            ActionData actionData = (ActionData) this.libQspProxyImpl.getNativeMethods().QSPGetActionData(i,this.userId);
            QspListItem action = new QspListItem();
            action.index = i;
            action.text = true ? htmlProcessor.removeHtmlTags(actionData.name) : actionData.name;
            actions.add(action);
        }
        return actions;
    }

    private ArrayList<QspListItem> getObjects(HtmlProcessor htmlProcessor) {
        ArrayList<QspListItem> objects = new ArrayList<>();
        int count = this.libQspProxyImpl.getNativeMethods().QSPGetObjectsCount(this.userId);
        for (int i = 0; i < count; i++) {
            ObjectData objectResult = (ObjectData) this.libQspProxyImpl.getNativeMethods().QSPGetObjectData(i,this.userId);
            QspListItem object = new QspListItem();
            object.index = i;
            object.text = true ? htmlProcessor.removeHtmlTags(objectResult.name) : objectResult.name;
            objects.add(object);
        }
        return objects;
    }

}
