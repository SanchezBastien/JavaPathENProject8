package com.openclassrooms.tourguide.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    private int defaultProximityBuffer = 10000;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    private final List<Attraction> attractions;
    private final Map<UUID, Integer> rewardCache = new ConcurrentHashMap<>();

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
        // Cache permanent des attractions pour éviter de les recalculer
        this.attractions = gpsUtil.getAttractions();
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Optimisation du calcul des récompenses :
     * - Traitement parallèle des attractions pour un utilisateur.
     * - Utilisation d'un cache pour éviter de recalculer les mêmes points.
     * - Utilisation d'un HashSet pour vérifier rapidement les récompenses déjà attribuées.
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();

        // Ensemble des attractions déjà récompensées pour l'utilisateur
        Set<String> rewardedAttractions = user.getUserRewards().stream()
                .map(r -> r.attraction.attractionName)
                .collect(Collectors.toSet());

        for (VisitedLocation visitedLocation : userLocations) {
            attractions.parallelStream().forEach(attraction -> {
                if (!rewardedAttractions.contains(attraction.attractionName) &&
                        nearAttraction(visitedLocation, attraction)) {
                    int points = getRewardPoints(attraction, user);
                    synchronized (user) {
                        user.addUserReward(new UserReward(visitedLocation, attraction, points));
                        rewardedAttractions.add(attraction.attractionName);
                    }
                }
            });
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) <= attractionProximityRange;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        if (proximityBuffer == Integer.MAX_VALUE) {
            return true;
        }
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }

    /**
     * Mise en cache des points pour chaque attraction afin d'éviter des appels multiples coûteux.
     */
    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCache.computeIfAbsent(attraction.attractionId,
                id -> rewardsCentral.getAttractionRewardPoints(id, user.getUserId()));
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
}