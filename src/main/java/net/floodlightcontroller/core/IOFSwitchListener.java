/**
 *    Copyright 2011, Big Switch Networks, Inc. 
 *    Originally created by David Erickson, Stanford University
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

package net.floodlightcontroller.core;

/**
 * 
 * 
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
// 监听处理交换机的加入和移除事件
public interface IOFSwitchListener {

	// 当一个SW连接到Controller时触发（fired），并且这个SW已经发送feature reply消息
	public void addedSwitch(IOFSwitch sw);

	// 当一个SW与Controller断开连接时触发（fired）
	public void removedSwitch(IOFSwitch sw);

	// 每个listener都会分配一个名字
	public String getName();
}
