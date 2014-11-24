package org.openflow.protocol.factory;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

/**
 * The interface to factories used for retrieving OFMessage instances. All
 * methods are expected to be thread-safe.
 * 
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFMessageFactory {
	// 根据消息类型得到具体的实例
	public OFMessage getMessage(OFType t);

	// 尝试从ChannelBuffer中解析出尽可能多的OFMessage，从position开始，止于一个解析的消息之后
	public List<OFMessage> parseMessage(ChannelBuffer data)
			throws MessageParseException;

	// 得到负责创建openflow action 的工厂
	public OFActionFactory getActionFactory();
}
