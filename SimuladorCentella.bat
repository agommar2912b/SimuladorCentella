@echo off
echo Ejecutando SimuladorCentella.bat
if exist gradlew (
    call gradlew bootJar
) else (
    call gradle bootJar
)
for %%f in (build\libs\*-SNAPSHOT.jar) do (
    set JAR_FILE=%%f
    goto :found
)
:found
echo Ejecutando: java -jar %JAR_FILE%
java -jar "%JAR_FILE%"
pause