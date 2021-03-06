package org.eea.keycloak.broker.cas;

import org.jboss.logging.Logger;
import org.keycloak.models.IdentityProviderModel;

public class CasIdentityProviderConfig extends IdentityProviderModel {
	protected static final Logger logger = Logger.getLogger(CasIdentityProviderConfig.class);
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_CAS_LOGIN_SUFFFIX = "login";
	private static final String DEFAULT_CAS_LOGOUT_SUFFFIX = "logout";

	private boolean trustAllCertificates;

	public CasIdentityProviderConfig(final IdentityProviderModel model) {
		super(model);
	}

	public String getCasServerUrlPrefix() {
		return getConfig().get("casServerUrlPrefix");
	}

	public String getCasServerLoginUrl() {
		return String.format("%s/%s", getConfig().get("casServerUrlPrefix"), DEFAULT_CAS_LOGIN_SUFFFIX);
	}

	public String getCasServerLogoutUrl() {
		return String.format("%s/%s", getConfig().get("casServerUrlPrefix"), DEFAULT_CAS_LOGOUT_SUFFFIX);
	}

	public String getCasServiceValidateUrl() {
		return String.format("%s/%s", getConfig().get("casServerUrlPrefix"), getConfig().get("casValidationEndpoint"));
	}

	public boolean isGateway() {
		return Boolean.valueOf(getConfig().get("gateway"));
	}

	public boolean isRenew() {
		return Boolean.valueOf(getConfig().get("renew"));
	}

	public void setCasServerUrlPrefix(final String casServerUrlPrefix) {
		getConfig().put("casServerUrlPrefix", casServerUrlPrefix);
	}
	public void setGateway(final boolean gateway) {
		getConfig().put("gateway", String.valueOf(gateway));
	}

	public void setCasValidationEndpoint(final String casValidationEndpoint) {
		getConfig().put("casValidationEndpoint", casValidationEndpoint);
	}

	public void setRenew(final boolean renew) {
		getConfig().put("renew", String.valueOf(renew));
	}

	public String getTrustAllCertificates(){return getConfig().get("trustAllCertificates");}

	public Boolean isTrustAllCertificates(){return Boolean.valueOf(getConfig().get("trustAllCertificates"));}

	public void setTrustAllCertificates(final Boolean trustAllCertificates){getConfig().put("trustAllCertificates",String.valueOf(trustAllCertificates));}
}
