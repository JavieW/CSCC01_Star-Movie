package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;

import java.io.IOException;
import java.io.OutputStream;

public class BaconPath implements HttpHandler {
    private static Database database;

    public BaconPath(Database db) {
        database = db;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            String actorId = null;

            if (deserialized.has("actorId"))
                actorId = deserialized.getString("actorId");
            else {
                r.sendResponseHeaders(400, -1);
                return;
            }

            String response = database.computeBaconPath(actorId);
            if (response.equals("NNF")) {
                r.sendResponseHeaders(400, -1);
            } else {
                r.sendResponseHeaders(200, response.length());
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } catch (JSONException e) {
            r.sendResponseHeaders(400, -1);
        } catch (NoSuchRecordException e) {
            r.sendResponseHeaders(404, -1);
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }
    }
}
