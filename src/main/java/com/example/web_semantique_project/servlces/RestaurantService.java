package com.example.web_semantique_project.servlces;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.update.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class RestaurantService {
    private static final String RDF_FILE_PATH = "/home/nizar/Desktop/test_ontologie.rdf";
    private static final String NAMESPACE = "http://www.semanticweb.org/food-rescue-ontology#";

    public List<Map<String, Object>> getAllRestaurants() {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String queryString = """
            PREFIX ns: <http://www.semanticweb.org/food-rescue-ontology#>
            SELECT ?name ?address ?contactNumber ?operatingHours ?ownedByUsername
            WHERE {
                ?restaurant a ns:Restaurant .
                ?restaurant ns:name ?name .
                ?restaurant ns:address ?address .
                ?restaurant ns:contactNumber ?contactNumber .
                ?restaurant ns:operatingHours ?operatingHours .
                OPTIONAL { ?restaurant ns:ownedByUsername ?ownedByUsername }
            }
        """;



        Query query = QueryFactory.create(queryString);
        List<Map<String, Object>> restaurantList = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, Object> restaurantData = new HashMap<>();

                String name = soln.get("name").toString();
                String address = soln.contains("address") ? soln.get("address").toString() : null;
                String contactNumber = soln.contains("contactNumber") ? soln.get("contactNumber").toString() : null;
                String operatingHours = soln.contains("operatingHours") ? soln.get("operatingHours").toString() : null;
                String ownedByUsername = soln.contains("ownedByUsername") ? soln.get("ownedByUsername").toString() : null;

                restaurantData.put("name", name);
                restaurantData.put("address", address);
                restaurantData.put("contactNumber", contactNumber);
                restaurantData.put("operatingHours", operatingHours);
                restaurantData.put("ownedByUsername", ownedByUsername);

                restaurantList.add(restaurantData);
            }
        }

        return restaurantList;
    }

    public boolean addRestaurant(Map<String, String> restaurantData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Check if restaurant already exists
        String checkQuery = """
            PREFIX ns: <%s>
            ASK {
                ?restaurant ns:name "%s"
            }
        """.formatted(NAMESPACE, restaurantData.get("name"));

        Query query = QueryFactory.create(checkQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            if (qexec.execAsk()) {
                return false; // Restaurant already exists
            }
        }

        // Create a new restaurant resource
        String restaurantUri = NAMESPACE + "Restaurant_" + UUID.randomUUID().toString();
        Resource restaurant = model.createResource(restaurantUri);
        Property nameProperty = model.createProperty(NAMESPACE + "name");
        Property addressProperty = model.createProperty(NAMESPACE + "address");
        Property contactProperty = model.createProperty(NAMESPACE + "contactNumber");
        Property hoursProperty = model.createProperty(NAMESPACE + "operatingHours");
        Property ownedByUsernameProperty = model.createProperty(NAMESPACE + "ownedByUsername");
        Property typeProperty = model.createProperty(NAMESPACE + "Restaurant");

        // Add properties
        restaurant.addProperty(RDF.type, typeProperty);
        restaurant.addProperty(nameProperty, restaurantData.get("name"));
        restaurant.addProperty(addressProperty, restaurantData.get("address"));
        restaurant.addProperty(contactProperty, restaurantData.get("contactNumber"));
        restaurant.addProperty(hoursProperty, restaurantData.get("operatingHours"));
        if (restaurantData.containsKey("ownedByUsername")) {
            restaurant.addProperty(ownedByUsernameProperty, restaurantData.get("ownedByUsername"));
        }

        // Save the model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRestaurant(String originalName, Map<String, String> updatedData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Find the restaurant resource
        String findQuery = """
            PREFIX ns: <%s>
            SELECT ?restaurant
            WHERE {
                ?restaurant a ns:Restaurant .
                ?restaurant ns:name "%s"
            }
        """.formatted(NAMESPACE, originalName);

        Query query = QueryFactory.create(findQuery);
        Resource restaurantResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                restaurantResource = soln.getResource("restaurant");
            } else {
                return false; // Restaurant not found
            }
        }

        // Remove existing properties
        StmtIterator iter = model.listStatements(restaurantResource, null, (RDFNode) null);
        model.remove(iter);

        // Add updated properties
        Property nameProperty = model.createProperty(NAMESPACE + "name");
        Property addressProperty = model.createProperty(NAMESPACE + "address");
        Property contactProperty = model.createProperty(NAMESPACE + "contactNumber");
        Property hoursProperty = model.createProperty(NAMESPACE + "operatingHours");
        Property ownedByUsernameProperty = model.createProperty(NAMESPACE + "ownedByUsername");
        Property typeProperty = model.createProperty(NAMESPACE + "Restaurant");

        restaurantResource.addProperty(RDF.type, typeProperty);
        restaurantResource.addProperty(nameProperty, updatedData.get("name"));
        restaurantResource.addProperty(addressProperty, updatedData.get("address"));
        restaurantResource.addProperty(contactProperty, updatedData.get("contactNumber"));
        restaurantResource.addProperty(hoursProperty, updatedData.get("operatingHours"));
        if (updatedData.containsKey("ownedByUsername")) {
            restaurantResource.addProperty(ownedByUsernameProperty, updatedData.get("ownedByUsername"));
        }

        // Save the updated model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRestaurant(String restaurantName) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String findRestaurantQuery = """
            PREFIX ns: <%s>
            SELECT ?restaurant
            WHERE {
                ?restaurant a ns:Restaurant .
                ?restaurant ns:name "%s" .
            }
        """.formatted(NAMESPACE, restaurantName);

        Query query = QueryFactory.create(findRestaurantQuery);
        Resource restaurantResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                restaurantResource = soln.getResource("restaurant");
            } else {
                return false;
            }
        }

        StmtIterator iter = model.listStatements(restaurantResource, null, (RDFNode) null);
        model.remove(iter);

        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}