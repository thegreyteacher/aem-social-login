package com.tgt.core.aem.social.login.scribe;

import com.tgt.core.aem.social.login.constants.SocialLoginConstants;
import org.apache.oltu.oauth2.common.OAuth;
import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.scribe.oauth.OAuthService} for Google OAuth 2.
 * <p>
 * Note - We have to override {@link org.scribe.oauth.OAuthService#getAccessToken(Token, Verifier)} because the
 * params used by default OAuth2 Scribe service are different from what Google new OAuth2 API expects.
 */
public class GoogleScribeService extends OAuth20ServiceImpl {

    private final Logger LOGGER = LoggerFactory.getLogger(GoogleScribeService.class);

    private final GoogleScribeApi api;
    private final OAuthConfig config;

    public GoogleScribeService(GoogleScribeApi api, OAuthConfig config) {
        super(api, config);
        this.api = api;
        this.config = config;
    }

    /**
     * Method to be used by AEM's OAuth Authentication Handler to get the access token using the authorization code.
     *
     * @param requestToken {@link Token}
     * @param verifier     {@link Verifier}
     * @return {@link Token}
     */
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        OAuthRequest request = new OAuthRequest(this.api.getAccessTokenVerb(),
                this.api.getAccessTokenEndpoint());
        request.addQuerystringParameter(OAuth.OAUTH_CLIENT_ID, this.config.getApiKey());
        request.addQuerystringParameter(OAuth.OAUTH_CLIENT_SECRET, this.config.getApiSecret());
        request.addQuerystringParameter(OAuth.OAUTH_CODE, verifier.getValue());
        request.addQuerystringParameter(OAuth.OAUTH_REDIRECT_URI, this.config.getCallback());
        request.addQuerystringParameter(OAuth.OAUTH_GRANT_TYPE, SocialLoginConstants.AUTHORIZATION_CODE);

        Response response = request.send();
        LOGGER.debug("Response code is {} for request : {}", response.getCode(), request.getUrl());
        return this.api.getAccessTokenExtractor().extract(response.getBody());
    }

}
