# simple-service-webapp-quarkus

Image-serving API for the Cinemas booking platform. Serves movie posters, venue photos, and profile pictures from a configurable filesystem directory. Runs on port **8085** under context path `/simple-service-webapp`.

## URL contract (unchanged from WildFly era)

```
/simple-service-webapp/webapi/myresource                          → health check ("Got it")
/simple-service-webapp/webapi/myresource/images/{file}            → general images
/simple-service-webapp/webapi/myresource/images/movies/{file}     → movie posters
/simple-service-webapp/webapi/myresource/images/venues/{file}     → venue photos
/simple-service-webapp/webapi/myresource/images/profiles/{file}   → profile pictures
```

## Pictures folder layout

```
pictures/
  images/    → general / hero images
  movies/    → movie poster JPEGs (103 files)
  venues/    → venue photos (9 files)
  profiles/  → user profile pictures
```

## Configuration

| Property | Default | Env var | Description |
|----------|---------|---------|-------------|
| `pictures.base-dir` | `../pictures` | `PICTURES_BASE_DIR` | Filesystem path to pictures root |
| `quarkus.http.port` | `8085` | — | HTTP listen port |

In Kubernetes, the pictures folder is mounted as an `emptyDir` volume at `/pictures` and must be populated after pod startup:

```bash
POD=$(kubectl -n cinemas get pod -l app=simple-service-webapp -o jsonpath='{.items[0].metadata.name}')
kubectl -n cinemas cp pictures/ $POD:/pictures/
kubectl -n cinemas exec $POD -- sh -c 'mv /pictures/pictures/* /pictures/ && rmdir /pictures/pictures'
```

## Build & Run

```bash
./mvnw quarkus:dev                                # dev mode on port 8085
./mvnw package -DskipTests                         # package for container
podman build -t simple-service-webapp-quarkus:local .
```

## Part of the Cinemas platform

| Service | Repo | Role |
|---------|------|------|
| dalogin-quarkus | [igeorge0902/dalogin-quarkus](https://github.com/igeorge0902/dalogin-quarkus) | Auth gateway |
| mbook-quarkus | [igeorge0902/mbook-quarkus](https://github.com/igeorge0902/mbook-quarkus) | User/device API |
| mbooks-quarkus | [igeorge0902/mbooks-quarkus](https://github.com/igeorge0902/mbooks-quarkus) | Movie/booking/payment API |
| **simple-service-webapp-quarkus** | this repo | Image server |
| k8infra | [igeorge0902/k8infra](https://github.com/igeorge0902/k8infra) | Kubernetes manifests, SQL fixes, deploy runbook |
