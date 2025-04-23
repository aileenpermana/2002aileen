package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an HDB Officer in the BTO system.
 * Extends the User class with officer-specific functionality.
 */
public class HDBOfficer extends User {
    private String officerID;
    private List<Project> handlingProjects;
    
    /**
     * Constructor with MaritalStatus enum
     */
    public HDBOfficer(String name, String NRIC, String password, int age, MaritalStatus maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.handlingProjects = new ArrayList<>();
    }
    
    /**
     * Constructor with marital status as String
     */
    public HDBOfficer(String name, String NRIC, String password, int age, String maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.handlingProjects = new ArrayList<>();
    }
    
    /**
     * Get the officer's ID
     * @return officer ID
     */
    public String getOfficerID() {
        return officerID;
    }
    
    /**
     * Check if officer is handling any project in the given date range
     * @param start start date
     * @param end end date
     * @return true if handling a project in the date range, false otherwise
     */
    public boolean isHandlingProject(Date start, Date end) {
        for (Project project : handlingProjects) {
            Date projectStart = project.getApplicationOpenDate();
            Date projectEnd = project.getApplicationCloseDate();
            
            // Check if date ranges overlap
            if (!(end.before(projectStart) || start.after(projectEnd))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Register to handle a project
     * @param project the project to register for
     * @return true if registration successful, false otherwise
     */
    public boolean registerForProject(Project project) {
        // Check if already handling the project
        if (handlingProjects.contains(project)) {
            return false;
        }
        
        // Check if already handling another project in the same period
        if (isHandlingProject(project.getApplicationOpenDate(), project.getApplicationCloseDate())) {
            return false;
        }
        
        // Check if already applied for this project as an applicant
        // This would require checking if this officer has any applications for this project
        // For simplicity, this check is omitted here
        
        // Request to handle the project
        // The actual addition to handlingProjects would happen after approval
        return true;
    }
    
    /**
     * Add a project to the list of projects this officer is handling
     * @param project the project to add
     */
    public void addHandlingProject(Project project) {
        if (!handlingProjects.contains(project)) {
            handlingProjects.add(project);
        }
    }
    
    /**
     * Get the list of projects this officer is handling
     * @return list of projects
     */
    public List<Project> getHandlingProjects() {
        return new ArrayList<>(handlingProjects);
    }
    
    /**
     * Check if officer is handling a specific project
     * @param project the project to check
     * @return true if handling, false otherwise
     */
    public boolean isHandlingProject(Project project) {
        return handlingProjects.contains(project);
    }
    
    /**
     * Book a flat for an applicant
     * @param application the application
     * @param flat the flat to book
     * @return true if booking successful, false otherwise
     */
    public boolean bookFlat(Application application, Flat flat) {
        // Check if officer is handling the project
        if (!isHandlingProject(application.getProject())) {
            return false;
        }
        
        // Check if application is successful and can be booked
        if (!application.canBook()) {
            return false;
        }
        
        // Check if flat is available
        if (flat.isBooked()) {
            return false;
        }
        
        // Book the flat
        flat.setBookedByApplication(application);
        application.setBookedFlat(flat);
        application.setStatus(ApplicationStatus.BOOKED);
        
        // Update the applicant
        Applicant applicant = application.getApplicant();
        applicant.setBookedFlat(flat);
        
        return true;
    }
    
    /**
     * Update the number of available flats for a specific flat type
     * @param project the project
     * @param flatType the type of flat
     * @param count the new count
     */
    public void updateFlatAvailability(Project project, FlatType flatType, int count) {
        // Check if officer is handling the project
        if (isHandlingProject(project)) {
            project.setAvailableUnitsByType(flatType, count);
        }
    }
    
    /**
     * Retrieve an application by NRIC
     * @param nric the applicant's NRIC
     * @return the application or null if not found
     */
    public Application retrieveApplicationByNRIC(String nric, Project project) {
        // In a real system, this would involve searching through a database
        // For simplicity, we'll assume this is done elsewhere and this is just an interface method
        return null;
    }
}