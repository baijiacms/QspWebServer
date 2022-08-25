package com.qsp.player.libqsp;

/**
 * libqsp call
 */
public interface LibQspCallbacks {
    void RefreshInt(int userId);

    void ShowPicture(String path,int userId);

    void SetTimer(int msecs,int userId);

    void ShowMessage(String message,int userId);

    void PlayFile(String path, int volume,int userId);

    boolean IsPlayingFile(final String path,int userId);

    void CloseFile(String path,int userId);

    void OpenGame(String filename,int userId);

    void SaveGame(String filename,int userId);

    String InputBox(String prompt,int userId);

    int GetMSCount(int userId);

    void AddMenuItem(String name, String imgPath,int userId);

    void ShowMenu(int userId);

    void DeleteMenu(int userId);

    void Wait(int msecs,int userId);

    void ShowWindow(int type, boolean isShow,int userId);

    byte[] GetFileContents(String path,int userId);

    void ChangeQuestPath(String path,int userId);


}
