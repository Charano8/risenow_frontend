# RiseNow Web Application

This is the responsive web version of the RiseNow app, designed to match the mobile experience.

## Prerequisites

- [Node.js](https://nodejs.org/) (includes npm) installed on your computer.

## Getting Started

1. Open your terminal (Command Prompt, PowerShell, or Terminal).
2. Navigate to this directory:
   ```bash
   cd "C:\Users\dlnko\AndroidStudioProjects\RiseNow\web"
   ```
3. Install the development server:
   ```bash
   npm install
   ```
4. Start the application:
   ```bash
   npm start
   ```
5. The application will be running at:
   [http://localhost:3000](http://localhost:3000)

## Features

- **Responsive Design**: Works on Desktop, Mobile, and Tablet browsers.
- **Backend Sync**: Connects to the Flask backend at `http://127.0.0.1:5000`.
- **Identity Matching**: Uses the same colors and glassmorphism theme as the mobile app.

## Project Structure

- `index.html`: Main application entry and UI structure.
- `style.css`: Theming and responsive layouts.
- `app.js`: Application logic and API integration.
- `package.json`: Server configuration.
