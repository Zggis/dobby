#!/bin/bash
echo "---Checking for 'runtime' folder---"
java -jar -Dspring.profiles.active=docker ./app.jar
