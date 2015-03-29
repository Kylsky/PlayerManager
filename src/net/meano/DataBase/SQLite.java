package net.meano.DataBase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.TimeZone;
import net.meano.PlayerManager.PlayerManagerMain;

public class SQLite{
	private File DataBaseFile;
	private Connection DataBaseConnection;
	public PlayerManagerMain PMM;

	public SQLite(File dbFile, PlayerManagerMain plugin) {
		PMM = plugin;
		DataBaseFile = dbFile; // ���ݿ��ļ�
		File dbDir = dbFile.getParentFile(); // ��ȡ�ļ�·��
		dbDir.mkdir(); // �����ļ���
		if (!dbFile.exists()) { // ������ݿ��ļ�������
			try {
				dbFile.createNewFile(); // �������ݿ��ļ�
			} catch (IOException e) {
			}
		}
		try {
			Class.forName("org.sqlite.JDBC"); // ����sqlite��
		} catch (ClassNotFoundException e) {
		}
		Open(); // ���ݿ����Ӵ�

		try {
			// ���ݿ����
			Statement DataBaseStatement = DataBaseConnection.createStatement();
			// ��ʱ����30s
			DataBaseStatement.setQueryTimeout(30);
			// �������� ���������������PMPlayers���洢����б�
			DataBaseStatement.executeUpdate(	"CREATE TABLE IF NOT EXISTS PMPlayers "
										+ "(PlayerName VARCHAR(30) NOT NULL UNIQUE, "
										+ "UUID VARCHAR(130) NOT NULL UNIQUE, "
										+ "PlayerLevel INT NOT NULL, "
										+ "TodayFirstLogin INTEGER NOT NULL, "
										+ "ComboType VARCHAR(10) NOT NULL, "
										+ "TodayLimitMinute INT NOT NULL, "
										+ "ComboExpireTime INTEGER NOT NULL, "
										+ "ClientStatu VARCHAR(10) NOT NULL, "
										+ "ClientNoCheck VARCHAR(10) NOT NULL, "
										+ "AwardMinute INT NOT NULL," 
										+ "ContinuousDays INT NOT NULL,"
										+ "OnlineMinutes INT NOT NULL);");
			DatabaseMetaData md = DataBaseConnection.getMetaData();
			ResultSet rs = md.getColumns(null, null, "PMPlayers", "ContinuousDays");
			if (!rs.next()) {
				PMM.getLogger().info("���ڲ�������ContinuousDays��");
				PreparedStatement ps = DataBaseConnection.prepareStatement("ALTER TABLE PMPlayers ADD ContinuousDays INT NOT NULL DEFAULT ( 0 );");
				ps.executeUpdate();
				PMM.getLogger().info("����ContinuousDays������ɡ�");
			}
			rs = md.getColumns(null, null, "PMPlayers", "OnlineMinutes");
			if (!rs.next()) {
				PMM.getLogger().info("���ڲ�������OnlineMinutes��");
				PreparedStatement ps = DataBaseConnection.prepareStatement("ALTER TABLE PMPlayers ADD OnlineMinutes INT NOT NULL DEFAULT ( 0 );");
				ps.executeUpdate();
				PMM.getLogger().info("����OnlineMinutes������ɡ�");
			}
			rs = md.getColumns(null, null, "PMPlayers", "AwardMinute");
			if (!rs.next()) {
				PMM.getLogger().info("���ڲ�������AwardMinute��");
				PreparedStatement ps = DataBaseConnection.prepareStatement("ALTER TABLE PMPlayers ADD AwardMinute INT NOT NULL DEFAULT ( 0 );");
				ps.executeUpdate();
				PMM.getLogger().info("����AwardMinute������ɡ�");
			}
		} catch (SQLException e) {
			PMM.getLogger().info(e.getLocalizedMessage());
		}
	}

	public void Open() {
		try {
			this.DataBaseConnection = DriverManager.getConnection("jdbc:sqlite:" + DataBaseFile.getPath());
		} catch (SQLException e) {

		}
	}

	public void Close() {
		try {
			if (DataBaseConnection != null && !DataBaseConnection.isClosed())
				DataBaseConnection.close();
		} catch (SQLException e) {
		}
	}

	// �������޴����
	public boolean HasPlayer(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next())
				return true;
			else
				return false;
		} catch (SQLException e) {
			return false;
		}
	}

	// ������
	public void AddNewPlayer(String PlayerName, String UUID) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("INSERT INTO PMPlayers"
													+ "(PlayerName, " 
													+ "UUID, " 
													+ "PlayerLevel, " 
													+ "TodayFirstLogin, " 
													+ "ComboType," 
													+ "TodayLimitMinute, " 
													+ "ComboExpireTime, " 
													+ "ClientStatu, " 
													+ "ClientNoCheck, " 
													+ "AwardMinute,"
													+ "ContinuousDays," 
													+ "OnlineMinutes)" 
													+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?);");
			ps.setString(1, PlayerName.toLowerCase());
			ps.setString(2, UUID);
			ps.setInt(3, 0);
			ps.setLong(4, System.currentTimeMillis());
			ps.setString(5, "Normal");
			ps.setInt(6, 120);
			ps.setLong(7, 0);
			ps.setString(8, "Offline");
			ps.setString(9, "false");
			ps.setInt(10, 0);
			ps.setInt(11, 0);
			ps.setInt(12, 0);
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ��ȡ���״ε�½ʱ��
	public long GetTodayFirstLogin(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getLong("TodayFirstLogin");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}
	//�������һ�ε�½����ʱ��
	public int CalculateDaysLast(String PlayerName){
		long TimeTodayFirstLogin = GetTodayFirstLogin(PlayerName);
		return CalculateDaysDiff(System.currentTimeMillis(),TimeTodayFirstLogin);
	}
	
	//�ж��Ƿ�Ϊ���״ε�½
	public boolean isTodayFirstPlay(String PlayerName) {
		if (CalculateDaysLast(PlayerName)<= 0) {
			return false;
		} else {
			return true;
		}
	}
	//��������long time������֮��
	public int CalculateDaysDiff(long TimeFirst,long TimeSecond){
		return CalculateDays(TimeFirst)-CalculateDays(TimeSecond);
	}
	
	//����long time��������
	public int CalculateDays(long TimeToCalculate){
		Calendar CalculateDate= Calendar.getInstance();
		CalculateDate.setTimeInMillis(TimeToCalculate);
		CalculateDate.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return CalculateDate.get(Calendar.DAY_OF_YEAR)+(CalculateDate.get(Calendar.YEAR)*1000);
	}

	// �������״ε�½ʱ��
	public void UpdateTodayFirstLogin(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET TodayFirstLogin=? WHERE PlayerName=?;");
			ps.setLong(1, System.currentTimeMillis());
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ��ȡ�ײ�����
	public String GetComboType(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getString("ComboType");
			} else
				return null;
		} catch (SQLException e) {
			return null;
		}
	}

	// �����ײ�����
	public void SetComboType(String PlayerName, String ComboType) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ComboType=? WHERE PlayerName=?;");
			ps.setString(1, ComboType);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ��õ�����ʱʱ��
	public int GetTodayLimitMinute(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("TodayLimitMinute");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// ���õ����޳�ʱ��
	public void SetTodayLimitMinute(String PlayerName, int TodayLimitMinute) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET TodayLimitMinute=? WHERE PlayerName=?;");
			ps.setInt(1, TodayLimitMinute);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ����ײ�����
	public long GetComboExpireTime(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getLong("ComboExpireTime");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// �趨�ײ�����
	public void SetComboExpireTime(String PlayerName, long ExpireTime) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ComboExpireTime=? WHERE PlayerName=?;");
			ps.setLong(1, ExpireTime);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ����ײ�����
	public int GetAwardMinute(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("AwardMinute");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// �趨�ײ�����
	public void SetAwardMinute(String PlayerName, long MinuteTime) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET AwardMinute=? WHERE PlayerName=?;");
			ps.setLong(1, MinuteTime);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ˢ��������ʱ��
	public void UpdateLimitTime(int min) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET TodayLimitMinute=? WHERE ComboType=? or ComboType=?;");
			ps.setInt(1, min);
			ps.setString(2, "Normal");
			ps.setString(3, "B");
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// ˢ��AC��ҽ���ʱ��
	public void UpdateAwardTime(int min) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET AwardMinute=? WHERE ComboType in(?,?,?);");
			ps.setInt(1, min);
			ps.setString(2, "A");
			ps.setString(3, "C");
			ps.setString(4, "Forever");
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	//���Ŀͻ�������״̬
	public void SetClientStatu(String PlayerName,ClientStatu Statu) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ClientStatu=? WHERE PlayerName=?;");
			ps.setString(1, Statu.name());
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	//��ȡ�ͻ�������״̬
	public ClientStatu GetClientStatu(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return ClientStatu.valueOf(result.getString("ClientStatu"));
			} else
				return null;
		} catch (SQLException e) {
			return null;
		}
	}
	
	// ���������½����
	public int GetContinuousDays(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("ContinuousDays");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// �趨������½����
	public void SetContinuousDays(String PlayerName, long ContinuousDays) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ContinuousDays=? WHERE PlayerName=?;");
			ps.setLong(1, ContinuousDays);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	// ���ר�ÿͻ������߷�����
	public int GetOnlineMinutes(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("OnlineMinutes");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// �趨ר�ÿͻ������߷�����
	public void SetOnlineMinutes(String PlayerName, long OnlineMinutes) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET OnlineMinutes=? WHERE PlayerName=?;");
			ps.setLong(1, OnlineMinutes);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	public Connection getConnection() {
		return this.DataBaseConnection;
	}
}
