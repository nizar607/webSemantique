package com.example.web_semantique_project.rest;

import com.example.web_semantique_project.servlces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin(origins = "*", maxAge = 3600)

public class GlobalController {




    /****************************************************************************************************************/

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping("/restaurants")
    public List<Map<String, Object>> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @PostMapping("/restaurants")
    public ResponseEntity<String> addRestaurant(@RequestBody Map<String, String> restaurantData) {
        try {
            // Basic validation
            if (!isValidRestaurantData(restaurantData)) {
                return ResponseEntity
                        .badRequest()
                        .body("Missing required fields. Please provide name, address, contactNumber, and operatingHours");
            }

            boolean isAdded = restaurantService.addRestaurant(restaurantData);

            if (isAdded) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body("Restaurant '" + restaurantData.get("name") + "' successfully added");
            } else {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Restaurant already exists or could not be added");
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding restaurant: " + e.getMessage());
        }
    }

    @PutMapping("/restaurants/{name}")
    public ResponseEntity<String> updateRestaurant(
            @PathVariable String name,
            @RequestBody Map<String, String> updatedData) {
        try {
            // Basic validation
            if (!isValidRestaurantData(updatedData)) {
                return ResponseEntity
                        .badRequest()
                        .body("Missing required fields. Please provide name, address, contactNumber, and operatingHours");
            }

            boolean isUpdated = restaurantService.updateRestaurant(name, updatedData);

            if (isUpdated) {
                return ResponseEntity
                        .ok()
                        .body("Restaurant '" + name + "' successfully updated");
            } else {
                return ResponseEntity
                        .notFound()
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating restaurant: " + e.getMessage());
        }
    }

    @DeleteMapping("/restaurants/{name}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable String name) {
        try {
            boolean isDeleted = restaurantService.deleteRestaurant(name);

            if (isDeleted) {
                return ResponseEntity
                        .ok()
                        .body("Restaurant '" + name + "' successfully deleted");
            } else {
                return ResponseEntity
                        .notFound()
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting restaurant: " + e.getMessage());
        }
    }

    private boolean isValidRestaurantData(Map<String, String> restaurantData) {
        return restaurantData != null &&
                restaurantData.containsKey("name") &&
                restaurantData.containsKey("address") &&
                restaurantData.containsKey("contactNumber") &&
                restaurantData.containsKey("operatingHours") &&
                !restaurantData.get("name").trim().isEmpty() &&
                !restaurantData.get("address").trim().isEmpty() &&
                !restaurantData.get("contactNumber").trim().isEmpty() &&
                !restaurantData.get("operatingHours").trim().isEmpty();
    }


    /**************************************************************************************************************/


    @Autowired
    private CommunityCenterService communityCenterService;

    @GetMapping("/community-centers")
    public List<Map<String, Object>> getAllCommunityCenters() {
        return communityCenterService.getAllCommunityCenters();
    }

    @PostMapping("/community-centers")
    public ResponseEntity<String> addCommunityCenter(@RequestBody Map<String, String> centerData) {
        try {
            // Basic validation
            if (!isValidCenterData(centerData)) {
                return ResponseEntity
                        .badRequest()
                        .body("Missing required fields. Please provide name, address, and contactNumber");
            }

            boolean isAdded = communityCenterService.addCommunityCenter(centerData);

            if (isAdded) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body("Community Center '" + centerData.get("name") + "' successfully added");
            } else {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Community Center already exists or could not be added");
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding community center: " + e.getMessage());
        }
    }

    @PutMapping("/community-centers/{name}")
    public ResponseEntity<String> updateCommunityCenter(
            @PathVariable String name,
            @RequestBody Map<String, String> updatedData) {
        try {
            // Basic validation
            if (!isValidCenterData(updatedData)) {
                return ResponseEntity
                        .badRequest()
                        .body("Missing required fields. Please provide name, address, and contactNumber");
            }

            boolean isUpdated = communityCenterService.updateCommunityCenter(name, updatedData);

            if (isUpdated) {
                return ResponseEntity
                        .ok()
                        .body("Community Center '" + name + "' successfully updated");
            } else {
                return ResponseEntity
                        .notFound()
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating community center: " + e.getMessage());
        }
    }

    @DeleteMapping("/community-centers/{name}")
    public ResponseEntity<String> deleteCommunityCenter(@PathVariable String name) {
        try {
            boolean isDeleted = communityCenterService.deleteCommunityCenter(name);

            if (isDeleted) {
                return ResponseEntity
                        .ok()
                        .body("Community Center '" + name + "' successfully deleted");
            } else {
                return ResponseEntity
                        .notFound()
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting community center: " + e.getMessage());
        }
    }

    private boolean isValidCenterData(Map<String, String> centerData) {
        return centerData != null &&
                centerData.containsKey("name") &&
                centerData.containsKey("address") &&
                centerData.containsKey("contactNumber") &&
                !centerData.get("name").trim().isEmpty() &&
                !centerData.get("address").trim().isEmpty() &&
                !centerData.get("contactNumber").trim().isEmpty();
    }

    /**************************************************************************************************************/
    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<String> addUser(@RequestBody Map<String, String> userData) {
        boolean success = userService.addUser(userData);
        if (success) {
            return ResponseEntity.ok("User added successfully");
        } else {
            return ResponseEntity.badRequest().body("User already exists or invalid data");
        }
    }

    @PutMapping("users/{username}")
    public ResponseEntity<String> updateUser(
            @PathVariable String username,
            @RequestBody Map<String, String> updatedData) {
        boolean success = userService.updateUser(username, updatedData);
        if (success) {
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("users/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        boolean success = userService.deleteUser(username);
        if (success) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**************************************************************************************************************/

    @Autowired
    private EventService eventService;

    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> getAllEvents() {
        List<Map<String, Object>> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @PostMapping ("/events")
    public ResponseEntity<String> addEvent(@RequestBody Map<String, String> eventData) {
        boolean success = eventService.addEvent(eventData);
        if (success) {
            return ResponseEntity.ok("Event added successfully");
        } else {
            return ResponseEntity.badRequest().body("Event already exists on this date or invalid data");
        }
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<String> updateEvent(
            @PathVariable String eventId,
            @RequestBody Map<String, String> updatedData) {
        boolean success = eventService.updateEvent(eventId, updatedData);
        if (success) {
            return ResponseEntity.ok("Event updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable String eventId) {
        boolean success = eventService.deleteEvent(eventId);
        if (success) {
            return ResponseEntity.ok("Event deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**************************************************************************************************************/
    @Autowired
    private DonationService donationService;

    @GetMapping("donations")
    public ResponseEntity<List<Map<String, Object>>> getAllDonations() {
        List<Map<String, Object>> donations = donationService.getAllDonations();
        return new ResponseEntity<>(donations, HttpStatus.OK);
    }

    @PostMapping("donations")
    public ResponseEntity<String> addDonation(@RequestBody Map<String, String> donationData) {
        try {
            boolean isAdded = donationService.addDonation(donationData);
            if (isAdded) {
                return new ResponseEntity<>("Donation added successfully", HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Donation already exists", HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding donation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("donations/{donationId}")
    public ResponseEntity<String> updateDonation(@PathVariable String donationId,
                                                 @RequestBody Map<String, String> updatedData) {
        try {
            boolean isUpdated = donationService.updateDonation(donationId, updatedData);
            if (isUpdated) {
                return new ResponseEntity<>("Donation updated successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Donation not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating donation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("donations/{donationId}")
    public ResponseEntity<String> deleteDonation(@PathVariable String donationId) {
        try {
            boolean isDeleted = donationService.deleteDonation(donationId);
            if (isDeleted) {
                return new ResponseEntity<>("Donation deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Donation not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting donation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**************************************************************************************************************/


    @Autowired
    private ShelterService shelterService;

    @GetMapping("/shelters")
    public ResponseEntity<List<Map<String, Object>>> getAllShelters() {
        List<Map<String, Object>> shelters = shelterService.getAllShelters();
        return ResponseEntity.ok(shelters);
    }

    @PostMapping("/shelters")
    public ResponseEntity<String> addShelter(@RequestBody Map<String, String> shelterData) {
        boolean success = shelterService.addShelter(shelterData);
        if (success) {
            return ResponseEntity.ok("Shelter added successfully");
        } else {
            return ResponseEntity.badRequest().body("Shelter already exists or invalid data");
        }
    }

    @PutMapping("shelters/{shelterId}")
    public ResponseEntity<String> updateShelter(
            @PathVariable String shelterId,
            @RequestBody Map<String, String> updatedData) {
        boolean success = shelterService.updateShelter(shelterId, updatedData);
        if (success) {
            return ResponseEntity.ok("Shelter updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("shelters/{shelterId}")
    public ResponseEntity<String> deleteShelter(@PathVariable String shelterId) {
        boolean success = shelterService.deleteShelter(shelterId);
        if (success) {
            return ResponseEntity.ok("Shelter deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**************************************************************************************************************/


}
