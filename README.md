# FemFort - Safe Campus Navigation 🛡️

![FemFort Banner](https://via.placeholder.com/1200x300?text=FemFort+Safe+Campus+Navigation)

**FemFort** is a smart, crowdsourced safety navigation web application designed for the **WIE-HackEarth 2025** hackathon. It empowers women and campus residents to make informed decisions about their routes by providing real-time safety ratings and hazard alerts.

## 🚀 Problem Statement
Navigating large campuses or urban areas at night can be intimidating. Existing maps provide the shortest route but often ignore safety factors like lighting, crowd density, and ongoing construction. FemFort bridges this gap by integrating community-driven safety data into route planning.

## ✨ Features

-   **📍 Safe Route Finder**: Calculates the optimal path between zones using a custom algorithm that balances distance with a safety score.
-   **⭐ Crowdsourced Ratings**: Users can rate zones on a scale of 1-5 and leave comments about their safety experience.
-   **🗺️ Live Safety Map**: Visualizes safe (green), moderate (orange), and high-risk (red) zones instantly.
-   **⚠️ Hazard Reporting**: Users can report specific threats like "Poor Lighting" or "Overcrowded Areas".
-   **🛡️ Admin Dashboard**: Comprehensive tools for moderators to flag spam reviews, manage zones, and view safety hotspots.
-   **🆘 Emergency Support**: Quick access to nearby support centers and emergency contacts.

## 🛠️ Tech Stack

-   **Backend**: Java 17 (Jakarta EE Servlets), Jetty 11, Gson
-   **Database**: PostgreSQL 14+
-   **Frontend**: HTML5, CSS3, Vanilla JavaScript, Leaflet.js
-   **Build Tool**: Maven
-   **IDE Config**: Eclipse / VS Code (Java Extension Pack)

## 📂 Project Structure

```
femfort/
├── src/
│   ├── main/
│   │   ├── java/com/femfort/
│   │   │   ├── dao/          # Database Access (JDBC)
│   │   │   ├── model/        # Data Models (POJOs)
│   │   │   ├── service/      # Business Logic (Routing, Scoring)
│   │   │   ├── servlet/      # REST API Endpoints
│   │   │   └── util/         # Utilities
│   │   ├── resources/
│   │   │   ├── schema.sql    # Database Schema
│   │   │   └── seed_data.sql # Initial Seed Data
│   │   └── webapp/
│   │       ├── css/          # Stylesheets
│   │       ├── js/           # Client-side Logic
│   │       ├── index.html    # Landing Page
│   │       ├── map.html      # Main Application
│   │       └── admin.html    # Admin Dashboard
├── pom.xml                   # Maven Dependencies
└── README.md                 # Documentation
```

## 🏁 Getting Started

### Prerequisites
-   **Java JDK 17** or higher
-   **Maven 3.8+**
-   **PostgreSQL** installed and running

### Installation

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/yourusername/femfort.git
    cd femfort
    ```

2.  **Database Setup**
    Create a PostgreSQL database named `femfortdb`.
    ```sql
    CREATE DATABASE femfortdb;
    ```
    *Note: The application defaults to user `postgres` and password `123456`. Update `src/main/java/com/femfort/dao/DatabaseConnectionManager.java` if your credentials differ.*

3.  **Build and Run**
    Use the Jetty Maven plugin to start the server.
    ```bash
    mvn jetty:run
    ```
    *The application will automatically initialize the database tables and seed data on the first run.*

4.  **Access the App**
    -   **Home**: [http://localhost:8080/femfort/](http://localhost:8080/femfort/)
    -   **Admin Panel**: [http://localhost:8080/femfort/admin.html](http://localhost:8080/femfort/admin.html)
        -   **Username**: `admin`
        -   **Password**: `admin123`

## 📡 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/zones` | Get all zones with current safety scores |
| `GET` | `/api/routes` | Calculate safest path (params: `startId`, `endId`) |
| `POST` | `/api/ratings` | Submit a new safety rating |
| `PUT` | `/api/ratings` | Moderate a rating (Admin only) |
| `POST` | `/api/auth/login` | Admin authentication |
