package net.meano.DataBase;

import java.sql.Connection;

public interface SQLData {
	// �������ݿ�
	public void Open();

	// �ر����ݿ�
	public void Close();

	// ������ݿ�����
	public Connection getConnection();
}
