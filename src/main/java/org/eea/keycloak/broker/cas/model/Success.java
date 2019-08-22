package org.eea.keycloak.broker.cas.model;

import org.eea.keycloak.broker.cas.jaxb.AttributesAdapter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class Success implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "user", namespace = "https://ecas.ec.europa.eu/cas/schemas")
	private String user;

	@XmlElement(name = "attributes", namespace = "https://ecas.ec.europa.eu/cas/schemas")
	@XmlJavaTypeAdapter(AttributesAdapter.class)
	private Map<String, Object> attributes = new HashMap<>();

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(final Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return String.format("Success [user=%s, attributes=%s]", user, attributes);
	}
}