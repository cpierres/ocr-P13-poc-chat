# ocr-P13-poc-chat

POC de chat en temps réel avec WebSocket pour le projet 13 - Architecture microservices

## Démarrage avec Docker Compose

#### **Prérequis**
- Se placer dans le **répertoire racine du projet** (où se trouve le fichier `compose.yaml`)
- Le fichier `compose.yaml` doit être présent à la racine

#### **Démarrer l'environnement complet**

```bash
# Se placer dans le répertoire racine du projet
cd ocr-P13-poc-chat

# Démarrer tous les services avec le fichier compose.yaml (nom de projet: ocr-p13-poc-chat)
docker-compose -f compose.yaml -p ocr-p13-poc-chat up -d --build

# Vérifier que tous les services sont démarrés
docker-compose -f compose.yaml -p ocr-p13-poc-chat ps

# Voir les logs en temps réel
docker-compose -f compose.yaml -p ocr-p13-poc-chat logs -f
```

#### **Commandes utiles**

```bash
# Arrêter tous les services
docker-compose -f compose.yaml -p ocr-p13-poc-chat down

# Redémarrer un service spécifique
docker-compose -f compose.yaml -p ocr-p13-poc-chat restart backend

# Reconstruire et redémarrer
docker-compose -f compose.yaml -p ocr-p13-poc-chat up -d --build --force-recreate

# Voir les logs d'un service spécifique
docker-compose -f compose.yaml -p ocr-p13-poc-chat logs -f backend
```

## Services disponibles

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| Backend | 8083 | http://localhost:8083 | API REST + WebSocket |
| PostgreSQL | 5432 | localhost:5432 | Base de données |
| Redis | 6379 | localhost:6379 | Cache et sessions |
