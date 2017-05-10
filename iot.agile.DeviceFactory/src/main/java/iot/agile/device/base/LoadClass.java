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

/**
 *
 * @author agile
 */
public class LoadClass extends ClassLoader {
    public Class<?> getClassFromBytes(String name, byte[] b) {
        Class<?> c = this.defineClass(name, b, 0, b.length);
        return c;
    }
}
