package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import net.meano.DataBase.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.meano.PlayerManager.PlayerManagerMain;

public class MinuteTick implements Runnable {
	public PlayerManagerMain PMM;
	public SQLite SQLData;
	public MinuteTick(PlayerManagerMain P){
		PMM = P;
		SQLData = P.SQLData;
	}
	@Override
	public void run() {
		SQLData.Close();
		SQLData.Open();
		UpdateTime();
		for (Player player : Bukkit.getOnlinePlayers()) {
			String ComboType = SQLData.GetComboType(player.getName());
			if (ComboType.equals("Normal")) {
				MinuteNormal(player);
			} else if (ComboType.equals("B")) {
				MinuteB(player);
			}
		}
	}
	
	//����������ʱ��
	public void UpdateTime(){
		long LongTime = System.currentTimeMillis();
		if ((PMM.getTimeHours(LongTime) == 6) || (PMM.getTimeHours(LongTime) == 18)) {
			if (PMM.getTimeMinutes(LongTime) < 3 && (!PMM.isUpdate)) {
				SQLData.UpdateLimitTime(120);
				SQLData.UpdateAwardTime(120);
				Bukkit.broadcastMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "��λ�����ң��������Ѿ������˴�ҵ��������ʱ����ÿ��6���18����£�����ʱ��ʹ��/pm me �鿴��");
				PMM.isUpdate = true;
			} else if (PMM.getTimeMinutes(LongTime) > 3) {
				PMM.isUpdate = false;
			}
		} else {
		}
	}
	
	//Normal�ײ�ÿ���ӽ��еĴ���
	public void MinuteNormal(Player player){
		int LimitTime = SQLData.GetTodayLimitMinute(player.getName());
		if (LimitTime > 0) {
			if (LimitTime == 1) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l�������ʱ���Ѿ����꣬��������������������õĽ���ʱ�䣬�����������Ľ���ʱ�䣬�����������߳���Ϸ").toString());
			}
			SQLData.SetTodayLimitMinute(player.getName(), LimitTime - 1);
		} else {
			int AwardMinute = SQLData.GetAwardMinute(player.getName());
			if (AwardMinute > 0) {
				SQLData.SetAwardMinute(player.getName(), AwardMinute - 1);
			} else {
				player.kickPlayer(ChatColor.GOLD + "�װ��������ң������������ʱ���Ѿ����꣬������������ʱ��Ҳ�������꣬����ѡ�������������ʱ�ײ����µ�½��Ϸ�����߶���������������ȡʱ����");
			}
		}
		if(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online)){
			int OnlineMinutes = SQLData.GetOnlineMinutes(player.getName());
			if (OnlineMinutes >= 0){
				SQLData.SetOnlineMinutes(player.getName(), OnlineMinutes + 1);
			}
		}
	}
	
	//B�ײ�ÿ���ӽ��еĴ���
	public void MinuteB(Player player){
		String Week = PMM.getWeekString(System.currentTimeMillis());
		if (Week.equals("������") || Week.equals("������") || Week.equals("������")) {
		} else {
			int LimitTime = SQLData.GetTodayLimitMinute(player.getName());
			if (LimitTime > 0) {
				if (LimitTime == 1) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l�������ʱ���Ѿ����꣬��������������������õĽ���ʱ�䣬�����������Ľ���ʱ�䣬�����������߳���Ϸ").toString());
				}
				SQLData.SetTodayLimitMinute(player.getName(), LimitTime - 1);
			} else {
				int AwardMinute = SQLData.GetAwardMinute(player.getName());
				if (AwardMinute > 0) {
					SQLData.SetAwardMinute(player.getName(), AwardMinute - 1);
				} else {
					player.kickPlayer(ChatColor.GOLD + "�װ���B�ײ���ң������ǹ����գ�����������ʱ���Ѿ����꣬������������ʱ��Ҳ�������꣬��ӭ������������Ϸ��");
				}
			}
		}
		if(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online)){
			int OnlineMinutes = SQLData.GetOnlineMinutes(player.getName());
			if (OnlineMinutes >= 0){
				SQLData.SetOnlineMinutes(player.getName(), OnlineMinutes + 1);
			}
		}
	}
}
