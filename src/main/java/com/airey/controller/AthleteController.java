package com.airey.controller;

import com.airey.domain.activity.Activity;
import com.airey.domain.athlete.Athlete;
import com.airey.service.ActivityService;
import com.airey.service.AthleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/athlete")
public class AthleteController {
    @Autowired
    private AthleteService athleteService;

    @Autowired
    private ActivityService activityService;

    @RequestMapping("/{id}")
    public Athlete retrieveAthlete(@PathVariable String id) {
        return athleteService.get(id);
    }

    @RequestMapping("/{id}/activities")
    public List<Activity> retrieveAthleteActivities(@PathVariable String id, @RequestParam String dateAfter) {
        return activityService.getActivities(id, dateAfter);
    }

    @RequestMapping("/{id}/activities/{type}")
    public List<Activity> retrieveAthleteRunningActivities(@PathVariable String id, @PathVariable String type, @RequestParam String dateAfter) {
        return activityService.getRunningActivities(id, type, dateAfter);
    }
}