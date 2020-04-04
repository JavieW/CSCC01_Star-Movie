package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Movie implements HttpHandler{
    private static Database database;

    public Movie(Database db) {
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
        try{
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            String name = null;
            String movieId = null;

            if (deserialized.has("name"))
                name = deserialized.getString("name");

            if (deserialized.has("movieId"))
                movieId = deserialized.getString("movieId");

            if (name == null || movieId == null) {
                r.sendResponseHeaders(400,-1);
                return;
            }
            database.addMovie(name, movieId);
            r.sendResponseHeaders(200, -1);
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

            String movieId = null;

            if (deserialized.has("movieId"))
                movieId = deserialized.getString("movieId");
            else {
                r.sendResponseHeaders(400, -1);
                return;
            }

            String response = database.getMovie(movieId);
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
