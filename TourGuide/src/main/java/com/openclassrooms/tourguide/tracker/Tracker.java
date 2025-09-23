package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

/**
 * Le {@code Tracker} est un composant chargé de suivre et mettre à jour
 * la localisation des utilisateurs à intervalles réguliers
 * Il s'exécute dans un thread dédié et utilise un pool de threads pour
 * paralléliser la mise à jour des localisations lorsque le nombre
 * d'utilisateurs est important
 * Fonctionnalités principales :
 * Récupération de la liste des utilisateurs depuis {@link TourGuideService}
 * Mise à jour de leur position en arrière-plan
 * Gestion de l'arrêt propre via {@link #stopTracking()}
 */
public class Tracker extends Thread {
    private Logger logger = LoggerFactory.getLogger(Tracker.class);

    /**
     * Intervalle entre deux cycles de suivi des utilisateurs (en secondes).
     * Ici fixé à 5 minutes.
     */
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);


    /**
     * Pool de threads pour exécuter les tâches de suivi des utilisateurs en parallèle.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    private final TourGuideService tourGuideService;
    private boolean stop = false;

    /**
     * Constructeur du tracker
     * @param tourGuideService le service principal de l'application, utilisé pour
     * récupérer les utilisateurs et mettre à jour leurs positions
     */
    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
        // Lancement automatique du thread au démarrage
        executorService.submit(this);
    }

    /**
     * Permet d'arrêter proprement le suivi des utilisateurs
     * Cette méthode interrompt la boucle principale et arrête le pool de threads
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    /**
     * Méthode principale exécutée par le thread du tracker
     * Étapes :
     *   <li>Récupérer la liste des utilisateurs
     *   <li>Mettre à jour leurs positions via {@link TourGuideService#trackUserLocation(User)}
     *   <li>Attendre l'intervalle défini avant de recommencer
     */
    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();

        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }
            // Étape 1 : récupération des utilisateurs
            List<User> users = tourGuideService.getAllUsers();
            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");

            stopWatch.start();

            // Étape 2 : mise à jour parallèle des positions
            users.parallelStream().forEach(user -> {
                try {
                    tourGuideService.trackUserLocation(user);
                } catch (Exception e) {
                    logger.error("Error tracking user " + user.getUserName(), e);
                }
            });

            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
            stopWatch.reset();

            // Étape 3 : pause entre deux cycles
            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
