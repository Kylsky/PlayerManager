package net.meano.PlayerManager;

import java.io.File;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;
import net.meano.DataBase.SQLite;
import net.meano.PlayerServer.Server;
import net.meano.PlayerManager.MinuteTick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerManagerMain extends JavaPlugin {
	public static PlayerManagerMain PMM; 
	public Server PlayerSocket;
	public SQLite SQLData;
	public boolean isUpdate = false;
	public String[] SetWhitelist = new String[3];
	public void onEnable() {
		PMM = this;
		getLogger().info("PlayerManager 0.1,by Meano. ��������.");
		PluginManager PM = Bukkit.getServer().getPluginManager();
		PM.registerEvents(new PlayerManagerListeners(this), this);
		SQLData = new SQLite(new File(getDataFolder(), "PMData.db"), this);
		SocketInitialize();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new MinuteTick(this), 1 * 15 * 12, 1 * 60 * 20);
		SetWhitelist[0] = "Meano";
		SetWhitelist[1] = "Meano";
		SetWhitelist[2] = "Meano";
		/*
		 * for(OfflinePlayer p: Bukkit.getOfflinePlayers()){ String
		 * BufferFileName; BufferFileName =
		 * p.getUniqueId().toString()+"_["
		 * +p.getName()+"]["+getDateString
		 * (p.getFirstPlayed())+"]["+getDateString
		 * (p.getLastPlayed())+"].tmp"; File BufferFile = new
		 * File(this.getDataFolder(),BufferFileName);
		 * if(!BufferFile.exists()){ try { BufferFile.createNewFile(); }
		 * catch (IOException e) { e.printStackTrace(); } }
		 * getLogger().info(BufferFileName); }
		 */
		// getLogger().info(Bukkit.getOfflinePlayer("Meano").getUniqueId().toString());
	}

	public void onDisable() {
		SQLData.Close();
		getLogger().info("���ڹرն˿�25566��");
		PlayerSocket.CloseServer();
	}
	
	public String getDateString(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		@SuppressWarnings("resource")
		Formatter ft = new Formatter(Locale.CHINA);
		return ft.format("%1$tY��%1$tm��%1$td��", cal).toString().replaceAll(":", "_");
	}

	public int getTimeMinutes(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return cal.get(Calendar.MINUTE);
	}

	public int getTimeHours(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public String getWeekString(long dt) {
		String[] weekDays = { "������", "����һ", "���ڶ�", "������", "������", "������", "������" };
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dt);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return weekDays[w];
	}

	public void SocketInitialize() {
		PlayerSocket = new Server(this);
		PlayerSocket.start();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("playermanager")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("combo")) {
					if (!sender.isOp())
						return true;
					if (args.length == 4) {
						String ComboType = null;
						long ExpireTime = System.currentTimeMillis();
						if (!SQLData.HasPlayer(args[1])) {
							sender.sendMessage("û�������ң�");
							return true;
						}
						if (args[2].equalsIgnoreCase("a")) {
							ComboType = "A";
						} else if (args[2].equalsIgnoreCase("b")) {
							ComboType = "B";
						} else if (args[2].equalsIgnoreCase("c")) {
							ComboType = "C";
						} else if (args[2].equalsIgnoreCase("forever")) {
							ComboType = "Forever";
						} else if (args[2].equalsIgnoreCase("normal")) {
							ComboType = "Normal";
						} else {
							sender.sendMessage("û�д��ײ�");
							return true;
						}
						if (args[3].endsWith("d")) {
							ExpireTime += ((long) Integer.parseInt(args[3].replace("d", ""))) * 1000 * 60 * 60 * 24;
						} else if (args[3].endsWith("mon")) {
							ExpireTime += ((long) Integer.parseInt(args[3].replace("mon", ""))) * 1000 * 60 * 60 * 24 * 30;
						} else {
							sender.sendMessage("ʱ���������");
							return true;
						}
						SQLData.Close();
						SQLData.Open();
						SQLData.SetComboType(args[1], ComboType);
						SQLData.SetComboExpireTime(args[1], ExpireTime);
						sender.sendMessage("�ɹ��������" + args[1] + "���ײ�Ϊ: " + ComboType + "�ײ�,�ײ͵�����" + getDateString(ExpireTime));
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm combo ��� �ײ� ʱ��");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("check")) {
					if (!sender.isOp())
						return true;
					if (args.length == 2) {
						String ComboType = null;
						int TodayLimit = 0;
						int AwardMinute = 0;
						long ExpireTime = 0;
						SQLData.Close();
						SQLData.Open();
						if (!SQLData.HasPlayer(args[1])) {
							sender.sendMessage(ChatColor.BLUE + "û�������ң�");
							return true;
						}
						ComboType = SQLData.GetComboType(args[1]);
						TodayLimit = SQLData.GetTodayLimitMinute(args[1]);
						ExpireTime = SQLData.GetComboExpireTime(args[1]);
						AwardMinute = SQLData.GetAwardMinute(args[1]);
						//for (double Tps : MinecraftServer.getServer().recentTps) {
						//	sender.sendMessage("TPS: " + Tps);
						//}
						sender.sendMessage(ChatColor.BLUE + "���" + args[1] + "��Ϸʱ�������ѯ:");
						sender.sendMessage(ChatColor.YELLOW + "ʱ���ײ�����:" + ComboType + "�ײ�");
						if (ComboType.equals("Normal")) {
							sender.sendMessage(ChatColor.YELLOW + "��ҽ���ʣ��ʱ��:" + TodayLimit + "����");
							sender.sendMessage(ChatColor.YELLOW + "����ۻ�������ʱ��:" + AwardMinute + "����");
						} else if (ComboType.equals("Forever")) {
							sender.sendMessage(ChatColor.YELLOW + "�ײ���������");
						} else if (ComboType.equals("B")) {
							sender.sendMessage(ChatColor.YELLOW + "��ҽ���ʣ��ʱ��:" + TodayLimit + "����");
							sender.sendMessage(ChatColor.YELLOW + "����ۻ�������ʱ��:" + AwardMinute + "����");
							sender.sendMessage(ChatColor.YELLOW + "�ײ͵�����: " + getDateString(ExpireTime));
						} else {
							sender.sendMessage(ChatColor.YELLOW + "�ײ͵�����: " + getDateString(ExpireTime));
						}
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm check ���");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("me")) {
					if (sender instanceof Player) {
						String ComboType;
						int TodayLimit;
						int AwardMinute;
						long ExpireTime;
						SQLData.Close();
						SQLData.Open();
						ComboType = SQLData.GetComboType(sender.getName());
						TodayLimit = SQLData.GetTodayLimitMinute(sender.getName());
						ExpireTime = SQLData.GetComboExpireTime(sender.getName());
						AwardMinute = SQLData.GetAwardMinute(sender.getName());
						sender.sendMessage(ChatColor.BLUE + "���" + sender.getName() + "��Ϸʱ�������ѯ:");
						sender.sendMessage(ChatColor.YELLOW + "ʱ���ײ�����:" + ComboType + "�ײ�");
						if (ComboType.equals("Normal")) {
							sender.sendMessage(ChatColor.YELLOW + "��ҽ���ʣ�����ʱ��:" + TodayLimit + "����");
							sender.sendMessage(ChatColor.YELLOW + "����ۻ�������ʱ��:" + AwardMinute + "����");
						} else if (ComboType.equals("B")) {
							sender.sendMessage(ChatColor.YELLOW + "��ҽ���ʣ��ʣ��ʱ��:" + TodayLimit + "����");
							sender.sendMessage(ChatColor.YELLOW + "����ۻ�������ʱ��:" + AwardMinute + "����");
							sender.sendMessage(ChatColor.YELLOW + "�ײ͵�����: " + getDateString(ExpireTime) + " �ڴ�֮ǰ����������������Ϸʱ�䲻�ܲ����ơ�");
						} else if (ComboType.equals("C") || ComboType.equals("A")) {
							sender.sendMessage(ChatColor.YELLOW + "�ײ͵�����: " + getDateString(ExpireTime) + " �ڴ�֮ǰ������Ϸʱ�䶼���ܲ����ơ�");
						} else {
							sender.sendMessage(ChatColor.RED + "����������ң���������ʱ���������ƣ�");
						}
						return true;
					} else {
						sender.sendMessage("��������ֻ�������ִ��");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("today")) {
					if (!sender.isOp())
						return true;
					if (args.length == 3) {
						SQLData.Close();
						SQLData.Open();
						if (SQLData.HasPlayer(args[1])) {
							SQLData.SetTodayLimitMinute(args[1], Integer.parseInt(args[2]));
							sender.sendMessage(ChatColor.BLUE + "�ɹ��趨��� " + args[1] + " �ķ�����Ϊ " + args[2]);
						} else {
							sender.sendMessage(ChatColor.BLUE + "û��������");
						}
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm today ��� ������");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("award")) {
					if (!sender.isOp() && !sender.hasPermission("PlayerManager.Award"))
						return true;
					if (args.length == 3) {
						if (SQLData.HasPlayer(args[1])) {
							if (!sender.isOp()) {
								int HasAwardMinute = SQLData.GetAwardMinute(sender.getName());
								if (HasAwardMinute < Integer.parseInt(args[2])) {
									sender.sendMessage(ChatColor.BLUE + "��Ŀ��ý���������Ϊ" + HasAwardMinute + "��������������ҷ���������");
									return true;
								} else {
									HasAwardMinute = HasAwardMinute - Integer.parseInt(args[2]);
									SQLData.SetAwardMinute(sender.getName(), HasAwardMinute);
									sender.sendMessage(ChatColor.BLUE + "���ý�������ʱ��ʣ��" + HasAwardMinute + "���ӡ�");
								}
							}
							int AwardMinute = SQLData.GetAwardMinute(args[1]);
							sender.sendMessage(ChatColor.BLUE + "��� " + args[1] + " �ķ�����������������Ϊ " + AwardMinute);
							AwardMinute += Integer.parseInt(args[2]);
							SQLData.SetAwardMinute(args[1], AwardMinute);
							sender.sendMessage(ChatColor.BLUE + "�ɹ�������� " + args[1] + " �ķ����� " + args[2] + " ���ӣ���ǰ���ӵ��" + AwardMinute + "���������ӡ�");
						} else {
							sender.sendMessage(ChatColor.BLUE + "û��������");
						}
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm award ��� ������");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("relimit")) {
					if (!sender.isOp())
						return true;
					if (args.length == 2) {
						SQLData.UpdateLimitTime(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.BLUE + "�ɹ�ˢ��������ҵķ�����");
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm relimit ������");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("reaward")) {
					if (!sender.isOp())
						return true;
					if (args.length == 2) {
						SQLData.UpdateAwardTime(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.BLUE + "�ɹ�ˢ�������ײ���ҵĽ���������");
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm reaward ������");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("whitelist")) {
					if (!sender.hasPermission("PlayerManager.Whitelist")) {
						sender.sendMessage(ChatColor.RED + "��û��Ȩ����ӣ�");
						return true;
					}
					if (args.length == 2) {
						int i = 0;
						for (i = 0; i < 3; i++) {
							if (SetWhitelist[i].equals("Meano")) {
								SetWhitelist[i] = args[1];
								sender.sendMessage(ChatColor.BLUE + args[1] + "�ɹ���ӵ����������б�");
								return true;
							}
						}
						sender.sendMessage(ChatColor.BLUE + "�������б������������" + SetWhitelist[0] + SetWhitelist[1] + SetWhitelist[2] + "��Ԥ������������������һ�û��������������ӡ�");
						SetWhitelist[0] = args[1];
						SetWhitelist[1] = "Meano";
						SetWhitelist[2] = "Meano";
						sender.sendMessage(ChatColor.BLUE + "�ɹ���Ӱ�����Ԥ���б�");
						return true;
					} else {
						sender.sendMessage("��������ȷ��/pm whitelist ���ID");
						return true;
					}
				}else if(args[0].equalsIgnoreCase("info")){
					if(sender instanceof Player){
						Player Pinfo = (Player) sender;
						sender.sendMessage(ChatColor.BLUE+"�װ������ ["+Pinfo.getName()+"] ����"+getDateString(Pinfo.getFirstPlayed())+"�����������������");
						return true;
					}else{
						sender.sendMessage("ֻ�������ʹ������ָ�");
					}
					
				}else {
					sender.sendMessage("/pm me ��ѯ�Լ���ʱ�����ײ�ʣ�����");
					return true;
				}
			} else {
				sender.sendMessage("/pm me ��ѯ�Լ���ʱ�����ײ�ʣ�����");
				return true;
			}
		}
		return false;
	}
}
