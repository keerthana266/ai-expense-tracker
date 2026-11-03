@echo off
title ExpenseAI - One-Click Launcher
echo ========================================================
echo        ExpenseAI - Advanced AI Expense Tracker
echo ========================================================
echo Starting Backend (Port 8080) and Frontend (Port 5173)...
echo.

start "ExpenseAI Backend" "%~dp0start_backend.bat"
timeout /t 3 >nul

start "ExpenseAI Frontend" "%~dp0start_frontend.bat"
timeout /t 4 >nul

start http://localhost:5173

echo ========================================================
echo ExpenseAI is running!
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080
echo Simulator: Run 'simulate_expense_ai.py' to test SMS/UPI
echo ========================================================
