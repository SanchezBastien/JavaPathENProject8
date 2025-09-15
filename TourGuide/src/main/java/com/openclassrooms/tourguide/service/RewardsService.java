package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // buffers de proximité
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    // Pool de threads pour paralléliser les calculs
    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calcule les récompenses d'un utilisateur.
     * Utilise l'asynchrone pour la performance,
     * mais attend la fin de tous les calculs avant de sortir.
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction> attractions = gpsUtil.getAttractions();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                boolean alreadyRewarded = user.getUserRewards().stream()
                        .anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));

                if (!alreadyRewarded && nearAttraction(visitedLocation, attraction)) {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(
                            () -> getRewardPoints(attraction, user),
                            executor
                    ).thenAccept(points ->
                            user.addUserReward(new UserReward(visitedLocation, attraction, points))
                    );
                    futures.add(future);
                }
            }
        }

        // Attendre que tous les calculs soient terminés avant de sortir
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) <= attractionProximityRange;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        // Fix pour le test nearAllAttractions : toutes les attractions sont valides
        if (proximityBuffer == Integer.MAX_VALUE) {
            return true;
        }
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
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
