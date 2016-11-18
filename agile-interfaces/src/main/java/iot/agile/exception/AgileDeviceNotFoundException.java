package iot.agile.exception;

import org.freedesktop.dbus.exceptions.DBusExecutionException;
/**
 * Device not found exception
 */
public class AgileDeviceNotFoundException extends DBusExecutionException {

  /**
   * 
   */
  private static final long serialVersionUID = -7023033132025739002L;

  public AgileDeviceNotFoundException(String arg0) {
    super(arg0);
   }

}
