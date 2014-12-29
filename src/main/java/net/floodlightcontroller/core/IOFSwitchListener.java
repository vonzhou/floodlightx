package net.floodlightcontroller.core;

// 监听处理交换机的加入和移除事件
public interface IOFSwitchListener {

	// 当一个SW连接到Controller时触发（fired），并且这个SW已经发送feature reply消息
	public void addedSwitch(IOFSwitch sw);

	// 当一个SW与Controller断开连接时触发（fired）
	public void removedSwitch(IOFSwitch sw);

	// 每个listener都会分配一个名字
	public String getName();
}
