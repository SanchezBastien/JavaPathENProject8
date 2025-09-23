package com.openclassrooms.tourguide.user;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

/**
 * Représente une récompense associée à une visite d'attraction pour un utilisateur
 * Cette classe lie une visite spécifique effectuée par un utilisateur
 * à une attraction et contient le nombre de points de récompense attribués
 */
public class UserReward {

    /** Localisation visitée par l'utilisateur. */
	public final VisitedLocation visitedLocation;
    /** Attraction associée à la visite. */
	public final Attraction attraction;
    /** Points de récompense gagnés pour cette visite. */
	private int rewardPoints;

    /**
     * Crée une nouvelle récompense utilisateur
     * @param visitedLocation la localisation visitée par l'utilisateur
     * @param attraction l'attraction liée à la localisation
     * @param rewardPoints le nombre de points attribués
     */
	public UserReward(VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
		this.rewardPoints = rewardPoints;
	}
	
	public UserReward(VisitedLocation visitedLocation, Attraction attraction) {
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
	}

    /**
     * Définit le nombre de points de récompense attribués
     * @param rewardPoints le nombre de points à définir
     */
	public void setRewardPoints(int rewardPoints) {
		this.rewardPoints = rewardPoints;
	}

    /**
     * Récupère le nombre de points de récompense attribués
     * @return le nombre de points de récompense
     */
	public int getRewardPoints() {
		return rewardPoints;
	}
	
}
