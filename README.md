# Salon System

A complete  booking system with Java Spring Boot backend, Blazor frontend, and PostgreSQL database.
The system works as an multiTenant system. 

## Architecture

The system consists of three main components:

- **Backend**: Java Spring Boot REST API (port 8080)
- **Frontend**: Blazor Server application (port 8081)
- **Database**: PostgreSQL 16 (port 5432)

## Prerequisites

Before starting the application, you need to have the following installed:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)
- Git (optional, for cloning the repository)

## Getting Started

### 1. Start the application

To start the entire system, run the following command in the project root:

```bash
docker-compose up --build
```

This will:
- Build backend and frontend images from scratch
- Start PostgreSQL database
- Start Spring Boot backend
- Start Blazor frontend
- Create a Docker network for inter-service communication

**First time** it may take a few minutes to build all images.

### 2. Start in detached mode (background)

If you want to run the containers in the background:

```bash
docker-compose up -d --build
```

### 3. Access the application

Once all services are running, you can access:

- **Frontend**: http://localhost:8081
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432 

### Database credentials

- **Host**: localhost
- **Port**: 5432
- **Database**: postgres
- **Username**: 
- **Password**: *''''''''''''''*

## Docker Compose Commands

### View status of running containers

```bash
docker-compose ps
```

### View logs from all services

```bash
docker-compose logs
```

### View logs from a specific service

```bash
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres
```

### Follow logs in real-time

```bash
docker-compose logs -f
```

### Stop all services

```bash
docker-compose down
```

### Stop and remove volumes (deletes database data)

```bash
docker-compose down -v
```

### Restart a specific service

```bash
docker-compose restart backend
```

### Rebuild and restart after code changes

```bash
docker-compose up --build -d
```

### Rebuild only a specific service

```bash
docker-compose up --build -d backend
```

### View resource usage

```bash
docker stats
```

## Troubleshooting

### Problem: Port already in use

If you get an error that a port is already in use:

1. Find the process using the port:
   ```bash
   netstat -ano | findstr :8080
   netstat -ano | findstr :8081
   netstat -ano | findstr :5432
   ```

2. Stop the existing process or change the port in `docker-compose.yml`

### Problem: Database connection error

If the backend cannot connect to the database:

```bash
# Check if postgres is healthy
docker-compose ps

# View postgres logs
docker-compose logs postgres

# Restart services in correct order
docker-compose down
docker-compose up -d postgres
# Wait until postgres is healthy
docker-compose up -d backend frontend
```

### Problem: Changes not showing

If your code changes are not appearing:

```bash
# Rebuild images without cache
docker-compose build --no-cache

# Start services again
docker-compose up -d
```

### Access container shell

To access a container's shell for debugging:

```bash
# Backend (Java)
docker exec -it salon-backend sh

# Frontend (Blazor)
docker exec -it salon-frontend sh

# Database
docker exec -it salon-postgres psql -U postgres
```

## Database Management

### Backup database

```bash
docker exec salon-postgres pg_dump -U postgres postgres > backup.sql
```

### Restore database

```bash
cat backup.sql | docker exec -i salon-postgres psql -U postgres postgres
```

### Connect to database with psql

```bash
docker exec -it salon-postgres psql -U postgres
```

## Development

### Local development without Docker

If you want to run services locally without Docker:

1. **Database**: Start PostgreSQL locally or use Docker only for database
   ```bash
   docker-compose up -d postgres
   ```

2. **Backend**: Run from IDE or command line
   ```bash
   cd Salon
   ./mvnw spring-boot:run
   ```

3. **Frontend**: Run from IDE or command line
   ```bash
   cd BlazorSalonApp/BlazorSalonApp
   dotnet run
   ```

## Cleanup

### Remove stopped containers

```bash
docker-compose rm
```

### Remove all unused images

```bash
docker system prune -a
```

### Remove volumes (WARNING: deletes all data)

```bash
docker volume rm salonsystem_postgres_data
```

## Support

For issues or questions, check:
- Docker logs: `docker-compose logs`
- Container status: `docker-compose ps`
- System resources: `docker stats`
