package com.qsp.player.libqsp;

import com.baijiacms.qsp.common.QspConstants;

import java.util.Objects;

/**
 * dll调用核心类
 * Методы данного класса определены в <code>androidqspwrapper.c</code>.
 */
public class NativeMethods  {
    private final LibQspCallbacks callbacks;

    static {
        // System.loadLibrary("ndkqsp");
        System.load(QspConstants.QSP_DLL_PATH);
    }

    public NativeMethods(LibQspCallbacks callbacks) {
        this.callbacks = Objects.requireNonNull(callbacks, "callbacks");
    }

    public native void CallBacksInit(int userId);
    public native void QSPInit(int userId);

    public native void QSPDeInit(int userId);

    public native boolean QSPIsInCallBack(int userId);

    public native void QSPEnableDebugMode(boolean isDebug,int userId);

    public native Object QSPGetCurStateData(int userId);//!!!STUB

    public native String QSPGetVersion(int userId);

    public native int QSPGetFullRefreshCount(int userId);

    public native String QSPGetQstFullPath(int userId);

    public native String QSPGetCurLoc(int userId);

    public native String QSPGetMainDesc(int userId);

    public native boolean QSPIsMainDescChanged(int userId);

    public native String QSPGetVarsDesc(int userId);

    public native boolean QSPIsVarsDescChanged(int userId);

    public native Object QSPGetExprValue(int userId);//!!!STUB

    public native void QSPSetInputStrText(String val,int userId);

    public native int QSPGetActionsCount(int userId);

    public native Object QSPGetActionData(int ind,int userId);//!!!STUB

    public native boolean QSPExecuteSelActionCode(boolean isRefresh,int userId);

    public native boolean QSPSetSelActionIndex(int ind, boolean isRefresh,int userId);

    public native int QSPGetSelActionIndex(int userId);

    public native boolean QSPIsActionsChanged(int userId);

    public native int QSPGetObjectsCount(int userId);

    public native Object QSPGetObjectData(int ind,int userId);//!!!STUB

    public native boolean QSPSetSelObjectIndex(int ind, boolean isRefresh,int userId);

    public native int QSPGetSelObjectIndex(int userId);

    public native boolean QSPIsObjectsChanged(int userId);

    public native void QSPShowWindow(int type, boolean isShow,int userId);

    public native Object QSPGetVarValuesCount(String name,int userId);

    public native Object QSPGetVarValues(String name, int ind,int userId);//!!!STUB

    public native int QSPGetMaxVarsCount(int userId);

    public native Object QSPGetVarNameByIndex(int index,int userId);//!!!STUB

    public native boolean QSPExecString(String s, boolean isRefresh,int userId);

    public native boolean QSPExecLocationCode(String name, boolean isRefresh,int userId);

    public native boolean QSPExecCounter(boolean isRefresh,int userId);

    public native boolean QSPExecUserInput(boolean isRefresh,int userId);

    public native Object QSPGetLastErrorData(int userId);

    public native String QSPGetErrorDesc(int errorNum,int userId);

    public native boolean QSPLoadGameWorld(String fileName,int userId);

    public native boolean QSPLoadGameWorldFromData(byte data[], int dataSize, String fileName,int userId);

    public native boolean QSPSaveGame(String fileName, boolean isRefresh,int userId);

    public native byte[] QSPSaveGameAsData(boolean isRefresh,int userId);

    public native boolean QSPOpenSavedGame(String fileName, boolean isRefresh,int userId);

    public native boolean QSPOpenSavedGameFromData(byte data[], int dataSize, boolean isRefresh,int userId);

    public native boolean QSPRestartGame(boolean isRefresh,int userId);

    public native void QSPSelectMenuItem(int index,int userId);
    //public native void QSPSetCallBack(int type, QSP_CALLBACK func)
}
