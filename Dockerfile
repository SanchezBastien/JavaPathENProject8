# Étape 1 : Build Maven avec installation des dépendances locales
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copier uniquement le pom.xml et les libs d'abord (meilleur cache Docker)
COPY TourGuide/pom.xml ./TourGuide/pom.xml
COPY TourGuide/libs ./TourGuide/libs

# Installer les JARs locaux dans le repo Maven
RUN mvn install:install-file -Dfile=TourGuide/libs/gpsUtil.jar -DgroupId=com.tourguide -DartifactId=gpsutil -Dversion=1.0.0 -Dpackaging=jar && \
    mvn install:install-file -Dfile=TourGuide/libs/TripPricer.jar -DgroupId=com.tourguide -DartifactId=trippricer -Dversion=1.0.0 -Dpackaging=jar && \
    mvn install:install-file -Dfile=TourGuide/libs/RewardCentral.jar -DgroupId=com.tourguide -DartifactId=rewardcentral -Dversion=1.0.0 -Dpackaging=jar

# Copier le reste du code
COPY TourGuide ./TourGuide

# Compiler le projet et produire le JAR
RUN cd TourGuide && mvn clean package -DskipTests

# Étape 2 : Image finale pour exécution (plus légère)
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/TourGuide/target/*.jar app.jar

# Exposer le port sur lequel tourne l'application
EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "app.jar"]