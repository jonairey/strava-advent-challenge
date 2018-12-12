package com.airey.service.impl;

import com.airey.domain.athlete.Athlete;
import com.airey.service.AthleteService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AthleteServiceImpl implements AthleteService {
    private final Gson gson = new GsonBuilder().create();
    private static final String URI_GET_ATHLETE = "https://www.strava.com/api/v3/athlete";
    private static final Logger LOG = LoggerFactory.getLogger(AthleteServiceImpl.class);

    @Override
    public Athlete get(final String id) {
        try (final CloseableHttpClient client = HttpClients.custom().build()) {
            final HttpGet get = new HttpGet(URI_GET_ATHLETE);
            get.addHeader("Authorization", "Bearer " + id);
            final String responseBody = EntityUtils.toString(client.execute(get).getEntity());
            LOG.debug("Response was {}", responseBody);
            return gson.fromJson(responseBody, Athlete.class);
        } catch (Throwable e) {
            LOG.error(e.getMessage());
        }

        return null;
    }
}
