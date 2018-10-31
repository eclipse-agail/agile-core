package org.eclipse.agail.protocolmanager.persistence;

import java.util.List;

import org.eclipse.agail.protocols.config.ProtocolConfig;

public class ProtocolWithConfig {
	
	String protocolId;
	List<ProtocolConfig> configurations;
	
	public ProtocolWithConfig() {
		// TODO Auto-generated constructor stub
	}
	
	public ProtocolWithConfig(String protocolId, List<ProtocolConfig> configs) {
		this.protocolId = protocolId;
		this.configurations = configs;
	}
	
	/**
	 * @return the protocol
	 */
	public String getProtocolId() {
		return protocolId;
	}
	
	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}
	
	/**
	 * @return the configurations
	 */
	public List<ProtocolConfig> getConfigurations() {
		return configurations;
	}
	
	/**
	 * @param configurations the configurations to set
	 */
	public void setConfigurations(List<ProtocolConfig> configurations) {
		this.configurations = configurations;
	}
	
}
