package com.openclassrooms.tourguide.helper;

/**
 * Classe utilitaire pour la configuration des tests internes.
 * Elle permet de définir et récupérer le nombre d'utilisateurs internes
 * simulés dans l'application pour des tests de performance ou de charge.
 * Par défaut, ce nombre est fixé à 100 mais peut être augmenté jusqu'à 100 000
 * pour tester la scalabilité de l'application.
 */
public class InternalTestHelper {

    /**
     * Nombre d'utilisateurs internes simulés pour les tests.
     * Par défaut fixé à 100, mais peut être modifié pour simuler une charge plus importante.
     */
	private static int internalUserNumber = 100;

    /**
     * Définit le nombre d'utilisateurs internes simulés.
     * @param internalUserNumber le nouveau nombre d'utilisateurs à générer.
     */
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}

    /**
     * Récupère le nombre actuel d'utilisateurs internes simulés.
     * @return le nombre d'utilisateurs internes configuré.
     */

    public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
