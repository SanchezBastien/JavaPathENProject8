package com.openclassrooms.tourguide;

/**
 * Représente un objet de transfert de données (DTO) pour une attraction située
 * à proximité d'un utilisateur
 * Ce DTO est utilisé pour renvoyer les informations nécessaires concernant une
 * attraction, la position de l'utilisateur et les détails de la récompense
 * associée, dans le cadre d'une réponse à une requête API
 * </p>
 */
public class NearbyAttractionDTO {
    public String attractionName;
    public double attractionLatitude;
    public double attractionLongitude;
    public double userLatitude;
    public double userLongitude;

    /** Distance entre l'utilisateur et l'attraction en miles. */
    public double distanceInMiles;

    /** Nombre de points de récompense offerts pour la visite de cette attraction. */
    public int rewardPoints;

    /**
     * Constructeur complet permettant de créer un objet DTO représentant une
     * attraction proche.
     *
     * @param attractionName     le nom de l'attraction
     * @param attractionLatitude la latitude de l'attraction
     * @param attractionLongitude la longitude de l'attraction
     * @param userLatitude       la latitude de l'utilisateur
     * @param userLongitude      la longitude de l'utilisateur
     * @param distanceInMiles    la distance entre l'utilisateur et l'attraction en miles
     * @param rewardPoints       le nombre de points de récompense attribués
     */
    public NearbyAttractionDTO(String attractionName, double attractionLatitude, double attractionLongitude,
                               double userLatitude, double userLongitude, double distanceInMiles, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distanceInMiles = distanceInMiles;
        this.rewardPoints = rewardPoints;
    }
}
