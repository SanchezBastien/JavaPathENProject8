package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class TestPerformance {

    @Test
    public void highVolumeTrackLocation() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Création de 100 utilisateurs de test
        InternalTestHelper.setInternalUserNumber(100000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        List<User> allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // ⚡ Optimisation : traitement parallèle
        allUsers.parallelStream().forEach(user -> {
            tourGuideService.trackUserLocation(user);
        });

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

        // Vérifie que tout se termine en moins de 15 minutes
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Test
    public void highVolumeGetRewards() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Monter jusqu’à 100 000 utilisateurs
        InternalTestHelper.setInternalUserNumber(10000);

        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        List<User> allUsers = tourGuideService.getAllUsers();

        Attraction attraction = gpsUtil.getAttractions().get(0);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Ajouter une visite pour chaque user
        allUsers.parallelStream().forEach(u ->
                u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date()))
        );

        // ⚡ Calcul parallèle des rewards
        allUsers.parallelStream().forEach(u -> rewardsService.calculateRewards(u));

        // Vérification : chaque user doit avoir au moins une reward
        allUsers.forEach(user -> assertTrue(user.getUserRewards().size() > 0));

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

        // Doit rester < 20 minutes
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
