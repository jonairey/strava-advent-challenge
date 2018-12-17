package com.airey.controller;

import com.airey.domain.activity.Activity;
import com.airey.domain.athlete.Athlete;
import com.airey.domain.authorization.Authorization;
import com.airey.service.ActivityService;
import com.airey.service.AthleteService;
import com.airey.service.AuthenticationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.math.RoundingMode.DOWN;

@RestController
@RequestMapping("/go")
public class DoAllController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private ActivityService activityService;

    @Value("#{'${auth.codes}'.split(',')}")
    private List<String> authCodes;

    private final Gson gson = new GsonBuilder().create();
    private static final Integer LAST_DAY = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();
    private static final String DATE_AFTER = "1543622400";
    private static final Double MILE_IN_METRES = 1609.34;
    private static final String ACTIVITY_TYPE = "run";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private static final String SEPERATOR = "\t";
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

        for (final String line : lines) {
            builder.append(line);
            builder.append(NEWLINE);
        }

        LOG.debug("Completed Do All Go.");
        LOG.debug("--------------------");
        LOG.debug(builder.toString());
        return builder.toString();
    }

    @RequestMapping("/sort")
    public String goSort() {
        LOG.debug("Day of Month is {}", LAST_DAY);
        LOG.debug("Athletes are {}", authCodes);

        //Get Authentications
        final List<String> auths = getAuths(authCodes);
        LOG.debug("Auths are {}", auths);

        //Get Activities
        final List<String> lines = new ArrayList<>();
        lines.add(buildHeader());
        lines.addAll(getAthleteLinesSorted(auths));

        //Print CSV
        final StringBuilder builder = new StringBuilder();

        for (final String line : lines) {
            builder.append(line);
            builder.append(NEWLINE);
        }

        LOG.debug("Completed Do All Go Sort.");
        LOG.debug("--------------------");
        LOG.debug(builder.toString());
        return builder.toString();
    }

    private List<String> getAuths(final List<String> authCodes) {
        final List<CompletableFuture<String>> futures =
                authCodes.stream().map(authCode -> getAuthsAsync(authCode)).collect(Collectors.toList());
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private CompletableFuture<String> getAuthsAsync(final String authCode) {
        return CompletableFuture.supplyAsync(() -> {
            final Authorization auth = authenticationService.authenticate(authCode.split(":")[0]);
            LOG.debug("Auth is {}", auth);

            if (auth != null && auth.getAccessToken() != null) {
                return auth.getAccessToken();
            } else {
                LOG.error("Auth {} is null", authCode);
                return null;
            }
        });
    }

    private String buildHeader() {
        final StringBuilder header = new StringBuilder();
        header.append(format("First Name%sLast Name%sGender%s", SEPERATOR, SEPERATOR, SEPERATOR));
        IntStream.rangeClosed(1, LAST_DAY).forEach(i -> header.append(format("%s%s", i, SEPERATOR)));
        header.append("Total");
        return header.toString();
    }

    private List<String> getAthleteLines(final List<String> auths) {
        final List<CompletableFuture<String>> futures =
                auths.stream().map(accessToken -> getAthleteLinesAsync(accessToken)).collect(Collectors.toList());
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private Collection<String> getAthleteLinesSorted(final List<String> auths) {
        final List<CompletableFuture<String>> futures =
                auths.stream().map(accessToken -> getAthleteLinesAsync(accessToken)).collect(Collectors.toList());
        final List<String> unsortedList = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        final SortedMap<Double, String> activitiesMap = new TreeMap<Double, String>(Collections.reverseOrder()) {
        };

        for (final String activityLine : unsortedList) {
            final Double distance = Double.valueOf(activityLine.substring(activityLine.lastIndexOf(SEPERATOR) + 1));
            activitiesMap.put(distance, activityLine);
        }

        return activitiesMap.values();
    }

    private CompletableFuture<String> getAthleteLinesAsync(final String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            final Athlete athlete = athleteService.get(accessToken);
            final List<Activity> activities = activityService.getRunningActivities(accessToken, ACTIVITY_TYPE, DATE_AFTER);
            final List<Double> activitiesDouble = plotActivities(activities);
            Double distanceSum = activitiesDouble.stream().mapToDouble(Double::doubleValue).sum();
            distanceSum = new BigDecimal(distanceSum).setScale(2, DOWN).doubleValue();
            return buildLine(athlete, activitiesDouble) + convertToMiles(distanceSum);
        });
    }

    private List<Double> plotActivities(final List<Activity> activities) {
        final Map<Integer, Double> dates = generateDateMap();
        final List<Double> activitiesDouble = new ArrayList<>();

        for (final Activity activity : activities) {
            if (isActivityValid(activity)) {
                try {
                    LOG.debug("Activity is {}", gson.toJson(activity));

                    try {
                        final Date date = DATE_FORMAT.parse(activity.getStartDate());
                        final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        addActivityToMap(dates, localDate.getDayOfMonth(), activity);
                    } catch (NumberFormatException e) {
                        LOG.error("Failed to parse date for activity {}", activity, e);
                    }
                } catch (ParseException e) {
                    LOG.error("Unreadable date for activity {}", activity, e);
                }
            }
        }

        IntStream.rangeClosed(1, LAST_DAY).forEach(i -> activitiesDouble.add(dates.get(i)));
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

    private void addActivityToMap(final Map<Integer, Double> dates, final Integer date, final Activity activity) {
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
        builder.append(format("%s%s%s%s%s%s",
                athlete.getFirstname(), SEPERATOR,
                athlete.getLastname(), SEPERATOR,
                athlete.getSex(), SEPERATOR));

        for (int i = 0; i < LAST_DAY; i++) {
            builder.append(format("%s%s", convertToMiles(activitiesStrings.get(i)), SEPERATOR));
        }

        return builder.toString();
    }

    private String convertToMiles(final Double distanceMetres) {
        LOG.debug("Distance in metres is {}", distanceMetres);
        final BigDecimal distanceMilesBigDecimal = new BigDecimal(distanceMetres / MILE_IN_METRES).setScale(2, DOWN);
        final String distanceMilesString = new DecimalFormat("0.00").format(distanceMilesBigDecimal);
        LOG.debug("Distance in miles is {}", distanceMilesString);
        return distanceMilesString;
    }
}
