package com.example.rest;

import com.example.api.Sessions;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Serves pictures from the configured pictures base directory.
 * <p>
 * URL contract (unchanged from WildFly era):
 * <pre>
 *   /simple-service-webapp/webapi/myresource/images/{sub}/{file}
 *   /simple-service-webapp/webapi/myresource/images/{file}
 * </pre>
 * <p>
 * The pictures folder layout is:
 * <pre>
 *   pictures/
 *     images/    → general / hero images  (was ~/Pictures/Exports/)
 *     movies/    → movie posters
 *     venues/    → venue photos
 *     profiles/  → user profile pictures
 * </pre>
 */
@Path("/myresource")
public class ImageResource {

    private static final Logger log = Logger.getLogger(ImageResource.class);

    @ConfigProperty(name = "pictures.base-dir", defaultValue = "../pictures")
    String picturesBaseDir;

    /** Base URL of the Apache reverse proxy (reads WILDFLY_URL env var, same as dalogin). */
    @ConfigProperty(name = "admin.service-url", defaultValue = "http://localhost:8888")
    String serviceUrl;

    /* ---------------------------------------------------------- */
    /*  GET /myresource  — simple health-check text (kept from    */
    /*  the original WildFly resource)                            */
    /* ---------------------------------------------------------- */

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response root() {
        return Response.ok("Got it").build();
    }

    /* ---------------------------------------------------------- */
    /*  /images/{file}  — general images (was ~/Pictures/Exports) */
    /* ---------------------------------------------------------- */

    @GET
    @Path("/images/{image}")
    @Produces("image/*")
    public Response getImage(@PathParam("image") String image) throws IOException {
        return serveFile("images", image);
    }

    /* ---------------------------------------------------------- */
    /*  /images/movies/{file}                                     */
    /* ---------------------------------------------------------- */

    @GET
    @Path("/images/movies/{image}")
    @Produces("image/*")
    public Response getMovieImage(@PathParam("image") String image) throws IOException {
        return serveFile("movies", image);
    }

    /* ---------------------------------------------------------- */
    /*  /images/venues/{file}                                     */
    /* ---------------------------------------------------------- */

    @GET
    @Path("/images/venues/{image}")
    @Produces("image/*")
    public Response getVenueImage(@PathParam("image") String image) throws IOException {
        return serveFile("venues", image);
    }

    /* ---------------------------------------------------------- */
    /*  /images/profiles/{file}                                   */
    /* ---------------------------------------------------------- */

    @GET
    @Path("/images/profiles/{image}")
    @Produces("image/*")
    public Response getProfileImage(@PathParam("image") String image) throws IOException {
        return serveFile("profiles", image);
    }

    /* ---------------------------------------------------------- */
    /*  Shared helper                                             */
    /* ---------------------------------------------------------- */

    private Response serveFile(String subfolder, String fileName) throws IOException {
        // Guard against path-traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new NotFoundException("Invalid file name");
        }

        java.nio.file.Path file = java.nio.file.Path.of(picturesBaseDir, subfolder, fileName);

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            throw new NotFoundException("Image not found: " + fileName);
        }

        byte[] data = Files.readAllBytes(file);

        // Derive MIME type from file name
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        return Response.ok(data, mimeType).build();
    }

    /* ---------------------------------------------------------- */
    /*  GET /myresource/admin  — active sessions / devices        */
    /*  Proxies to dalogin /login/activeSessions via HTTP.        */
    /*  dalogin reads the activeUsers map from its ServletContext  */
    /*  and returns [{sessionId, user, deviceId, creationTime}].  */
    /* ---------------------------------------------------------- */

    @GET
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessions(@Context HttpHeaders headers) {
        // Forward cookies from the incoming request so dalogin can identify the caller
        String cookieHeader = null;
        List<String> cookies = headers.getRequestHeader("Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            cookieHeader = String.join("; ", cookies);
        }

        String activeSessionsUrl = serviceUrl + "/login/activeSessions";
        log.infof("Fetching active sessions from %s", activeSessionsUrl);

        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(activeSessionsUrl).get();

        if (cookieHeader != null) {
            requestBuilder.header("Cookie", cookieHeader);
        }

        try {
            okhttp3.Response upstream = client.newCall(requestBuilder.build()).execute();
            String body = upstream.body() != null ? upstream.body().string() : "[]";
            int status = upstream.code();
            log.infof("Active sessions response: status=%d, count=%d bytes", status, body.length());
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build();
        } catch (IOException e) {
            log.errorf(e, "Failed to fetch active sessions from %s", activeSessionsUrl);
            return Response.status(502)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Failed to reach dalogin active sessions\",\"message\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
