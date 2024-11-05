package com.example.web_semantique_project.servlces;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class UserService {
    private static final String RDF_FILE_PATH = "/home/nizar/Desktop/test_ontologie.rdf";
    private static final String NAMESPACE = "http://www.semanticweb.org/food-rescue-ontology#";

    public List<Map<String, Object>> getAllUsers() {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String queryString = """
            PREFIX ns: <http://www.semanticweb.org/food-rescue-ontology#>
            SELECT ?user ?username ?role
            WHERE {
                ?user a ?type .
                ?user ns:username ?username .
                ?user ns:role ?role .
                FILTER(?type IN (ns:DonorUser, ns:RecipientUser, ns:AdminUser))
            }
        """;

        Query query = QueryFactory.create(queryString);
        List<Map<String, Object>> userList = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, Object> userData = new HashMap<>();

                String username = soln.get("username").toString();
                String role = soln.contains("role") ? soln.get("role").toString() : null;
                String userId = soln.get("user").toString().replace(NAMESPACE, "");

                userData.put("username", username);
                userData.put("role", role);
                userData.put("id", userId);

                userList.add(userData);
            }
        }

        return userList;
    }

public boolean addUser(Map<String, String> userData) {
    Model model = ModelFactory.createDefaultModel();
    FileManager.get().readModel(model, RDF_FILE_PATH);

    // Check if admin user already exists
    String userId = userData.get("id");
    String checkQuery = """
        PREFIX ns: <%s>
        ASK {
            ?user a ns:AdminUser .
            ?user ns:id "%s"
        }
    """.formatted(NAMESPACE, userId);

    Query query = QueryFactory.create(checkQuery);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
        if (qexec.execAsk()) {
            return false; // Admin user already exists
        }
    }

    // Create a new admin user resource
    String userUri = NAMESPACE + "AdminUser_" + userId;
    Resource user = model.createResource(userUri);
    Property idProperty = model.createProperty(NAMESPACE + "id");
    Property usernameProperty = model.createProperty(NAMESPACE + "username");
    Property roleProperty = model.createProperty(NAMESPACE + "role");

    // Add properties
    user.addProperty(RDF.type, model.createResource(NAMESPACE + "AdminUser"));
    user.addProperty(idProperty, userId);
    user.addProperty(usernameProperty, userData.get("username"));
    user.addProperty(roleProperty, "Administrator");

    // Save the model
    try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
        model.write(out, "RDF/XML");
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

public boolean updateUser(String userId, Map<String, String> updatedData) {
    Model model = ModelFactory.createDefaultModel();
    FileManager.get().readModel(model, RDF_FILE_PATH);

    // Find the admin user resource
    String userUri = NAMESPACE + "AdminUser_" + userId;
    Resource userResource = model.getResource(userUri);

    if (userResource == null) {
        return false; // Admin user not found
    }

    // Remove existing properties
    StmtIterator iter = model.listStatements(userResource, null, (RDFNode) null);
    model.remove(iter);

    // Add updated properties
    Property idProperty = model.createProperty(NAMESPACE + "id");
    Property usernameProperty = model.createProperty(NAMESPACE + "username");
    Property roleProperty = model.createProperty(NAMESPACE + "role");

    userResource.addProperty(RDF.type, model.createResource(NAMESPACE + "AdminUser"));
    userResource.addProperty(idProperty, userId);
    userResource.addProperty(usernameProperty, updatedData.get("username"));
    userResource.addProperty(roleProperty, "Administrator");

    // Save the updated model
    try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
        model.write(out, "RDF/XML");
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

public boolean deleteUser(String userId) {
    Model model = ModelFactory.createDefaultModel();
    FileManager.get().readModel(model, RDF_FILE_PATH);

    String userUri = NAMESPACE + "AdminUser_" + userId;
    Resource userResource = model.getResource(userUri);

    if (userResource == null) {
        return false; // Admin user not found
    }

    StmtIterator iter = model.listStatements(userResource, null, (RDFNode) null);
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