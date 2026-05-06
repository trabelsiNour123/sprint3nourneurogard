# =============================================================================
# Dockerfile - Jenkins avec Docker CLI intégré
# =============================================================================
# Image Jenkins LTS étendue avec Docker CLI pour permettre le build
# d'images Docker directement depuis les pipelines Jenkins (Docker-outside-of-Docker).
# =============================================================================

FROM jenkins/jenkins:lts

# Passer en root pour installer Docker CLI
USER root

# Installer les pré-requis + Docker CLI
RUN apt-get update && \
    apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg \
        lsb-release && \
    # Ajouter la clé GPG officielle Docker
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    # Ajouter le dépôt Docker
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    # Installer Docker CLI uniquement (pas le daemon)
    apt-get update && \
    apt-get install -y docker-ce-cli && \
    # Nettoyage pour réduire la taille de l'image
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Ajouter l'utilisateur jenkins au groupe docker
RUN groupadd -f docker && usermod -aG docker jenkins

# Revenir à l'utilisateur jenkins
USER jenkins
