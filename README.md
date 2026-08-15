# Banking Software Backend

Training project with Spring Boot.

## Prerequisites

- **Java 17** (required — the build targets Java 17; Lombok fails silently under newer JDKs)
- **Docker** (for the MySQL container)
- **Maven wrapper** (included — use `./mvnw`, no global Maven install needed)

## Local setup

### 1. Start MySQL

The app uses a MySQL container defined in `docker-compose.yml`. It reads credentials
from environment variables, so create a `.env` file in the repo root first:

```env
MYSQL_ROOT_PASSWORD=your-root-password
MYSQL_DATABASE=bank
MYSQL_USER=bank
MYSQL_PASSWORD=your-password
MYSQL_PORT=3307
```

Then start the container:

```bash
docker compose up -d
```

### 2. Configure the secrets profile

The app activates the `secrets` profile (see `src/main/resources/application.properties`),
which holds credentials, JWT/RSA keys, and mail config. Create
`src/main/resources/application-secrets.properties` locally — it is gitignored and must
never be committed. At minimum it needs the database credentials and the RSA keys:

```properties
# datasource
spring.datasource.url=jdbc:mysql://localhost:${MYSQL_PORT}/bank
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}

# RSA keys (used to sign/verify JWTs)
rsa.public-key=classpath:certs/public.pem
rsa.private-key=classpath:certs/private.pem

# mail (optional)
spring.mail.host=localhost
spring.mail.port=3025
spring.mail.username=
spring.mail.password=
```

Place the RSA keypairs under `src/main/resources/certs/` (`public.pem` and `private.pem`).
See `.gitignore` — `certs/*.pem` is excluded from version control.

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The app starts on **port 9001**: `http://localhost:9001`

### 4. Run the tests

Tests run on an in-memory H2 database with the `test` profile — no MySQL, secrets, or
Flyway needed:

```bash
./mvnw test
```

CI runs the same command on every pull request (see `.github/workflows/ci.yml`).

## Auth: user vs customer

The API distinguishes two actor types, each with its own auth endpoint. Both return a
JWT; pass it as `Authorization: Bearer <token>` on protected endpoints.

| Actor     | Login endpoint                          | Notes                                       |
|-----------|-----------------------------------------|---------------------------------------------|
| User      | `POST /api/v1/auth/authenticate`        | Staff/internal user; can create customers   |
| Customer  | `POST /api/v1/customer-auth/authenticate` | End-user; manages own accounts/transactions |

Both accept the same body:

```json
{
  "username": "your-username",
  "password": "your-password"
}
```

### Login as a user

```bash
curl -X POST http://localhost:9001/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin-pass"}'
```

### Login as a customer

```bash
curl -X POST http://localhost:9001/api/v1/customer-auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "cust1", "password": "cust-pass"}'
```

## Example flow: create a transaction

1. **Log in as a customer** and capture the token:

```bash
TOKEN=$(curl -s -X POST http://localhost:9001/api/v1/customer-auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "cust1", "password": "cust-pass"}' | jq -r '.token')
```

2. **Find an account UUID** (the customer's own accounts):

```bash
curl -s http://localhost:9001/api/v1/customer-accounts \
  -H "Authorization: Bearer $TOKEN"
```

3. **Create a transaction** (e.g. a deposit):

```bash
curl -X POST http://localhost:9001/api/v1/accounts/{accountUUID}/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "transactionType": "DEPOSIT",
    "description": "top up"
  }'
```

Replace `{accountUUID}` with the UUID from step 2. `transactionType` is one of
`DEPOSIT`, `WITHDRAWAL`, `PAYMENT`, `TRANSFER`. Amount must be
positive; withdrawals/payments/transfers are rejected with a `400` if funds are
insufficient.

**Currency conversion** is a two-leg operation via a dedicated endpoint — it debits
the source account by `amount` and credits the target account by `amount × exchangeRate`:

```bash
curl -X POST http://localhost:9001/api/v1/accounts/{sourceAccountUUID}/transactions/currency-conversion \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "targetAccountUUID": "{targetAccountUUID}",
    "amount": 100.00,
    "exchangeRate": 1.10
  }'
```

Both accounts must belong to the authenticated customer; the exchange rate is
caller-provided.

## Known gotchas

- **Customer temporary password**: a customer created via `POST /api/v1/customers` gets a
  randomly generated temporary password, returned **once** in the creation response as
  `temporaryPassword` (`username` defaults to their email). Change it after first login via
  `PUT /api/v1/customer-auth/update-my-credentials`.
- **JDK mismatch**: building with anything other than Java 17 produces `cannot find
  symbol` errors on all Lombok-generated methods. Install Java 17 and point `JAVA_HOME`
  at it.
- **Secrets missing**: the app will not boot without `application-secrets.properties`
  and the RSA certs.
- **Database schema**: managed by Flyway (`src/main/resources/db/migration`). Never edit
  an applied migration — add a new `V{N+1}__` file instead.
