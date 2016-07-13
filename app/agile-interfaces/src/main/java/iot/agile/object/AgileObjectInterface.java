
package iot.agile.object;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface AgileObjectInterface {
  
  public static int DEFAULT_DBUS_CONNECTION = DBusConnection.SESSION;
  
  public void dbusConnect(String busName, String busPath, DBusInterface iface) throws DBusException;
  public void dbusDisconnect();
  
}
