----------- Projet de Jeu avec Sockets -----------
Ce projet de programmation Java implémente un jeu utilisant des sockets pour permettre la communication entre un serveur et des clients. Le jeu prend en charge la création d'un personnage, le combat entre un personnage et un robot ou un système de joueur contre joueur (PVP)


----------- Classes -----------
Le projet est constitué des classes suivantes :

1. Server
La classe Server est responsable de la gestion du serveur. Elle écoute les connexions des clients et lance un menu dès qu'un joueur sont connectés. Le serveur assure aussi la mise en commun de deux sockets si les clients souhaitent faire un combat client vs client.

2. Fight
La classe Fight représente un combat contre un robot ou un joueur. Elle gère les personnages, les tours de chaque personne,les attaques. La classe Fight permet donc de gérer toutes le gameplay.

3. Client
La classe Client est utilisée pour créer des clients qui se connectent au serveur. Chaque client représente un joueur. Il envoie les actions du joueur au serveur et reçoit les mises à jour de l'état du jeu.

4. Character
La classe Character définit le personnage de chaque jouer, elle en permet la création, leurs statistiques et aussi la capacité à montée de niveau et de fait, l'augmentation d'une statistique de manière aléatoire. De plus, le robot peut aussi être créé.

5. GameMenu
La classe GameMenu est tout simplement une classe qui permet à chaque client de choisir ce qu'il souhaite faire.

----------- Instructions d'exécution -----------
Pour exécuter le programme, suivez les étapes suivantes :

- Ouvrez votre IDE et ouvrez le dossier.
- Assurez-vous que les fichiers sont tous dans le même package (répertoire) de votre projet.
- Compilez les fichiers source si nécessaire (certains IDE le font automatiquement).

Pour exécuter le serveur :

- Ouvrez la classe Server dans l'éditeur.
- Modifier le port dans le code si nécessaire par défaut : 5000 (ne pas oublier de mettre le même dans la classe Client)
- Recherchez l'option d'exécution et cliquez dessus (par exemple, un bouton d'exécution verte ou un menu contextuel).

Pour exécuter un client :

- Ouvrez la classe Client dans l'éditeur.
- Recherchez l'option d'exécution.
- Avant de lancer le client, assurez-vous que le serveur est déjà en cours d'exécution.
- Cliquez sur l'option d'exécution pour démarrer un client.

Répétez l'étape précédente pour chaque client que vous souhaitez lancer.


----------- A savoir -----------

Si un joueur ne joue pas ou se déconnecte pendant 15 secondes, le joueur adverse sera informé qu'il a gagné la partie.