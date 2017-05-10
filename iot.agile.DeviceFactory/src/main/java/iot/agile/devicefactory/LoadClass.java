/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iot.agile.devicefactory;

/**
 *
 * @author agile
 */
public class LoadClass extends ClassLoader {
    protected Class<?> getClassFromBytes(String name, byte[] b) {
        Class<?> c = this.defineClass(name, b, 0, b.length);
        return c;
    }
}
