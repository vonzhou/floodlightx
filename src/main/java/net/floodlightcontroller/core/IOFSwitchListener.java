package net.floodlightcontroller.core;

// �������������ļ�����Ƴ��¼�
public interface IOFSwitchListener {

	// ��һ��SW���ӵ�Controllerʱ������fired�����������SW�Ѿ�����feature reply��Ϣ
	public void addedSwitch(IOFSwitch sw);

	// ��һ��SW��Controller�Ͽ�����ʱ������fired��
	public void removedSwitch(IOFSwitch sw);

	// ÿ��listener�������һ������
	public String getName();
}
