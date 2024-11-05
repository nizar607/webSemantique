package com.example.web_semantique_project.servlces;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class DonationService {
    private static final String RDF_FILE_PATH = "/home/nizar/Desktop/test_ontologie.rdf";
    private static final String NAMESPACE = "http://www.semanticweb.org/food-rescue-ontology#";

    public boolean addDonation(Map<String, String> donationData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Check if donation already exists for the same date and amount
        String checkQuery = """
            PREFIX ns: <%s>
            ASK {
                ?donation ns:donationDate "%s" ;
                         ns:donationAmount "%s"
            }
        """.formatted(NAMESPACE, donationData.get("donationDate"), donationData.get("donationAmount"));

        Query query = QueryFactory.create(checkQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            if (qexec.execAsk()) {
                return false; // Donation already exists
            }
        }

        // Create a new donation resource
        String donationUri = NAMESPACE + "TimeDonation_" + UUID.randomUUID().toString();
        Resource donation = model.createResource(donationUri);
        Property dateProperty = model.createProperty(NAMESPACE + "donationDate");
        Property amountProperty = model.createProperty(NAMESPACE + "donationAmount");
        Resource typeProperty = model.createResource(NAMESPACE + "TimeDonation");

        // Add properties
        donation.addProperty(RDF.type, typeProperty);
        donation.addProperty(dateProperty, donationData.get("donationDate"));
        donation.addProperty(amountProperty, donationData.get("donationAmount"));

        // Save the model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Rest of the service methods remain the same...
    public List<Map<String, Object>> getAllDonations() {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String queryString = """
            PREFIX ns: <http://www.semanticweb.org/food-rescue-ontology#>
            SELECT ?donation ?donationDate ?donationAmount
            WHERE {
                ?donation a ns:TimeDonation .
                ?donation ns:donationDate ?donationDate .
                ?donation ns:donationAmount ?donationAmount .
            }
        """;

        Query query = QueryFactory.create(queryString);
        List<Map<String, Object>> donationList = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, Object> donationData = new HashMap<>();

                String donationId = soln.get("donation").toString().replace(NAMESPACE, "");
                String donationDate = soln.contains("donationDate") ? soln.get("donationDate").toString() : null;
                String donationAmount = soln.contains("donationAmount") ? soln.get("donationAmount").toString() : null;

                donationData.put("id", donationId);
                donationData.put("donationDate", donationDate);
                donationData.put("donationAmount", donationAmount);

                donationList.add(donationData);
            }
        }

        return donationList;
    }

    public boolean updateDonation(String donationId, Map<String, String> updatedData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String findQuery = """
            PREFIX ns: <%s>
            SELECT ?donation
            WHERE {
                ?donation a ns:TimeDonation .
                FILTER(str(?donation) = "%s%s")
            }
        """.formatted(NAMESPACE, NAMESPACE, donationId);

        Query query = QueryFactory.create(findQuery);
        Resource donationResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                donationResource = soln.getResource("donation");
            } else {
                return false;
            }
        }

        StmtIterator iter = model.listStatements(donationResource, null, (RDFNode) null);
        model.remove(iter);

        Property dateProperty = model.createProperty(NAMESPACE + "donationDate");
        Property amountProperty = model.createProperty(NAMESPACE + "donationAmount");
        Resource typeProperty = model.createResource(NAMESPACE + "TimeDonation");

        donationResource.addProperty(RDF.type, typeProperty);
        donationResource.addProperty(dateProperty, updatedData.get("donationDate"));
        donationResource.addProperty(amountProperty, updatedData.get("donationAmount"));

        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDonation(String donationId) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String findDonationQuery = """
            PREFIX ns: <%s>
            SELECT ?donation
            WHERE {
                ?donation a ns:TimeDonation .
                FILTER(str(?donation) = "%s%s")
            }
        """.formatted(NAMESPACE, NAMESPACE, donationId);

        Query query = QueryFactory.create(findDonationQuery);
        Resource donationResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                donationResource = soln.getResource("donation");
            } else {
                return false;
            }
        }

        StmtIterator iter = model.listStatements(donationResource, null, (RDFNode) null);
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