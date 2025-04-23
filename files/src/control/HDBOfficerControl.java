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
        
        // Also check for registrations with matching NRIC (for applicants checking their officer registrations)
        String officerNRIC = officer.getNRIC();
        
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            if (regOfficer.getOfficerID().equals(officer.getOfficerID()) || 
                regOfficer.getNRIC().equals(officerNRIC)) {
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
     * Register an officer for a project
     * @param officer the officer
     * @param project the project
     * @return true if registration is successful, false otherwise
     */
    public boolean registerOfficer(HDBOfficer officer, Project project) {
        // Check if officer is already registered for this project
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            
            if (regOfficer.getNRIC().equals(officer.getNRIC()) && 
                regProject.getProjectID().equals(project.getProjectID())) {
                return false; // Already registered
            }
        }
        
        // Check if project has available slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Check if officer is handling another project in the same period
        if (officer.isHandlingProject(project.getApplicationOpenDate(), project.getApplicationCloseDate())) {
            return false;
        }
        
        // Check if officer has applied for this project as an applicant
        ApplicationControl appControl = new ApplicationControl();
        if (appControl.hasApplicationForProject(officer.getNRIC(), project)) {
            return false; // Cannot be both applicant and officer for same project
        }
        
        // Create registration record
        Map<String, Object> registration = new HashMap<>();
        registration.put("officer", officer);
        registration.put("project", project);
        registration.put("status", RegistrationStatus.PENDING);
        registration.put("date", new Date());
        
        // Add to list
        officerRegistrations.add(registration);
        
        // Save to file
        return saveOfficerRegistrations();
    }
    
    /**
     * Update the status of an officer registration
     * @param officer the officer
     * @param project the project
     * @param status the new status
     * @return true if update is successful, false otherwise
     */
    public boolean updateRegistrationStatus(HDBOfficer officer, Project project, RegistrationStatus status) {
        // Find and update registration
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            
            if (regOfficer.getNRIC().equals(officer.getNRIC()) && 
                regProject.getProjectID().equals(project.getProjectID())) {
                
                // If already has this status, nothing to do
                if (reg.get("status") == status) {
                    return true;
                }
                
                reg.put("status", status);
                
                // If approved, add officer to project
                if (status == RegistrationStatus.APPROVED) {
                    // Add officer to project
                    project.addOfficer(officer);
                    
                    // Add project to officer's handling list
                    officer.addHandlingProject(project);
                    
                    // Decrement available slots
                    project.decrementOfficerSlots();
                    
                    // Update project
                    ProjectControl projectControl = new ProjectControl();
                    projectControl.updateProject(project);
                }
                
                return saveOfficerRegistrations();
            }
        }
        
        return false; // Registration not found
    }
    
    /**
     * Load officer registrations from file
     * @return list of registration records
     */
    private List<Map<String, Object>> loadOfficerRegistrations() {
        List<Map<String, Object>> loadedRegistrations = new ArrayList<>();
        
        try {
            File registrationsFile = new File("files/resources/OfficerRegistrations.csv");
            
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
                        
                        // Find or create officer
                        HDBOfficer officer = findOrCreateOfficer(officerNRIC);
                        
                        // Find or create project
                        Project project = findOrCreateProject(projectID);
                        
                        // Create registration record
                        Map<String, Object> registration = new HashMap<>();
                        registration.put("officer", officer);
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
            
            try (PrintWriter writer = new PrintWriter(new FileWriter("files/resources/OfficerRegistrations.csv"))) {
                // Write header
                writer.println("OfficerNRIC,ProjectID,Status,RegistrationDate");
                
                // Write registrations
                for (Map<String, Object> reg : officerRegistrations) {
                    HDBOfficer officer = (HDBOfficer) reg.get("officer");
                    Project project = (Project) reg.get("project");
                    RegistrationStatus status = (RegistrationStatus) reg.get("status");
                    Date date = (Date) reg.get("date");
                    
                    writer.println(
                        officer.getNRIC() + "," +
                        project.getProjectID() + "," +
                        status + "," +
                        date.getTime()
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving receipt: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find an application by NRIC and project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return the application, or null if not found
     */
    public Application findApplicationByNRICAndProject(String nric, Project project) {
        // Delegate to ApplicationControl to find the application
        ApplicationControl appControl = new ApplicationControl();
        return appControl.findApplicationByNRICAndProject(nric, project);
    }
    
    /**
     * Get all handled projects by an officer
     * @param officer the HDB officer
     * @return list of projects the officer is handling
     */
    public List<Project> getHandledProjects(HDBOfficer officer) {
        List<Project> handledProjects = new ArrayList<>();
        
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            Project project = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            if (regOfficer.getNRIC().equals(officer.getNRIC()) && 
                status == RegistrationStatus.APPROVED) {
                handledProjects.add(project);
            }
        }
        
        return handledProjects;
    }
    
    /**
     * Reply to an enquiry
     * @param enquiry the enquiry to reply to
     * @param officer the officer making the reply
     * @param replyContent the reply content
     * @return true if reply was successful, false otherwise
     */
    public boolean replyToEnquiry(Enquiry enquiry, HDBOfficer officer, String replyContent) {
        // Check if officer is handling the project
        if (!officer.isHandlingProject(enquiry.getProject())) {
            return false;
        }
        
        // Format reply with officer info
        String formattedReply = "HDB Officer " + officer.getName() + " [" + 
                             officer.getOfficerID() + "]: " + replyContent;
        
        // Add reply to enquiry
        EnquiryControl enquiryControl = new EnquiryControl();
        return enquiryControl.addReply(enquiry, formattedReply, officer);
    }
    
    /**
     * Get booked applications for a project
     * @param project the project
     * @return list of booked applications
     */
    public List<Application> getBookedApplications(Project project) {
        ApplicationControl appControl = new ApplicationControl();
        return appControl.getBookedApplications(project);
    }
    
    /**
     * Get successful (approved but not yet booked) applications for a project
     * @param project the project
     * @return list of successful applications
     */
    public List<Application> getSuccessfulApplications(Project project) {
        ApplicationControl appControl = new ApplicationControl();
        return appControl.getSuccessfulApplications(project);
    }
    
    /**
     * Check if a user is already an officer for a project
     * @param nric the user's NRIC
     * @param project the project
     * @return true if user is an officer for the project, false otherwise
     */
    public boolean isOfficerForProject(String nric, Project project) {
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            
            if (regOfficer.getNRIC().equals(nric) && 
                regProject.getProjectID().equals(project.getProjectID()) &&
                status == RegistrationStatus.APPROVED) {
                return true;
            }
        }
        
        return false;
    }

    
    /**
     * Helper method to find or create an officer by NRIC
     * @param nric the officer's NRIC
     * @return the officer object
     */
    private HDBOfficer findOrCreateOfficer(String nric) {
        // In a real system, this would check a database or repository
        // For now, create a placeholder officer
        return new HDBOfficer(
            "Officer", // Placeholder name
            nric,
            "password",
            30, // Placeholder age
            "Married", // Placeholder marital status
            "HDBOfficer"
        );
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
     * Book a flat for an applicant
     * @param officer the HDB officer processing the booking
     * @param application the application
     * @param flatType the flat type to book
     * @return true if booking is successful, false otherwise
     */
    public boolean bookFlat(HDBOfficer officer, Application application, FlatType flatType) {
        // Check if officer is handling the project
        if (!officer.isHandlingProject(application.getProject())) {
            return false;
        }
        
        // Check if application is successful and can be booked
        if (!application.canBook()) {
            return false;
        }
        
        Project project = application.getProject();
        
        // Check if there are available units of this type
        if (project.getAvailableUnitsByType(flatType) <= 0) {
            return false;
        }
        
        // Generate a new flat
        String flatID = "F" + project.getProjectID() + "-" + flatType.toString().charAt(0) + "-" + 
                       (project.getTotalUnitsByType(flatType) - project.getAvailableUnitsByType(flatType) + 1);
        
        Flat newFlat = new Flat(flatID, project, flatType);
        
        // Book the flat
        newFlat.setBookedByApplication(application);
        application.setBookedFlat(newFlat);
        application.setStatus(ApplicationStatus.BOOKED);
        
        // Update the applicant
        Applicant applicant = application.getApplicant();
        applicant.setBookedFlat(newFlat);
        
        // Update available units count
        project.decrementAvailableUnits(flatType);
        
        // Update application in storage
        ApplicationControl appControl = new ApplicationControl();
        appControl.updateApplication(application);
        
        // Update project in storage
        ProjectControl projectControl = new ProjectControl();
        projectControl.updateProject(project);
        
        return true;
    }
    
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