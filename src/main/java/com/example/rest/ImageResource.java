package com.example.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;

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

    @ConfigProperty(name = "pictures.base-dir", defaultValue = "../pictures")
    String picturesBaseDir;

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
}

