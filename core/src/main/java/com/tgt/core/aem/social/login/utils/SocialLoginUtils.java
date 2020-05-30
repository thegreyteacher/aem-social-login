package com.tgt.core.aem.social.login.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.scribe.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Utilities for Social Login
 */
public final class SocialLoginUtils {

    private SocialLoginUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialLoginUtils.class);


    /**
     * Parse the JSON response from social provider's profile data using {@link Response#getBody()}.
     *
     * @param response {@link Response}
     * @return map generated from the JSON.
     * @throws IOException in case of error in parsing JSON response
     */
    public static Map<String, String> parseProfileDataResponse(Response response) throws IOException {
        String providerResponseBody = response.getBody();

        try {
            //Type for Gson to parse json to a Map<String, String>
            Type stringMapType = (new TypeToken<Map<String, String>>() {
            }).getType();
            Map<String, String> properties = new Gson().fromJson(providerResponseBody, stringMapType);
            LOGGER.debug("User details parsed from social provider response : {}", properties);
            return properties;
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.error("Unable to parse Json from social provider", "parseProfileDataResponse",
                    providerResponseBody);
            throw new IOException(
                    "Error in parsing social provider response body to Map<String, String>");
        }
    }

}
