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
/*
 * Copyright 2016 CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.agail.http;

import org.eclipse.agail.Device;
import org.eclipse.agail.DeviceManager;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.ProtocolManager;
import java.util.HashMap;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class PathPattern {
  
  static public Map<String, Class<? extends DBusInterface>> map = new HashMap();
  
  static public Class get(String path) {
    return get(path, "");
  }
  
  static public Class get(String path, String prefix) {
    
    if(map.isEmpty()) {
      
      map.put("devices", DeviceManager.class);
      map.put("device", Device.class);
      
      map.put("protocol", Protocol.class);
      map.put("protocols", ProtocolManager.class);

    }
    
    String[] parts = path.replace(prefix, "").split("/");
    return map.getOrDefault(parts[0], null);
  }
  
}
