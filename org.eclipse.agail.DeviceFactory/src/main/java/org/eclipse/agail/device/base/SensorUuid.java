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
package org.eclipse.agail.device.base;

public class SensorUuid {
	public String serviceUuid;
	public String charValueUuid;
	public String charConfigUuid;
	public String charFreqUuid;

	public SensorUuid(String service, String charValueUuid, String charConfig, String charFreq) {
		this.serviceUuid = service;
		this.charValueUuid = charValueUuid;
		this.charConfigUuid = charConfig;
		this.charFreqUuid = charFreq;
	}
}
