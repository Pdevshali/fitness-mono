# 🏋️ Fitness Mono — AI-Powered Fitness Tracking API

A **monolithic Spring Boot REST API** that helps users track their fitness activities and receive personalized AI-generated recommendations using the **Groq AI** (LLaMA 3.3 70B model). The application features JWT-based authentication, progressive fitness profile building, and intelligent coaching that gets smarter as more data is logged.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Data Models](#-data-models)
- [API Endpoints](#-api-endpoints)
- [Setup & Configuration](#-setup--configuration)
- [Environment Variables](#-environment-variables)
- [Running the Application](#-running-the-application)
- [API Usage Examples](#-api-usage-examples)
- [Security Architecture](#-security-architecture)
- [AI Recommendation Engine](#-ai-recommendation-engine)
- [Swagger / OpenAPI Docs](#-swagger--openapi-docs)

---

## ✨ Features

- 🔐 **JWT Authentication** — Stateless token-based security (register + login)
- 🏃 **Activity Tracking** — Log workouts with optional detailed metrics (distance, heart rate, sets/reps, etc.)
- 🤖 **AI Recommendations** — Personalized fitness coaching powered by Groq's LLaMA 3.3 70B model
- 📊 **Activity Analysis** — Detects consistency patterns, favorite activity types, and average duration over the last 30 days
- 👤 **Progressive User Profiles** — Start with just email + name; add fitness level, goals, weight, and height later
- 📄 **Swagger UI** — Interactive API documentation available out of the box
- 🛡️ **Role-Based Access Control** — `USER` and `ADMIN` roles enforced via Spring Security
- ♻️ **Auto-Retry on Rate Limits** — Groq API calls retry up to 3 times with exponential backoff

---

## 🛠 Tech Stack

| Category         | Technology                          |
|-----------------|-------------------------------------|
| Language         | Java 21                             |
| Framework        | Spring Boot 4.0.2                   |
| Security         | Spring Security + JWT (JJWT 0.13)   |
| Persistence      | Spring Data JPA + Hibernate         |
| Database         | MySQL                               |
| AI Integration   | Groq API (OpenAI-compatible format) |
| AI Model         | LLaMA 3.3 70B Versatile             |
| API Docs         | SpringDoc OpenAPI 3 (Swagger UI)    |
| Build Tool       | Maven                               |
| Utilities        | Lombok, Jackson, Bean Validation    |

---

## 📁 Project Structure

```
fitness-mono/
├── src/main/java/com/pdev/fitnessMono/
│   ├── FitnessMonoApplication.java          # Entry point
│   │
│   ├── config/
│   │   └── OpenApiConfig.java               # Swagger / OpenAPI configuration
│   │
│   ├── controller/
│   │   ├── AuthController.java              # POST /api/auth/register, /login
│   │   ├── ActivityController.java          # POST /api/activities, GET /api/activities/user/{userId}
│   │   └── RecommendationController.java    # POST /api/recommendations/generate, GET endpoints
│   │
│   ├── dtos/
│   │   ├── ActivityRequest.java             # Input: create activity
│   │   ├── ActivityResponse.java            # Output: activity data
│   │   ├── AiResponse.java                  # Output: AI recommendations (improvements, suggestions, safety)
│   │   ├── LoginRequest.java                # Input: email + password
│   │   ├── LoginResponse.java               # Output: JWT token + user info
│   │   ├── RecommendationsRequest.java      # Input: userId
│   │   ├── RegisterRequest.java             # Input: registration fields
│   │   └── UserResponse.java               # Output: user profile data
│   │
│   ├── exception/
│   │   └── GlobalExceptionHandler.java      # Centralized error handling
│   │
│   ├── model/
│   │   ├── User.java                        # JPA entity: fitness_user table
│   │   ├── Activity.java                    # JPA entity: activity table (metrics/context as JSON)
│   │   ├── Recommendations.java             # JPA entity: AI recommendation records
│   │   ├── ActivityContext.java             # Embedded object: intensity, RPE, notes
│   │   ├── ActivityMetrics.java             # Embedded object: distance, HR, steps, etc.
│   │   ├── ActivityType.java                # Enum: RUNNING, CYCLING, SWIMMING, etc.
│   │   ├── FitnessGoal.java                 # Enum: WEIGHT_LOSS, MUSCLE_GAIN, etc.
│   │   ├── FitnessLevel.java                # Enum: BEGINNER, INTERMEDIATE, ADVANCED
│   │   ├── IntensityLevel.java              # Enum: LOW, MODERATE, HIGH, VERY_HIGH
│   │   └── UserRole.java                    # Enum: USER, ADMIN
│   │
│   ├── repository/
│   │   ├── ActivityRepository.java          # findTop5ByUserIdOrderByCreatedAtDesc, findByUserId
│   │   ├── RecommendationsRepository.java
│   │   └── UserRepository.java
│   │
│   ├── security/
│   │   ├── CustomUserDetailsService.java    # Loads user by email for Spring Security
│   │   ├── JwtAuthFilter.java              # Validates JWT on every request
│   │   ├── JwtUtils.java                   # Token generation & parsing
│   │   └── SecurityConfig.java             # Security filter chain + BCrypt config
│   │
│   └── service/
│       ├── ActivityAnalysisService.java     # Computes 30-day stats for AI context
│       ├── AiService.java                   # Calls Groq API, builds prompts, parses responses
│       ├── ActivityService.java / Impl      # Business logic: create & fetch activities
│       ├── RecommendationsService.java / Impl # Orchestrates AI call + saves recommendations
│       └── UserService.java / Impl          # Registration, login, profile mapping
│
└── src/main/resources/
    └── application.properties               # All configuration (uses env variables)
```

---

## 📊 Data Models

### User (`fitness_user` table)

| Field          | Type         | Required | Default           | Notes                    |
|----------------|--------------|----------|-------------------|--------------------------|
| `id`           | String (UUID)| Auto     | —                 | Primary key              |
| `email`        | String       | ✅       | —                 | Unique                   |
| `password`     | String       | ✅       | —                 | BCrypt hashed            |
| `firstName`    | String       | ✅       | —                 |                          |
| `lastName`     | String       | ✅       | —                 |                          |
| `weight`       | Double       | ❌       | null              | In kilograms             |
| `height`       | Double       | ❌       | null              | In centimetres           |
| `fitnessLevel` | Enum         | ❌       | `BEGINNER`        | BEGINNER / INTERMEDIATE / ADVANCED |
| `primaryGoal`  | Enum         | ❌       | `GENERAL_FITNESS` | See Fitness Goals        |
| `role`         | Enum         | Auto     | `USER`            | USER / ADMIN             |

### Activity (`activity` table)

| Field            | Type         | Required | Notes                                  |
|------------------|--------------|----------|----------------------------------------|
| `id`             | String (UUID)| Auto     | Primary key                            |
| `user` (FK)      | User         | ✅       | Many-to-One                            |
| `type`           | Enum         | ✅       | See Activity Types                     |
| `duration`       | Integer      | ✅       | In minutes                             |
| `caloriesBurned` | Integer      | ❌       | Optional estimate                      |
| `startTime`      | DateTime     | ❌       |                                        |
| `metrics`        | JSON         | ❌       | `ActivityMetrics` (distance, HR, etc.) |
| `context`        | JSON         | ❌       | `ActivityContext` (intensity, RPE)     |

### ActivityMetrics (stored as JSON column)

| Field             | Type    | Used For                         |
|-------------------|---------|----------------------------------|
| `distance`        | Double  | Running, Cycling, Swimming (km)  |
| `averageHeartRate`| Integer | Cardio activities (bpm)          |
| `weightLifted`    | Double  | Strength training (kg)           |
| `sets`            | Integer | Strength training                |
| `reps`            | Integer | Strength training                |
| `steps`           | Integer | Walking, Running                 |
| `notes`           | String  | Free-text notes                  |

### ActivityContext (stored as JSON column)

| Field               | Type   | Description                          |
|---------------------|--------|--------------------------------------|
| `perceivedIntensity`| Enum   | LOW / MODERATE / HIGH / VERY_HIGH    |
| `rpe`               | Integer| Rate of Perceived Exertion (1–10)    |
| `notes`             | String | How they felt, any issues            |

---

## 🌐 API Endpoints

### Auth — `/api/auth`

| Method | Endpoint             | Auth Required | Description             |
|--------|----------------------|---------------|-------------------------|
| POST   | `/api/auth/register` | ❌ Public      | Register a new user     |
| POST   | `/api/auth/login`    | ❌ Public      | Login and receive JWT   |

---

### Activities — `/api/activities`

| Method | Endpoint                        | Auth Required | Description                      |
|--------|---------------------------------|---------------|----------------------------------|
| POST   | `/api/activities`               | ✅ Bearer JWT  | Log a new workout activity       |
| GET    | `/api/activities/user/{userId}` | ✅ Bearer JWT  | Get all activities for a user    |

---

### Recommendations — `/api/recommendations`

| Method | Endpoint                                  | Auth Required | Description                                |
|--------|-------------------------------------------|---------------|--------------------------------------------|
| POST   | `/api/recommendations/generate`           | ✅ Bearer JWT  | Generate AI recommendations for a user     |
| GET    | `/api/recommendations/user/{userId}`      | ✅ Bearer JWT  | Fetch all recommendations for a user       |
| GET    | `/api/recommendations/activity/{activityId}` | ✅ Bearer JWT | Fetch recommendations for a specific activity |

---

## ⚙️ Setup & Configuration

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8+
- A free [Groq API key](https://console.groq.com) (for AI recommendations)

### Database Setup

Create a MySQL database:

```sql
CREATE DATABASE fitness_db;
```

Hibernate auto-creates the tables on first run (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🔑 Environment Variables

The application reads all secrets from environment variables. Set the following before running:

| Variable       | Description                                      | Example                                  |
|----------------|--------------------------------------------------|------------------------------------------|
| `DB_URL`       | JDBC URL for your MySQL database                 | `jdbc:mysql://localhost:3306/fitness_db` |
| `DB_USER`      | MySQL username                                   | `root`                                   |
| `DB_PASS`      | MySQL password                                   | `yourpassword`                           |
| `GROK_API_KEY` | Groq API key from [console.groq.com](https://console.groq.com) | `gsk_xxxxx`             |
| `JWT_SECRET`   | Secret key for signing JWTs (min 32 chars)       | `mySecretKey1234567890abcdef123456`       |

> **Windows (PowerShell):**
> ```powershell
> $env:DB_URL="jdbc:mysql://localhost:3306/fitness_db"
> $env:DB_USER="root"
> $env:DB_PASS="yourpassword"
> $env:GROK_API_KEY="gsk_xxxxx"
> $env:JWT_SECRET="mySecretKey1234567890abcdef123456"
> ```

---

## 🚀 Running the Application

**Using Maven Wrapper:**

```bash
./mvnw spring-boot:run
```

**On Windows:**

```cmd
mvnw.cmd spring-boot:run
```

**Or build a JAR and run it:**

```bash
./mvnw clean package -DskipTests
java -jar target/fitness-mono-0.0.1-SNAPSHOT.jar
```

The application starts on **`http://localhost:8080`** by default.

---

## 📝 API Usage Examples

### 1. Register a User

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "jane.smith@example.com",
  "password": "fitness2024",
  "firstName": "Jane",
  "lastName": "Smith",
  "weight": 65.5,
  "height": 170.0,
  "fitnessLevel": "INTERMEDIATE",
  "primaryGoal": "WEIGHT_LOSS"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-...",
  "email": "jane.smith@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "fitnessLevel": "INTERMEDIATE",
  "primaryGoal": "WEIGHT_LOSS",
  "createdAt": "2024-01-15T10:00:00"
}
```

---

### 2. Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "jane.smith@example.com",
  "password": "fitness2024"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { ... }
}
```

> Use the `token` in subsequent requests as: `Authorization: Bearer <token>`

---

### 3. Log an Activity (minimal)

```http
POST /api/activities
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-...",
  "type": "RUNNING",
  "duration": 30
}
```

---

### 4. Log an Activity (detailed)

```http
POST /api/activities
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-...",
  "type": "WEIGHTLIFTING",
  "duration": 45,
  "caloriesBurned": 200,
  "startTime": "2024-01-15T18:00:00",
  "metrics": {
    "weightLifted": 80.0,
    "sets": 4,
    "reps": 12,
    "notes": "Bench press workout"
  },
  "context": {
    "perceivedIntensity": "HIGH",
    "rpe": 8,
    "notes": "Felt strong today!"
  }
}
```

---

### 5. Generate AI Recommendations

```http
POST /api/recommendations/generate
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-..."
}
```

**Response:**
```json
{
  "improvements": [
    "Increase your weekly run frequency to build aerobic base",
    "Add a rest day between weight sessions for recovery"
  ],
  "suggestions": [
    "Try interval running (HIIT) to boost calorie burn",
    "Incorporate yoga sessions for flexibility and recovery"
  ],
  "safety": [
    "Warm up for at least 5 minutes before each session",
    "Stay hydrated — aim for 2–3 litres of water daily"
  ]
}
```

---

## 🔐 Security Architecture

```
Request → JwtAuthFilter → SecurityFilterChain → Controller
```

1. **`JwtAuthFilter`** — Intercepts every request, extracts and validates the JWT from the `Authorization: Bearer ...` header.
2. **`CustomUserDetailsService`** — Loads user by email from the database for Spring Security context.
3. **`SecurityConfig`** — Configures route-level access:
   - `/api/auth/**` → **Public** (no token needed)
   - `/swagger-ui/**`, `/v3/api-docs/**` → **Public**
   - `/api/admin/**` → **ADMIN role only**
   - All other routes → **Authenticated** (valid JWT required)
4. **`JwtUtils`** — Issues tokens with `userId` and `role` claims. Token validity: **48 hours** (172800000 ms).
5. **Passwords** — Stored using **BCrypt** hashing.

---

## 🤖 AI Recommendation Engine

The AI pipeline works in three steps:

```
Request (userId)
    │
    ├─► Fetch User Profile (fitness level, goal, weight, height)
    ├─► Fetch last 5 Activities from DB
    ├─► ActivityAnalysisService → 30-day stats:
    │       - Total activities
    │       - Most frequent activity type
    │       - Average session duration
    │       - Consistency check (≥3/week = consistent)
    │
    └─► Build Prompt → Call Groq API (LLaMA 3.3 70B)
            │
            └─► Parse JSON response → AiResponse
                    {improvements, suggestions, safety}
```

**Retry Logic:** If the Groq API returns a `429 Too Many Requests`, the service automatically retries up to **3 times** with exponential backoff (2s, 4s, 6s).

**Progressive Intelligence:**
- **Day 1** — Generic advice based on activity type only
- **Week 1** — Considers recent activity patterns
- **Month 1** — Personalised with full user profile data
- **Ongoing** — Gets smarter with every logged workout

---

## 📚 Swagger / OpenAPI Docs

Interactive API documentation is available once the app is running:

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI (interactive) |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON spec |

> No authentication is required to access the Swagger UI.

---

## 🏷️ Enums Reference

### Activity Types
`RUNNING` · `CYCLING` · `SWIMMING` · `WEIGHTLIFTING` · `YOGA` · `WALKING` · `HIIT` · `CARDIO` · `STRENGTH_TRAINING` · `OTHER`

### Fitness Levels
`BEGINNER` · `INTERMEDIATE` · `ADVANCED`

### Fitness Goals
`WEIGHT_LOSS` · `MUSCLE_GAIN` · `ENDURANCE` · `STRENGTH` · `GENERAL_FITNESS` · `HEALTH_MAINTENANCE`

### Intensity Levels (for ActivityContext)
`LOW` · `MODERATE` · `HIGH` · `VERY_HIGH`

---

## 🧑‍💻 Author

**Pdev** — `com.pdev`

---

*Built with ❤️ using Spring Boot 4, Groq AI, and MySQL.*
