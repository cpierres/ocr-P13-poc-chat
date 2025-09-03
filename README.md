# ocr-P13-poc-chat

POC de chat en temps réel avec WebSocket pour le projet 13 - Architecture microservices

## Démarrage avec Docker Compose (stack complet)

#### **Prérequis**
- Se placer dans le **répertoire racine du projet** (où se trouve le fichier `docker-compose.prodlocal.yml`)
- Le fichier `docker-compose.prodlocal.yml` doit être présent à la racine

#### **Démarrer le stack complet**

```bash
# Se placer dans le répertoire racine du projet
cd ocr-P13-poc-chat

docker-compose -f docker-compose.prodlocal.yaml -p ocr-p13-poc-chat-prodlocal up -d --build
```


## Démarrage avec Docker Compose (mode dev frontend)

#### **Prérequis**
- Se placer dans le **répertoire racine du projet** (où se trouve le fichier `docker-compose.yaml`)
- Le fichier `docker-compose.yaml` doit être présent à la racine

#### **Démarrer l'environnement base de données+redis+backend**

```bash
# Se placer dans le répertoire racine du projet
cd ocr-P13-poc-chat

# Démarrer tous les services avec le fichier docker-compose.yaml (nom de projet: ocr-p13-poc-chat)
docker-compose -f docker-compose.yaml -p ocr-p13-poc-chat up -d --build

```

## Services disponibles

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| Backend | 8083 | http://localhost:8083 | API REST + WebSocket |
| PostgreSQL | 5432 | localhost:5432 | Base de données |
| Redis | 6379 | localhost:6379 | Cache et sessions |


## Démarrer le frontend
Pour démarrer le serveur de développement Angular local, exécuter dans le terminal :

```bash
# Se placer dans le sous-répertoire du frontend
cd .\frontend\
npm run start
```

## Ouvrir deux sessions dans deux browsers différents 

- copier l'url : http://localhost:4200/
- la première session se connecte en tant que CLIENT
  - le CLIENT doit `Créer un ticket et démarrer le chat`
  - Entrer un premier message
- la seconde session se connecte en tant que AGENT
  - l'AGENT clique sur le ticket OPEN
  - Entrer un message pour répondre

> Les messages s'affichent instantanément de part et d'autre.