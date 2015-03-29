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
	PlayerManagerMain PMM;
	PermissionsPlugin Perm;
	
	//��ʼ��
	public PlayerManagerListeners(PlayerManagerMain GetPlugin) {
		PMM = GetPlugin;
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
			PMM.getLogger().info("��Ҳ��ڰ�������");
			for (String p : PMM.SetWhitelist) {
				if (p.equalsIgnoreCase(PlayerName)) {
					Bukkit.broadcast(p + "�ڰ�������Ԥ���б��У��ɹ���Ӱ�������", "PlayerManager.Whitelist");
					Bukkit.getOfflinePlayer(event.getUniqueId()).setWhitelisted(true);
				}
			}
		}
		boolean isOnline = false;
		String OnlinePlayerName = null;
		for (Player P : PMM.getServer().getOnlinePlayers()) {
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
		PMM.SQLData.Close();
		PMM.SQLData.Open();
		// ���ڴ����
		if (PMM.SQLData.HasPlayer(PlayerName)) {
			if (PMM.SQLData.GetComboType(PlayerName).equals("Normal")) {
				if ((PMM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PMM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
					event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "�װ��������ң��������ʱ����Ϊ�������������õ�ʱ���Ѿ����꣬����ѡ�������������ʱ�ײͣ���ȴ�6���18���ʱ���������µ�½��Ϸ��");
				}
			} else if (PMM.SQLData.GetComboType(PlayerName).equals("B")) {
				String Week = PMM.getWeekString(System.currentTimeMillis());
				if (Week.equals("������") || Week.equals("������") || Week.equals("������")) {
				} else {
					if ((PMM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PMM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
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
		Player player = event.getPlayer();
		boolean FirstPlay = false;
		int ContinuousDays = -1;
		// ������
		for (int i = 0; i < 3; i++) {
			if (PMM.SetWhitelist[i].equalsIgnoreCase(PlayerName)) {
				PMM.SetWhitelist[i] = "Meano";
				player.setWhitelisted(true);
				PMM.getLogger().info("��������֤��ʼ���� " + PlayerName + " �����������");
			}
		}
		PMM.SQLData.Close();
		PMM.SQLData.Open();
		if (!PMM.SQLData.HasPlayer(player.getName())) {
			PMM.SQLData.AddNewPlayer(player.getName(), player.getUniqueId().toString());
			player.sendMessage(ChatColor.AQUA + "�װ�����ң���ã����������������Ҫ������ʱ��ͽ�Ǯ����ά�����ָı��ƶȡ�");
			player.sendMessage(ChatColor.AQUA + "Ϊ���������÷�չ���ֲ��ƻ���Ϸ�ڹ�ƽ���޶�������ÿ����4Сʱ��Ϸʱ�䡣");
			player.sendMessage(ChatColor.AQUA + "֮ǰ�������������Ľ����ר���ײͣ������ڷ�������Ϸ����ҿ���ѡ�����������ײͣ�");
			player.sendMessage(ChatColor.GREEN + "�ײ�A��25Ԫ/�£�һ��������Ϸ����ʱ�������赱��" + ChatColor.YELLOW + "10�γƺŸ��ĺ�2��Ƥ���޸ġ�");
			player.sendMessage(ChatColor.GREEN + "�ײ�B��12Ԫ/�£�һ�������������ղ���ʱ�������赱��" + ChatColor.YELLOW + "3�γƺŸ��ġ�");
			PMM.getLogger().info(PlayerName + " ����������ݿ�");
		} else {
			//�ж��Ƿ��ǽ����һ�ε�½
			FirstPlay = PMM.SQLData.isTodayFirstPlay(PlayerName);
			if (FirstPlay) {
				PMM.getLogger().info(PlayerName + " �����һ�ε�½������һ�ε�½���� " + PMM.SQLData.CalculateDaysLast(PlayerName) + " �졣");
				PMM.SQLData.UpdateTodayFirstLogin(PlayerName);
				ContinuousDays = CalculateContinuousDays(player);
			} else {
				PMM.getLogger().info(PlayerName + "���ն�ε�½��ʣ���������ʱ�� " + PMM.SQLData.GetTodayLimitMinute(PlayerName) + "�֡�");
			}
			PlayerCombo = PMM.SQLData.GetComboType(PlayerName);
			if (PlayerCombo.equals("Normal")) {
				NormalLogin(player);
			} else if (PlayerCombo.equals("A")) {
				ALogin(player);
			} else if (PlayerCombo.equals("B")) {
				BLogin(player);
			} else if (PlayerCombo.equals("C")) {
				CLogin(player);
			} else if (PlayerCombo.equals("Forever")) {
				ForeverLogin(player);
			}
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PMM, new ClientCheck(ContinuousDays, player, PMM), 1*20*60);
		}
	}

	//Normal�ײ͵�½����
	public void NormalLogin(Player player){
		player.sendMessage(ChatColor.GREEN + "�װ��������ң���ã���������������Ҫ������ʱ��ͽ�Ǯ����ά����");
		player.sendMessage(ChatColor.GREEN + "Ϊ�˷��������÷�չ���ֲ��ƻ���Ϸ�ڹ�ƽ���޶�������ÿ��4Сʱ����Ϸʱ��");
		player.sendMessage(ChatColor.GREEN + "���´�6���18��ʱ������ǰ��ʣ��" + PMM.SQLData.GetTodayLimitMinute(player.getName()) + "������Ϸʱ��");
		player.sendMessage(ChatColor.GREEN + "����ͨ��֧������΢�ţ��绰��֧������ʱ�ײͣ���ѯȺ326355263��ʹ��/pm me��ѯʱ����");
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
	}
	
	//A�ײ͵�½����
	public void ALogin(Player player){
		boolean ComboSuit = false;
		String PlayerName = player.getName();
		long ExpireTime = PMM.SQLData.GetComboExpireTime(PlayerName);
		if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
			PMM.SQLData.SetComboType(PlayerName, "Normal");
			PMM.SQLData.SetTodayLimitMinute(PlayerName, 120);
			player.sendMessage(ChatColor.YELLOW + "�װ���A�ײ���ң���ã���л���Է�������֧���븶����");
			player.sendMessage(ChatColor.YELLOW + "�����ײ��Ѿ����ڣ���Ϊ��ת��Ϊ��ͨ�����ң�ÿ��������4Сʱ�����Ϸʱ�䣬ף�����죡");
		} else {
			player.sendMessage(ChatColor.YELLOW + "�װ���A�ײ���ң���ã���л���Է�������֧���븶����");
			player.sendMessage(ChatColor.YELLOW + "�����ײ͵�����Ϊ:" + PMM.getDateString(ExpireTime) + "��ף�����죡");
		}
		// �ײ�AȨ�޴���
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboA")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboA");
	}
	
	//B�ײ͵�½����
	public void BLogin(Player player){
		boolean ComboSuit = false;
		String PlayerName = player.getName();
		long ExpireTime = PMM.SQLData.GetComboExpireTime(PlayerName);
		if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
			PMM.SQLData.SetComboType(PlayerName, "Normal");
			PMM.SQLData.SetTodayLimitMinute(PlayerName, 120);
			player.sendMessage(ChatColor.YELLOW + "�װ���B�ײ���ң���ã���л���Է�������֧���븶����");
			player.sendMessage(ChatColor.YELLOW + "�����ײ��Ѿ����ڣ���Ϊ��ת��Ϊ��ͨ�����ң�ÿ��������4Сʱ�����Ϸʱ�䣬ף�����죡");
		} else {
			String Week = PMM.getWeekString(System.currentTimeMillis());
			if (Week.equals("������") || Week.equals("������") || Week.equals("������")) {
				player.sendMessage(ChatColor.YELLOW + "�װ���B�ײ���ң���ã���л���Է�������֧���븶����");
				player.sendMessage(ChatColor.YELLOW + "�����ײ͵�����Ϊ:" + PMM.getDateString(ExpireTime) + "��ף�����죡");
			} else {
				player.sendMessage(ChatColor.YELLOW + "�װ���B�ײ���ң���ã���л���Է�������֧���븶����");
				player.sendMessage(ChatColor.YELLOW + "�����ǹ����գ���ֻ�������Ϸʱ�䣬ʣ�����ʱ�䣺" + PMM.SQLData.GetTodayLimitMinute(PlayerName) + "���ӣ�ף�����죡");
			}
		}
		// �ײ�BȨ�޴���
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboB")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboB");
	}
	
	//C�ײ͵�½����
	public void CLogin(Player player){
		boolean ComboSuit = false;
		String PlayerName = player.getName();
		long ExpireTime = PMM.SQLData.GetComboExpireTime(PlayerName);
		long CurrentTime = System.currentTimeMillis();
		long SpaceTime = CurrentTime - player.getLastPlayed();
		if (SpaceTime > 1000 * 60 * 60 * 24) {
			ExpireTime += SpaceTime - 1000 * 60 * 60 * 24;
			PMM.SQLData.SetComboExpireTime(player.getName(), ExpireTime);
			player.sendMessage("C�ײ����" + player.getName() + "��ʱ" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "��");
			PMM.getLogger().info("C�ײ����" + player.getName() + "��ʱ" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "��");
		}
		if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
			PMM.SQLData.SetComboType(PlayerName, "Normal");
			PMM.SQLData.SetTodayLimitMinute(PlayerName, 120);
			player.sendMessage(ChatColor.YELLOW + "�װ���C�ײ���ң���ã���л���Է�������֧���븶����");
			player.sendMessage(ChatColor.YELLOW + "�����ײ��Ѿ����ڣ���Ϊ��ת��Ϊ��ͨ�����ң�ÿ��������4Сʱ�����Ϸʱ��");
		} else {
			player.sendMessage(ChatColor.YELLOW + "�װ���C�ײ���ң���ã���л���Է�������֧���븶����");
			player.sendMessage(ChatColor.YELLOW + "�����ײ͵�����Ϊ:" + PMM.getDateString(ExpireTime) + "��ף�����졣");
		}
		// �ײ�CȨ�޴���
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboC")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboC");
	}
	
	//Forever�ײ͵�½����
	public void ForeverLogin(Player player){
		boolean ComboSuit = false;
		//String PlayerName = player.getName();
		player.sendMessage(ChatColor.RED + "�װ���������ң���ã���л���Է�������֧���븶����");
		player.sendMessage(ChatColor.RED + "������������������ʱ�����֣�ף�����졣");
		// �ײ�ForeverȨ�޴���
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboA")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboA");
	}
	
	//���㽱��ʱ��
	public int CalculateContinuousDays(Player player){
		String PlayerName = player.getName();
		int ContinuousDays = 0;
		if(PMM.SQLData.GetOnlineMinutes(PlayerName)>120){
			ContinuousDays = PMM.SQLData.GetContinuousDays(PlayerName);		//������½����
			int AwardMinute = PMM.SQLData.GetAwardMinute(PlayerName);			//����������
			//������½����+1
			ContinuousDays = ContinuousDays + 1;
			PMM.SQLData.SetContinuousDays(PlayerName, ContinuousDays);
			if(ContinuousDays < 7){
				PMM.SQLData.SetAwardMinute(PlayerName, AwardMinute+5*ContinuousDays);;
			}else{
				PMM.SQLData.SetAwardMinute(PlayerName, AwardMinute+30);
			}
		}else {
			ContinuousDays = 0;
			PMM.SQLData.SetContinuousDays(PlayerName, 0);
		}
		return ContinuousDays;
	}
	
	//����˳���Ϸ�¼�
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String PlayerName = event.getPlayer().getName();
		if(PMM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			PMM.SQLData.SetClientStatu(PlayerName, ClientStatu.Offline);
			PMM.getLogger().info("ʹ��ר�ÿͻ��˵����: " + PlayerName + "�Ѿ����ߡ�");
		}
	}
}
