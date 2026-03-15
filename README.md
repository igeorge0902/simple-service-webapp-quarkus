# simple-service-webapp-quarkus

Quarkus replacement for the legacy WildFly `simple-service-webapp`.  
Serves pictures (movie posters, venue photos, profile images, and general hero images) from a configurable filesystem directory.

## URL contract (unchanged)

All existing clients (iOS app, dalogin HTML pages) reference:

```
/simple-service-webapp/webapi/myresource/images/{file}
/simple-service-webapp/webapi/myresource/images/movies/{file}
/simple-service-webapp/webapi/myresource/images/venues/{file}
/simple-service-webapp/webapi/myresource/images/profiles/{file}
```

These paths are preserved exactly.

## Pictures folder layout

```
pictures/
  images/    → general / hero images (referenced by main.html)
  movies/    → movie poster JPEGs
  venues/    → venue photos
  profiles/  → user profile pictures
```

## Configuration

| Property            | Default        | Description                       |
|---------------------|----------------|-----------------------------------|
| `pictures.base-dir` | `../pictures`  | Filesystem path to pictures root  |
| `quarkus.http.port` | `8085`         | HTTP listen port                  |

In Kubernetes the pictures folder is mounted from a PersistentVolumeClaim or ConfigMap and `PICTURES_BASE_DIR` is set via env var.

## Build & Run

```bash
./mvnw quarkus:dev          # dev mode (port 8085)
./mvnw package -DskipTests  # package for container
```

## Container image

```bash
podman build -t simple-service-webapp-quarkus:local .
```

