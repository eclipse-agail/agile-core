package iot.agile.object;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"parameters"})
public class StatusType extends Struct {
	@Position(0)
	@JsonProperty("status")
	public final String status;
	
	 @JsonCreator
	public StatusType(@JsonProperty("status")String status){
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
