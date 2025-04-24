package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to Applications in the BTO system.
 */
public class ApplicationControl {
    private static final String APPLICATIONS_FILE = "files/resources/ApplicationList.csv";
    private static final String WITHDRAWAL_REQUESTS_FILE = "files/resources/WithdrawalRequests.csv";
    private List<Application> applications;
    private List<Map<String, Object>> withdrawalRequests;
    
    /**
     * Constructor initializes the applications list from storage
     */
    public ApplicationControl() {
        this.applications = loadApplications();
        this.withdrawalRequests = loadWithdrawalRequests();
    }
    
    /**
     * Submit a new application
     * @param applicant the applicant
     * @param project the project to apply for
     * @return true if submission is successful, false otherwise
     */
    public boolean submitApplication(Applicant applicant, Project project) {
        // Check if applicant already has an active application
        if (applicant.hasActiveApplications()) {
            return false;
        }
        
        // Check if applicant is eligible for this project
        if (!project.checkEligibility(applicant, project.getProjectID())) {
            return false;
        }
        
        // Check if project is open for application
        if (!project.isOpenForApplication()) {
            return false;
        }
        
        // Check if project is visible
        if (!project.isVisible()) {
            return false;
        }
        
        // Create new application
        Application application = new Application(applicant, project);
        
        // Add to applicant's list
        applicant.addApplication(application);
        
        // Add to list
        applications.add(application);
        
        // Save to file
        return saveApplications();
    }
    
    /**
 * Helper method to find or create an applicant by NRIC with actual user data
 * This method should replace the existing findOrCreateApplicant method in ApplicationControl
 * 
 * @param nric the applicant's NRIC
 * @return the applicant object with actual data
 */
private Applicant findOrCreateApplicant(String nric) {
    // Try to find the actual applicant data using UserDataLookup
    User user = UserDataLookup.findUserByNRIC(nric);
    
    if (user != null) {
        // If found but not an Applicant, convert to Applicant
        if (user instanceof Applicant) {
            return (Applicant) user;
        } else {
            // Create a new Applicant with the actual user data
            return new Applicant(
                user.getName(),
                user.getNRIC(),
                user.getPassword(),
                user.getAge(),
                user.getMaritalStatus(),
                "Applicant"
            );
        }
    }
    
    // If not found, create a placeholder applicant
    // Use the UserDataLookup utility for consistency
    return (Applicant) UserDataLookup.createFallbackUser(nric, "Applicant");
}

public boolean withdrawApplication(Application application) {
    if (!application.canWithdraw()) {
        return false;
    }
    
    // Store the original status for later use
    ApplicationStatus originalStatus = application.getStatus();
    
    // Update the application status to withdrawal requested
    application.setStatus(ApplicationStatus.WITHDRAW);
    application.setStatusUpdateDate(new Date());
    
    // If this is a BOOKED application, we need to capture the flat info for logging purposes
    String bookedFlatInfo = "";
    if (originalStatus == ApplicationStatus.BOOKED && application.getBookedFlat() != null) {
        Flat bookedFlat = application.getBookedFlat();
        bookedFlatInfo = String.format("Flat [ID: %s, Type: %s]", 
                                     bookedFlat.getFlatID(), 
                                     bookedFlat.getType().getDisplayValue());
        
        System.out.println("Withdrawal requested for booked flat: " + bookedFlatInfo);
    }
    
    boolean success = updateApplication(application);
    if (success) {
        System.out.println("Application status updated to WITHDRAW. Original status: " + originalStatus);
        saveApplications();
    }
    
    return success;
}

    
    
    
    /**
     * Get all applications in the system
     * @return list of all applications
     */
    public List<Application> getAllApplications() {
        return new ArrayList<>(applications);
    }
    
    /**
     * Get all applications for a project
     * @param project the project
     * @return list of all applications for the project
     */
    public List<Application> getAllApplications(Project project) {
        List<Application> projectApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID())) {
                projectApplications.add(app);
            }
        }
        
        return projectApplications;
    }
    
    public List<Application> getPendingApplications(Project project) {
        List<Application> pendingApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID()) && 
                app.getStatus() == ApplicationStatus.PENDING) {
                pendingApplications.add(app);
            }
        }
        
        return pendingApplications;
    }

    
    
    /**
     * Get successful (approved but not yet booked) applications for a project
     * @param project the project
     * @return list of successful applications
     */
    public List<Application> getSuccessfulApplications(Project project) {
        List<Application> successfulApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID()) && 
                app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                successfulApplications.add(app);
            }
        }
        
        return successfulApplications;
    }
    
    /**
     * Get booked applications for a project
     * @param project the project
     * @return list of booked applications
     */
    public List<Application> getBookedApplications(Project project) {
        List<Application> bookedApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID()) && 
                app.getStatus() == ApplicationStatus.BOOKED) {
                bookedApplications.add(app);
            }
        }
        
        return bookedApplications;
    }
    
    public List<Application> getWithdrawalRequests(Project project) {
        List<Application> withdrawalRequests = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID()) && 
                app.getStatus() == ApplicationStatus.WITHDRAW) {
                withdrawalRequests.add(app);
            }
        }
        
        return withdrawalRequests;
    }

    
    
    /**
     * Update an application
     * @param application the application to update
     * @return true if update is successful, false otherwise
     */
    public boolean updateApplication(Application application) {
        // Find and update application
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getApplicationID().equals(application.getApplicationID())) {
                applications.set(i, application);
                return saveApplications();
            }
        }
        
        return false; // Application not found
    }
    
    /**
     * Get applications for an applicant
     * @param applicant the applicant
     * @return list of applications for the applicant
     */
    public List<Application> getApplicationsForApplicant(Applicant applicant) {
        List<Application> applicantApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantApplications.add(app);
            }
        }
        
        return applicantApplications;
    }
    
    /**
     * Get application by ID
     * @param applicationID the application ID
     * @return the application, or null if not found
     */
    public Application getApplicationByID(String applicationID) {
        for (Application app : applications) {
            if (app.getApplicationID().equals(applicationID)) {
                return app;
            }
        }
        
        return null;
    }
    
    /**
     * Find an application by applicant NRIC and project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return the application, or null if not found
     */
    public Application findApplicationByNRICAndProject(String nric, Project project) {
        for (Application app : applications) {
            if (app.getApplicant().getNRIC().equals(nric) && 
                app.getProject().getProjectID().equals(project.getProjectID())) {
                return app;
            }
        }
        
        return null;
    }
    
    /**
     * Check if an applicant has an application for a project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return true if has application, false otherwise
     */
    public boolean hasApplicationForProject(String nric, Project project) {
        return findApplicationByNRICAndProject(nric, project) != null;
    }
    
    /**
     * Process applications for a project (approve or reject)
     * @param project the project
     * @param decisions map of application IDs to decisions (true=approve, false=reject)
     * @return number of applications processed successfully
     */
    public int processApplications(Project project, Map<String, Boolean> decisions) {
        int processed = 0;
        
        for (Application app : getPendingApplications(project)) {
            Boolean decision = decisions.get(app.getApplicationID());
            if (decision == null) {
                continue; // No decision for this application
            }
            
            if (decision) {
                // Approve application
                // Check if there are available units of the requested type
                FlatType requestedType = determineEligibleFlatType(app);
                
                if (requestedType != null && project.getAvailableUnitsByType(requestedType) > 0) {
                    app.setStatus(ApplicationStatus.SUCCESSFUL);
                    
                    // Decrement available units
                    project.decrementAvailableUnits(requestedType);
                    
                    // Update project
                    ProjectControl projectControl = new ProjectControl();
                    projectControl.updateProject(project);
                    
                    processed++;
                } else {
                    // Not enough units, reject instead
                    app.setStatus(ApplicationStatus.UNSUCCESSFUL);
                    processed++;
                }
            } else {
                // Reject application
                app.setStatus(ApplicationStatus.UNSUCCESSFUL);
                processed++;
            }
            app.setStatusUpdateDate(new Date());
            boolean success = updateApplication(app);
            if (success) {
                saveApplications();
            }
        }
        
        
        return processed;
    }

    public List<Application> getApplicationByUser(Applicant applicant) {
        List<Application> allApplication = getAllApplications();
        List<Application> applicantApplication = new ArrayList<>();
        
        for (Application app : allApplication) {
            if (app.getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantApplication.add(app);
            }
        }
        
        return applicantApplication;
    }
    
    
    /**
     * Determine which flat type an applicant is eligible for
     * @param application the application
     * @return the eligible flat type, or null if none
     */
    private FlatType determineEligibleFlatType(Application application) {
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            if (project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
            // For married couples, check which type they're applying for
            // In a real system, this would be stored in the application
            // For now, we'll return the largest available flat type
            if (project.hasFlatType(FlatType.THREE_ROOM)) {
                return FlatType.THREE_ROOM;
            } else if (project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        }
        
        return null;
    }
    
    private List<Application> loadApplications() {
        List<Application> loadedApplications = new ArrayList<>();
        
        try {
            File applicationsFile = new File(APPLICATIONS_FILE);
            
            // If file doesn't exist, create it with header
            if (!applicationsFile.exists()) {
                File parentDir = applicationsFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(applicationsFile))) {
                    writer.println("ApplicationID,ApplicantNRIC,ProjectID,Status,ApplicationDate,StatusUpdateDate,BookedFlatID");
                }
                return loadedApplications;
            }
            
            try (Scanner fileScanner = new Scanner(applicationsFile)) {
                // Skip header if exists
                if (fileScanner.hasNextLine()) {
                    fileScanner.nextLine();
                }
                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    
                    String[] values = line.split(",");
                    if (values.length < 6) {
                        System.err.println("ERROR: Invalid application line format: " + line);
                        continue; // Invalid line
                    }
                    
                    // Parse application data
                    try {
                        String applicationID = values[0].trim();
                        String applicantNRIC = values[1].trim();
                        String projectID = values[2].trim();
                        String statusStr = values[3].trim();
                        long applicationDate = Long.parseLong(values[4].trim());
                        long statusUpdateDate = Long.parseLong(values[5].trim());
                        String bookedFlatID = values.length > 6 ? values[6].trim() : "";
                        
                        // Convert status string to enum - Extra debug logging for important statuses
                        ApplicationStatus status;
                        try {
                            status = ApplicationStatus.valueOf(statusStr);
                            
                        } catch (IllegalArgumentException e) {
                            System.err.println("ERROR: Invalid status in application: " + statusStr);
                            System.err.println("Defaulting to PENDING status");
                            status = ApplicationStatus.PENDING;
                        }
                        
                        // Find or create applicant
                        Applicant applicant = findOrCreateApplicant(applicantNRIC);
                        
                        // Find or create project
                        Project project = findOrCreateProject(projectID);
                        
                        // Create application with ID and dates
                        Application application = new Application(applicant, project);
                        
                        // Use reflection to set fields that don't have setters
                        setPrivateField(application, "applicationID", applicationID);
                        setPrivateField(application, "status", status);
                        setPrivateField(application, "applicationDate", new Date(applicationDate));
                        setPrivateField(application, "statusUpdateDate", new Date(statusUpdateDate));
                        
                        // Set booked flat if any
                        if (!bookedFlatID.isEmpty()) {
                            Flat flat = new Flat(bookedFlatID, project, determineFlatTypeFromID(bookedFlatID));
                            application.setBookedFlat(flat);
                            applicant.setBookedFlat(flat);
                        }
                        
                        // Add to list
                        loadedApplications.add(application);
                        
                    } catch (Exception e) {
                        System.err.println("Error parsing application data: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading applications: " + e.getMessage());
        }
        
        return loadedApplications;
    }

    
    
    /**
     * Load withdrawal requests from file
     * @return list of withdrawal requests
     */
    private List<Map<String, Object>> loadWithdrawalRequests() {
        List<Map<String, Object>> loadedRequests = new ArrayList<>();
        
        try {
            File requestsFile = new File(WITHDRAWAL_REQUESTS_FILE);
            
            // If file doesn't exist, create it with header
            if (!requestsFile.exists()) {
                File parentDir = requestsFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(requestsFile))) {
                    writer.println("ApplicationID,RequestDate,Status");
                }
                return loadedRequests;
            }
            
            try (Scanner fileScanner = new Scanner(requestsFile)) {
                // Skip header if exists
                if (fileScanner.hasNextLine()) {
                    fileScanner.nextLine();
                }
                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    String[] values = line.split(",");
                    if (values.length < 3) continue; // Invalid line
                    
                    // Parse request data
                    try {
                        String applicationID = values[0].trim();
                        long requestDate = Long.parseLong(values[1].trim());
                        String status = values[2].trim();
                        
                        // Find application
                        Application application = getApplicationByID(applicationID);
                        if (application == null) {
                            continue; // Skip if application not found
                        }
                        
                        // Create request
                        Map<String, Object> request = new HashMap<>();
                        request.put("application", application);
                        request.put("requestDate", new Date(requestDate));
                        request.put("status", status);
                        
                        // Add to list
                        loadedRequests.add(request);
                        
                    } catch (Exception e) {
                        System.err.println("Error parsing withdrawal request data: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading withdrawal requests: " + e.getMessage());
        }
        
        return loadedRequests;
    }
    
    public boolean saveApplications() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Create backup of existing file before saving
            File existingFile = new File(APPLICATIONS_FILE);
            if (existingFile.exists()) {
                String backupFile = APPLICATIONS_FILE + ".bak";
                try {
                    java.nio.file.Files.copy(existingFile.toPath(), new File(backupFile).toPath(), 
                                     java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Backup created: " + backupFile);
                } catch (Exception e) {
                    System.err.println("Warning: Could not create backup: " + e.getMessage());
                }
            }
            
            System.out.println("Saving " + applications.size() + " applications to " + APPLICATIONS_FILE);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(APPLICATIONS_FILE))) {
                // Write header
                writer.println("ApplicationID,ApplicantNRIC,ProjectID,Status,ApplicationDate,StatusUpdateDate,BookedFlatID");
                
                // Write applications - Make sure status is properly saved
                for (Application app : applications) {
                    // Create a debug string to log special statuses
                    if (app.getStatus() == ApplicationStatus.WITHDRAW || 
                        app.getStatus() == ApplicationStatus.UNSUCCESSFUL) {
                        
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(app.getApplicationID()).append(",");
                    sb.append(app.getApplicant().getNRIC()).append(",");
                    sb.append(app.getProject().getProjectID()).append(",");
                    sb.append(app.getStatus()).append(","); // Save enum name
                    sb.append(app.getApplicationDate().getTime()).append(",");
                    sb.append(app.getStatusUpdateDate().getTime());
                    
                    // Add booked flat ID if any
                    if (app.getBookedFlat() != null) {
                        sb.append(",").append(app.getBookedFlat().getFlatID());
                    }
                    
                    writer.println(sb.toString());
                }
                
                System.out.println("Applications saved successfully.");
                return true;
            }
        } catch (IOException e) {
            System.err.println("ERROR saving applications: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return false;
        }
    }
    
    /**
     * Save withdrawal requests to file
     * @return true if successful, false otherwise
     */
    private boolean saveWithdrawalRequests() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(WITHDRAWAL_REQUESTS_FILE))) {
                // Write header
                writer.println("ApplicationID,RequestDate,Status");
                
                // Write requests
                for (Map<String, Object> request : withdrawalRequests) {
                    Application app = (Application) request.get("application");
                    Date requestDate = (Date) request.get("requestDate");
                    String status = (String) request.get("status");
                    
                    writer.println(
                        app.getApplicationID() + "," +
                        requestDate.getTime() + "," +
                        status
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving withdrawal requests: " + e.getMessage());
            return false;
        }
    }
    
    
    
    /**
     * Helper method to find or create a project by ID
     * @param projectID the project ID
     * @return the project object*/
     
    private Project findOrCreateProject(String projectID) {
        // Get the project from project control if possible
        ProjectControl projectControl = new ProjectControl();
        List<Project> allProjects = projectControl.getAllProjects();
        
        for (Project p : allProjects) {
            if (p.getProjectID().equals(projectID)) {
                return p;
            }
        }
        
        // If not found, create a placeholder
        return new Project(
            projectID,
            "Project", // Placeholder name
            "Neighborhood", // Placeholder neighborhood
            new HashMap<>(), // Placeholder units
            new Date(), // Placeholder open date
            new Date(), // Placeholder close date
            null, // Placeholder manager
            5 // Placeholder officer slots
        );
    }
    
    /**
     * Helper method to use reflection to set private fields
     * @param object the object
     * @param fieldName the field name
     * @param value the value to set
     */
    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            System.err.println("Error setting private field: " + e.getMessage());
        }
    }
    
    /**
     * Determine flat type from flat ID
     * @param flatID the flat ID
     * @return the flat type
     */
    private FlatType determineFlatTypeFromID(String flatID) {
        // Assume flat ID format like "F{projectID}-{flatTypeChar}-{number}"
        // where flatTypeChar is 'T' for TWO_ROOM or 'H' for THREE_ROOM
        try {
            char typeChar = flatID.split("-")[1].charAt(0);
            if (typeChar == 'T') {
                return FlatType.TWO_ROOM;
            } else if (typeChar == 'H') {
                return FlatType.THREE_ROOM;
            }
        } catch (Exception e) {
            // Default to TWO_ROOM if we can't determine
        }
        
        return FlatType.TWO_ROOM;
    }
}