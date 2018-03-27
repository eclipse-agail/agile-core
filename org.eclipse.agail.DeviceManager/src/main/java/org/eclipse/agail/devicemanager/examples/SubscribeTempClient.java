/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
package org.eclipse.agail.devicemanager.examples;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.Protocol;
import org.eclipse.agail.Protocol.NewRecordSignal;
 
public class SubscribeTempClient {
	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(SubscribeTempClient.class);

	public static void main(String[] args) {
		 try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			connection.addSigHandler(Protocol.NewRecordSignal.class	, new DBusSigHandler<Protocol.NewRecordSignal>() {

				@Override
				public void handle(NewRecordSignal sig) {
 					logger.info("new value readed");
					logger.info(new String(sig.record));
 
				}
			
			
			}); 
		} catch (DBusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
