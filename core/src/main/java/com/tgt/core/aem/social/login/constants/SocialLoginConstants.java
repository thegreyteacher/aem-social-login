package com.tgt.core.aem.social.login.constants;

/**
 * Constants file for AEM social login
 */
public final class SocialLoginConstants {

    private SocialLoginConstants() {
    }

    public static final String THE_GREY_TEACHER = "thegreyteacher";

    /**
     * {@value org.apache.oltu.oauth2.common.OAuth#OAUTH_GRANT_TYPE} to get access token from social
     * provider.
     */
    public static final String AUTHORIZATION_CODE = "authorization_code";

    ////////////////////   Google Social Login  /////////////////////////

    /**
     * Google URL to get authorization code when scope is not present.
     */
    public static final String GOOGLE_AUTHORIZE_URL =
            "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=%s&redirect_uri=%s";

    /**
     * Google URL to get authorization code when scope is present.
     */
    public static final String GOOGLE_SCOPED_AUTHORIZE_URL = GOOGLE_AUTHORIZE_URL + "&scope=%s";

    /**
     * Google URL to get the Access Token
     */
    public static final String GOOGLE_ACCESS_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    /**
     * Google URL to fetch user details using access token
     */
    public static final String GOOGLE_USER_DETAILS_URL = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";

    /**
     * Google providers property which will be used to create user ID in AEM.
     * <p>
     * In our case we will use user's email as AEM user ID so this will be the property in user details data fetched
     * from Google having user's email ID as value.
     */
    public static final String GOOGLE_PROVIDER_USER_ID_PROPERTY_NAME = "email";

}
