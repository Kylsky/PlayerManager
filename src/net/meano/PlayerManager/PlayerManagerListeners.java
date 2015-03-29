package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

public class PlayerManagerListeners implements Listener {
	PlayerManagerMain PPM;
	PermissionsPlugin Perm;

	public PlayerManagerListeners(PlayerManagerMain GetPlugin) {
		PPM = GetPlugin;
		Perm = (PermissionsPlugin) Bukkit.getPluginManager().getPlugin("PermissionsBukkit");
	}
	
	//���Ԥ��½�¼�
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		String PreLoginIP = event.getAddress().getHostName();
		String PlayerName = event.getName();
		OfflinePlayer player = Bukkit.getOfflinePlayer(event.getUniqueId());
		// ������
		if (!player.isWhitelisted()) {
			PPM.getLogger().info("��Ҳ��ڰ�������");
			for (String p : PPM.SetWhitelist) {
				if (p.equalsIgnoreCase(PlayerName)) {
					Bukkit.broadcast(p + "�ڰ�������Ԥ���б��У��ɹ���Ӱ�������", "PlayerManager.Whitelist");
					Bukkit.getOfflinePlayer(event.getUniqueId()).setWhitelisted(true);
				}
			}
		}
		boolean isOnline = false;
		String OnlinePlayerName = null;
		for (Player P : PPM.getServer().getOnlinePlayers()) {
			if (P.getAddress().getHostName().equals(PreLoginIP)) {
				OnlinePlayerName = P.getName();
				if(!OnlinePlayerName.equals(event.getName())){
					isOnline = true;
					continue;
				}
			}
		}
		if (isOnline) {
			Bukkit.broadcast("���" + OnlinePlayerName + "��ͼ��" + event.getName() + "��½��Ϸ����С�ŵ�½���ɣ�ip��ַ" + PreLoginIP, "PlayerManager.Warn");
		}
		PPM.SQLData.Close();
		PPM.SQLData.Open();
		// ���ڴ����
		if (PPM.SQLData.HasPlayer(PlayerName)) {
			if (PPM.SQLData.GetComboType(PlayerName).equals("Normal")) {
				if ((PPM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PPM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
					event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "�װ��������ң��������ʱ����Ϊ�������������õ�ʱ���Ѿ����꣬����ѡ�������������ʱ�ײͣ���ȴ�6���18���ʱ���������µ�½��Ϸ��");
				}
			} else if (PPM.SQLData.GetComboType(PlayerName).equals("B")) {
				String Week = PPM.getWeekString(System.currentTimeMillis());
				if (Week.equals("������") || Week.equals("������") || Week.equals("������")) {
				} else {
					if ((PPM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PPM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
						event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "�װ����ײ�B��ң�������" + Week + "���������ʱ����Ϊ�������������õ�ʱ���Ѿ����꣬����ѡ�񲹲�۹������������ʱ�ײ�A����ȴ�6���18���ʱ���������µ�½��Ϸ��");
					}
				}
			}
		}
	}
	
	//��ҵ�½��Ϸ�¼�
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		String PlayerName = event.getPlayer().getName();
		String PlayerCombo = null;
		boolean FirstPlay = false;
		// ������
		for (int i = 0; i < 3; i++) {
			if (PPM.SetWhitelist[i].equalsIgnoreCase(PlayerName)) {
				PPM.SetWhitelist[i] = "Meano";
				event.getPlayer().setWhitelisted(true);
				PPM.getLogger().info("��������֤��ʼ���� " + PlayerName + " �����������");
			}
		}
		PPM.SQLData.Close();
		PPM.SQLData.Open();
		if (!PPM.SQLData.HasPlayer(event.getPlayer().getName())) {
			PPM.SQLData.AddNewPlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
			event.getPlayer().sendMessage(ChatColor.AQUA + "�װ�����ң���ã����������������Ҫ������ʱ��ͽ�Ǯ����ά�����ָı��ƶȡ�");
			event.getPlayer().sendMessage(ChatColor.AQUA + "Ϊ���������÷�չ���ֲ��ƻ���Ϸ�ڹ�ƽ���޶�������ÿ����4Сʱ��Ϸʱ�䡣");
			event.getPlayer().sendMessage(ChatColor.AQUA + "֮ǰ�������������Ľ����ר���ײͣ������ڷ�������Ϸ����ҿ���ѡ�����������ײͣ�");
			event.getPlayer().sendMessage(ChatColor.GREEN + "�ײ�A��25Ԫ/�£�һ��������Ϸ����ʱ�������赱��" + ChatColor.YELLOW + "10�γƺŸ��ĺ�2��Ƥ���޸ġ�");
			event.getPlayer().sendMessage(ChatColor.GREEN + "�ײ�B��12Ԫ/�£�һ�������������ղ���ʱ�������赱��" + ChatColor.YELLOW + "3�γƺŸ��ġ�");
			PPM.getLogger().info(PlayerName + " ����������ݿ�");
		} else {
			FirstPlay = PPM.SQLData.isTodayFirstPlay(PlayerName);
			if (FirstPlay) { // �յ�һ�ε�½
				PPM.getLogger().info(PlayerName + " �����һ�ε�½������һ�ε�½���� " + PPM.SQLData.CalculateDaysLast(PlayerName) + " �졣");
				PPM.SQLData.UpdateTodayFirstLogin(PlayerName);
			} else {
				PPM.getLogger().info(PlayerName + "���ն�ε�½��ʣ���������ʱ�� " + PPM.SQLData.GetTodayLimitMinute(PlayerName) + "�֡�");
			}
			boolean ComboSuit = false;
			PlayerCombo = PPM.SQLData.GetComboType(PlayerName);
			if (PlayerCombo.equals("Normal")) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "�װ��������ң���ã���������������Ҫ������ʱ��ͽ�Ǯ����ά����");
				event.getPlayer().sendMessage(ChatColor.GREEN + "Ϊ�˷��������÷�չ���ֲ��ƻ���Ϸ�ڹ�ƽ���޶�������ÿ��4Сʱ����Ϸʱ��");
				event.getPlayer().sendMessage(ChatColor.GREEN + "���´�6���18��ʱ������ǰ��ʣ��" + PPM.SQLData.GetTodayLimitMinute(PlayerName) + "������Ϸʱ��");
				event.getPlayer().sendMessage(ChatColor.GREEN + "����ͨ��֧������΢�ţ��绰��֧������ʱ�ײͣ���ѯȺ326355263��ʹ��/pm me��ѯʱ����");
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
			} else if (PlayerCombo.equals("A")) {
				long ExpireTime = PPM.SQLData.GetComboExpireTime(PlayerName);
				if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
					PPM.SQLData.SetComboType(PlayerName, "Normal");
					PPM.SQLData.SetTodayLimitMinute(PlayerName, 120);
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���A�ײ���ң���ã���л���Է�������֧���븶����");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ײ��Ѿ����ڣ���Ϊ��ת��Ϊ��ͨ�����ң�ÿ��������4Сʱ�����Ϸʱ�䣬ף�����죡");
				} else {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���A�ײ���ң���ã���л���Է�������֧���븶����");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ײ͵�����Ϊ:" + PPM.getDateString(ExpireTime) + "��ף�����죡");
				}
				// �ײ�AȨ�޴���
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboA")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboA");
			} else if (PlayerCombo.equals("B")) {
				long ExpireTime = PPM.SQLData.GetComboExpireTime(PlayerName);
				if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
					PPM.SQLData.SetComboType(PlayerName, "Normal");
					PPM.SQLData.SetTodayLimitMinute(PlayerName, 120);
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���B�ײ���ң���ã���л���Է�������֧���븶����");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ײ��Ѿ����ڣ���Ϊ��ת��Ϊ��ͨ�����ң�ÿ��������4Сʱ�����Ϸʱ�䣬ף�����죡");
				} else {
					String Week = PPM.getWeekString(System.currentTimeMillis());
					if (Week.equals("������") || Week.equals("������") || Week.equals("������")) {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���B�ײ���ң���ã���л���Է�������֧���븶����");
						event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ײ͵�����Ϊ:" + PPM.getDateString(ExpireTime) + "��ף�����죡");
					} else {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���B�ײ���ң���ã���л���Է�������֧���븶����");
						event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ǹ����գ���ֻ�������Ϸʱ�䣬ʣ�����ʱ�䣺" + PPM.SQLData.GetTodayLimitMinute(PlayerName) + "���ӣ�ף�����죡");
					}
				}
				// �ײ�BȨ�޴���
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboB")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboB");
			} else if (PlayerCombo.equals("C")) {
				long ExpireTime = PPM.SQLData.GetComboExpireTime(PlayerName);
				long CurrentTime = System.currentTimeMillis();
				long SpaceTime = CurrentTime - event.getPlayer().getLastPlayed();
				if (SpaceTime > 1000 * 60 * 60 * 24) {
					ExpireTime += SpaceTime - 1000 * 60 * 60 * 24;
					PPM.SQLData.SetComboExpireTime(event.getPlayer().getName(), ExpireTime);
					event.getPlayer().sendMessage("C�ײ����" + event.getPlayer().getName() + "��ʱ" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "��");
					PPM.getLogger().info("C�ײ����" + event.getPlayer().getName() + "��ʱ" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "��");
				}
				if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
					PPM.SQLData.SetComboType(PlayerName, "Normal");
					PPM.SQLData.SetTodayLimitMinute(PlayerName, 120);
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���C�ײ���ң���ã���л���Է�������֧���븶����");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ײ��Ѿ����ڣ���Ϊ��ת��Ϊ��ͨ�����ң�ÿ��������4Сʱ�����Ϸʱ��");
				} else {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�װ���C�ײ���ң���ã���л���Է�������֧���븶����");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "�����ײ͵�����Ϊ:" + PPM.getDateString(ExpireTime) + "��ף�����졣");
				}
				// �ײ�CȨ�޴���
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboC")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboC");
			} else if (PlayerCombo.equals("Forever")) {
				event.getPlayer().sendMessage(ChatColor.RED + "�װ���������ң���ã���л���Է�������֧���븶����");
				event.getPlayer().sendMessage(ChatColor.RED + "������������������ʱ�����֣�ף�����졣");
				// �ײ�ForeverȨ�޴���
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboA")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboA");
			}
		}
	}
	
	public void CalculateContinuousDays(Player player){
		String PlayerName = player.getName();
		if(PPM.SQLData.GetOnlineMinutes(PlayerName)>120){
			int ContinuousDays = PPM.SQLData.GetContinuousDays(PlayerName);		//������½����
			int AwardMinute = PPM.SQLData.GetAwardMinute(PlayerName);			//����������
			player.sendMessage(ChatColor.GREEN + "�����ã���������ʹ�÷�����ר�ÿͻ��˽��е�½������ʱ��������Сʱ " + ContinuousDays + "�졣");
			if(ContinuousDays < 7){
				player.sendMessage(ChatColor.GREEN + "��ý�������ʱ�� " + ContinuousDays*5 + " ���ӡ�");
				PPM.SQLData.SetAwardMinute(PlayerName, AwardMinute+5*ContinuousDays);
			}else{
				player.sendMessage(ChatColor.GREEN + "��ý�������ʱ�� 30 ���ӡ�");
				PPM.SQLData.SetAwardMinute(PlayerName, AwardMinute+30);
			}
			//������½����+1
			PPM.SQLData.SetContinuousDays(PlayerName, ContinuousDays+1);
		}else {
			//��ǩ
			player.sendMessage(ChatColor.GREEN + "�����ã�������δʹ�÷�����ר�ÿͻ��˽��е�½������ʱ��������Сʱ��������½�������㡣");
			PPM.SQLData.SetContinuousDays(PlayerName, 0);
		}
		player.sendMessage(ChatColor.GREEN + "����ÿ��ʹ��ר�ÿͻ��˵�½��������ʱ�䳬��2Сʱ�����(������½����x5)���ӵĽ���ʱ�䡣");
		player.sendMessage(ChatColor.GREEN + "����ʱ��ÿ�������������㣬�뾡��ʹ�á�");
	}
	//����˳���Ϸ�¼�
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String PlayerName = event.getPlayer().getName();
		if(PPM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			PPM.SQLData.SetClientStatu(PlayerName, ClientStatu.Offline);
			PPM.getLogger().info("ʹ��ר�ÿͻ��˵����: " + PlayerName + "�Ѿ����ߡ�");
		}
	}
}
