package ca.utoronto.utm.mcs;

import org.json.JSONObject;
import org.neo4j.driver.v1.*;

import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

public class Database implements AutoCloseable
{
    private final Driver driver;

    public Database(String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run("CREATE CONSTRAINT ON (a:Actor) ASSERT a.actorId IS UNIQUE;");
                    tx.run("CREATE CONSTRAINT ON (m:Movie) ASSERT m.movieId IS UNIQUE;");
                    return null;
                }
            } );
        }

    }

    @Override
    public void close()
    {
        driver.close();
    }

    public void addActor(String name, String actorId) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "CREATE (a:Actor) SET a.name = $name, a.actorId = $actorId",
                            parameters( "name", name, "actorId", actorId) );
                    return null;
                }
            } );
        }
    }

    public String getActor(String actorId) {

        try ( Session session = driver.session() ) {

            String result = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run("MATCH (a:Actor{actorId:$actorId}) " +
                                    "RETURN a {.actorId, .name, movies: [(a) -[:ACTED_IN]-> (m:Movie) | m.movieId]}",
                            parameters("actorId", actorId));

                    JSONObject obj = new JSONObject(result.single().asMap());
                    String jsonString = obj.toString();
                    return jsonString.substring(5, jsonString.length() -1);
                }
            });
            return result;
        }
    }

    public void addMovie(String name, String movieId) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "CREATE (a:Movie) SET a.name = $name, a.movieId = $movieId ",
                            parameters( "name", name, "movieId", movieId) );
                    return null;
                }
            } );
        }
    }

    public String getMovie(String movieId) {

        try ( Session session = driver.session() ) {

            String result = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run("MATCH (m:Movie{movieId:$movieId}) " +
                                    "RETURN m {.movieId, .name, actors: [(m) <-[:ACTED_IN]- (a:Actor) | a.actorId]}",
                            parameters("movieId", movieId));

                    JSONObject obj = new JSONObject(result.single().asMap());
                    String jsonString = obj.toString();
                    return jsonString.substring(5, jsonString.length() -1);
                }
            });
            return result;
        }
    }

    public String addRelationship(String actorId, String movieId) {
        try ( Session session = driver.session() )
        {
            String result = session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx )
                {

                    try{
                        tx.run( "MATCH (a:Actor {actorId:$actorId }) RETURN a",
                                parameters( "actorId", actorId)).single().asMap();
                    } catch (Exception e) {
                        return "NNF"; // Node Not Found
                    }

                    try{
                        tx.run( "MATCH (m:Movie {movieId:$movieId }) RETURN m",
                                parameters( "movieId", movieId)).single().asMap();
                    } catch (Exception e) {
                        return "NNF"; // Node Not Found
                    }
                    tx.run( "MATCH (a:Actor {actorId: $actorId}), " +
                                    "(m:Movie {movieId: $movieId}) " +
                                        "MERGE ((a) -[:ACTED_IN{actorId:$actorId, movieId:$movieId}]->(m))",
                            parameters( "actorId", actorId, "movieId", movieId));
                    return "";
                }
            } );
            return result;
        }
    }

    public String getRelationship(String actorId, String movieId) {
        try ( Session session = driver.session() )
        {
            String result = session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "MATCH (a:Actor {actorId: $actorId}), " +
                                    "(m:Movie {movieId: $movieId}) " +
                                    "RETURN a {.actorId, movieId:m.movieId, " +
                                    "hasRelationship: EXISTS((a)-[:ACTED_IN]->(m))}",
                            parameters( "actorId", actorId, "movieId", movieId));
                    JSONObject obj = new JSONObject(result.single().asMap());
                    String jsonString = obj.toString();
                    return jsonString.substring(5, jsonString.length() -1);
                }
            } );
            return result;
        }
    }

    public String computeBaconNumber(String actorId) {
        try ( Session session = driver.session() ) {
            String number;
            if (actorId.equals("nm0000102")){
                number = "{\"baconNumber\":\"0\"}";
                return number;
            }
            number = session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx )
                {
                    try{
                        tx.run( "MATCH (s:Actor {actorId:$start }) RETURN s",
                                parameters( "start", actorId)).single().asMap();
                    } catch (Exception e) {
                        return "NNF"; // Node Not Found
                    }
                    StatementResult result = tx.run( "MATCH (s:Actor {actorId: $start}), " +
                                    "(e:Actor {actorId: 'nm0000102'}), " +
                                    "p = shortestPath((s)-[*]-(e)) " +
                                    "RETURN s {baconNumber: toString(length(p)/2)}",
                            parameters( "start", actorId));

                    JSONObject obj = new JSONObject(result.single().asMap());
                    String jsonString = obj.toString();
                    return jsonString.substring(5, jsonString.length() -1);
                }
            } );
            return number;
        }
    }

    public String computeBaconPath(String actorId) {
        try ( Session session = driver.session() ) {
            String result;
            if (actorId.equals("nm0000102")){
                result = "{\"baconNumber\": \"0\", \"baconPath\":[]}";
                return result;
            }
            result = session.writeTransaction( new TransactionWork<String>() {
                @Override
                public String execute( Transaction tx ) {
                    try{
                        tx.run( "MATCH (s:Actor {actorId:$start }) RETURN s",
                                parameters( "start", actorId)).single().asMap();
                    } catch (Exception e) {
                        return "NNF"; // Node Not Found
                    }

                    StatementResult result = tx.run( "MATCH (s:Actor {actorId:$start }), " +
                                    "(e:Actor {actorId: 'nm0000102'}), p = shortestPath((s)-[*]-(e)) "+
                                    "return { baconNumber: toString(length(p)/2), "+
                                    "baconPath: [ i in range(0, length(p)-1) where i%2 = 0 or "+
                                    "i=length(p)-1|{actorId:relationships(p)[i].actorId, "+
                                    "movieId:relationships(p)[i].movieId}]} as a",
                            parameters( "start", actorId));
                    Map<String, Object> map = result.single().asMap();
                    JSONObject obj = new JSONObject(map);
                    String jsonString = obj.toString();
                    return jsonString.substring(5, jsonString.length() -1);
                }
            } );
            return result;
        }
    }
}