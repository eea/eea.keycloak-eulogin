package org.eea.keycloak.broker.cas;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.http.HttpHost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.eea.keycloak.broker.cas.model.ServiceResponse;
import org.eea.keycloak.broker.cas.model.Success;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eea.keycloak.broker.cas.util.UrlHelper;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

public class CasIdentityProvider extends AbstractIdentityProvider<CasIdentityProviderConfig> {

	protected static final Logger logger = Logger.getLogger(CasIdentityProvider.class);
	protected static final Logger LOGGER_DUMP_USER_PROFILE = Logger.getLogger("org.keycloak.social.user_profile_dump");

	public static final String USER_ATTRIBUTES = "UserAttributes";

	private final Client client;

	public CasIdentityProvider(final KeycloakSession session, final CasIdentityProviderConfig config) {
		super(session, config);
		//client = ResteasyClientBuilder.newClient(ResteasyProviderFactory.getInstance());
    ApacheHttpClient4Engine engine = null;
    try {
      engine = new ApacheHttpClient4Engine(createAllTrustingClient());
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    client = new ResteasyClientBuilder().httpEngine(engine).build();
	}
  private DefaultHttpClient createAllTrustingClient() throws GeneralSecurityException {
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

    TrustStrategy trustStrategy = new TrustStrategy() {
      public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return true;
      }
    };
    SSLSocketFactory factory = new SSLSocketFactory(trustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
    registry.register(new Scheme("https", 443, factory));

    ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(registry);

    mgr.setMaxTotal(1000);
    mgr.setDefaultMaxPerRoute(1000);

    DefaultHttpClient client = new DefaultHttpClient(mgr, new DefaultHttpClient().getParams());
		HttpClientBuilder.create().build();
	//	HttpHost proxy=new HttpHost("",1,"");
	//	CloseableHttpClient httpClient = HttpClientBuilder.create().setProxy(proxy).build();
    return client;
  }
	@Override
	public Response performLogin(final AuthenticationRequest request) {
		try {
			URI authenticationUrl = UrlHelper.createAuthenticationUrl(getConfig(), request).build();
			return Response.seeOther(authenticationUrl).build();
		} catch (Exception e) {
			throw new IdentityBrokerException("Could send authentication request to cas provider.", e);
		}
	}

	@Override
	public Response keycloakInitiatedBrowserLogout(final KeycloakSession session, final UserSessionModel userSession, final UriInfo uriInfo,
			final RealmModel realm) {
		URI logoutUrl = UrlHelper.createLogoutUrl(getConfig(), userSession, realm, uriInfo).build();
		return Response.status(302).location(logoutUrl).build();
	}

	@Override
	public Response retrieveToken(final KeycloakSession session, final FederatedIdentityModel identity) {
		return Response.ok(identity.getToken()).type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Object callback(final RealmModel realm, final org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback callback, final EventBuilder event) {
		return new Endpoint(callback, realm, event);
	}

	public final class Endpoint {
		AuthenticationCallback callback;
		RealmModel realm;
		EventBuilder event;

		@Context
		protected KeycloakSession session;

		@Context
		protected ClientConnection clientConnection;

		@Context
		protected HttpHeaders headers;

		@Context
		protected UriInfo uriInfo;

		Endpoint(final AuthenticationCallback callback, final RealmModel realm, final EventBuilder event) {
			this.callback = callback;
			this.realm = realm;
			this.event = event;
		}

		@GET
		public Response authResponse(@QueryParam(UrlHelper.PROVIDER_PARAMETER_TICKET) final String ticket, @QueryParam(UrlHelper.PROVIDER_PARAMETER_STATE) final String state) {
			try {
				CasIdentityProviderConfig config = getConfig();
				BrokeredIdentityContext federatedIdentity = getFederatedIdentity(client, config, ticket, uriInfo, state);

				return callback.authenticated(federatedIdentity);
			} catch (Exception e) {
				logger.error("Failed to call delegating authentication identity provider's callback method.", e);
			}
			event.event(EventType.LOGIN);
			event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
			return ErrorPage.error(session, null, Status.EXPECTATION_FAILED, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
		}

		@GET
		@Path("logout_response")
		public Response logoutResponse(@Context final UriInfo uriInfo, @QueryParam("state") final String state) {
			UserSessionModel userSession = session.sessions().getUserSession(realm, state);
			if (userSession == null) {
				logger.error("no valid user session");
				EventBuilder e = new EventBuilder(realm, session, clientConnection);
				e.event(EventType.LOGOUT);
				e.error(Errors.USER_SESSION_NOT_FOUND);
				return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
			}
			if (userSession.getState() != UserSessionModel.State.LOGGING_OUT) {
				logger.error("usersession in different state");
				EventBuilder e = new EventBuilder(realm, session, clientConnection);
				e.event(EventType.LOGOUT);
				e.error(Errors.USER_SESSION_NOT_FOUND);
				return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.SESSION_NOT_ACTIVE);
			}
			return AuthenticationManager.finishBrowserLogout(session, realm, userSession, uriInfo, clientConnection, headers);
		}

		private BrokeredIdentityContext getFederatedIdentity(final Client client, final CasIdentityProviderConfig config, final String ticket,
				final UriInfo uriInfo, final String state) {
			Response response = null;
			try {
				WebTarget target = client.target(
            UrlHelper.createValidateServiceUrl(config, ticket, uriInfo, state));
				response = target.request(MediaType.APPLICATION_XML_TYPE).get();
				if (response.getStatus() != 200) {
					throw new Exception("Failed : HTTP error code : " + response.getStatus());
				}

				response.bufferEntity();
				if (LOGGER_DUMP_USER_PROFILE.isDebugEnabled()) {
					LOGGER_DUMP_USER_PROFILE.debug("User Profile XML Data for provider " + config.getAlias() + ": " + response.readEntity(String.class));
				}

				ServiceResponse serviceResponse = response.readEntity(ServiceResponse.class);
				if (serviceResponse.getFailure() != null) {
					throw new Exception(serviceResponse.getFailure().getCode() + "(" + serviceResponse.getFailure().getDescription()
							+ ") for authentication by External IdP " + config.getProviderId());
				}
				Success success = serviceResponse.getSuccess();
				BrokeredIdentityContext user = new BrokeredIdentityContext(success.getUser());
				user.setUsername(success.getUser());
				user.getContextData().put(USER_ATTRIBUTES, success.getAttributes());
				user.setIdpConfig(config);
				user.setIdp(CasIdentityProvider.this);
				user.setCode(state);
				return user;
			} catch (Exception e) {
				throw new IdentityBrokerException("Could not fetch attributes from External IdP's userinfo endpoint.", e);
			} finally {
				if (response != null) {
					response.close();
				}
			}
		}
	}
}
