# =============================================================================
# Dockerfile - Payment Service (Multi-stage build)
# =============================================================================
# Utilise un build multi-stage pour optimiser la taille de l'image finale.
# Stage 1 : Image JRE légère pour l'exécution uniquement.
# =============================================================================

# ---------------------------------------------------------------------------
# Stage de production : JRE 17 Alpine (image légère ~80MB)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# Métadonnées de l'image
LABEL maintainer="NeuroGuard Team"
LABEL service="payment-service"
LABEL description="Payment microservice for NeuroGuard platform"

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Répertoire de travail
WORKDIR /app

# Copier le JAR généré par Maven (passé via l'artifact CI)
COPY target/*.jar app.jar

# Changer le propriétaire du fichier
RUN chown appuser:appgroup app.jar

# Utiliser l'utilisateur non-root
USER appuser

# Port exposé par le microservice (à adapter selon application.properties)
EXPOSE 8085

# Healthcheck pour vérifier que le service est opérationnel
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Point d'entrée avec options JVM optimisées pour les conteneurs
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
