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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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

    private static final Integer LAST_DAY = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();
    private static final String DATE_AFTER = "1543622400";
    private static final Double MILE_IN_METRES = 1609.34;
    private static final String ACTIVITY_TYPE = "run";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String NEWLINE = "\n";
    private static final Logger LOG = LoggerFactory.getLogger(DoAllController.class);

    @RequestMapping
    public String go() {
        LOG.debug("Day of Month is {}", LAST_DAY);
        LOG.debug("Athletes are {}", authCodes);

        //Get Authentications
        final List<String> auths = getAuths(authCodes);
        LOG.debug("Auths are {}", auths);

        //Get Activities
        final List<String> lines = new ArrayList<>();
        lines.add(buildHeader());
        lines.addAll(getAthleteLines(auths));

        //Print CSV
        final StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            builder.append(line.replaceAll("'", "\""));
            builder.append(NEWLINE);
        }

        return builder.toString();
    }

    private List<String> getAuths(final String[] authCodes) {
        final List<CompletableFuture<String>> futures =
                Arrays.stream(authCodes).map(authCode -> getAuthsAsync(authCode)).collect(Collectors.toList());
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private CompletableFuture<String> getAuthsAsync(String authCode) {
        return CompletableFuture.supplyAsync(() -> {
            final Authorization auth = authenticationService.authenticate(authCode.split(":")[0]);
            LOG.debug("Auth is {}", auth);

            if (auth != null && auth.getAccessToken() != null) {
                return auth.getAccessToken();
            } else {
                return null;
            }
        });
    }

    private String buildHeader() {
        final StringBuilder header = new StringBuilder();
        header.append("'First Name','Last Name','Gender',");
        IntStream.rangeClosed(1, LAST_DAY).forEach(i -> header.append(format("'%s',", i)));
        header.append("'Total'");
        return header.toString();
    }

    private List<String> getAthleteLines(final List<String> auths) {
        final List<CompletableFuture<String>> futures =
                auths.stream().map(accessToken -> getAthleteLinesAsync(accessToken)).collect(Collectors.toList());
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private CompletableFuture<String> getAthleteLinesAsync(String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            final Athlete athlete = athleteService.get(accessToken);
            final List<Activity> activities = activityService.getRunningActivities(accessToken, ACTIVITY_TYPE, DATE_AFTER);
            final List<Double> activitiesDouble = plotActivities(activities);
            final Double distanceSum = activitiesDouble.stream().mapToDouble(Double::doubleValue).sum();
            return buildLine(athlete, activitiesDouble) + "'" + distanceSum + "'";
        });
    }

    private List<Double> plotActivities(final List<Activity> activities) {
        final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Map<Integer, Double> dates = generateDateMap();
        final List<Double> activitiesDouble = new ArrayList<>();

        for (final Activity activity : activities) {
            if (isActivityValid(activity)) {
                try {
                    final Date date = format.parse(activity.getStartDate());
                    final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    addActivityToMap(dates, localDate.getDayOfMonth(), activity);
                } catch (ParseException e) {
                    LOG.error("Unreadable date {}", activity.getStartDate(), e);
                }
            }
        }

        IntStream.rangeClosed(1, LAST_DAY).forEach(i -> {
                    final Double distance = new BigDecimal(dates.get(i) / MILE_IN_METRES)
                            .setScale(2, ROUND_DOWN).doubleValue();
                    activitiesDouble.add(distance);
                }
        );

        return activitiesDouble;
    }

    private Map<Integer, Double> generateDateMap() {
        final Map<Integer, Double> map = new HashMap<>();

        for (int i = 1; i <= LAST_DAY; i++) {
            map.put(i, 0.0);
        }

        return map;
    }

    private boolean isActivityValid(final Activity activity) {
        return activity.getType().equals("Run");
    }

    private void addActivityToMap(Map<Integer, Double> dates, Integer date, Activity activity) {
        Double distance;

        if (dates.containsKey(date)) {
            distance = dates.get(date);
            distance += activity.getDistance();
        } else {
            distance = activity.getDistance();
        }

        dates.put(date, distance);
    }

    private String buildLine(final Athlete athlete, final List<Double> activitiesStrings) {
        final StringBuilder builder = new StringBuilder();
        builder.append(format("'%s','%s','%s',", athlete.getFirstname(), athlete.getLastname(), athlete.getSex()));

        for (int i = 0; i < LAST_DAY; i++) {
            builder.append(format("'%s',", activitiesStrings.get(i)));
        }

        return builder.toString();
    }
}
