#!/bin/bash
echo "Ejecutando SimuladorCentella.sh"
set -e
if [ -f "./gradlew" ]; then
  ./gradlew build
else
  gradle build
fi
JAR_FILE=$(ls build/libs/*.jar | head -n 1)
echo "Ejecutando: java -jar $JAR_FILE"
java -jar "$JAR_FILE"
read -p "Presiona enter para salir"