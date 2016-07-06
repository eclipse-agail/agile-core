
package iot.agile;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface AgileDbusInterface extends DBusInterface {
  
  public DBusConnection dbusConnect() throws DBusException;
  public void dbusDisconnect();
  
}
