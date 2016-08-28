package iot.agile.devicemanager.examples;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.LoggerFactory;

import iot.agile.Protocol;
import iot.agile.Protocol.NewRecordSignal;
 
public class SubscribeTempClient {
	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(SubscribeTempClient.class);

	public static void main(String[] args) {
		 try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			connection.addSigHandler(Protocol.NewRecordSignal.class	, new DBusSigHandler<Protocol.NewRecordSignal>() {

				@Override
				public void handle(NewRecordSignal sig) {
 					logger.info("new value readed");
					logger.info(new String(sig.record));
 
				}
			
			
			}); 
		} catch (DBusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
