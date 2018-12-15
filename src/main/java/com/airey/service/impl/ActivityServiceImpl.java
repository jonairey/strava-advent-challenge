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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final Gson gson = new GsonBuilder().create();
    private static final String URI_GET_ATHLETE_ACTIVITIES = "https://www.strava.com/api/v3/athlete/activities?per_page=200";
    private static final Logger LOG = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Override
    public List<Activity> getActivities(final String athleteId, final String dateAfter) {
        final List<Activity> activities = new ArrayList<>();
        activities.addAll(getActivitiesInternal(athleteId, "1", dateAfter)); //TODO page all
        return activities;
    }

    @Override
    public List<Activity> getRunningActivities(final String athleteId, final String activityType, final String dateAfter) {
        final List<Activity> activities = new ArrayList<>();
        activities.addAll(getActivitiesInternal(athleteId, "1", dateAfter)); //TODO page all
        return activities.stream()
                .filter(activity -> activity.getType().equalsIgnoreCase(activityType))
                .collect(Collectors.toList());
    }

    private List<Activity> getActivitiesInternal(final String athleteId, final String page, final String dateAfter) {
        try (final CloseableHttpClient client = HttpClients.custom().build()) {
            final String url = URI_GET_ATHLETE_ACTIVITIES + "&page=" + page + "&after=" + dateAfter;
            LOG.debug("Calling URL {}", url);
            final HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", "Bearer " + athleteId);
            final String responseBody = EntityUtils.toString(client.execute(get).getEntity());
            LOG.debug("Response was {}", responseBody);
            Type listType = new TypeToken<List<Activity>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
