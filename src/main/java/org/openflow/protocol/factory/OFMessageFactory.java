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
	// ������Ϣ���͵õ������ʵ��
	public OFMessage getMessage(OFType t);

	// ���Դ�ChannelBuffer�н����������ܶ��OFMessage����position��ʼ��ֹ��һ����������Ϣ֮��
	public List<OFMessage> parseMessage(ChannelBuffer data)
			throws MessageParseException;

	// �õ����𴴽�openflow action �Ĺ���
	public OFActionFactory getActionFactory();
}
