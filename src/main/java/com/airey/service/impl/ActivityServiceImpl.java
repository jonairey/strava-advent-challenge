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

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final Gson gson = new GsonBuilder().create();
    private static final String URI_GET_ATHLETE_ACTIVITIES = "https://www.strava.com/api/v3/athlete/activities?per_page=200";
    private static final Logger LOG = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Override
    public List<Activity> getActivities(final String athleteId, final String dateAfter, String dateBefore) {
        final List<Activity> activities = new ArrayList<>();
        List<Activity> activitiesPage;
        int page = 1;

        do {
            activitiesPage = getActivitiesInternal(athleteId, page++, dateAfter, dateBefore);
            activities.addAll(activitiesPage);
        } while (isNotEmpty(activitiesPage));

        return activities;
    }

    @Override
    public List<Activity> getRunningActivities(final String athleteId, final String activityType,
                                               final String dateAfter, final String dateBefore) {
        final List<Activity> activities = new ArrayList<>();
        List<Activity> activitiesPage;
        int page = 1;

        do {
            activitiesPage = getActivitiesInternal(athleteId, page++, dateAfter, dateBefore);
            activities.addAll(activitiesPage);
        } while (isNotEmpty(activitiesPage));

        return activities.stream()
                .filter(activity -> activity.getType().equalsIgnoreCase(activityType))
                .collect(Collectors.toList());
    }

    private List<Activity> getActivitiesInternal(final String athleteId, final int page, final String dateAfter, String dateBefore) {
        try (final CloseableHttpClient client = HttpClients.custom().build()) {
            final String url = URI_GET_ATHLETE_ACTIVITIES + "&page=" + page +
                    (isEmpty(dateAfter) ? "" : "&after=" + dateAfter) +
                    (isEmpty(dateAfter) ? "" : "&before=" + dateBefore);
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
