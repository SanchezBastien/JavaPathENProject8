package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    private final RewardCentral rewardCentral = new RewardCentral();

    public final Tracker tracker;
    boolean testMode = true;

    private static final String tripPricerApiKey = "test-server-api-key";
    private final Map<String, User> internalUserMap = new HashMap<>();

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    /* ================== API methods ================== */

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() : trackUserLocation(user);
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }

    public void addUser(User user) {
        internalUserMap.putIfAbsent(user.getUserName(), user);
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream()
                .mapToInt(UserReward::getRewardPoints)
                .sum();

        List<Provider> providers = tripPricer.getPrice(
                tripPricerApiKey,
                user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(),
                cumulativeRewardPoints
        );

        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * Retourne les 5 attractions les plus proches du point visité.
     */
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        return gpsUtil.getAttractions().stream()
                .sorted(Comparator.comparingDouble(
                        a -> getDistance(visitedLocation.location, a)))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Calcule la distance (miles) entre une position et une attraction.
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
        double nauticalMiles = 60 * Math.toDegrees(angle);
        return 1.15077945 * nauticalMiles; // miles
    }

    public double getDistance(Location loc, Attraction attraction) {
        return getDistance(loc, new Location(attraction.latitude, attraction.longitude));
    }

    /**
     * Retourne les points de récompense pour une attraction donnée.
     */
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /* ================== Internal/test methods ================== */

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tracker.stopTracking()));
    }

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            User user = new User(UUID.randomUUID(), userName, "000", userName + "@tourGuide.com");
            generateUserLocationHistory(user);
            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> user.addToVisitedLocations(
                new VisitedLocation(
                        user.getUserId(),
                        new Location(generateRandomLatitude(), generateRandomLongitude()),
                        getRandomTime()
                )
        ));
    }

    private double generateRandomLongitude() {
        return -180 + new Random().nextDouble() * 360;
    }

    private double generateRandomLatitude() {
        return -85.05112878 + new Random().nextDouble() * 170.10225756;
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}