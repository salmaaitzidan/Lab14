# SecureStorage

Application Android (Java) — persistance locale sécurisée.

## Fonctionnalités

| Stockage | Classe | Usage |
|---|---|---|
| SharedPreferences | `AppPrefs` | Nom, langue, thème |
| EncryptedSharedPreferences | `SecurePrefs` | Token chiffré AES256-GCM |
| Fichiers internes | `InternalTextStore` | Texte UTF-8 |
| Fichiers internes JSON | `StudentsJsonStore` | Liste étudiants |
| Cache | `CacheStore` | Données temporaires |
| Externe app-specific | `ExternalAppFilesStore` | Export contrôlé |

## Architecture

```
com.securestorage.app
├── ui/           MainActivity.java
├── prefs/        AppPrefs.java · SecurePrefs.java
├── files/        InternalTextStore.java · StudentsJsonStore.java
├── cache/        CacheStore.java
├── external/     ExternalAppFilesStore.java
└── model/        Student.java
```

## Prérequis

- Android Studio Hedgehog ou supérieur
- API 26+ (Android 8.0)
- Java 11

## Installation

1. Cloner le dépôt
```bash
git clone https://github.com/<compte>/SecureStorageSharedPref.git
```
2. **File → Open** → sélectionner le dossier
3. Sync Gradle → **Run**

## Sécurité

- Token jamais loggé (longueur uniquement)
- `MODE_PRIVATE` pour tous les fichiers internes
- `EncryptedSharedPreferences` + `MasterKey` (Keystore Android)
- Nettoyage complet via **Effacer tout**

## Device File Explorer

```
/data/data/com.securestorage.app/
├── files/
│   ├── students.json
│   └── note.txt
├── cache/
│   └── last_ui.txt
└── shared_prefs/
    ├── app_prefs.xml
    └── secure_prefs.xml  ← chiffré
```
