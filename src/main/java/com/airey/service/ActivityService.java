package com.airey.service;

import com.airey.domain.activity.Activity;

import java.util.List;

public interface ActivityService {
    List<Activity> getActivities(String athleteId, String dateAfter);

    List<Activity> getRunningActivities(String athleteId, String activityType, String dateAfter);
}
