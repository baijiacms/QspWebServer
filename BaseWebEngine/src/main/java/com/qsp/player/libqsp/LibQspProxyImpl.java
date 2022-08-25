package com.qsp.player.libqsp;

import com.baijiacms.qsp.common.QspConstants;
import com.baijiacms.qsp.common.WindowType;
import com.baijiacms.qsp.dto.GameObject;
import com.baijiacms.qsp.dto.GameStatus;
import com.baijiacms.qsp.dto.QspMenuItem;
import com.baijiacms.qsp.dto.RefreshRequest;
import com.baijiacms.qsp.player.GameInterface;
import com.baijiacms.qsp.player.PlayerEngine;
import com.baijiacms.qsp.player.service.HtmlProcessor;
import com.baijiacms.qsp.player.thread.LibQspThread;
import com.baijiacms.qsp.util.Uri;
import com.baijiacms.qsp.vi.AudioPlayer;
import com.baijiacms.qsp.vo.GameVo;
import com.baijiacms.qsp.web.UserManager;
import com.baijiacms.qsp.web.dto.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import static com.baijiacms.qsp.util.FileUtil.*;
import static com.baijiacms.qsp.util.StringUtil.isNotEmpty;

public class LibQspProxyImpl implements LibQspProxy, LibQspCallbacks{
    private static final Logger logger = LoggerFactory.getLogger(LibQspProxyImpl.class);

    private final ReentrantLock libQspLock = new ReentrantLock();
    private final GameObject gameObject = new GameObject();
    private GameInterface gamePlayer;

    private final HtmlProcessor htmlProcessor;
    private final AudioPlayer audioPlayer;
    private LibQspThread libQspThread;
    private GameStatus gameStatus;
    public static final String QUICK_SAVE_NAME = "quickSave";
    private int userId;
    NativeMethods nativeMethods;
    public LibQspProxyImpl(int userId,GameStatus gameStatus,
                           HtmlProcessor htmlProcessor,
                           AudioPlayer audioPlayer, GameInterface gamePlayer) {
        this.userId=userId;
        this.gameStatus = gameStatus;
        this.htmlProcessor = htmlProcessor;
        this.audioPlayer = audioPlayer;
        this.gamePlayer = gamePlayer;
    }

    // region LibQspProxy

    @Override
    public void start() {
        nativeMethods=  new NativeMethods(this);
        logger.info("command:start");
        this.libQspThread = new LibQspThread(userId, this);
        this.libQspThread.start();
        this.libQspThread.qspStart(gamePlayer);
    }

    @Override
    public void stop() {


    }

    @Override
    public void runGame(final String id, final String title, final File dir, final File file) {

        logger.info("command:runGame");
        doRunGame(id, title, dir, file);
    }

    private void doRunGame(final String id, final String title, final File dir, final File file) {

        logger.info("doRunGame Thread:" + Thread.currentThread().getName());


        gameObject.reset();
        gameObject.gameRunning = true;
        gameObject.gameId = id;
        gameObject.gameTitle = title;
        gameObject.gameDir = dir;
        gameObject.gameFile = file;
        this.libQspThread.loadGameWorld(gameObject, gamePlayer);
        gameStatus.gameStartTime = System.currentTimeMillis();
        gameStatus.lastMsCountCallTime = 0;


    }

    @Override
    public void restartGame() {

        logger.info("command:restartGame");
        GameObject state = gameObject;
        doRunGame(state.gameId, state.gameTitle, state.gameDir, state.gameFile);

    }

    @Override
    public void loadGameState(final Uri uri) {

        this.libQspThread.loadGameState(uri, this.gamePlayer);
    }

    @Override
    public void saveGameState(final Uri uri) {
        logger.info("command:saveGameState" + uri);
        if (uri != null) {

            logger.info("command:saveGameStateuri:" + uri.getmFile());
        }
        this.libQspThread.qspSaveGameAsData(gamePlayer, uri);
    }

    @Override
    public void onActionSelected(final int index) {

        logger.info("command:onActionSelected");

        this.libQspThread.qspSetSelActionIndex(index, gamePlayer);
    }

    @Override
    public void onActionClicked(final int index) {

        logger.info("command:onActionClicked");
        this.libQspThread.qspExecuteSelActionCode(index, gamePlayer);
    }

    @Override
    public void onObjectSelected(final int index) {

        logger.info("command:onObjectSelected");
        this.libQspThread.qspSetSelObjectIndex(index, gamePlayer);
    }

    @Override
    public void onInputAreaClicked() {

        logger.info("command:onInputAreaClicked");
        final GameInterface inter = gamePlayer;
        if (inter == null) {
            return;
        }

        String input = inter.showInputBox("userInput");
        this.libQspThread.qspSetInputStrText(input, gamePlayer);
    }

    @Override
    public void execute(final String code) {
        String code2 = code.trim();
        logger.info("command:execute:" + code2);
        if ("OPENGAME".equals(code2)) {
            if (gameStatus.isBigKuyash) {

                this.gameStatus.isOpenSaveWindow = true;
                return;
            }
        }
        this.libQspThread.qspExecString(code, gamePlayer);
    }

    @Override
    public void executeCounter() {

        logger.info("command:executeCounter");
        if (libQspLock.isLocked()) {
            return;
        }

        this.libQspThread.qspExecCounter(this.gamePlayer);
    }

    @Override
    public GameObject getGameObject() {
        //刷新
        return gameObject;
    }


    @Override
    public void qspFileToText(GameVo gameVo, String toFile) {
        this.libQspThread.qspFileToText(gameVo, toFile);
    }

    @Override
    public void toGemFile(GameVo gameVo, String toFile) {

        this.libQspThread.toGemFile(gameVo, toFile);
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }
    public NativeMethods getNativeMethods() {
        return nativeMethods;
    }
    @Override
    public LibQspThread getLibQspThread() {
        return libQspThread;
    }





    @Override
    public void RefreshInt(int userId) {
        logger.info("command:RefreshInt"+userId);
        User user= UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        LibQspThread libQspThread= gamePlayer.getLibQspProxy().getLibQspThread();
        RefreshRequest request = libQspThread.getRefreshInterfaceRequest(htmlProcessor);
        GameInterface inter = gamePlayer;
        if (inter != null) {
            inter.refresh(request);
        }
    }

    @Override
    public void ShowPicture(String path,int userId) {
        logger.info("command:ShowPicture");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();

        GameInterface inter = gamePlayer;
        if (inter != null && isNotEmpty(path)) {
            inter.showPicture(path);
        }
    }

    @Override
    public void SetTimer(int msecs,int userId) {
        logger.info("command:SetTimer");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();

        GameInterface inter = gamePlayer;
        if (inter != null) {
            inter.setCounterInterval(msecs);
        }
    }

    @Override
    public void ShowMessage(String message,int userId) {
        logger.info("command:ShowMessage");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameInterface inter = gamePlayer;
        if (inter != null) {
            inter.showMessage(message);
        }
    }

    @Override
    public void PlayFile(String path, int volume,int userId) {
        logger.info("command:PlayFile");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        AudioPlayer audioPlayer=gamePlayer.getAudioPlayer();
        GameStatus gameStatus=gamePlayer.getGameStatus();
        if (isNotEmpty(path)) {
            audioPlayer.playFile(gameStatus, path, volume);
        }
    }

    @Override
    public boolean IsPlayingFile(final String path,int userId) {
        logger.info("command:IsPlayingFile");

        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return false;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        AudioPlayer audioPlayer=gamePlayer.getAudioPlayer();
        return isNotEmpty(path) && audioPlayer.isPlayingFile(path);
    }

    @Override
    public void CloseFile(String path,int userId) {

        logger.info("command:CloseFile");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        AudioPlayer audioPlayer=gamePlayer.getAudioPlayer();

        if (isNotEmpty(path)) {
            audioPlayer.closeFile(path);
        } else {
            audioPlayer.closeAllFiles();
        }
    }

    @Override
    public void OpenGame(String filename,int userId) {

        logger.info("command:OpenGame");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameStatus gameStatus=gamePlayer.getGameStatus();
        GameObject gameObject= gamePlayer.getLibQspProxy().getGameObject();

        if (StringUtils.isEmpty(filename)) {
            gameStatus.isOpenSaveWindow = true;
            return;
        }
        File savesDir = getOrCreateDirectory(gameObject.gameDir, "saves");
        if (StringUtils.isEmpty(filename)) {
            filename = QUICK_SAVE_NAME;
        }

        if (filename.endsWith(".sav") == false) {
            filename = filename + ".sav";
        }
        File saveFile = findFileOrDirectory(savesDir, filename);
        gameStatus.refreshAll();
        if (saveFile == null || saveFile.exists() == false) {
            logger.error("Save file not found: " + gameObject.gameDir + "/" + filename);
            return;
        }
        GameInterface inter = gamePlayer;
        if (inter != null) {
            gamePlayer.getLibQspProxy().loadGameState(Uri.toUri(saveFile));
        }
    }

    @Override
    public void SaveGame(String filename,int userId) {

        logger.info("command:SaveGame:" + filename);
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameStatus gameStatus=gamePlayer.getGameStatus();

        if (StringUtils.isEmpty(filename)) {
            gameStatus.isOpenSaveWindow = true;
            return;
        }
        GameInterface inter = gamePlayer;
        gameStatus.refreshAll();
        if (inter != null) {
            inter.saveGame(filename);
        }
    }

    @Override
    public String InputBox(String prompt,int userId) {
        logger.info("command:InputBox");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return null;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();

        GameInterface inter = gamePlayer;
        return inter != null ? inter.showInputBox(prompt) : null;
    }

    @Override
    public int GetMSCount(int userId) {
        logger.info("command:GetMSCount");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return 0;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameStatus gameStatus=gamePlayer.getGameStatus();
        long now = System.currentTimeMillis();
        if (gameStatus.lastMsCountCallTime == 0) {
            gameStatus.lastMsCountCallTime = gameStatus.gameStartTime;
        }
        int dt = (int) (now - gameStatus.lastMsCountCallTime);
        gameStatus.lastMsCountCallTime = now;

        return dt;
    }

    @Override
    public void AddMenuItem(String name, String imgPath,int userId) {
        logger.info("command:AddMenuItem");

        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameObject gameObject= gamePlayer.getLibQspProxy().getGameObject();
        QspMenuItem item = new QspMenuItem();
        item.imgPath = imgPath;
        item.name = name;
        gameObject.menuItems.add(item);
    }

    @Override
    public void ShowMenu(int userId) {

        logger.info("command:ShowMenu");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        LibQspThread libQspThread= gamePlayer.getLibQspProxy().getLibQspThread();
        GameInterface inter = gamePlayer;
        if (inter == null) {
            return;
        }

        int result = inter.showMenu();
        if (result != -1) {
            libQspThread.qspSelectMenuItem(result);
        }
    }

    @Override
    public void DeleteMenu(int userId) {

        logger.info("command:DeleteMenu");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameObject gameObject= gamePlayer.getLibQspProxy().getGameObject();
        gameObject.menuItems.clear();
    }

    @Override
    public void Wait(int msecs,int userId) {

        logger.info("command:Wait");
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException ex) {
            logger.error("Wait failed", ex);
        }
    }

    @Override
    public void ShowWindow(int type, boolean isShow,int userId) {

        logger.info("command:ShowWindow");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        AudioPlayer audioPlayer=gamePlayer.getAudioPlayer();
        GameInterface inter = gamePlayer;
        if (inter != null) {
            WindowType windowType = WindowType.values()[type];
            inter.showWindow(windowType, isShow);
        }
    }

    @Override
    public byte[] GetFileContents(String path,int userId) {

        logger.info("command:GetFileContents");
        return getFileContents(path);
    }

    @Override
    public void ChangeQuestPath(String path,int userId) {

        logger.info("command:ChangeQuestPath");
        User user=UserManager.INSTANCE.getUser(userId);
        if(user==null)
        {
            return;
        }
        PlayerEngine gamePlayer=user.getHtmlHandler().getmPlayerEngine();
        GameObject gameObject= gamePlayer.getLibQspProxy().getGameObject();
        logger.info("command:ChangeQuestPath");
        File dir = new File(path);
        if (!dir.exists()) {
            logger.error("Game directory not found: " + path);
            return;
        }
        if (!gameObject.gameDir.equals(dir)) {
            gameObject.gameDir = dir;
        }
    }


}
