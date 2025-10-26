# 💌 Skye's Portfolio Backend

This is the backend service for **Skye's personal portfolio**.  
Its primary role is to act as a **secure, stateless email microservice**.  
It handles the “Contact Me” form by receiving visitor data, authenticating the request, and securely sending the message to Skye’s inbox via **Mailgun**.

I did this with so much love, I hope this improve your website experience! Sweetie, I love you <3.

---

## ⚙️ Features

- 🔒 **Secure API Endpoint:** A single `POST /api/contact` endpoint for all contact form submissions.  
- 🔑 **API Key Authentication:** Protected by a custom `ApiKeyAuthFilter`, requiring a secret `X-CLIENT-KEY` header.  
- 🌐 **Strict CORS Policy:** Only accepts requests from `luvrksnsnskyedev.space`, blocking all other origins.  
- ⚡ **Reactive & Asynchronous:** Built with **Spring WebFlux** (Project Reactor) for a non-blocking, high-performance architecture.  
- 🐳 **Container-Ready:** Includes a multi-stage `Dockerfile` for a small, secure, and efficient production image.  
- 🧩 **Centralized Configuration:** Uses a single `application.yml` populated by environment variables, following the **12-Factor App** methodology.  

---

## 🛠️ Tech Stack

- ☕ **Java 21**  
- 🌱 **Spring Boot 3** (WebFlux, Security, R2DBC)  
- ⚛️ **Project Reactor** (Reactive Programming)  
- 🧠 **Neon Tech** (Serverless PostgreSQL)  
- 📬 **JavaMailSender** (Mailgun SMTP integration)  
- 🔄 **MapStruct** (DTO mapping)  
- 🐋 **Docker**  
- ☁️ **Deployed on Render**

---

## 🚀 Configuration & Deployment

This project uses a single `application.yml` file that relies **entirely on environment variables**.  
There are no separate `dev` or `prod` profiles — the environment dictates the configuration.

To run the app (locally with Docker or in production on Render), you **must** provide the following environment variables:

| Environment Variable | Example Value | Description |
| :------------------- | :------------- | :----------- |
| `DB_URL` | `r2dbc:postgresql://ep-sweet-queen...` | The full R2DBC connection string from Neon. |
| `DB_USER` | `usernamedb` | The database username. |
| `DB_PASSWORD` | `(secret)` | The database password. |
| `ICLOUD_SKYE_EMAIL` | `skyemail` | Destination email address (Skye's inbox). |
| `MAILGUN_DOMAIN` | `usernamemailing@sandbox0d2d...` | Mailgun domain/SMTP username. |
| `MAILGUN_PASSWORD` | `(secret)` | Mailgun SMTP password. |
| `PORT` | `8080` | Port on which the app will run. |
| `ADDRESS` | `0.0.0.0` | Bind address (use `0.0.0.0` in containers). |
| `API_KEY_BACKEND` | `testkey` | Secret API key for the `X-CLIENT-KEY` header. |
| `LEVEL_LOG` | `DEBUG` | Log level (`DEBUG`, `INFO`, `WARN`). |

---

### 🐳 1. Running with Docker (Recommended)

This is the easiest way to run the app locally — and the same way it runs in production.

1. **Create an `.env` file** (e.g., `local.env`) in the project root with all the variables above.
2. **Build the Docker image:**

   ```bash
   docker build -t skye-backend .
   ```

3. **Run the container** using your `.env` file:

   ```bash
   docker run --env-file ./local.env -p 8080:8080 skye-backend
   ```

   *Note: Ensure `PORT=8080` in the `.env` file to match the `-p` flag.*

---

### ☁️ 2. Deployment on Render

Render automatically builds and deploys the service using the `Dockerfile`.

1. Push the code to GitHub or GitLab.  
2. Create a new **Web Service** on Render and point it to your repository.  
3. In the **Environment** tab, add all the environment variables listed above.  
4. Render will build the image, inject the variables, and start your service.  

---

## 🔒 API Endpoint

### `POST /api/contact`

Sends a new contact message to Skye.

**Required Header:**

- `X-CLIENT-KEY: <your_api_key_backend_value>`

**Request Body (`application/json`):**

```json
{
  "email": "visitor@example.com",
  "username": "A Curious Visitor",
  "topic": "Love your portfolio!",
  "message": "Hi Skye, your work is amazing. Let's connect!"
}
```

**Example cURL Request:**

```bash
curl -X POST 'http://localhost:8080/api/contact'   -H "Content-Type: application/json"   -H "X-CLIENT-KEY: testkey"   -d '{
    "email": "Regre@example.com",
    "username": "Regre",
    "topic": "cURL Test",
    "message": "This is a test message from ur bf who loves you! from the command line."
  }'
```

---

## 🧭 Roadmap (Future Improvements)

The current `MailService` handles errors by logging them.  
The next major feature is to implement a **robust retry mechanism** using the database.

### 🕒 Email Failure Retry Logic (Cron Job)

1. **Database Schema:**  
   Create a `failed_emails` table in Neon to store messages that fail to send.  

2. **Update Error Handling:**  
   Modify the `MailService`’s `onErrorResume` block — instead of just logging errors, it should store failed messages in the database.  

3. **Implement Scheduler:**  
   Add a new `@Scheduled` method (`EmailRetryScheduler`) to run periodically (e.g., every hour).  
   This will:
   - Query unsent emails  
   - Attempt to resend them  
   - Delete them on success or increment a `retry_count` on failure  
