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

/**
 * Service responsable du calcul et de la gestion des récompenses pour les utilisateurs.

 * Ce service détermine si un utilisateur se trouve à proximité d'une attraction et lui
 * attribue des points de récompense via l'API RewardCentral.

 * Il contient des optimisations telles que :
 * Un cache pour les points des attractions afin de réduire les appels coûteux.
 * Le traitement parallèle pour le calcul des récompenses sur plusieurs attractions.
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    private final List<Attraction> attractions;
    private final Map<UUID, Integer> rewardCache = new ConcurrentHashMap<>();

    /**
     * Constructeur du service de récompenses
     * @param gpsUtil Service pour récupérer les données GPS.
     * @param rewardCentral Service centralisé pour obtenir les points de récompense.
     */
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
        // Cache permanent des attractions pour éviter de les recalculer
        this.attractions = gpsUtil.getAttractions();
    }

    /**
     * Définit une valeur personnalisée pour la distance de proximité
     * @param proximityBuffer Nouvelle distance de proximité en miles
     */

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * Réinitialise la distance de proximité à sa valeur par défaut.
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calcule et attribue les récompenses pour un utilisateur en fonction des attractions visitées
     * Pour chaque position visitée par l'utilisateur, ce service vérifie si elle se situe à
     * proximité d'une attraction et attribue des points si aucune récompense n'a déjà été accordée
     * @param user L'utilisateur pour lequel calculer les récompenses.
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

    /**
     * Vérifie si une localisation donnée est dans la zone de proximité d'une attraction.
     * @param attraction L'attraction cible.
     * @param location   La localisation à comparer.
     * @return {@code true} si la distance est inférieure ou égale à la distance de proximité.
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) <= attractionProximityRange;
    }

    /**
     * Vérifie si une position visitée est proche d'une attraction.
     * @param visitedLocation Localisation visitée par l'utilisateur
     * @param attraction Attraction à vérifier
     * @return {@code true} si la distance est inférieure ou égale au buffer défini
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        if (proximityBuffer == Integer.MAX_VALUE) {
            return true;
        }
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }

    /**
     * Récupère les points de récompense pour une attraction donnée
     * Utilise un cache pour éviter les appels multiples au service externe RewardCentral
     * @param attraction L'attraction ciblée
     * @param user L'utilisateur concerné
     * @return Le nombre de points de récompense attribués
     */
    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCache.computeIfAbsent(attraction.attractionId,
                id -> rewardsCentral.getAttractionRewardPoints(id, user.getUserId()));
    }

    /**
     * Calcule la distance en miles entre deux points géographiques.
     * @param loc1 Première localisation.
     * @param loc2 Deuxième localisation.
     * @return La distance en miles.
     */
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