package com.airey.controller;

import com.airey.domain.activity.Activity;
import com.airey.domain.athlete.Athlete;
import com.airey.domain.authorization.Authorization;
import com.airey.service.ActivityService;
import com.airey.service.AthleteService;
import com.airey.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.math.BigDecimal.ROUND_DOWN;

@RestController
@RequestMapping("/go")
public class DoAllController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private ActivityService activityService;

    @Value("${auth.codes}")
    private String[] authCodes;

    private static final Double MILE_IN_METRES = 1609.34;
    private static final String ACTIVITY_TYPE = "run";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String NEWLINE = "\n";
    private static final Logger LOG = LoggerFactory.getLogger(DoAllController.class);

    @RequestMapping
    public String go() {
        LOG.debug("Athletes are {}", authCodes);

        //Get Authentications
        final List<String> auths = new ArrayList<>();

        for (final String authCode : authCodes) {
            final Authorization auth = authenticationService.authenticate(authCode.split(":")[1]);

            if (auth != null && auth.getAccessToken() != null) {
                auths.add(auth.getAccessToken());
            }
        }

        LOG.debug("Auths are {}", auths);

        //Get Activities
        final List<String> lines = new ArrayList<>();
        lines.add(buildHeader());

        for (final String access : auths) {
            final Athlete athlete = athleteService.get(access);
            final List<Activity> activities = activityService.getRunningActivities(access, ACTIVITY_TYPE);
            final List<String> activitiesString = plotActivities(activities);
            lines.add(buildLine(athlete, activitiesString));
        }

        //Print CSV
        final StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            builder.append(line.replaceAll("'", "\""));
            builder.append(NEWLINE);
        }

        return builder.toString();
    }

    private String buildHeader() {
        final StringBuilder header = new StringBuilder();
        header.append("'First Name','Last Name','Sex',");
        IntStream.rangeClosed(1, 31).forEach(i -> header.append(format("'%s',", i)));
        return header.toString();
    }

    private List<String> plotActivities(final List<Activity> activities) {
        final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Map<Integer, Double> dates = new HashMap<>();
        final List<String> activitiesString = new ArrayList<>();

        for (final Activity activity : activities) {
            if (isActivityValid(activity)) {
                try {
                    final Date date = format.parse(activity.getStartDate());
                    final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LOG.debug("Day of month is {}", localDate.getDayOfMonth());
                    addActivityToMap(dates, localDate.getDayOfMonth(), activity);
                } catch (ParseException e) {
                    LOG.error("Unreadable date {}", activity.getStartDate(), e);
                }
            }
        }

        IntStream.rangeClosed(1, 31).forEach(i -> {
                    final Double distance = dates.get(i);

                    if (distance == null) {
                        activitiesString.add("0");
                    } else {
                        activitiesString.add("" + distance);
                    }
                }
        );

        return activitiesString;
    }

    private boolean isActivityValid(final Activity activity) {
        return activity.getDistance() >= MILE_IN_METRES && activity.getType().equals("Run");
    }

    private void addActivityToMap(Map<Integer, Double> dates, Integer date, Activity activity) {
        Double distance;

        if (dates.containsKey(date)) {
            distance = dates.get(date);
            distance += activity.getDistance();
        } else {
            distance = activity.getDistance();
        }

        dates.put(date, new BigDecimal(distance / MILE_IN_METRES).setScale(2, ROUND_DOWN).doubleValue());
    }

    private String buildLine(final Athlete athlete, final List<String> activitiesStrings) {
        final StringBuilder builder = new StringBuilder();
        builder.append(format("'%s','%s','%s',", athlete.getFirstname(), athlete.getLastname(), athlete.getSex()));

        for (int i = 0; i < 31; i++) {
            builder.append(format("'%s',", activitiesStrings.get(i)));
        }

        return builder.toString();
    }
}
