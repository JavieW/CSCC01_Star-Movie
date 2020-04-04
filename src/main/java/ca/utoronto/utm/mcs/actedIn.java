package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class actedIn implements HttpHandler {
    private static Database database;

    public actedIn(Database db) {
        database = db;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            } else if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePut(HttpExchange r) throws IOException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            String movieId = null;
            String actorId = null;

            if (deserialized.has("movieId"))
                movieId = deserialized.getString("movieId");

            if (deserialized.has("actorId"))
                actorId = deserialized.getString("actorId");

            if (movieId == null || actorId == null) {
                r.sendResponseHeaders(400, -1);
                return;
            }
            if(database.addRelationship(actorId, movieId) == null) {
                r.sendResponseHeaders(200, -1);
            } else
                r.sendResponseHeaders(500, -1);
        } catch (JSONException e) {
            r.sendResponseHeaders(400, -1);
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
        }
    }

    private void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            String actorId = null;
            String movieId = null;

            if (deserialized.has("actorId"))
                actorId = deserialized.getString("actorId");
            if (deserialized.has("movieId"))
                movieId = deserialized.getString("movieId");

            if (movieId == null || actorId == null) {
                r.sendResponseHeaders(400, -1);
                return;
            }
            String response = database.getRelationship(actorId, movieId);
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (JSONException e) {
            r.sendResponseHeaders(400, -1);
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
        }
    }
}
