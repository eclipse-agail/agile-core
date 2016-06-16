 This repository contain the alpha version of the following AGILE componets:

	- iot.agile.protocol.BLE:
               DBus interface name = "iot.agile.protocol.BLE"
							 DBus interface path = "/iot/agile/protocol/ble";
               Label: Agile Bluetooh Low Energy
               Description: allows clients to discover, connect, and disconnect
							 BLE devices(All implemented). Moreover, provides methods to write
							 and read into/from BLE Devices.
	- iot.agile.ProtcolManager
               DBus interface name = "iot.agile.ProtocolManager"
               DBus interface path = "/iot/agile/ProtocolManager";
               Label: Agile Protocol Manager
               Description: Provide a DBus interface to instantiate device
							 discovery (Implimented for BLE),
							 add and remove protocols.
	- iot.agile.DeviceManager
	       DBus interface name = "iot.agile.DeviceManger"
               DBus interface path = "/iot/agile/DeviceManager";
               Label: AGILE Device Manager
               Description: Allows clients to create/register a new device
							 object in the device manager
               Example:
	- AGILE Device (iot.agile.Device)
               DBus interface base name = "iot.agile.device"
               DBus interface base path = "/iot/agile/Device/";
               Label: AGILE Device Manager
               Description: Allows clients to connect and disconnect devices
							 (both Implimented for BLE), and to read/write data from/to the
							  device and to subcribe for notification.
               Example:
