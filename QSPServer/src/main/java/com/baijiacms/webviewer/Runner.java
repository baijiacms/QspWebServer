package com.baijiacms.webviewer;

import com.baijiacms.qsp.QspEngineServer;
import com.baijiacms.qsp.common.QspConstants;
import javax.swing.*;

/**
 * printf("%s","123")
 */
public class Runner {

    public  static void main(String[] args) throws Exception {
        int port= QspConstants.HTTP_PORT;
        if(args!=null&&args.length>=1)
        {
            try {
                port = Integer.parseInt(args[0]);
                QspConstants.HTTP_PORT=port;
//                QspConstants.HTTP_LOCAL_URL ="http://127.0.0.1:"+ port;
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        QspEngineServer qspEngineServer=new QspEngineServer(port);
        qspEngineServer.start();
        System.out.println("http://127.0.0.1:" + qspEngineServer.getLocalPort());
        System.out.println("------------------Server PORT:" + qspEngineServer.getLocalPort()+" -----------");
        qspEngineServer.join(); //这个是阻断器

    }
}