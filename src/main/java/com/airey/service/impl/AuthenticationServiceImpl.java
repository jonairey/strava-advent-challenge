package com.airey.service.impl;

import com.airey.domain.authorization.Authorization;
import com.airey.service.AuthenticationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Value("${strava.client.id}")
    private String stravaClientId;

    @Value("${strava.client.secret}")
    private String stravaClientSecret;

    private final Gson gson = new GsonBuilder().create();
    private static final String STRAVA_GRANT_TYPE = "authorization_code";
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    public Authorization authenticate(final String id) {
        try (final CloseableHttpClient client = HttpClients.custom().build()) {
            final String url = format("https://www.strava.com/oauth/token?client_id=%s&client_secret=%s&code=%s&grant_type=%s",
                    stravaClientId, stravaClientSecret, id, STRAVA_GRANT_TYPE);
            final HttpPost post = new HttpPost(url);
            final HttpEntity entity = client.execute(post).getEntity();
            final String responseBody = EntityUtils.toString(entity);
            LOG.debug("Authenticating user {}", id);
            LOG.debug("Response was {}", responseBody);
            return gson.fromJson(responseBody, Authorization.class);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
}
