/**
 *    Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior
 *    University
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

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
