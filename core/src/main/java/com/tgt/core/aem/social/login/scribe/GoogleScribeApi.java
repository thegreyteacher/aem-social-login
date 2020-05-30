package com.tgt.core.aem.social.login.scribe;

import com.tgt.core.aem.social.login.constants.SocialLoginConstants;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scribe {@link org.scribe.builder.api.Api} for Google OAuth2.
 * <p>
 * Note - AEM uses very old version of {@code Scribe} API which doesn't have any support for Google OAuth2.
 * We can't upgrade it to latest version as well because the new Scribe library has a different group ID and
 * artifact ID which has completely different package structure. AEM internal OAuth flow is fully dependent on the
 * Scribe APIs so we have to create/update the social login details as per the new APIs provided by the social provider.
 */
public class GoogleScribeApi extends DefaultApi20 {

    private final Logger LOGGER = LoggerFactory.getLogger(GoogleScribeApi.class);

    /**
     * This method will be called to generate the full URL to get the Authorization Code.
     *
     * @param oAuthConfig {@link OAuthConfig}
     * @return final URL to get the Authorization code
     */
    @Override
    public String getAuthorizationUrl(OAuthConfig oAuthConfig) {

        String authorizationUrl;
        if (oAuthConfig.hasScope()) {
            //please note that AEM use "," internally to join multiple scopes while google expects multiple scopes
            // to be space separated. In our case we will use the single scope i.e user.email, so, in case you want to
            // use multiple scopes then format the oAuthConfig.getScope() value accordingly before passing it to Google.
            authorizationUrl = String.format(SocialLoginConstants.GOOGLE_SCOPED_AUTHORIZE_URL, oAuthConfig.getApiKey(),
                    OAuthEncoder.encode(oAuthConfig.getCallback()),
                    OAuthEncoder.encode(oAuthConfig.getScope()));
        } else {
            authorizationUrl = String.format(SocialLoginConstants.GOOGLE_AUTHORIZE_URL, oAuthConfig.getApiKey(),
                    OAuthEncoder.encode(oAuthConfig.getCallback()));
        }
        LOGGER.debug("Google OAuth 2 URL to get the authentication code: {}", authorizationUrl);
        return authorizationUrl;
    }

    /**
     * @return URL to get an Access Token using Authorization code
     */
    @Override
    public String getAccessTokenEndpoint() {
        return SocialLoginConstants.GOOGLE_ACCESS_TOKEN_ENDPOINT;
    }

    /**
     * @return request method type to get the code. Need to override this because Google uses POST and
     * default is GET.
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new GoogleScribeService(this, config);
    }

    /**
     * @return {@link AccessTokenExtractor} to be used to parse access token from Google's response.
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }

}
