package net.meano.PlayerServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import net.meano.DataBase.ClientStatu;
import net.meano.PlayerManager.PlayerManagerMain;

public class Server extends Thread {
	ServerSocket PMServer = null;
	Socket SocketAccept = null;
	InputStreamReader SocketReader = null;
	PlayerManagerMain PMM;
	String ReceiveString;
	String[] ReceiveClientInfo;
	int ReceiveLength = 0;
	char ReceiveChars[] = new char[30];

	public Server(PlayerManagerMain GetPlugin) {
		PMM = GetPlugin;
		try {
			PMServer = new ServerSocket(25566);
		} catch (IOException e) {
			PMM.getLogger().info(e.getCause().getMessage());
		}
	}

	public void CloseServer() {
		try {
			PMServer.close();
		} catch (IOException e) {
			PMM.getLogger().info(e.getCause().getMessage());
		}
	}

	public void run() {
		PMM.getLogger().info("��ʼ����25566�˿�");
		while (!PMServer.isClosed()) {
			try {
				SocketAccept = PMServer.accept();
				SocketAccept.setSoTimeout(2000);
				SocketReader = new InputStreamReader(SocketAccept.getInputStream(),"UTF8");
				ReceiveLength = SocketReader.read(ReceiveChars);
				ReceiveString = new String(ReceiveChars, 0, ReceiveLength);
				SocketAccept.close();
				SocketReader.close();
				ReceiveClientInfo = ReceiveString.split("\\$", 5);
				if (ReceiveClientInfo.length == 5) {
					PMM.getLogger().info("���:" + ReceiveClientInfo[1] + "�汾:" + ReceiveClientInfo[2] + "����:" + ReceiveClientInfo[3]);
					if(ReceiveClientInfo[3].equals("Connetc")){
						PMM.SQLData.SetClientStatu(ReceiveClientInfo[1], ClientStatu.Online);
					}
				}
				ReceiveString = null;
			} catch (Exception e) {
				try {
					SocketAccept.close();
					SocketReader.close();
				} catch (IOException e1) {
				}
				PMM.getLogger().info(e.getLocalizedMessage());
			}
		}
	}
}
