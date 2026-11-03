# ExpenseAI 💰

**ExpenseAI** is an AI-powered expense tracker that helps users manage their spending and understand their financial habits.

## Features

- **Automatic Expense Tracking:** Reads payment notifications and records expenses using AI.
- **AI Roast Mode:** Gives funny warnings about overspending.
- **Broke Date Prediction:** Predicts when your money might run out.
- **Dashboard:** View expenses, budgets, and spending insights.

## Tech Stack

- Frontend: React, Vite, JavaScript
- Backend: Java, Spring Boot
- Database: PostgreSQL
- AI: Google Gemini API
- Mobile: Android, Kotlin
- Tools: Git, GitHub, Maven

## Getting Started

1. Clone the repository.
2. Set up PostgreSQL and create the database `ai-expense-tracker`.
3. Configure your database credentials and Gemini API key.
4. Start the backend:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

5. Start the frontend in a new terminal:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

## Project Goal

To make expense tracking smarter, easier, and more personalized with AI.

**Note:** Broke Date Prediction provides estimates based on available spending data.