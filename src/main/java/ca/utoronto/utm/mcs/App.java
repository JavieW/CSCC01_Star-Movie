package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
    static int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Database database = new Database("bolt://localhost:7687", "neo4j", "neo4j");
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/v1/addActor", new Actor(database));
        server.createContext("/api/v1/addMovie", new Movie(database));
        server.createContext("/api/v1/addRelationship", new actedIn(database));
        server.createContext("/api/v1/getActor", new Actor(database));
        server.createContext("/api/v1/getMovie", new Movie(database));
        server.createContext("/api/v1/hasRelationship", new actedIn(database));
        server.createContext("/api/v1/computeBaconNumber", new BaconNumber(database));
        server.createContext("/api/v1/computeBaconPath", new BaconPath(database));
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
