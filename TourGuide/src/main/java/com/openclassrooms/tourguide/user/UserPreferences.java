package com.openclassrooms.tourguide.user;

/**
 * Représente les préférences de voyage d'un utilisateur
 * Cette classe contient les paramètres que l'utilisateur peut configurer,
 * tels que la proximité maximale des attractions, la durée du voyage,
 * le nombre de billets, ainsi que le nombre d'adultes et d'enfants
 */
public class UserPreferences {

    /** Distance maximale en miles pour considérer une attraction comme proche. */
	private int attractionProximity = Integer.MAX_VALUE;
    /** Durée du voyage en jours. */
	private int tripDuration = 1;
    /** Nombre total de billets pour le voyage. */
	private int ticketQuantity = 1;
    /** Nombre d'adultes participant au voyage. */
	private int numberOfAdults = 1;
    /** Nombre d'enfants participant au voyage. */
	private int numberOfChildren = 0;

    /**
     * Constructeur par défaut initialisant les préférences utilisateur
     * avec des valeurs standards.
     */
	public UserPreferences() {
	}

    /**
     * Définit la distance maximale en miles pour qu'une attraction soit considérée proche
     * @param attractionProximity la distance maximale en miles
     */
	public void setAttractionProximity(int attractionProximity) {
		this.attractionProximity = attractionProximity;
	}

    /**
     * Récupère la distance maximale en miles pour qu'une attraction soit considérée proche
     * @return la distance maximale en miles
     */
	public int getAttractionProximity() {
		return attractionProximity;
	}

    /**
     * Récupère la durée du voyage en jours
     * @return la durée du voyage en jours
     */
	public int getTripDuration() {
		return tripDuration;
	}

    /**
     * Définit la durée du voyage en jours
     * @param tripDuration la durée du voyage en jours
     */
	public void setTripDuration(int tripDuration) {
		this.tripDuration = tripDuration;
	}

    /**
     * Récupère le nombre de billets nécessaires pour le voyage.
     * @return le nombre de billets
     */
	public int getTicketQuantity() {
		return ticketQuantity;
	}

    /**
     * Définit le nombre de billets nécessaires pour le voyage.
     * @param "ticketQuantity" le nombre de billets
     */
	public void setTicketQuantity(int ticketQuantity) {
		this.ticketQuantity = ticketQuantity;
	}

    /**
     * Récupère le nombre d'adultes participant au voyage
     * @return le nombre d'adultes
     */
	public int getNumberOfAdults() {
		return numberOfAdults;
	}

    /**
     * Définit le nombre d'adultes participant au voyage.
     * @param numberOfAdults le nombre d'adultes
     */
	public void setNumberOfAdults(int numberOfAdults) {
		this.numberOfAdults = numberOfAdults;
	}

    /**
     * Récupère le nombre d'enfants participant au voyage.
     *
     * @return le nombre d'enfants
     */
	public int getNumberOfChildren() {
		return numberOfChildren;
	}

    /**
     * Définit le nombre d'enfants participant au voyage.
     * @param numberOfChildren le nombre d'enfants
     */
	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}

}
