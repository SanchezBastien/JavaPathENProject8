package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

/**
 * Représente un utilisateur de l'application TourGuide
 * Cette classe contient toutes les informations liées à un utilisateur
 * y compris ses coordonnées personnelles, ses visites, ses récompenses
 * ses préférences de voyage, ainsi que l'historique des fournisseurs
 * de services touristiques
 */
public class User {
    /** Identifiant unique de l'utilisateur. */
	private final UUID userId;
    /** Nom de l'utilisateur. */
	private final String userName;
	private String phoneNumber;
	private String emailAddress;
    /** Date de la dernière connexion de l'utilisateur. */
	private Date latestLocationTimestamp;
    /** Liste des localisations visitées par l'utilisateur. */
	private List<VisitedLocation> visitedLocations = new CopyOnWriteArrayList<>();
    /** Liste des récompenses obtenues par l'utilisateur. */
    private List<UserReward> userRewards = new CopyOnWriteArrayList<>();
    /** Préférences de voyage de l'utilisateur. */
	private UserPreferences userPreferences = new UserPreferences();
    /** Liste des fournisseurs de services touristiques associés à l'utilisateur. */
	private List<Provider> tripDeals = new ArrayList<>();
    /**
     * Crée un nouvel utilisateur avec les informations principales
     * @param userId l'identifiant unique de l'utilisateur
     * @param userName le nom de l'utilisateur
     * @param phoneNumber le numéro de téléphone de l'utilisateur
     * @param emailAddress l'adresse e-mail de l'utilisateur
     */
	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}

    /**
     * Récupère l'identifiant unique de l'utilisateur
     * @return l'identifiant utilisateur
     */
	public UUID getUserId() {
		return userId;
	}

    /**
     * Récupère le nom de l'utilisateur
     * @return le nom de l'utilisateur
     */
	public String getUserName() {
		return userName;
	}

    /**
     * Définit le numéro de téléphone de l'utilisateur
     * @param phoneNumber le numéro de téléphone à définir
     */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

    /**
     * Récupère le numéro de téléphone de l'utilisateur
     * @return le numéro de téléphone
     */
	public String getPhoneNumber() {
		return phoneNumber;
	}

    /**
     * Définit l'adresse e-mail de l'utilisateur
     * @param emailAddress la nouvelle adresse e-mail
     */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

    /**
     * Récupère l'adresse e-mail de l'utilisateur
     * @return l'adresse e-mail
     */
	public String getEmailAddress() {
		return emailAddress;
	}

    /**
     * Définit la date de la dernière connexion de l'utilisateur
     * @param latestLocationTimestamp la nouvelle date de connexion
     */
	public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
		this.latestLocationTimestamp = latestLocationTimestamp;
	}

    /**
     * Récupère la date de la dernière connexion de l'utilisateur
     * @return la date de la dernière connexion
     */
	public Date getLatestLocationTimestamp() {
		return latestLocationTimestamp;
	}

    /**
     * Ajoute une localisation visitée à la liste des visites de l'utilisateur.
     * @param visitedLocation la localisation visitée à ajouter
     */
	public void addToVisitedLocations(VisitedLocation visitedLocation) {
		visitedLocations.add(visitedLocation);
	}

    /**
     * Récupère la liste des localisations visitées par l'utilisateur
     * @return la liste des localisations visitées
     */
	public List<VisitedLocation> getVisitedLocations() {
		return visitedLocations;
	}

	public void clearVisitedLocations() {
		visitedLocations.clear();
	}

    /**
     * Ajoute une récompense à la liste des récompenses de l'utilisateur
     * en évitant les doublons pour une même attraction
     * @param userReward la récompense à ajouter
     */
    public void addUserReward(UserReward userReward) {
        boolean alreadyRewarded = userRewards.stream()
                .anyMatch(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName));
        if (!alreadyRewarded) {
            userRewards.add(userReward);
        }
    }

    /**
     * Récupère la liste des récompenses obtenues par l'utilisateur
     * @return la liste des récompenses
     */
	public List<UserReward> getUserRewards() {
		return userRewards;
	}

    /**
     * Récupère les préférences de voyage de l'utilisateur
     * @return les préférences utilisateur
     */
	public UserPreferences getUserPreferences() {
		return userPreferences;
	}

    /**
     * Définit les préférences de voyage de l'utilisateur
     * @param userPreferences les nouvelles préférences utilisateur
     */
	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

    /**
     * Récupère la localisation la plus récente visitée par l'utilisateur
     * @return la dernière localisation visitée ou {@code null} si aucune n'existe
     */
	public VisitedLocation getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}

    /**
     * Définit la liste des offres touristiques disponibles pour l'utilisateur
     * @param tripDeals la nouvelle liste des offres touristiques
     */
	public void setTripDeals(List<Provider> tripDeals) {
		this.tripDeals = tripDeals;
	}

    /**
     * Récupère la liste des offres touristiques associées à l'utilisateur
     * @return la liste des offres touristiques
     */
	public List<Provider> getTripDeals() {
		return tripDeals;
	}

}
