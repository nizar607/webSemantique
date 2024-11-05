package com.example.web_semantique_project.servlces;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class ShelterService {
    private static final String RDF_FILE_PATH = "/home/nizar/Desktop/test_ontologie.rdf";
    private static final String NAMESPACE = "http://www.semanticweb.org/food-rescue-ontology#";

    public List<Map<String, Object>> getAllShelters() {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String queryString = """
            PREFIX ns: <http://www.semanticweb.org/food-rescue-ontology#>
            SELECT ?shelter ?name ?address ?contactNumber
            WHERE {
                ?shelter a ns:Shelter .
                ?shelter ns:name ?name .
                ?shelter ns:address ?address .
                ?shelter ns:contactNumber ?contactNumber .
            }
        """;

        Query query = QueryFactory.create(queryString);
        List<Map<String, Object>> shelterList = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, Object> shelterData = new HashMap<>();

                String shelterId = soln.get("shelter").toString().replace(NAMESPACE, "");
                String name = soln.get("name").toString();
                String address = soln.get("address").toString();
                String contactNumber = soln.get("contactNumber").toString();

                shelterData.put("id", shelterId);
                shelterData.put("name", name);
                shelterData.put("address", address);
                shelterData.put("contactNumber", contactNumber);

                shelterList.add(shelterData);
            }
        }

        return shelterList;
    }

    public boolean addShelter(Map<String, String> shelterData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Check if shelter already exists
        String checkQuery = """
            PREFIX ns: <%s>
            ASK {
                ?shelter a ns:Shelter ;
                        ns:name "%s"
            }
        """.formatted(NAMESPACE, shelterData.get("name"));

        Query query = QueryFactory.create(checkQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            if (qexec.execAsk()) {
                return false; // Shelter already exists
            }
        }

        // Create a new shelter resource
        String shelterUri = NAMESPACE + "Shelter_" + UUID.randomUUID().toString();
        Resource shelter = model.createResource(shelterUri);
        Property nameProperty = model.createProperty(NAMESPACE + "name");
        Property addressProperty = model.createProperty(NAMESPACE + "address");
        Property contactNumberProperty = model.createProperty(NAMESPACE + "contactNumber");
        Resource typeProperty = model.createResource(NAMESPACE + "Shelter");

        // Add properties
        shelter.addProperty(RDF.type, typeProperty);
        shelter.addProperty(nameProperty, shelterData.get("name"));
        shelter.addProperty(addressProperty, shelterData.get("address"));
        shelter.addProperty(contactNumberProperty, shelterData.get("contactNumber"));

        // Save the model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateShelter(String shelterId, Map<String, String> updatedData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Find the shelter resource
        String findQuery = """
            PREFIX ns: <%s>
            SELECT ?shelter
            WHERE {
                ?shelter a ns:Shelter .
                FILTER(str(?shelter) = "%s%s")
            }
        """.formatted(NAMESPACE, NAMESPACE, shelterId);

        Query query = QueryFactory.create(findQuery);
        Resource shelterResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                shelterResource = soln.getResource("shelter");
            } else {
                return false; // Shelter not found
            }
        }

        // Remove existing properties
        StmtIterator iter = model.listStatements(shelterResource, null, (RDFNode) null);
        model.remove(iter);

        // Add updated properties
        Property nameProperty = model.createProperty(NAMESPACE + "name");
        Property addressProperty = model.createProperty(NAMESPACE + "address");
        Property contactNumberProperty = model.createProperty(NAMESPACE + "contactNumber");
        Resource typeProperty = model.createResource(NAMESPACE + "Shelter");

        shelterResource.addProperty(RDF.type, typeProperty);
        shelterResource.addProperty(nameProperty, updatedData.get("name"));
        shelterResource.addProperty(addressProperty, updatedData.get("address"));
        shelterResource.addProperty(contactNumberProperty, updatedData.get("contactNumber"));

        // Save the updated model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteShelter(String shelterId) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String findShelterQuery = """
            PREFIX ns: <%s>
            SELECT ?shelter
            WHERE {
                ?shelter a ns:Shelter .
                FILTER(str(?shelter) = "%s%s")
            }
        """.formatted(NAMESPACE, NAMESPACE, shelterId);

        Query query = QueryFactory.create(findShelterQuery);
        Resource shelterResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                shelterResource = soln.getResource("shelter");
            } else {
                return false;
            }
        }

        StmtIterator iter = model.listStatements(shelterResource, null, (RDFNode) null);
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