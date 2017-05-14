package com.peersafe.chainbet.logic.wifibackrestore;

import android.content.Context;

import java.io.IOException;

public class WifiBackRestoreServerRunner
{
	private static WifiBackRestoreServerLogic server;
	public static boolean serverIsRunning = false;

    /**
     * 启动http服务器
	 * @param port
     * @param bIsImport
	 * @param context
	 */
    public static void startServer(int port, boolean bIsImport, Context context)
	{
		server = new WifiBackRestoreServerLogic(context, port, bIsImport);
		try
		{
			if (!serverIsRunning)
			{
				server.start();
				serverIsRunning = true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


    /**
     * 停止http服务器
     */
    public static void stopServer()
    {
		if (server != null)
        {
			server.stop();
			serverIsRunning = false;
		}
	}
}