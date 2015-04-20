package org.evosuite.runtime.vnet;

import org.evosuite.runtime.vnet.VirtualNetwork.ConnectionType;

import java.io.Serializable;

/**
 * Immutable class used to store connection info 
 */
public class EndPointInfo  implements Serializable {
	
	private final String host;
	private final int port;
	private final ConnectionType type;
	
	public EndPointInfo(String host, int port, ConnectionType type) {
		super();
		this.host = host;
		this.port = port;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EndPointInfo other = (EndPointInfo) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public ConnectionType getType() {
		return type;
	}
}

