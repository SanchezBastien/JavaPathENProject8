package com.openclassrooms.tourguide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.service.RewardsService;

/**
 * Classe de configuration Spring Boot pour l'application TourGuide
 * Déclare les beans principaux utilisés dans l'application, notamment
 * les services externes comme {@link GpsUtil} et {@link RewardCentral}
 * ainsi que les services internes comme {@link RewardsService}
 */
@Configuration
public class TourGuideModule {

    /**
     * Fournit une instance de {@link GpsUtil}
     * {@code GpsUtil} est utilisé pour accéder aux informations de localisation
     * telles que la position des utilisateurs et des attractions
     * @return une instance de {@link GpsUtil}
     */
	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

    /**
     * Fournit une instance du service de gestion des récompenses
     * Ce service utilise {@link GpsUtil} et {@link RewardCentral} pour déterminer
     * quelles récompenses attribuer en fonction des visites et de la proximité des attractions
     * @return une instance de {@link RewardsService}
     */
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}

    /**
     * Fournit une instance de {@link RewardCentral}
     * {@code RewardCentral} est utilisé pour gérer le calcul et la distribution
     * des points de récompense associés aux visites d'attractions
     * @return une instance de {@link RewardCentral}
     */
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
