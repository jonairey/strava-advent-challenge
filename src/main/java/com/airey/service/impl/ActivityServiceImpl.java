package com.airey.service.impl;

import com.airey.domain.activity.Activity;
import com.airey.service.ActivityService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final Gson gson = new GsonBuilder().create();
    private static final String URI_GET_ATHLETE_ACTIVITIES = "https://www.strava.com/api/v3/athlete/activities";
    private static final String DATE_AFTER = "1543622400";
    private static final Logger LOG = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Override
    public List<Activity> getActivities(final String athleteId) {
        return getActivitiesInternal(athleteId);
    }

    @Override
    public List<Activity> getRunningActivities(final String athleteId, final String activityType) {
        return getActivitiesInternal(athleteId).stream()
                .filter(activity -> activity.getType().equalsIgnoreCase(activityType))
                .collect(Collectors.toList());
    }

    private List<Activity> getActivitiesInternal(final String athleteId) {
        try (final CloseableHttpClient client = HttpClients.custom().build()) {
            final HttpGet get = new HttpGet(URI_GET_ATHLETE_ACTIVITIES + "?after=" + DATE_AFTER);
            get.addHeader("Authorization", "Bearer " + athleteId);
            final String responseBody = EntityUtils.toString(client.execute(get).getEntity());
            Type listType = new TypeToken<List<Activity>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
}
