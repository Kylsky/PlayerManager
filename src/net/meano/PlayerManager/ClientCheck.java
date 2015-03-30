package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ClientCheck implements Runnable {
	public int ContinuousDays;
	public Player player;
	public PlayerManagerMain PMM;
	public String PlayerCombo;
	public ClientCheck(int CDays, Player p, String Combo, PlayerManagerMain P){
		ContinuousDays = CDays;
		player = p;
		PlayerCombo = Combo;
		PMM = P;
	}
	@Override
	public void run(){
		if(!player.isOnline()) return;
		String PlayerName = player.getName();
		if(PMM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			player.sendMessage(ChatColor.BLUE + "��л��ʹ��ר�ÿͻ��˵�½Meano��");
			if (PlayerCombo.equals("Normal") || PlayerCombo.equals("B")) {
				player.sendMessage(ChatColor.DARK_PURPLE + "ÿ��ʹ��ר�ÿͻ��˵�½��������Сʱ���������������½�������ڶ��콫��ã�5*��������������������");
				if(ContinuousDays == 0){
					player.sendMessage(ChatColor.DARK_PURPLE + "����û�г�����Сʱ��ר�пͻ��˵�½��¼������δ������߽���ʱ��");
				}else if (ContinuousDays > 0){
					int Minute = (ContinuousDays>7)?30:(ContinuousDays*5);
					player.sendMessage(ChatColor.DARK_PURPLE + "����������½" + ContinuousDays +"�죬���" + Minute + "���ӽ���ʱ�䣬����ʹ�ã�����һ�����㽫���㡣");
				}
			}
		} else {
			player.sendMessage(ChatColor.BLUE + "��δʹ��Meano��ר�ÿͻ��˵�½Meano�����޷���÷���������ʱ�佱����Ȩ����");
		}
	}
}
