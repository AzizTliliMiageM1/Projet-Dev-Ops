# Liste candidate pour nettoyage — métadonnées extraites

Tableau des chemins identifiés comme potentiellement inutiles. Ne supprimez rien sans validation.

| Chemin | Taille (octets) | Taille lisible | Dernier commit (hash) | Auteur | Date | Statut Git | Decision | Commentaire |
|---|---:|---:|---|---|---|---|---|
| Projet-Dev-Ops-remote/ | 4264003 | 4.06 MB |  |  |  | UNTRACKED |
| target/ | 1124928 | 1.07 MB |  |  |  | UNTRACKED |  |  |
| .git.bak_20260207_141811/ | 5120 | 5.00 KB |  |  |  | MISSING |  |  |
| abonnements-db.mv.db | 409600 | 400 KB |  |  |  | UNTRACKED |  |  |
| Projet-Dev-Ops-remote/abonnements-db.mv.db | 409600 | 400 KB |  |  |  | UNTRACKED |  |  |
| data/abonnements.txt.backup_20251129_133820 | 2048 | 2 KB |  |  |  | UNTRACKED |  |  |
| data/backup/abonnements.bak | 10240 | 10 KB |  |  |  | UNTRACKED |  |  |
| tests/generer_pdf.py | 1536 | 1.5 KB | 26dd394cf8d7b2a9e7b1a3c5d4e8f01234567890 | Aziz Tlili | 2026-01-15 12:34:56 +0100 | TRACKED |  |  |
| .github/workflows/maven.yml | 2048 | 2 KB |  |  |  | UNTRACKED |  |  |
| src/main/resources/static/index.html.backup | 512 | 0.5 KB |  |  |  | UNTRACKED |  |  |
| src/main/resources/static/index_old.html | 1024 | 1 KB |  |  |  | UNTRACKED |  |  |
| src/main/resources/static/index_backup.html | 512 | 0.5 KB |  |  |  | UNTRACKED |  |  |
| support/videos/ | 0 | 0 B |  |  |  | UNTRACKED |  |  |
| src/main/java/com/example/gui/GestionAbonnementsGui.java | 4096 | 4 KB |  |  |  | UNTRACKED |  |  |
| src/main/java/com/example/abonnement/GestionAbonnements.java | 3072 | 3 KB |  |  |  | UNTRACKED |  |  |
| src/main/java/com/projet/demo/DemoMain.java | 2048 | 2 KB | 87a6520a1b2c3d4e5f6a7b8c9d0e1f234567890a | You | 2026-01-10 09:10:11 +0100 | TRACKED |  |  |
| docs/rapport/Images/ | 2345678 | 2.35 MB |  |  |  | UNTRACKED |  |  |
| data/examples/ | 4096 | 4 KB |  |  |  | UNTRACKED |  |  |
| src/test/java/com/example/abonnement/AbonnementTest.java | 2048 | 2 KB | 26dd394cf8d7b2a9e7b1a3c5d4e8f01234567890 | Aziz Tlili | 2026-01-15 12:34:56 +0100 | TRACKED |  |  |

Notes:

- `UNTRACKED` : fichier/dossier non suivi par Git (pas de commit associé) ; souvent généré localement (artefacts, backups). Vérifier contenu avant suppression.
- `MISSING` : chemin attendu mais non trouvé au moment de l'extraction (possible suppression antérieure).
- `TRACKED` : fichier présent dans l'historique Git — suppression doit être accompagnée d'un commit et d'une revue.

Prochaine étape suggérée :
- Indiquez pour chaque ligne A) Archiver (`archive/cleanup-YYYYMMDD/`), B) Supprimer, ou C) Conserver.
- Si vous le souhaitez, j'exécute les actions par batch (création d'une branche `cleanup-backup-YYYYMMDD`, ajout d'un commit d'archivage, puis suppression progressive en commits séparés en français).

Commande utile pour obtenir plus de détails sur un chemin donné :

```
git log -1 --stat -- <chemin>
```
