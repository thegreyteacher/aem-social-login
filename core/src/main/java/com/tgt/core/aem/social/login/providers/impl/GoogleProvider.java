package com.tgt.core.aem.social.login.providers.impl;

import com.adobe.granite.auth.oauth.Provider;
import com.adobe.granite.auth.oauth.ProviderType;
import com.tgt.core.aem.social.login.constants.SocialLoginConstants;
import com.tgt.core.aem.social.login.scribe.GoogleScribeApi;
import com.tgt.core.aem.social.login.utils.SocialLoginUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Google OAuth2 {@link Provider}.
 */
@Component(service = Provider.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = GoogleProvider.GoogleProviderConfig.class)
public class GoogleProvider implements Provider {

    private final Logger LOGGER = LoggerFactory.getLogger(GoogleProvider.class);

    @ObjectClassDefinition(name = "The Grey Teacher Google Provider")
    public @interface GoogleProviderConfig {

        //this property name must be "oauth.provider.id" and all providers must have a unique ID
        @AttributeDefinition(name = "OAuth Provider ID", description = "Google Provider")
        String oauth_provider_id();

    }

    private Api googleScribeApiOAuth2;
    private String googleProviderId;

    @Activate
    protected void activate(GoogleProviderConfig googleProviderConfig) {
        googleScribeApiOAuth2 = new GoogleScribeApi();
        googleProviderId = googleProviderConfig.oauth_provider_id();
    }

    /**
     * @return OAuth type
     * @see ProviderType
     */
    @Override
    public ProviderType getType() {
        return ProviderType.OAUTH2;
    }

    /**
     * Specifies an instance of scribe {@link Api} to use for this provider.
     *
     * @return an instance of {@link GoogleScribeApi}
     */
    @Override
    public Api getApi() {
        return googleScribeApiOAuth2;
    }

    /**
     * OAuth provider's user details URL
     *
     * @return {@value SocialLoginConstants#GOOGLE_USER_DETAILS_URL}
     */
    @Override
    public String getDetailsURL() {
        return SocialLoginConstants.GOOGLE_USER_DETAILS_URL;
    }

    /**
     * Unique ID for this provider.
     * <p>
     * This will be configured in ProviderConfig to map this provider to that particular config.
     *
     * @return ID of this provider
     */
    @Override
    public String getId() {
        return googleProviderId;
    }

    /**
     * Readable name for this Provider
     *
     * @return name of this Provider
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Create an OAuthRequest to request protected data from the OAuth provider system.
     *
     * @param url URL from which user details will be fetched. Internally this will be one of the
     *            details/extendedDetails URL configured in this provider.
     * @return {@link OAuthRequest}
     */
    @Override
    public OAuthRequest getProtectedDataRequest(String url) {
        return new OAuthRequest(Verb.GET, url);
    }

    /**
     * Parse the OAuth Response for protected profile data during profile import
     *
     * @param response profile response from social provider
     * @return Map of profile properties
     * @throws IOException in case there is any error in parsing JSON response
     */
    @Override
    public Map<String, String> parseProfileDataResponse(Response response) throws IOException {
        return SocialLoginUtils.parseProfileDataResponse(response);
    }

    /**
     * Key name from provider response which will be used to find the user Id of the AEM user.
     * <p>
     * Note: This property must be present in the social provider OAuth response, actually in the map
     * returned by {@link this#parseProfileDataResponse(Response)}.
     *
     * @return {@value SocialLoginConstants#GOOGLE_PROVIDER_USER_ID_PROPERTY_NAME}
     */
    @Override
    public String getUserIdProperty() {
        return SocialLoginConstants.GOOGLE_PROVIDER_USER_ID_PROPERTY_NAME;
    }


    /**
     * Map the provider's user properties name to CQ user properties. This method will at least be
     * called to map properties fetched from {@link #getDetailsURL()}. If {@link
     * #getExtendedDetailsURLs(String)} is not null, this method will be called for the map of
     * properties fetched from each url.
     * <p>
     * Note - This map will be provided to sync handler to save properties in AEM.
     * Sync handler has the mapping of these keys with AEM user.
     *
     * @param srcUrl        URL from which user details had been fetched. Internally this will be one
     *                      of the details/extendedDetails URL configured in this provider.
     * @param clientId      in use to retrieve this set of properties
     * @param existing      CQ properties that have been mapped already
     * @param newProperties new properties that need to be mapped
     * @return the result of mapping the new properties, and combining with the existing
     */
    @Override
    public Map<String, Object> mapProperties(String srcUrl, String clientId,
                                             Map<String, Object> existing, Map<String, String> newProperties) {
        LOGGER.debug(
                "mapProperties -> properties fetched from {} with clientId {}, existing properties : {}, new properties : {}",
                srcUrl, clientId, existing, newProperties);

        Map<String, Object> mapped = new HashMap<>(existing);

        //we will add all the non null properties and use the properties name as it is
        newProperties.forEach((propName, propValue) -> {
            if (Objects.nonNull(propValue)) {
                mapped.put(propName, propValue);
            }
        });

        LOGGER.debug("mapProperties -> final properties map : {}", mapped);

        return mapped;
    }

    /**
     * Map the provider's user id to CRX user id. In our case we will use the email as it is.
     * <p>
     * Note - we can't authenticate a single account against different providers.
     *
     * @param userId provider's userId, in our case this will be user's email
     * @param props  map of all provider's properties for this userId. This map was generated from
     *               social provider's OAuth response using {@link this#mapProperties(String, String,
     *               Map, Map)}.
     * @return AEM user id
     */
    @Override
    public String mapUserId(String userId, Map<String, Object> props) {
        LOGGER.debug("user id: {}, properties: {}", userId, props);
        return userId;
    }

    /**
     * Return the property path where the access token will be stored (if ProviderConfig has access
     * token storage enabled)
     * <p>
     * Note - {@link com.adobe.granite.auth.oauth.AccessTokenProvider#getAccessToken(ResourceResolver, String, Map)}
     * uses {code oauth/oauthid-<strong>clientId</strong>} by default, so it is recommended to store the property at
     * this path.
     *
     * @param clientId in use
     * @return the property path where access token may be stored for a user.
     */
    @Override
    public String getAccessTokenPropertyPath(String clientId) {
        String accessTokenPath = new StringBuilder("oauth/oauthid-").append(clientId).toString();
        LOGGER.debug("Access token path: {}", accessTokenPath);
        return accessTokenPath;
    }

    /**
     * Return the property path where the oauth user id will be stored
     *
     * @param clientId in use
     * @return the property path where OAuth User ID will be stored.
     */
    @Override
    public String getOAuthIdPropertyPath(String clientId) {
        String oAuthIdPath = new StringBuilder("oauth/id-").append(clientId).toString();
        LOGGER.debug("OAuth ID path: {}", oAuthIdPath);
        return oAuthIdPath;
    }

    /**
     * Called after a user is created by Granite.
     * <p>
     * This method can be used to do any processing after the user has been created successfully like adding it to a
     * group on the basis of some conditions as per business requirements or mapping the user properties to a different
     * path.
     *
     * @param user {@link User}
     */
    @Override
    public void onUserCreate(User user) {
        //add user to a group or map properties.
    }

    /**
     * Called after a user is updated (i.e. profile data is mapped and applied to user that already
     * exists);
     *
     * @param user {@link User}
     */
    @Override
    public void onUserUpdate(User user) {
        //anything which has to be updated after user profile gets updated
    }

    /**
     * OAuth provider's user extended details URLs, depending on the specific scope
     *
     * @return empty String-array. (this can be used to fetch additional details from the Social provider)
     */
    @Override
    public String[] getExtendedDetailsURLs(String s) {
        return new String[0];
    }

    /**
     * OAuth provider's user extended details URLs, depending on the specific scope and previously
     * fetched data (e.g. {@link #getDetailsURL()}, {@link #getExtendedDetailsURLs(String)}).
     *
     * @param scope  allows to specify a list of property names for each scope
     * @param userId the userId
     * @param props  contains the data previously fetched.
     * @return the list of urls to fetch extended data from (this can be used to fetch additional details from the Social provider)
     */
    @Override
    public String[] getExtendedDetailsURLs(String scope, String userId, Map<String, Object> props) {
        return new String[0];
    }

    /**
     * Return the node path where the user should be created.
     * <p>
     * Note: This method is used when AEM creates the sync handler configuration automatically.
     * In our case we will provide the DefaultSyncHandler config manually. But as this method could be called by AEM
     * so we will provide a path where the user should be created.
     *
     * @param userId   current user Id, in the beginning it could be null
     * @param clientId in use when creating this user
     * @param props    map of all provider's properties for this user
     * @return relative path to store this user within /home/users
     */
    @Override
    public String getUserFolderPath(String userId, String clientId, Map<String, Object> props) {
        String userFolderPath = SocialLoginConstants.THE_GREY_TEACHER;
        if (StringUtils.isNotBlank(userId)) {
            userFolderPath += userId.charAt(0);
        }
        return userFolderPath;
    }

    /**
     * Use the request to get the User who has (or will have) oauth profile data attached
     * <p>
     * Note: This method is unused and never been called by AEM OAuth authentication handler.
     *
     * @param request {@link SlingHttpServletRequest}
     * @return the User or null, if no User is associated with the request
     */
    @Override
    public User getCurrentUser(SlingHttpServletRequest request) {
        LOGGER.warn("getCurrentUser -> this method wasn't expected to be called");
        return null;
    }

    /**
     * OAuth provider validate token URL
     * <p>
     * Note: This method is unused.
     *
     * @param clientId in use
     * @param token    {@link String}
     * @return url or null if validate token is not supported
     */
    @Override
    public String getValidateTokenUrl(String clientId, String token) {
        return null;
    }

    /**
     * Check the validity of a token
     * <p>
     * Note: This method is unused.
     *
     * @param responseBody {@link String}
     * @param clientId     in use
     * @return true if the response body contains the validity of the token, the token has been issued
     * for the provided clientId and the token type matches with the one provided
     */
    @Override
    public boolean isValidToken(String responseBody, String clientId, String tokenType) {
        return false;
    }

    /**
     * Parse the response body and return the userId contained in the response
     * <p>
     * Note: This method is unused.
     *
     * @param responseBody {@link String}
     * @return the userId contained in the response or null if is not contained
     */
    @Override
    public String getUserIdFromValidateTokenResponseBody(String responseBody) {
        return null;
    }

    /**
     * Parse the response body and return the error description contained in the response
     * <p>
     * Note: This method is unused.
     *
     * @param responseBody {@link String}
     * @return the error description contained in the response or null if is not contained
     */
    @Override
    public String getErrorDescriptionFromValidateTokenResponseBody(String responseBody) {
        return null;
    }

}
