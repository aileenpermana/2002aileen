package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to HDB Officers in the BTO system.
 */
public class HDBOfficerControl {
    private static final String OFFICER_REGISTRATIONS_FILE = "files/resources/OfficerRegistrations.csv";
    private static final String RECEIPTS_FILE = "files/resources/BookingReceipts.csv";
    private List<Map<String, Object>> officerRegistrations;
    
    /**
     * Constructor initializes the officer registrations list from storage
     */
    public HDBOfficerControl() {
        this.officerRegistrations = loadOfficerRegistrations();
    }
    
    /**
     * Get officer registrations for an officer
     * @param officer the officer
     * @return list of registration records
     */
    public List<Map<String, Object>> getOfficerRegistrations(HDBOfficer officer) {
        List<Map<String, Object>> registrations = new ArrayList<>();
        String officerNRIC = officer.getNRIC();
        
        for (Map<String, Object> reg : officerRegistrations) {
            User regUser = (User) reg.get("officer");
            if (regUser.getNRIC().equals(officerNRIC)) {
                registrations.add(reg);
            }
        }
        
        return registrations;
    }
    
    /**
     * Get officer registrations by NRIC
     * @param nric the user's NRIC
     * @return list of registration records
     */
    public List<Map<String, Object>> getOfficerRegistrationsByNRIC(String nric) {
        List<Map<String, Object>> registrations = new ArrayList<>();
        
        for (Map<String, Object> reg : officerRegistrations) {
            User regUser = (User) reg.get("officer");
            if (regUser.getNRIC().equals(nric)) {
                registrations.add(reg);
            }
        }
        
        return registrations;
    }
    
    /**
     * Get officer registrations for a project
     * @param project the project
     * @return list of registration records
     */
    public List<Map<String, Object>> getOfficerRegistrationsForProject(Project project) {
        List<Map<String, Object>> registrations = new ArrayList<>();
        
        for (Map<String, Object> reg : officerRegistrations) {
            Project regProject = (Project) reg.get("project");
            if (regProject.getProjectID().equals(project.getProjectID())) {
                registrations.add(reg);
            }
        }
        
        return registrations;
    }

        /**
     * Check if a user is registered as an HDB Officer
     * @param nric the NRIC to check
     * @return true if the user is an officer, false otherwise
     */
    public boolean isOfficer(String nric) {
        try {
            File officerFile = new File("files/resources/OfficerList.csv");
            
            if (!officerFile.exists()) {
                return false;
            }
            
            try (Scanner scanner = new Scanner(officerFile)) {
                // Skip header
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    String[] fields = line.split(",");
                    if (fields.length < 2) continue;
                    
                    if (fields[1].trim().equalsIgnoreCase(nric)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error checking officer status: " + e.getMessage());
        }
        
        return false;
    }
        
        // Add this method to HDBOfficerControl to sync officer projects on startup
    public void syncOfficerProjects() {
        // Loop through officer registrations
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer officer = (HDBOfficer) reg.get("officer");
            Project project = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            // If registration is approved, ensure the officer is handling the project
            if (status == RegistrationStatus.APPROVED) {
                officer.addHandlingProject(project);
                project.addOfficer(officer);
            }
        }
    }
        /**
     * Get all approved officers for a project
     * @param project the project
     * @return list of approved officers
     */
    public List<HDBOfficer> getApprovedOfficersForProject(Project project) {
        List<HDBOfficer> approvedOfficers = new ArrayList<>();
        
        // First get from the project's officers list which is guaranteed to contain HDBOfficers
        approvedOfficers.addAll(project.getOfficers());
        
        // Then add any from registrations that may not be in the project's list yet
        for (Map<String, Object> reg : officerRegistrations) {
            Project regProject = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            if (regProject.getProjectID().equals(project.getProjectID()) && 
                status == RegistrationStatus.APPROVED) {
                
                Object officer = reg.get("officer");
                // Only add if it's an HDBOfficer and not already in the list
                if (officer instanceof HDBOfficer) {
                    HDBOfficer hdbOfficer = (HDBOfficer) officer;
                    if (!approvedOfficers.contains(hdbOfficer)) {
                        approvedOfficers.add(hdbOfficer);
                    }
                }
            }
        }
        
        return approvedOfficers;
    }
    
    /**
     * Get pending officer registrations for a project
     * @param project the project
     * @return list of pending registration records
     */
    public List<Map<String, Object>> getPendingRegistrationsForProject(Project project) {
        List<Map<String, Object>> pendingRegistrations = new ArrayList<>();
        
        for (Map<String, Object> reg : officerRegistrations) {
            Project regProject = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            if (regProject.getProjectID().equals(project.getProjectID()) && 
                status == RegistrationStatus.PENDING) {
                pendingRegistrations.add(reg);
            }
        }
        
        return pendingRegistrations;
    }
    
    /**
     * Register a user as an officer candidate for a project
     * @param user the user registering (Applicant or HDBOfficer)
     * @param project the project
     * @return true if registration is successful, false otherwise
     */
    public boolean registerOfficerCandidate(User user, Project project) {
        // Check if user is already registered for this project
        for (Map<String, Object> reg : officerRegistrations) {
            User regUser = (User) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            
            if (regUser.getNRIC().equals(user.getNRIC()) && 
                regProject.getProjectID().equals(project.getProjectID())) {
                return false; // Already registered
            }
        }
        
        // Check if project has available slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Check if applicant is handling another project in the same period
        // This would require checking all approved registrations
        for (Map<String, Object> reg : officerRegistrations) {
            User regUser = (User) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            if (regUser.getNRIC().equals(user.getNRIC()) && 
                status == RegistrationStatus.APPROVED) {
                // Check if date ranges overlap
                Date start1 = regProject.getApplicationOpenDate();
                Date end1 = regProject.getApplicationCloseDate();
                Date start2 = project.getApplicationOpenDate();
                Date end2 = project.getApplicationCloseDate();
                
                // Check if date ranges overlap
                if (!(end1.before(start2) || start1.after(end2))) {
                    return false; // Date ranges overlap
                }
            }
        }
        
        // Check if user has applied for this project as an applicant
        ApplicationControl appControl = new ApplicationControl();
        if (appControl.hasApplicationForProject(user.getNRIC(), project)) {
            return false; // Cannot be both applicant and officer for same project
        }
        
        // Create registration record
        Map<String, Object> registration = new HashMap<>();
        registration.put("officer", user); // Store the user directly
        registration.put("project", project);
        registration.put("status", RegistrationStatus.PENDING);
        registration.put("date", new Date());
        
        // Add to list
        officerRegistrations.add(registration);
        
        // Save to file
        return saveOfficerRegistrations();
    }
    
    /**
 * Process the registration of an officer candidate with file update
 * This should replace the existing processOfficerRegistration method in HDBOfficerControl
 * 
 * @param user the original user who registered
 * @param project the project
 * @param approve true to approve, false to reject
 * @return true if processing was successful, false otherwise
 */
public boolean processOfficerRegistration(User user, Project project, boolean approve) {
    // Find the registration
    for (Map<String, Object> reg : officerRegistrations) {
        User regUser = (User) reg.get("officer");
        Project regProject = (Project) reg.get("project");
        RegistrationStatus status = (RegistrationStatus) reg.get("status");
        
        if (regUser.getNRIC().equals(user.getNRIC()) && 
            regProject.getProjectID().equals(project.getProjectID()) &&
            status == RegistrationStatus.PENDING) {
            
            if (approve) {
                // Check for available slots
                if (project.getAvailableOfficerSlots() <= 0) {
                    return false;
                }
                
                // Convert to HDBOfficer if not already one
                HDBOfficer officer;
                if (regUser instanceof HDBOfficer) {
                    officer = (HDBOfficer) regUser;
                } else {
                    // Create new HDBOfficer using actual user data
                    // First try to find actual user data
                    User actualUser = UserDataLookup.findUserByNRIC(regUser.getNRIC());
                    
                    if (actualUser != null) {
                        // Create HDBOfficer with actual user data
                        officer = new HDBOfficer(
                            actualUser.getName(),
                            actualUser.getNRIC(),
                            actualUser.getPassword(),
                            actualUser.getAge(),
                            actualUser.getMaritalStatus(),
                            "HDBOfficer"
                        );
                    } else {
                        // Create with regUser data as fallback
                        officer = new HDBOfficer(
                            regUser.getName(),
                            regUser.getNRIC(),
                            regUser.getPassword(),
                            regUser.getAge(),
                            regUser.getMaritalStatus(),
                            "HDBOfficer"
                        );
                    }
                    
                    // Add the user to OfficerList.csv if they don't already exist there
                    if (!isUserInOfficerFile(officer.getNRIC())) {
                        addUserToOfficerFile(officer);
                    }
                }
                
                // Update the registration
                reg.put("officer", officer);
                reg.put("status", RegistrationStatus.APPROVED);
                
                // Add officer to project
                project.addOfficer(officer);
                
                // Add project to officer's handling list
                officer.addHandlingProject(project);
                
                // Update project
                ProjectControl projectControl = new ProjectControl();
                projectControl.updateProject(project);
            } else {
                // Reject the registration
                reg.put("status", RegistrationStatus.REJECTED);
            }
            
            // Save changes
            return saveOfficerRegistrations();
        }
    }
    
    return false; // Registration not found
}

/**
 * Check if a user already exists in the OfficerList.csv file
 * @param nric the user's NRIC
 * @return true if user exists, false otherwise
 */
private boolean isUserInOfficerFile(String nric) {
    File officerFile = new File("files/resources/OfficerList.csv");
    if (!officerFile.exists()) {
        return false;
    }
    
    try (Scanner scanner = new Scanner(officerFile)) {
        // Skip header
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            
            String[] values = line.split(",");
            if (values.length < 2) continue;
            
            if (values[1].trim().equalsIgnoreCase(nric)) {
                return true; // Found the user
            }
        }
    } catch (Exception e) {
        System.err.println("Error checking officer file: " + e.getMessage());
    }
    
    return false;
}

/**
 * Add a user to the OfficerList.csv file
 * @param officer the officer to add
 * @return true if successful, false otherwise
 */
private boolean addUserToOfficerFile(HDBOfficer officer) {
    File officerFile = new File("files/resources/OfficerList.csv");
    boolean fileExists = officerFile.exists();
    
    try {
        // Create parent directories if they don't exist
        File parentDir = officerFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter fw = new FileWriter(officerFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            // Add header if file is new
            if (!fileExists) {
                out.println("Name,NRIC,Age,Marital Status,Password");
            }
            
            // Append the officer data
            out.println(
                officer.getName() + "," +
                officer.getNRIC() + "," +
                officer.getAge() + "," +
                officer.getMaritalStatusDisplayValue() + "," +
                officer.getPassword()
            );
            
            return true;
        }
    } catch (IOException e) {
        System.err.println("Error adding user to officer file: " + e.getMessage());
        return false;
    }
}
    
    /**
     * Check if a user is already an approved officer
     * @param nric the user's NRIC
     * @return true if already approved as an officer for any project, false otherwise
     */
    public boolean isAlreadyOfficer(String nric) {
        for (Map<String, Object> reg : officerRegistrations) {
            User regUser = (User) reg.get("officer");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            if (regUser.getNRIC().equals(nric) && 
                status == RegistrationStatus.APPROVED) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Load officer registrations from file
     * @return list of registration records
     */
    private List<Map<String, Object>> loadOfficerRegistrations() {
        List<Map<String, Object>> loadedRegistrations = new ArrayList<>();
        
        try {
            File registrationsFile = new File(OFFICER_REGISTRATIONS_FILE);
            
            // If file doesn't exist, create it with header
            if (!registrationsFile.exists()) {
                File parentDir = registrationsFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(registrationsFile))) {
                    writer.println("OfficerNRIC,ProjectID,Status,RegistrationDate");
                }
                return loadedRegistrations;
            }
            
            try (Scanner fileScanner = new Scanner(registrationsFile)) {
                // Skip header if exists
                if (fileScanner.hasNextLine()) {
                    fileScanner.nextLine();
                }
                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    String[] values = line.split(",");
                    if (values.length < 4) continue; // Invalid line
                    
                    // Parse registration data
                    try {
                        String officerNRIC = values[0].trim();
                        String projectID = values[1].trim();
                        String statusStr = values[2].trim();
                        long registrationDate = Long.parseLong(values[3].trim());
                        
                        // Convert status string to enum
                        RegistrationStatus status = RegistrationStatus.valueOf(statusStr);
                        
                        // Find or create user/officer based on status and actual user data
                        User user;
                        
                        // First try to find the actual user data
                        User actualUser = UserDataLookup.findUserByNRIC(officerNRIC);
                        
                        if (status == RegistrationStatus.APPROVED) {
                            // For approved registrations, create as HDBOfficer
                            if (actualUser != null) {
                                user = new HDBOfficer(
                                    actualUser.getName(),
                                    officerNRIC,
                                    actualUser.getPassword(),
                                    actualUser.getAge(),
                                    actualUser.getMaritalStatus(),
                                    "HDBOfficer"
                                );
                            } else {
                                user = UserDataLookup.createFallbackUser(officerNRIC, "HDBOfficer");
                            }
                        } else {
                            // For pending/rejected, create as the original user type
                            if (actualUser != null) {
                                user = actualUser;
                            } else {
                                user = UserDataLookup.createFallbackUser(officerNRIC, "Applicant");
                            }
                        }
                        
                        // Find or create project
                        Project project = findOrCreateProject(projectID);
                        
                        // Create registration record
                        Map<String, Object> registration = new HashMap<>();
                        registration.put("officer", user);
                        registration.put("project", project);
                        registration.put("status", status);
                        registration.put("date", new Date(registrationDate));
                        
                        // Add to list
                        loadedRegistrations.add(registration);
                        
                    } catch (Exception e) {
                        System.err.println("Error parsing officer registration data: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading officer registrations: " + e.getMessage());
        }
        
        return loadedRegistrations;
    }
    
    /**
     * Save officer registrations to file
     * @return true if successful, false otherwise
     */
    private boolean saveOfficerRegistrations() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(OFFICER_REGISTRATIONS_FILE))) {
                // Write header
                writer.println("OfficerNRIC,ProjectID,Status,RegistrationDate");
                
                // Write registrations
                for (Map<String, Object> reg : officerRegistrations) {
                    User user = (User) reg.get("officer");
                    Project project = (Project) reg.get("project");
                    RegistrationStatus status = (RegistrationStatus) reg.get("status");
                    Date date = (Date) reg.get("date");
                    
                    writer.println(
                        user.getNRIC() + "," +
                        project.getProjectID() + "," +
                        status + "," +
                        date.getTime()
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving officer registrations: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to find or create a project by ID
     * @param projectID the project ID
     * @return the project object
     */
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
            "Project " + projectID, // Placeholder name
            "Neighborhood", // Placeholder neighborhood
            new HashMap<>(), // Placeholder units
            new Date(), // Placeholder open date
            new Date(), // Placeholder close date
            null, // Placeholder manager
            5 // Placeholder officer slots
        );
    }
    
    // Other methods from the original class...

    
    /**
     * Generate a receipt for a booking
     * @param application the application with a booked flat
     * @return the receipt string
     */
    public String generateReceipt(Application application) {
        if (application.getStatus() != ApplicationStatus.BOOKED || application.getBookedFlat() == null) {
            return null; // Cannot generate receipt for non-booked applications
        }
        
        StringBuilder receipt = new StringBuilder();
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        Flat flat = application.getBookedFlat();
        
        receipt.append("====================================================\n");
        receipt.append("                BOOKING RECEIPT                     \n");
        receipt.append("====================================================\n");
        receipt.append("Receipt ID: REC-").append(application.getApplicationID()).append("\n");
        receipt.append("Date: ").append(new Date()).append("\n\n");
        
        receipt.append("APPLICANT DETAILS:\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("NRIC: ").append(applicant.getNRIC()).append("\n");
        receipt.append("Age: ").append(applicant.getAge()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatusDisplayValue()).append("\n\n");
        
        receipt.append("PROJECT DETAILS:\n");
        receipt.append("Project Name: ").append(project.getProjectName()).append("\n");
        receipt.append("Neighborhood: ").append(project.getNeighborhood()).append("\n\n");
        
        receipt.append("FLAT DETAILS:\n");
        receipt.append("Flat ID: ").append(flat.getFlatID()).append("\n");
        receipt.append("Flat Type: ").append(flat.getType().getDisplayValue()).append("\n\n");
        
        receipt.append("This receipt confirms the booking of the flat.\n");
        receipt.append("Please keep this receipt for your records.\n");
        receipt.append("====================================================\n");
        
        // Save receipt to file
        saveReceipt(application);
        
        return receipt.toString();
    }
    
    /**
     * Save a receipt to the receipts file
     * @param application the application with booking details
     */
    private void saveReceipt(Application application) {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources/Receipt.csv");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Check if file exists, create with header if not
            File receiptFile = new File("files/resources/Receipt.csv");
            boolean fileExists = receiptFile.exists();
            
            try (FileWriter fw = new FileWriter(receiptFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter writer = new PrintWriter(bw)) {
                
                // Add header if file is new
                if (!fileExists) {
                    writer.println("ReceiptID,ApplicationID,ApplicantNRIC,ProjectID,FlatID,FlatType,GenerationDate");
                }
                
                // Write receipt data
                Applicant applicant = application.getApplicant();
                Project project = application.getProject();
                Flat flat = application.getBookedFlat();
                
                String receiptID = "REC-" + application.getApplicationID();
                
                writer.println(
                    receiptID + "," +
                    application.getApplicationID() + "," +
                    applicant.getNRIC() + "," +
                    project.getProjectID() + "," +
                    flat.getFlatID() + "," +
                    flat.getType() + "," +
                    System.currentTimeMillis()
                );
            }
        } catch (IOException e) {
            System.out.println("Error loading officer registrations: " + e.getMessage());
        }
    }
    
    /**
     * Find an application by NRIC and project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return the application, or null if not found
     */
    public Application findApplicationByNRIC(String nric, Project project) {
        // Delegate to ApplicationControl to find the application
        ApplicationControl appControl = new ApplicationControl();
        return appControl.findApplicationByNRICAndProject(nric, project);
    }
}