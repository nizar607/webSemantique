package com.example.web_semantique_project.servlces;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class EventService {
    private static final String RDF_FILE_PATH = "/home/nizar/Desktop/test_ontologie.rdf";
    private static final String NAMESPACE = "http://www.semanticweb.org/food-rescue-ontology#";

    public List<Map<String, Object>> getAllEvents() {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String queryString = """
            PREFIX ns: <http://www.semanticweb.org/food-rescue-ontology#>
            SELECT ?event ?eventDate ?eventDuration
            WHERE {
                ?event a ns:CharityEvent .
                ?event ns:eventDate ?eventDate .
                ?event ns:eventDuration ?eventDuration .
            }
        """;

        Query query = QueryFactory.create(queryString);
        List<Map<String, Object>> eventList = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, Object> eventData = new HashMap<>();

                String eventId = soln.get("event").toString().replace(NAMESPACE, "");
                String eventDate = soln.contains("eventDate") ? soln.get("eventDate").toString() : null;
                String eventDuration = soln.contains("eventDuration") ? soln.get("eventDuration").toString() : null;

                eventData.put("id", eventId);
                eventData.put("eventDate", eventDate);
                eventData.put("eventDuration", eventDuration);

                eventList.add(eventData);
            }
        }

        return eventList;
    }

    public boolean addEvent(Map<String, String> eventData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Check if event already exists for the same date
        String checkQuery = """
            PREFIX ns: <%s>
            ASK {
                ?event a ns:CharityEvent ;
                       ns:eventDate "%s"
            }
        """.formatted(NAMESPACE, eventData.get("eventDate"));

        Query query = QueryFactory.create(checkQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            if (qexec.execAsk()) {
                return false; // Event already exists on this date
            }
        }

        // Create a new charity event resource
        String eventUri = NAMESPACE + "Event_" + UUID.randomUUID().toString();
        Resource event = model.createResource(eventUri);
        Property dateProperty = model.createProperty(NAMESPACE + "eventDate");
        Property durationProperty = model.createProperty(NAMESPACE + "eventDuration");
        Resource typeProperty = model.createResource(NAMESPACE + "CharityEvent");

        // Add properties
        event.addProperty(RDF.type, typeProperty);
        event.addProperty(dateProperty, eventData.get("eventDate"));
        event.addProperty(durationProperty, eventData.get("eventDuration"));

        // Save the model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEvent(String eventId, Map<String, String> updatedData) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        // Find the event resource
        String findQuery = """
            PREFIX ns: <%s>
            SELECT ?event
            WHERE {
                ?event a ns:CharityEvent .
                FILTER(str(?event) = "%s%s")
            }
        """.formatted(NAMESPACE, NAMESPACE, eventId);

        Query query = QueryFactory.create(findQuery);
        Resource eventResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                eventResource = soln.getResource("event");
            } else {
                return false; // Event not found
            }
        }

        // Remove existing properties
        StmtIterator iter = model.listStatements(eventResource, null, (RDFNode) null);
        model.remove(iter);

        // Add updated properties
        Property dateProperty = model.createProperty(NAMESPACE + "eventDate");
        Property durationProperty = model.createProperty(NAMESPACE + "eventDuration");
        Resource typeProperty = model.createResource(NAMESPACE + "CharityEvent");

        eventResource.addProperty(RDF.type, typeProperty);
        eventResource.addProperty(dateProperty, updatedData.get("eventDate"));
        eventResource.addProperty(durationProperty, updatedData.get("eventDuration"));

        // Save the updated model
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEvent(String eventId) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_FILE_PATH);

        String findEventQuery = """
            PREFIX ns: <%s>
            SELECT ?event
            WHERE {
                ?event a ns:CharityEvent .
                FILTER(str(?event) = "%s%s")
            }
        """.formatted(NAMESPACE, NAMESPACE, eventId);

        Query query = QueryFactory.create(findEventQuery);
        Resource eventResource = null;

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                eventResource = soln.getResource("event");
            } else {
                return false;
            }
        }

        StmtIterator iter = model.listStatements(eventResource, null, (RDFNode) null);
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