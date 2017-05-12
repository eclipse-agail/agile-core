/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
package iot.agile.device.base;

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
