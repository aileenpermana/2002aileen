package entity;

import control.ApplicationControl;
import control.HDBOfficerControl;
import control.ProjectControl;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an HDB Manager in the BTO system.
 * Extends the User class with manager-specific functionality.
 * Demonstrates inheritance (extends User) and method overriding.
 */
public class HDBManager extends User {
    private String managerID;
    private List<Project> managingProjects;
    
    /**
     * Constructor with MaritalStatus enum
     */
    public HDBManager(String name, String NRIC, String password, int age, MaritalStatus maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.managerID = "HM" + NRIC.substring(1, 8);
        this.managingProjects = new ArrayList<>();
    }
    
    /**
     * Constructor with marital status as String
     */
    public HDBManager(String name, String NRIC, String password, int age, String maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.managerID = "HM" + NRIC.substring(1, 8);
        this.managingProjects = new ArrayList<>();
    }
    
    /**
     * Get the manager's ID
     * @return manager ID
     */
    public String getManagerID() {
        return managerID;
    }
    
    /**
     * Check if manager is managing any project in the given date range
     * @param start start date
     * @param end end date
     * @return true if managing a project in the date range, false otherwise
     */
    public boolean isManagingProject(Date start, Date end) {
        for (Project project : managingProjects) {
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
     * Create a new BTO project
     * @param details map containing project details
     * @return the created project, or null if creation failed
     */
    public Project createProject(Map<String, Object> details) {
        // Extract project details
        String projectName = (String) details.get("projectName");
        String neighborhood = (String) details.get("neighborhood");
        Date openDate = (Date) details.get("openDate");
        Date closeDate = (Date) details.get("closeDate");
        int officerSlots = (int) details.get("officerSlots");
        
        // Check if manager is already managing a project in the same period
        if (isManagingProject(openDate, closeDate)) {
            return null;
        }
        
        // Create flat type units map
        Map<FlatType, Integer> totalUnits = new HashMap<>();
        totalUnits.put(FlatType.TWO_ROOM, (int) details.get("twoRoomUnits"));
        totalUnits.put(FlatType.THREE_ROOM, (int) details.get("threeRoomUnits"));
        
        // Create the project
        Project project = new Project(
            generateProjectID(projectName),
            projectName,
            neighborhood,
            totalUnits,
            openDate,
            closeDate,
            this,
            officerSlots
        );
        
        // Add to managing projects
        managingProjects.add(project);
        
        // Save to database via Project Control
        ProjectControl projectControl = new ProjectControl();
        projectControl.addProject(project);
        
        return project;
    }
    
    /**
 * Generate a project ID based on the project name
 * @param projectName the name of the project
 * @return a generated project ID
 */
private String generateProjectID(String projectName) {
    // Create a more consistent ID format:
    // First 3 chars of project name (uppercase) + sequential number (3 digits)
    String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
    
    // Get current projects to determine next sequence number
    ProjectControl projectControl = new ProjectControl();
    List<Project> allProjects = projectControl.getAllProjects();
    
    // Find the highest sequence number for projects with the same prefix
    int maxSequence = 0;
    for (Project project : allProjects) {
        String id = project.getProjectID();
        if (id.startsWith(prefix)) {
            try {
                // Extract the numeric part
                String numericPart = id.substring(prefix.length());
                int sequence = Integer.parseInt(numericPart);
                if (sequence > maxSequence) {
                    maxSequence = sequence;
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                // Ignore malformed IDs
            }
        }
    }
    
    // Next sequence number
    int nextSequence = maxSequence + 1;
    
    // Format with leading zeros (e.g., 001, 002, etc.)
    return prefix + String.format("%03d", nextSequence);
}
    
    /**
     * Edit an existing project
     * @param project the project to edit
     * @param details map containing updated details
     * @return true if edit was successful, false otherwise
     */
    public boolean editProject(Project project, Map<String, Object> details) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Update project details
        if (details.containsKey("projectName")) {
            project.setProjectName((String) details.get("projectName"));
        }
        
        if (details.containsKey("neighborhood")) {
            project.setNeighborhood((String) details.get("neighborhood"));
        }
        
        if (details.containsKey("openDate")) {
            project.setApplicationOpenDate((Date) details.get("openDate"));
        }
        
        if (details.containsKey("closeDate")) {
            project.setApplicationCloseDate((Date) details.get("closeDate"));
        }
        
        if (details.containsKey("twoRoomUnits")) {
            project.setNumberOfUnitsByType(FlatType.TWO_ROOM, (int) details.get("twoRoomUnits"));
        }
        
        if (details.containsKey("threeRoomUnits")) {
            project.setNumberOfUnitsByType(FlatType.THREE_ROOM, (int) details.get("threeRoomUnits"));
        }
        
        if (details.containsKey("officerSlots")) {
            project.setOfficerSlots((int) details.get("officerSlots"));
        }
        
        // Save changes via Project Control
        ProjectControl projectControl = new ProjectControl();
        return projectControl.updateProject(project);
    }
    
    /**
     * Delete a project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Remove from managing projects
        managingProjects.remove(project);
        
        // Delete via Project Control
        ProjectControl projectControl = new ProjectControl();
        return projectControl.deleteProject(project);
    }
    
    /**
     * Toggle the visibility of a project
     * @param project the project to toggle
     * @param visible whether the project should be visible
     * @return true if successful, false otherwise
     */
    public boolean toggleVisibility(Project project, boolean visible) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        project.setVisible(visible);
        
        // Save changes via Project Control
        ProjectControl projectControl = new ProjectControl();
        return projectControl.updateProject(project);
    }
    
    /**
     * Get all projects managed by this manager
     * @return list of projects
     */
    public List<Project> getManagedProjects() {
        return new ArrayList<>(managingProjects);
    }
    
    /**
 * Process an officer's registration for a project
 * Fixed version with proper type handling to avoid casting errors
 * 
 * @param user the user registering as an officer (can be any User type)
 * @param project the project
 * @param approve true to approve, false to reject
 * @return true if operation was successful, false otherwise
 */
public boolean processOfficerRegistration(User user, Project project, boolean approve) {
    // Check if manager is managing this project
    if (!managingProjects.contains(project)) {
        return false;
    }
    
    // Check if there are available slots for approval
    if (approve && project.getAvailableOfficerSlots() <= 0) {
        return false;
    }
    
    // Use HDBOfficerControl to handle the registration update
    HDBOfficerControl officerControl = new HDBOfficerControl();
    
    // Process the registration - no type casting needed since officerControl handles User objects
    return officerControl.processOfficerRegistration(user, project, approve);
}

    public void setManagingProjects(List<Project> managingProjects) {
        this.managingProjects = managingProjects;
    }
    // In HDBManager.java, add this method:

    /**
     * Add a project to the manager's list of managing projects
     * @param project the project to add
     */
    public void addManagedProject(Project project) {
        if (!this.managingProjects.contains(project)) {
            this.managingProjects.add(project);
        }
    }

    private FlatType determineEligibleFlatTypeForWithdrawal(Application application) {
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            if (applicant.getAge() >= 35 && project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
            // For married couples, check which type they're applying for
            // In a real system, this would be stored in the application
            // For now, we'll assume they're applying for 3-Room if available
            if (project.hasFlatType(FlatType.THREE_ROOM)) {
                return FlatType.THREE_ROOM;
            } else if (project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        }
        
        return null;
    }
    
    public boolean processWithdrawalRequest(Application application, boolean approve) {
        Project project = application.getProject();
        
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        if (approve) {
            // Store the current status for logging
            ApplicationStatus currentStatus = application.getStatus();
            System.out.println("Processing withdrawal approval. Current status: " + currentStatus);
            
            // Reset the application status to allow new applications
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            application.setStatusUpdateDate(new Date());
            
            // If the application was successful or booked, increment available units
            if (currentStatus == ApplicationStatus.SUCCESSFUL || currentStatus == ApplicationStatus.BOOKED) {
                // If flat was booked, free it
                if (currentStatus == ApplicationStatus.BOOKED) {
                    Flat bookedFlat = application.getBookedFlat();
                    if (bookedFlat != null) {
                        FlatType flatType = bookedFlat.getType();
                        
                        // Log before incrementing
                        System.out.println("Before incrementing: " + project.getAvailableUnitsByType(flatType) + 
                                         " available units of type " + flatType.getDisplayValue());
                        
                        // Increment the available units for this flat type
                        project.incrementAvailableUnits(flatType);
                        
                        // Log after incrementing
                        System.out.println("After incrementing: " + project.getAvailableUnitsByType(flatType) + 
                                         " available units of type " + flatType.getDisplayValue());
                        
                        // Clear references
                        bookedFlat.setBookedByApplication(null);
                        application.setBookedFlat(null);
                        
                        // Also update the applicant
                        Applicant applicant = application.getApplicant();
                        applicant.setBookedFlat(null);
                        
                        // Update the project in the system
                        ProjectControl projectControl = new ProjectControl();
                        boolean projectUpdated = projectControl.updateProject(project);
                        
                        System.out.println("Project updated: " + projectUpdated);
                    }
                } else if (currentStatus == ApplicationStatus.SUCCESSFUL) {
                    // For successful applications, we need to determine the flat type
                    FlatType flatType =  determineEligibleFlatTypeForWithdrawal(application);
                    if (flatType != null) {
                        project.incrementAvailableUnits(flatType);
                        
                        // Update the project in the system
                        ProjectControl projectControl = new ProjectControl();
                        projectControl.updateProject(project);
                    }
                }
            }

            
            
            // Update the application in the system
            ApplicationControl applicationControl = new ApplicationControl();
            boolean appUpdated = applicationControl.updateApplication(application);
            
            if (appUpdated) {
                applicationControl.saveApplications();
                System.out.println("Application updated successfully.");
                return true;
            } else {
                System.out.println("Failed to update application.");
                return false;
            }
        }
        
        return false;
    }
        
    }
