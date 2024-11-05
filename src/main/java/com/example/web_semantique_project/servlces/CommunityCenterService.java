package com.example.web_semantique_project.servlces;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class CommunityCenterService {
    private static final String RDF_FILE_PATH = "/home/nizar/Desktop/test_ontologie.rdf";
    private static final String NAMESPACE = "http://www.semanticweb.org/food-rescue-ontology#";

    public List<Map<String, Object>> getAllCommunityCenters() {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String queryString = """
            PREFIX ns: <http://www.semanticweb.org/food-rescue-ontology#>
            SELECT ?name ?address ?contactNumber
            WHERE {
                ?center a ns:CommunityCenter .
                ?center ns:name ?name .
                ?center ns:address ?address .
                ?center ns:contactNumber ?contactNumber .
            }
        """;

        Query query = QueryFactory.create(queryString);
        List<Map<String, Object>> centerList = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, Object> centerData = new HashMap<>();

                String name = soln.get("name").toString();
                String address = soln.contains("address") ? soln.get("address").toString() : null;
                String contactNumber = soln.contains("contactNumber") ? soln.get("contactNumber").toString() : null;

                centerData.put("name", name);
                centerData.put("address", address);
                centerData.put("contactNumber", contactNumber);

                centerList.add(centerData);
            }
        }

        return centerList;
    }

    public boolean addCommunityCenter(Map<String, String> centerData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Check if center already exists
        String checkQuery = """
            PREFIX ns: <%s>
            ASK {
                ?center ns:name "%s"
            }
        """.formatted(NAMESPACE, centerData.get("name"));

        Query query = QueryFactory.create(checkQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            if (qexec.execAsk()) {
                return false; // Center already exists
            }
        }

        // Create a new community center resource
        String centerUri = NAMESPACE + "CommunityCenter_" + UUID.randomUUID().toString();
        Resource center = model.createResource(centerUri);
        Property nameProperty = model.createProperty(NAMESPACE + "name");
        Property addressProperty = model.createProperty(NAMESPACE + "address");
        Property contactProperty = model.createProperty(NAMESPACE + "contactNumber");
        Resource typeProperty = model.createResource(NAMESPACE + "CommunityCenter");

        // Add properties
        center.addProperty(RDF.type, typeProperty);
        center.addProperty(nameProperty, centerData.get("name"));
        center.addProperty(addressProperty, centerData.get("address"));
        center.addProperty(contactProperty, centerData.get("contactNumber"));

        // Save the model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCommunityCenter(String originalName, Map<String, String> updatedData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Find the center resource
        String findQuery = """
            PREFIX ns: <%s>
            SELECT ?center
            WHERE {
                ?center a ns:CommunityCenter .
                ?center ns:name "%s"
            }
        """.formatted(NAMESPACE, originalName);

        Query query = QueryFactory.create(findQuery);
        Resource centerResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                centerResource = soln.getResource("center");
            } else {
                return false; // Center not found
            }
        }

        // Remove existing properties
        StmtIterator iter = model.listStatements(centerResource, null, (RDFNode) null);
        model.remove(iter);

        // Add updated properties
        Property nameProperty = model.createProperty(NAMESPACE + "name");
        Property addressProperty = model.createProperty(NAMESPACE + "address");
        Property contactProperty = model.createProperty(NAMESPACE + "contactNumber");
        Resource typeProperty = model.createResource(NAMESPACE + "CommunityCenter");

        centerResource.addProperty(RDF.type, typeProperty);
        centerResource.addProperty(nameProperty, updatedData.get("name"));
        centerResource.addProperty(addressProperty, updatedData.get("address"));
        centerResource.addProperty(contactProperty, updatedData.get("contactNumber"));

        // Save the updated model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCommunityCenter(String centerName) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String findCenterQuery = """
            PREFIX ns: <%s>
            SELECT ?center
            WHERE {
                ?center a ns:CommunityCenter .
                ?center ns:name "%s" .
            }
        """.formatted(NAMESPACE, centerName);

        Query query = QueryFactory.create(findCenterQuery);
        Resource centerResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                centerResource = soln.getResource("center");
            } else {
                return false;
            }
        }

        StmtIterator iter = model.listStatements(centerResource, null, (RDFNode) null);
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