# Etape 1 : Construction de l'application Angular
FROM node:22.14.0-alpine AS build
WORKDIR /app

# Récupérer la version du projet
ARG VERSION=1.0.0-SNAPSHOT
ENV VERSION=${VERSION}

# Copier les fichiers de configuration
COPY package*.json ./
RUN npm install

# Copier le code source
COPY . .

# Variable d'environnement pour gérer la configuration Angular dynamiquement (valeur par défaut : `prodlocal`)
ARG ENV_NAME=prodlocal
ENV ENV_NAME=${ENV_NAME}

RUN echo "ENV_NAME=${ENV_NAME}"

# Construire l'application en fonction de la configuration Angular spécifiée
# RUN npm run build -- --configuration=${ENV_NAME}
RUN chmod +x ./node_modules/.bin/* && \
    npm run build --verbose -- --configuration=${ENV_NAME}

# Etape 2 : Configuration du serveur Nginx pour le déploiement
FROM nginx:alpine

# Re-déclarer les variables ARG dans cette étape pour qu'elles soient disponibles
ARG VERSION=1.0.0-SNAPSHOT
ARG ENV_NAME=prodlocal

RUN echo "ENV_NAME=${ENV_NAME}"

# Ajouter des labels pour la traçabilité
LABEL version="${VERSION}"
LABEL environment="${ENV_NAME}"

# Copier les fichiers Angular générés
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Configuration de prod pour rediriger les requêtes vers l'API backend
# Copier le fichier de configuration Nginx en fonction de la variable
# d'environnement
COPY nginx.${ENV_NAME}.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
