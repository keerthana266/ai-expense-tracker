@echo off
title ExpenseAI Backend (Spring Boot)
cd /d "%~dp0backend"
echo ========================================================
echo        ExpenseAI - Spring Boot Backend (Port 8080)
echo ========================================================
echo Database: Resilient Embedded Storage (Starts at 0)
echo Kafka: Safe Resilient Mode (Offline-tolerant)
echo Gemini AI: Active (Auto Categorization & Roast)
echo ========================================================
cmd /c mvnw.cmd spring-boot:run
pause
