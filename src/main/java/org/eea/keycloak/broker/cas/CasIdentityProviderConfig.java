package org.eea.keycloak.broker.cas;

import org.jboss.logging.Logger;
import org.keycloak.models.IdentityProviderModel;

public class CasIdentityProviderConfig extends IdentityProviderModel {
	protected static final Logger logger = Logger.getLogger(CasIdentityProviderConfig.class);
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_CAS_LOGIN_SUFFFIX = "login";
	private static final String DEFAULT_CAS_LOGOUT_SUFFFIX = "logout";
	//private static final String DEFAULT_CAS_SERVICE_VALIDATE_SUFFFIX = "serviceValidate";
	//private static final String DEFAULT_CAS_SERVICE_VALIDATE_SUFFFIX = "laxValidate";

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
		//return String.format("%s/%s", getConfig().get("casServerUrlPrefix"), DEFAULT_CAS_SERVICE_VALIDATE_SUFFFIX);
		logger.error("endpoint bonico -->{}"+String.format("%s/%s", getConfig().get("casServerUrlPrefix"), getConfig().get("casValidationEndpoint")));
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
}
