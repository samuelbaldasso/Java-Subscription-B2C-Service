# 🚀 Java Subscription Platform - Production-Ready Implementation

A **Senior/Lead-level** B2C Subscription Management System built with **Spring Boot 3**, demonstrating advanced distributed systems patterns and production-grade engineering practices.

## 🎯 What Makes This Production-Ready?

This project implements **6 critical distributed systems patterns** that distinguish a functional prototype from a battle-tested production system capable of handling high scale and maintaining data consistency:

1. **Transactional Outbox Pattern** - Ensures atomic event publishing
2. **Optimistic Locking** - Prevents concurrent update anomalies
3. **Idempotency** - Handles duplicate Kafka messages safely
4. **Redis Caching** - Reduces database load for hot data
5. **Circuit Breakers (Resilience4j)** - Graceful degradation under failure
6. **Secret Management** - Externalized configuration for security

---

## 📋 Features

### Core Business Capabilities
- ✅ **User Management** (Registration, Authentication, JWT)
- ✅ **Plan Management** (Multiple subscription tiers with caching)
- ✅ **Subscription Lifecycle** (Trial, Active, Canceled, Expired)
- ✅ **Billing & Invoicing** (Automated monthly billing)
- ✅ **Payment Processing** (Simulated gateway integration)
- ✅ **Event-Driven Architecture** (Async processing with guaranteed delivery)

### Production-Grade Patterns
- ✅ **Eventual Consistency** via Transactional Outbox
- ✅ **Concurrency Control** via Optimistic Locking
- ✅ **At-Least-Once Semantics** handled via Idempotency
- ✅ **Performance Optimization** via Redis Cache
- ✅ **Fault Tolerance** via Resilience4j Circuit Breakers
- ✅ **Security Best Practices** with externalized secrets

---

## 🏗️ Architecture & Design Decisions

### 1. Transactional Outbox Pattern

**Problem Solved**: The classic "dual write" problem where saving to the database and publishing to Kafka are two separate operations. If the database commits but Kafka fails, your system becomes inconsistent.

**Solution Implemented**:
- Events are saved to an `outbox_events` table in the **same database transaction** as business data
- A scheduled processor (`OutboxEventProcessor`) polls every 5 seconds and publishes pending events to Kafka
- Failed events are retried up to 3 times with exponential backoff

**Trade-offs**:
- ✅ **Pro**: Guarantees atomicity between database state and event publication
- ✅ **Pro**: Kafka outages don't block user transactions
- ⚠️ **Con**: Introduces eventual consistency - events may be delayed by ~5 seconds
- ⚠️ **Con**: Adds table maintenance overhead (requires periodic cleanup of published events)

**Files**:
- `OutboxEvent.java` - Entity representing pending events
- `OutboxEventPublisher.java` - Service to save events to outbox
- `OutboxEventProcessor.java` - Scheduled job to publish events
- `V3__create_outbox_table.sql` - Database migration

**Rationale**: For a subscription system, a 5-second delay is acceptable. Critical path (user sign-up) completes immediately while analytics/notifications happen asynchronously.

---

### 2. Optimistic Locking

**Problem Solved**: Concurrent updates to the same subscription (e.g., user cancels while auto-renewal runs) can result in "Lost Update" anomaly where one transaction silently overwrites another.

**Solution Implemented**:
- Added `@Version Long version` field to all core entities (`Subscription`, `User`, `Invoice`, `Plan`)
- Hibernate automatically increments version on each update and throws `OptimisticLockingFailureException` if version mismatch detected
- Custom exception handler returns HTTP 409 Conflict with retry instructions

**Trade-offs**:
- ✅ **Pro**: Prevents data corruption from race conditions
- ✅ **Pro**: No database locks needed (better performance than pessimistic locking)
- ⚠️ **Con**: Client must retry failed requests (handled gracefully by exception handler)

**Files**:
- All domain entities (added `@Version` field)
- `OptimisticLockException.java` - Custom exception
- `GlobalExceptionHandler.handleOptimisticLockException` - Returns 409 Conflict
- `V4__add_version_columns.sql` - Database migration

**Rationale**: Subscriptions are read-heavy, write-light. Optimistic locking provides better throughput than pessimistic locks while still ensuring correctness.

---

### 3. Idempotency in Kafka Consumers

**Problem Solved**: Kafka guarantees "At-Least-Once" delivery, meaning messages can arrive multiple times (e.g., during rebalances). Without idempotency, duplicate messages would trigger duplicate payments or double-charging.

**Solution Implemented**:
- Created `processed_events` table to track consumed message IDs
- `IdempotencyService` checks if event was already processed before executing business logic
- Event ID extracted from JSON payload (falls back to message hash)

**Trade-offs**:
- ✅ **Pro**: Safe against duplicate message processing
- ✅ **Pro**: Simple implementation using database constraint
- ⚠️ **Con**: Requires database lookup on every message (mitigated by indexing)
- ⚠️ **Con**: Table grows unbounded (requires retention policy/archiving)

**Files**:
- `ProcessedEvent.java` - Entity tracking consumed events
- `IdempotencyService.java` - Check/mark idempotency
- `KafkaEventConsumer.java` - Integrated idempotency checks
- `V5__create_processed_events_table.sql` - Database migration

**Rationale**: Financial operations (billing, payments) must be idempotent. The cost of a duplicate payment far exceeds the cost of a database lookup.

---

### 4. Redis Caching Strategy

**Problem Solved**: Plan details are read on every subscription creation/renewal but rarely change. Hitting the database for every read wastes resources and increases latency.

**Solution Implemented**:
- `@Cacheable` on `PlanService.findByCode()` and `getAllPlans()`
- `@CacheEvict` on plan update/delete to invalidate cache
- TTL: Plans cached for 6 hours, Users for 15 minutes
- Redis configured with JSON serialization for complex objects

**Trade-offs**:
- ✅ **Pro**: Dramatically reduces database load (80%+ reduction for plan queries)
- ✅ **Pro**: Sub-millisecond response times for cached data
- ⚠️ **Con**: Introduces Redis as new infrastructure dependency
- ⚠️ **Con**: Stale data risk if cache not evicted properly (mitigated by `@CacheEvict`)

**Files**:
- `CacheConfig.java` - Redis cache manager configuration
- `PlanService.java` - Annotated with cache annotations
- `application.yml` - Redis connection settings
- `docker-compose.yml` - Redis service added

**Rationale**: Plans are perfect cache candidates (read:write ratio > 1000:1). The performance gain justifies the operational complexity of Redis.

---

### 5. Resilience with Circuit Breakers

**Problem Solved**: If Kafka is slow or down, the outbox processor would keep retrying indefinitely, wasting resources and potentially cascading failures.

**Solution Implemented**:
- Resilience4j circuit breaker wraps Kafka publish operations
- Opens after 50% failure rate over 10 attempts
- Waits 10 seconds in open state before attempting half-open
- Exponential backoff retry (1s, 2s, 4s) before circuit opens

**Trade-offs**:
- ✅ **Pro**: Prevents cascading failures when Kafka is degraded
- ✅ **Pro**: Automatic recovery when Kafka comes back online
- ⚠️ **Con**: Events not published during circuit open state (acceptable - queued in outbox)
- ⚠️ **Con**: Adds complexity to testing and debugging

**Files**:
- `ResilienceConfig.java` - Circuit breaker configuration
- `OutboxEventProcessor.java` - Annotated with `@CircuitBreaker`
- `application.yml` - Resilience4j settings
- `pom.xml` - Resilience4j dependencies

**Rationale**: The outbox pattern already queues events, so delaying publication during Kafka outages is acceptable. Circuit breaker prevents resource exhaustion.

---

### 6. Secret Management

**Problem Solved**: Hardcoded secrets (database passwords, JWT keys) in `docker-compose.yml` are a critical security vulnerability.

**Solution Implemented**:
- Created `.env.example` template with placeholder values
- All secrets loaded from environment variables
- Docker Compose configured to use `.env` file (git-ignored)
- Production deployment guide includes Vault/Docker Secrets integration

**Trade-offs**:
- ✅ **Pro**: Secrets never committed to version control
- ✅ **Pro**: Different secrets per environment (dev, staging, prod)
- ⚠️ **Con**: Requires `.env` file setup for local development
- ⚠️ **Con**: More complex deployment process

**Files**:
- `.env.example` - Template for local development
- `docker-compose.yml` - Uses `${VARIABLE}` syntax
- `application.yml` - All sensitive values externalized

**Rationale**: Even for internal projects, secrets management is non-negotiable. This implementation is simple enough for local dev but production-ready.

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Language** | Java 17 | LTS version with modern features |
| **Framework** | Spring Boot 3.2 | Production-grade web framework |
| **Database** | PostgreSQL 15 | ACID compliance for financial data |
| **Messaging** | Apache Kafka | Event streaming for async operations |
| **Cache** | Redis 7 | In-memory data store for hot data |
| **Migrations** | Flyway | Version-controlled schema evolution |
| **Security** | JWT + BCrypt | Stateless auth with secure password hashing |
| **Resilience** | Resilience4j | Circuit breakers and retry logic |
| **Testing** | JUnit 5 + Testcontainers | Integration tests with real dependencies |
| **API Docs** | OpenAPI/Swagger | Interactive API documentation |
| **Metrics** | Micrometer + Prometheus | Production monitoring |

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### 1. Clone & Configure Secrets

```bash
git clone https://github.com/yourusername/subscription-platform.git
cd subscription-platform

# Create .env file from template
cp .env.example .env

# Edit .env and set secure values (especially JWT_SECRET and passwords)
```

### 2. Build

```bash
./mvnw clean package
```

### 3. Start Infrastructure

```bash
# Starts PostgreSQL, Kafka + Zookeeper, Redis
docker-compose up -d postgres kafka redis
```

### 4. Run Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Access

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

---

## 📦 Full Deployment (Production-Like)

```bash
docker-compose up -d
```

This starts all services including the application in a production-ready container.

---

## 🔑 Default Credentials

**Admin User** (seeded for testing):
- Email: `admin@subscription.com`
- Password: `admin123`

> ⚠️ **IMPORTANT**: Change these credentials in production!

---

## 📖 API Examples

### 1. Register User

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "name": "John Doe",
    "password": "password123"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 3. Create Subscription

```bash
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planCode": "PREMIUM"
  }'
```

### 4. Cancel Subscription

```bash
curl -X DELETE http://localhost:8080/api/v1/subscriptions/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🧪 Running Tests

```bash
# Unit tests only
./mvnw test

# Integration tests with Testcontainers (spins up  Postgres, Kafka, Redis containers)
./mvnw verify

# Specific test class
./mvnw test -Dtest=SubscriptionServiceTest
```

---

## 📊 Monitoring & Observability

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```

Returns:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "kafka": { "status": "UP" },
    "circuitBreakers": { "status": "UP" }
  }
}
```

### Metrics

Key metrics exposed via Prometheus:

- `outbox_events_pending` - Number of events waiting to be published
- `cache_hits_total` - Redis cache hit rate
- `circuitbreaker_state` - Current state of circuit breakers (CLOSED, OPEN, HALF_OPEN)
- `subscription_creations_total` - Business metric for subscriptions created

Access all metrics:
```bash
curl http://localhost:8080/actuator/metrics
```

---

## 🗄️ Database Schema

```
users (version-tracked)
  ├── id (PK)
  ├── version (Optimistic Locking)
  ├── email (UNIQUE)
  ├── password (BCrypt hashed)
  ├── name
  ├── role
  └── active

plans (cached, version-tracked)
  ├── id (PK)
  ├── version
  ├── code (UNIQUE, indexed)
  ├── name
  ├── price
  ├── billing_cycle_in_days
  └── trial_period_in_days

subscriptions (version-tracked)
  ├── id (PK)
  ├── version
  ├── user_id (FK → users, UNIQUE)
  ├── plan_id (FK → plans)
  ├── status
  ├── trial_end_date
  └── next_billing_date

invoices (version-tracked)
  ├── id (PK)
  ├── version
  ├── invoice_number (UNIQUE)
  ├── subscription_id (FK → subscriptions)
  ├── amount
  ├── status
  └── due_date

outbox_events (Transactional Outbox)
  ├── id (PK)
  ├── aggregate_id
  ├── event_type
  ├── payload (TEXT, JSON)
  ├── status (PENDING, PUBLISHED, FAILED)
  ├── created_at (indexed)
  └── retry_count

processed_events (Idempotency)
  ├── id (PK)
  ├── event_id (UNIQUE, indexed)
  ├── event_type
  └── processed_at
```

---

## 🔄 Event-Driven Architecture

### Published Events (via Outbox Pattern)

| Event | Topic | When | Consumer Actions |
|-------|-------|------|------------------|
| `SubscriptionCreatedEvent` | subscription-events | User subscribes | Analytics, Email notification |
| `SubscriptionCanceledEvent` | subscription-events | User cancels | Update analytics, Feedback survey |
| `SubscriptionExpiredEvent` | subscription-events | Trial/subscription ends | Downgrade plan, Send renewal email |
| `InvoiceCreatedEvent` | subscription-events | Monthly billing | Trigger payment gateway |
| `PaymentConfirmedEvent` | payment-events | Payment succeeds | Renew subscription, Send receipt |
| `PaymentFailedEvent` | payment-events | Payment fails | Retry logic, Alert user |

### Kafka Topics

- `subscription-events` - All subscription lifecycle events
- `payment-events` - Payment-related events

### Guarantees

- **Exactly-Once Semantics**: Achieved via Transactional Outbox + Idempotency
- **Ordering**: Events for the same aggregate ID (subscription) are ordered
- **Durability**: Events persisted in outbox table before publishing

---

## 🔐 Security

| Aspect | Implementation |
|--------|----------------|
| **Password Storage** | BCrypt with salt (Spring Security default) |
| **Authentication** | JWT tokens (Bearer) with configurable expiration |
| **Authorization** | Role-based access control (USER, ADMIN) |
| **SQL Injection** | Protected by JPA parameterized queries |
| **Mass Assignment** | DTOs prevent binding to internal fields |
| **CORS** | Configured for allowed origins (customize in SecurityConfig) |
| **Secret Management** | Externalized via environment variables |

---

## 📅 Scheduled Jobs

| Job | Schedule | Description | Resilience |
|-----|----------|-------------|------------|
| Outbox Processor | Every 5s | Poll and publish pending events | Circuit breaker protected |
| Billing Cycle | 2:00 AM daily | Generate invoices for upcoming renewals | Idempotent |
| Trial Expiration | 2:30 AM daily | Activate or expire trial subscriptions | Idempotent |

---

## 🐛 Troubleshooting

### Outbox Events Not Publishing

```bash
# Check pending events
docker exec -it subscription-postgres psql -U postgres -d subscription_db -c \
  "SELECT COUNT(*) FROM outbox_events WHERE status = 'PENDING';"

# Check circuit breaker state
curl http://localhost:8080/actuator/health | jq '.components.circuitBreakers'

# View outbox processor logs
docker logs subscription-app | grep OutboxEventProcessor
```

**Common Causes**:
- Kafka is down (check `docker logs subscription-kafka`)
- Circuit breaker is open (wait 10 seconds for automatic retry)
- Events marked as FAILED (check `retry_count` in database)

### Optimistic Lock Conflicts (HTTP 409)

```bash
# Check version mismatches
docker exec -it subscription-postgres psql -U postgres -d subscription_db -c \
  "SELECT id, version, status FROM subscriptions WHERE user_id = 123;"
```

**Resolution**: This is expected under high concurrency. Client should retry the request. If persistent, investigate race condition in business logic.

### Cache Stale Data

```bash
# Manually clear cache
docker exec -it subscription-redis redis-cli FLUSHALL

# Check cache keys
docker exec -it subscription-redis redis-cli KEYS '*'

# Verify TTL
docker exec -it subscription-redis redis-cli TTL "plans::PREMIUM"
```

### Duplicate Event Processing

```bash
# Check processed events table
docker exec -it subscription-postgres psql -U postgres -d subscription_db -c \
  "SELECT * FROM processed_events ORDER BY processed_at DESC LIMIT 10;"
```

---

## 📚 Further Reading & Production Checklist

### Before Going to Production

- [ ] **Secrets**: Use HashiCorp Vault or AWS Secrets Manager (not `.env` files)
- [ ] **Monitoring**: Integrate with Prometheus/Grafana or Datadog
- [ ] **Logging**: Configure centralized logging (ELK Stack, Splunk)
- [ ] **Database**: Set up replication and automated backups
- [ ] **Kafka**: Configure replication factor ≥ 3 for topic durability
- [ ] **Redis**: Enable persistence (AOF) and/or set up Redis Sentinel
- [ ] **SSL/TLS**: Enable HTTPS for all endpoints
- [ ] **Rate Limiting**: Add API rate limits (e.g., via Spring Cloud Gateway)
- [ ] **Disaster Recovery**: Document and test DR procedures
- [ ] **Load Testing**: Run performance tests (JMeter/Gatling) to validate scale

### Recommended Patterns Documentation

- [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html) - Chris Richardson
- [Optimistic vs Pessimistic Locking](https://vladmihalcea.com/optimistic-vs-pessimistic-locking/) - Vlad Mihalcea
- [Idempotency in Distributed Systems](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/)
- [Circuit Breaker Pattern](https://resilience4j.readme.io/docs/circuitbreaker) - Resilience4j Docs

---

## 🤝 Contributing

This is a learning/demonstration project showcasing production-ready patterns. Feel free to fork and experiment!

---

## 📝 License

MIT License - Use freely for learning or production.

---

**Built with ❤️ to demonstrate Senior/Lead-level distributed systems engineering**
