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
 * Tracker : met à jour régulièrement la localisation des utilisateurs.
 * Optimisé pour supporter la charge massive grâce à un pool de threads.
 */
public class Tracker extends Thread {
    private Logger logger = LoggerFactory.getLogger(Tracker.class);

    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);

    // Utilisation d’un pool multi-thread pour paralléliser
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    private final TourGuideService tourGuideService;
    private boolean stop = false;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
        // lance le thread
        executorService.submit(this);
    }

    /**
     * Assure l'arrêt du Tracker proprement
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();

        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            List<User> users = tourGuideService.getAllUsers();
            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");

            stopWatch.start();

            // Exécution en parallèle des mises à jour de localisation
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

            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
