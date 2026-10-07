#!/bin/bash
# 1. Nettoyage radical des anciennes classes
rm -rf bin
mkdir -p bin

echo "[Framework] Recherche et compilation globale de tous les fichiers Java..."

# 2. On liste absolument TOUS les fichiers .java du projet dans un fichier temporaire
find . -name "*.java" > sources.txt

# 3. Compilation groupée de tous les fichiers répertoriés
javac -cp "lib/*" -d bin @sources.txt

# On vérifie si javac a échoué
if [ $? -ne 0 ]; then
    echo "❌ Échec de la compilation !"
    rm -f sources.txt
    exit 1
fi

# Nettoyage du fichier temporaire après succès
rm -f sources.txt

# 4. Packaging en framework.jar
jar cvf framework.jar -C bin .
echo "✅ Framework compilé avec succès dans framework.jar !"