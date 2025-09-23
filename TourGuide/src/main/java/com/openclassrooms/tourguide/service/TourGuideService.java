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

/**
 * Service principal gérant les fonctionnalités du guide touristique
 * Ce service s'occupe du suivi des utilisateurs, du calcul des récompenses,
 * et de la génération d'offres de voyages personnalisées
 */
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

    /**
     * Constructeur du service TourGuide.
     * @param gpsUtil Service de localisation GPS.
     * @param rewardsService Service de calcul des récompenses.
     */
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

    /**
     * Récupère la liste des récompenses obtenues par un utilisateur
     * @param user L'utilisateur cible
     * @return La liste des {@link UserReward} de l'utilisateur
     */
    public List<UserReward> getUserRewards(User user) {
        rewardsService.calculateRewards(user);
        return user.getUserRewards();
    }

    /**
     * Récupère la dernière position connue d'un utilisateur
     * Si aucune position n'est enregistrée, une nouvelle localisation est suivie
     * @param user L'utilisateur dont la position est demandée
     * @return La dernière localisation visitée
     */
    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() : trackUserLocation(user);
    }

    /**
     * Recherche un utilisateur par son nom.
     * @param userName Nom de l'utilisateur.
     * @return L'utilisateur trouvé ou {@code null} si inexistant.
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Retourne la liste de tous les utilisateurs suivis.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }

    /**
     * Ajoute un utilisateur au système.
     */
    public void addUser(User user) {
        internalUserMap.putIfAbsent(user.getUserName(), user);
    }

    /**
     * Calcule et retourne les offres de voyage personnalisées pour un utilisateur
     * @param user L'utilisateur cible
     * @return La liste des fournisseurs avec leurs offres
     */
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

    /**
     * Suit la position actuelle d'un utilisateur et met à jour ses récompenses
     * @param user L'utilisateur à suivre
     * @return La nouvelle localisation visitée
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * Récupère les cinq attractions les plus proches de la localisation donnée.
     * @param visitedLocation Localisation de départ.
     * @return Une liste des attractions les plus proches.
     */
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        return gpsUtil.getAttractions().stream()
                .sorted(Comparator.comparingDouble(
                        a -> getDistance(visitedLocation.location, a)))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Calcule la distance (en miles) entre deux localisations géographiques
     * @param loc1 Première localisation
     * @param loc2 Deuxième localisation
     * @return La distance en miles
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return 1.15077945 * nauticalMiles; // conversion en miles
    }

    /**
     * Calcule la distance entre une localisation et une attraction.
     * @param loc Localisation de départ.
     * @param attraction Attraction cible.
     * @return La distance en miles.
     */
    public double getDistance(Location loc, Attraction attraction) {
        return getDistance(loc, new Location(attraction.latitude, attraction.longitude));
    }

    /**
     * Récupère les points de récompense associés à une attraction pour un utilisateur donné.
     * @param attraction L'attraction ciblée.
     * @param user L'utilisateur concerné.
     * @return Le nombre de points de récompense.
     */
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /* ================== Internal/test methods ================== */

    /**
     * Ajoute un hook pour arrêter proprement le tracker lors de l'arrêt de l'application.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tracker.stopTracking()));
    }

    /**
     * Initialise des utilisateurs internes pour les tests de performance.
     */
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            User user = new User(UUID.randomUUID(), userName, "000", userName + "@tourGuide.com");
            generateUserLocationHistory(user);
            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    /**
     * Génère un historique aléatoire de 3 positions pour un utilisateur.
     */
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> user.addToVisitedLocations(
                new VisitedLocation(
                        user.getUserId(),
                        new Location(generateRandomLatitude(), generateRandomLongitude()),
                        getRandomTime()
                )
        ));
    }

    /**
     * Génère une longitude aléatoire
     * @return Une valeur comprise entre -180 et 180
     */
    private double generateRandomLongitude() {
        return -180 + new Random().nextDouble() * 360;
    }

    /**
     * Génère une latitude aléatoire
     * @return Une valeur comprise entre -85.05112878 et 85.05112878
     */
    private double generateRandomLatitude() {
        return -85.05112878 + new Random().nextDouble() * 170.10225756;
    }

    /**
     * Génère une date aléatoire dans les 30 derniers jours
     * @return Une date générée aléatoirement
     */
    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}