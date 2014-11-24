package org.openflow.protocol.factory;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.protocol.statistics.OFVendorStatistics;
import org.openflow.protocol.vendor.OFByteArrayVendorData;
import org.openflow.protocol.vendor.OFVendorData;
import org.openflow.protocol.vendor.OFVendorDataType;
import org.openflow.protocol.vendor.OFVendorId;

//创建openflow message和action
public class BasicFactory implements OFMessageFactory, OFActionFactory,
		OFStatisticsFactory, OFVendorDataFactory {
	@Override
	public OFMessage getMessage(OFType t) {
		return t.newInstance(); // 调用枚举类型的方法
	}

	@Override
	public List<OFMessage> parseMessage(ChannelBuffer data)
			throws MessageParseException {
		List<OFMessage> msglist = new ArrayList<OFMessage>();
		OFMessage msg = null;

		while (data.readableBytes() >= OFMessage.MINIMUM_LENGTH) {
			data.markReaderIndex(); // 标记读指针，注意ChannelBuffer和ByteBuffer的区别
			msg = this.parseMessageOne(data);
			if (msg == null) {
				data.resetReaderIndex(); // 如果失败则恢复read index
				break;
			} else {
				msglist.add(msg); // 成功解析，则将其加入列表
			}
		}

		if (msglist.size() == 0) {
			return null;
		}
		return msglist;
	}

	public OFMessage parseMessageOne(ChannelBuffer data)
			throws MessageParseException {
		try {
			OFMessage demux = new OFMessage();
			OFMessage ofm = null;

			if (data.readableBytes() < OFMessage.MINIMUM_LENGTH)
				return ofm;

			data.markReaderIndex();
			// 调用基类方法，得到OF header的字段如长度和消息类型
			demux.readFrom(data);
			data.resetReaderIndex();

			// 如果ChannelBuffer中不足一个消息长度，则返回空
			if (demux.getLengthU() > data.readableBytes())
				return ofm;
			// 否则根据类型，创建相应的消息对象
			ofm = getMessage(demux.getType());
			if (ofm == null)
				return null;
			// 如果相应的消息类中有OFActionFactory成员，就用当前类设置它
			if (ofm instanceof OFActionFactoryAware) {
				((OFActionFactoryAware) ofm).setActionFactory(this);
			}
			if (ofm instanceof OFMessageFactoryAware) {
				((OFMessageFactoryAware) ofm).setMessageFactory(this);
			}
			if (ofm instanceof OFStatisticsFactoryAware) {
				((OFStatisticsFactoryAware) ofm).setStatisticsFactory(this);
			}
			if (ofm instanceof OFVendorDataFactoryAware) {
				((OFVendorDataFactoryAware) ofm).setVendorDataFactory(this);
			}
			// 最后调用具体类的readFrom，从ChannelBuffer解析出该消息
			ofm.readFrom(data);
			if (OFMessage.class.equals(ofm.getClass())) {
				// advance the position for un-implemented messages
				data.readerIndex(data.readerIndex()
						+ (ofm.getLengthU() - OFMessage.MINIMUM_LENGTH));
			}

			return ofm;
		} catch (Exception e) {
			throw new MessageParseException(e);
		}
	}

	@Override
	public OFAction getAction(OFActionType t) {
		return t.newInstance();
	}

	@Override
	public List<OFAction> parseActions(ChannelBuffer data, int length) {
		return parseActions(data, length, 0);
	}

	@Override
	public List<OFAction> parseActions(ChannelBuffer data, int length, int limit) {
		List<OFAction> results = new ArrayList<OFAction>();
		OFAction demux = new OFAction();
		OFAction ofa;
		int end = data.readerIndex() + length;

		while (limit == 0 || results.size() <= limit) {
			if ((data.readableBytes() < OFAction.MINIMUM_LENGTH || (data
					.readerIndex() + OFAction.MINIMUM_LENGTH) > end))
				return results;

			data.markReaderIndex();
			demux.readFrom(data);
			data.resetReaderIndex();

			if ((demux.getLengthU() > data.readableBytes() || (data
					.readerIndex() + demux.getLengthU()) > end))
				return results;

			ofa = getAction(demux.getType());
			ofa.readFrom(data);
			if (OFAction.class.equals(ofa.getClass())) {
				// advance the position for un-implemented messages
				data.readerIndex(data.readerIndex()
						+ (ofa.getLengthU() - OFAction.MINIMUM_LENGTH));
			}
			results.add(ofa);
		}

		return results;
	}

	// 下面的action和statistics 与上面类型。
	@Override
	public OFActionFactory getActionFactory() {
		return this;
	}

	@Override
	public OFStatistics getStatistics(OFType t, OFStatisticsType st) {
		return st.newInstance(t);
	}

	@Override
	public List<OFStatistics> parseStatistics(OFType t, OFStatisticsType st,
			ChannelBuffer data, int length) {
		return parseStatistics(t, st, data, length, 0);
	}

	/**
	 * @param t
	 *            OFMessage type: should be one of stats_request or stats_reply
	 * @param st
	 *            statistics type of this message, e.g., DESC, TABLE
	 * @param data
	 *            buffer to read from
	 * @param length
	 *            length of statistics
	 * @param limit
	 *            number of statistics to grab; 0 == all
	 * 
	 * @return list of statistics
	 */

	@Override
	public List<OFStatistics> parseStatistics(OFType t, OFStatisticsType st,
			ChannelBuffer data, int length, int limit) {
		List<OFStatistics> results = new ArrayList<OFStatistics>();
		OFStatistics statistics = getStatistics(t, st);

		int start = data.readerIndex();
		int count = 0;

		while (limit == 0 || results.size() <= limit) {
			// TODO Create a separate MUX/DEMUX path for vendor stats
			if (statistics instanceof OFVendorStatistics)
				((OFVendorStatistics) statistics).setLength(length);

			/**
			 * can't use data.remaining() here, b/c there could be other data
			 * buffered past this message
			 */
			if ((length - count) >= statistics.getLength()) {
				if (statistics instanceof OFActionFactoryAware)
					((OFActionFactoryAware) statistics).setActionFactory(this);
				statistics.readFrom(data);
				results.add(statistics);
				count += statistics.getLength();
				statistics = getStatistics(t, st);
			} else {
				if (count < length) {
					/**
					 * Nasty case: partial/incomplete statistic found even
					 * though we have a full message. Found when NOX sent
					 * agg_stats request with wrong agg statistics length (52
					 * instead of 56)
					 * 
					 * just throw the rest away, or we will break framing
					 */
					data.readerIndex(start + length);
				}
				return results;
			}
		}
		return results; // empty; no statistics at all
	}

	@Override
	public OFVendorData getVendorData(OFVendorId vendorId,
			OFVendorDataType vendorDataType) {
		if (vendorDataType == null)
			return null;

		return vendorDataType.newInstance();
	}

	/**
	 * Attempts to parse and return the OFVendorData contained in the given
	 * ChannelBuffer, beginning right after the vendor id.
	 * 
	 * @param vendor
	 *            the vendor id that was parsed from the OFVendor message.
	 * @param data
	 *            the ChannelBuffer from which to parse the vendor data
	 * @param length
	 *            the length to the end of the enclosing message.
	 * @return an OFVendorData instance
	 */
	public OFVendorData parseVendorData(int vendor, ChannelBuffer data,
			int length) {
		OFVendorDataType vendorDataType = null;
		OFVendorId vendorId = OFVendorId.lookupVendorId(vendor);
		if (vendorId != null) {
			data.markReaderIndex();
			vendorDataType = vendorId.parseVendorDataType(data, length);
			data.resetReaderIndex();
		}

		OFVendorData vendorData = getVendorData(vendorId, vendorDataType);
		if (vendorData == null)
			vendorData = new OFByteArrayVendorData();

		vendorData.readFrom(data, length);

		return vendorData;
	}

}
