package org.eclipse.agail.device.instance;

import java.security.acl.LastOwnerException;
import java.util.*;

import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.Device;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.device.base.DeviceImp;
import org.eclipse.agail.exception.AgileNoResultException;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.DeviceStatusType;
import org.eclipse.agail.object.StatusType;

public class DaliDevice  extends DeviceImp implements Device {
    protected Logger logger = LoggerFactory.getLogger(DaliDevice.class);

    public static final String deviceTypeName = "DALI";

    /**
     * DALI Protocol imp DBus interface id
     */
    private static final String DALI_PROTOCOL_ID = "org.eclipse.agail.protocol.ONEM2M";
    /**
     * DALI Protocol imp DBus interface path
     */
    private static final String DALI_PROTOCOL_PATH = "/org/eclipse/agail/protocol/ONEM2M";

    private static final String DALI_COMPONENT = "DaliData";

    private DeviceStatusType deviceStatus = DeviceStatusType.DISCONNECTED;

    {
        profile.add(new DeviceComponent(DALI_COMPONENT, "onem2m"));
        subscribedComponents.put(DALI_COMPONENT, 0);
    }

    public DaliDevice(DeviceOverview deviceOverview) throws DBusException {
        super(deviceOverview);
        this.protocol = DALI_PROTOCOL_ID;
        String devicePath = AGILE_DEVICE_BASE_BUS_PATH + "onem2m" + deviceOverview.id;
        dbusConnect(deviceAgileID, devicePath, this);
        deviceProtocol = (Protocol) connection.getRemoteObject(DALI_PROTOCOL_ID, DALI_PROTOCOL_PATH, Protocol.class);
        try {
            deviceProtocol.Read(address, new HashMap<>());
        } catch (Exception e) {
            logger.warn("Error while reading", e);
        }
        logger.debug("Exposed device {} {}", deviceAgileID, devicePath);
    }

    public static boolean Matches(DeviceOverview d) {
        return d.name.toUpperCase().contains(deviceTypeName.toUpperCase());
    }

    @Override
    protected String DeviceRead(String componentName) {
        logger.debug("DeviceReading {} {}", componentName, protocol);
        if ((protocol.equals(DALI_PROTOCOL_ID)) && (deviceProtocol != null)) {
            logger.debug("DeviceReading is conected{}", isConnected());
            if (isConnected()) {
                if (isSensorSupported(componentName.trim())) {
                    try {
                        return new String(deviceProtocol.Read(DALI_COMPONENT, new HashMap<String, String>()));
                    } catch (DBusException e) {
                        logger.error("Error while reading", e);
                    }
                } else {
                    throw new AgileNoResultException("Componet not supported:" + componentName);
                }
            } else {
                throw new AgileNoResultException("Device not connected: " + deviceName);
            }
        } else {
            throw new AgileNoResultException("Protocol not supported: " + protocol);
        }
        throw new AgileNoResultException("Unable to read " + componentName);
    }

    @Override
    public void Subscribe(String componentName) {
        if ((protocol.equals(DALI_PROTOCOL_ID)) && (deviceProtocol != null)) {
            if (isConnected()) {
                if (isSensorSupported(componentName.trim())) {
                    try {
                        if (!hasOtherActiveSubscription()) {
                            addNewRecordSignalHandler();
                        }
                        if (!hasOtherActiveSubscription(componentName)) {
                            deviceProtocol.Subscribe(address, new HashMap<String, String>());
                        }
                        subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new AgileNoResultException("Component not supported:" + componentName);
                }
            } else {
                throw new AgileNoResultException("Device not connected: " + deviceName);
            }
        } else {
            throw new AgileNoResultException("Protocol not supported: " + protocol);
        }
    }

    @Override
    public synchronized void Unsubscribe(String componentName) throws DBusException {
        if ((protocol.equals(DALI_PROTOCOL_ID)) && (deviceProtocol != null)) {
            if (isConnected()) {
                if (isSensorSupported(componentName.trim())) {
                    try {
                        subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
                        if (!hasOtherActiveSubscription(componentName)) {
                            deviceProtocol.Unsubscribe(address, new HashMap<String, String>());
                        }
                        if (!hasOtherActiveSubscription()) {
                            removeNewRecordSignalHandler();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new AgileNoResultException("Component not supported:" + componentName);
                }
            } else {
                throw new AgileNoResultException("Device not connected: " + deviceName);
            }
        } else {
            throw new AgileNoResultException("Protocol not supported: " + protocol);
        }
    }

    @Override
    public void Connect() throws DBusException {
        deviceStatus = DeviceStatusType.CONNECTED;
        logger.info("Device connected {}", deviceID);
    }

    @Override
    public void Disconnect() throws DBusException {
        deviceStatus = DeviceStatusType.DISCONNECTED;
        logger.info("Device disconnected {}", deviceID);
    }

    @Override
    public StatusType Status() {
        return new StatusType(deviceStatus.toString());
    }

    //  @Override
    //  public void Execute(String command, Map<String, Variant> args) {
    //    if(command.equalsIgnoreCase(DeviceStatusType.ON.toString())){
    //      deviceStatus = DeviceStatusType.ON;
    //    }else if(command.equalsIgnoreCase(DeviceStatusType.OFF.toString())){
    //      deviceStatus = DeviceStatusType.OFF;
    //    }
    //  }
    //  
    protected boolean isConnected() {
        if (Status().getStatus().equals(DeviceStatusType.CONNECTED.toString()) || Status().getStatus().equals(DeviceStatusType.ON.toString())) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean isSensorSupported(String sensorName) {
        return DALI_COMPONENT.equals(sensorName);
    }

    @Override
    protected String formatReading(String sensorName, byte[] readData) {
        return new String(readData);
    }

    @Override
    protected String getComponentName(Map<String, String> profile) {
        return DALI_COMPONENT;
    }

    @Override
    public void Write(String componentName, String payload) {
        if ((protocol.equals(DALI_PROTOCOL_ID)) && (deviceProtocol != null)) {
            if (isConnected()) {
                if (isSensorSupported(componentName.trim())) {
                    try {
                        logger.debug("Device Write: {} at {} {}", componentName, address, payload);
                        HashMap<String, String> profile = new HashMap<String, String>();
                        profile.put("ipe", "dali");
                        profile.put("operation", "set_level");
                        deviceProtocol.Write(address, profile, payload.getBytes());
                    } catch (Exception ex) {
                        logger.error("Exception occured in Write: " + ex);
                    }
                } else {
                    throw new AgileNoResultException("Componet not supported:" + componentName);
                }
            } else {
                throw new AgileNoResultException("DALI Device not connected: " + deviceName);
            }
        } else {
            throw new AgileNoResultException("Protocol not supported: " + protocol);
        }
    }

    @Override
    public void Execute(String command) {
        logger.debug("Device command {}", command);
        try {
            deviceProtocol.Write(address, new HashMap<>(), command.getBytes());
        } catch (Exception e) {
            logger.error("Error while executing {}", command, e);
        }
    }

    @Override
    public List<String> Commands(){
        List<String> commands = new ArrayList<>();

        commands.add("set_level");
        return commands;
    }
}
