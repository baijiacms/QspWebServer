package com.baijiacms.qsp.template;

import com.baijiacms.qsp.common.FolderLoader;
import com.baijiacms.qsp.common.QspConstants;
import com.baijiacms.qsp.player.PlayerEngine;
import com.baijiacms.qsp.util.StreamUtils;
import com.baijiacms.qsp.vo.GameVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏选择界面
 *
 * @author cxy
 */
public class GameSelectTemplate {
    private Template indexTemplate;

    public GameSelectTemplate(VelocityEngine ve) {

        indexTemplate = ve.getTemplate("baijiacms/html/center/main.vm", QspConstants.CHARSET_STR);
    }

    public String getHtml() {
        List<GameVo> gameList = new ArrayList<>();
        FolderLoader.loadGameFolder(gameList);
        VelocityContext context = new VelocityContext();

        context.put("engineTitle", QspConstants.ENGINE_TITLE);
        context.put("engineVersion", QspConstants.ENGINE_VERSION);
        context.put("gameList", gameList);


        StringWriter writer = new StringWriter();
        indexTemplate.merge(context, writer);
        writer.flush();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }


    public String loadGame(PlayerEngine mPlayerEngine, String gameId) {

        if (StringUtils.isEmpty(gameId)) {
            return StreamUtils.BLANK_STR;
        }
        mPlayerEngine.getGameStatus().isOpenSaveWindow = false;
        mPlayerEngine.restartGame(gameId);
        return StreamUtils.SUCCESS_STR;
    }

    public String exportGameToText(PlayerEngine mPlayerEngine, String gameId) {
        String actionScript = gameId;
        if (StringUtils.isEmpty(actionScript)) {
            return StreamUtils.BLANK_STR;
        }
        mPlayerEngine.getGameStatus().setGamePathById(actionScript);
        GameVo gameVo = FolderLoader.getFolderMap().get(gameId);

        new File(gameVo.getGameFolder() + "/exportText/").mkdir();

        mPlayerEngine.getLibQspProxy().qspFileToText(gameVo, gameVo.getGameFolder() + "/exportText/source.txt");
        return StreamUtils.SUCCESS_STR;
    }

    public String exportGameToQsp(PlayerEngine mPlayerEngine, String gameId) {
        String actionScript = gameId;
        if (StringUtils.isEmpty(actionScript)) {
            return StreamUtils.BLANK_STR;
        }
        mPlayerEngine.getGameStatus().setGamePathById(actionScript);

        GameVo gameVo = FolderLoader.getFolderMap().get(gameId);
        new File(gameVo.getGameFolder() + "/exportQsp/").mkdir();
        mPlayerEngine.getLibQspProxy().toGemFile(gameVo, gameVo.getGameFolder() + "/exportQsp/game.qsp");
        return StreamUtils.SUCCESS_STR;
    }
}
