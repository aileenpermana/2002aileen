package control;

import entity.*;
import java.util.*;

/**
 * Controls operations related to HDB Managers in the BTO system.
 * Demonstrates the Controller pattern in MVC architecture.
 */
public class HDBManagerControl {
    private ProjectControl projectControl;
    
    /**
     * Constructor for HDBManagerControl
     */
    public HDBManagerControl() {
        this.projectControl = new ProjectControl();
    }
    
    /**
     * Get officer registrations for a project
     * @param project the project
     * @return list of officer registrations
     */
    public List<Map<String, Object>> getOfficerRegistrations(Project project) {
        // Delegate to HDBOfficerControl
        HDBOfficerControl officerControl = new HDBOfficerControl();
        return officerControl.getOfficerRegistrationsForProject(project);
    }
    
    /**
     * Process an officer registration
     * @param manager the manager processing the registration
     * @param officer the officer to approve or reject
     * @param project the project
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processOfficerRegistration(HDBManager manager, HDBOfficer officer, Project project, boolean approve) {
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Check if there are available slots for approval
        if (approve && project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Process the registration
        return manager.processOfficerRegistration(officer, project, approve);
    }
    
    /**
     * Process applications for a project
     * @param manager the manager processing the applications
     * @param applications the applications to process
     * @param approvals map of application IDs to approval status
     * @return number of applications processed successfully
     */
    public boolean processApplication(HDBManager manager, Application app, boolean isApproved) {
        ApplicationControl applicationControl = new ApplicationControl();
            if (isApproved) {
                // Approve application
                // Check if there are available units of the requested type
                FlatType requestedType = determineRequestedFlatType(app);
                
                if (app.getProject().getAvailableUnitsByType(requestedType) > 0) {
                    app.setStatus(ApplicationStatus.SUCCESSFUL);
                    
                } else {
                    // Not enough units, reject instead
                    app.setStatus(ApplicationStatus.UNSUCCESSFUL);
                }
            } else {
                // Reject application
                app.setStatus(ApplicationStatus.UNSUCCESSFUL);
            }
            
            app.setStatusUpdateDate(new Date());
            boolean success = applicationControl.updateApplication(app);
            if (success) {
                applicationControl.saveApplications();
            }
        
        
        return true;
    }
    
    /**
     * Determine which flat type an applicant is applying for
     * @param application the application
     * @return the requested flat type
     */
    private FlatType determineRequestedFlatType(Application application) {
        // In a real system, this would be determined from the application
        // This is a simplified placeholder implementation
        Applicant applicant = application.getApplicant();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            return FlatType.TWO_ROOM;
        } else {
            // For married couples, check which type they're applying for
            // This would be stored in the application in a real system
            // For now, we'll assume they're applying for 3-Room if available
            Project project = application.getProject();
            if (project.getAvailableUnitsByType(FlatType.THREE_ROOM) > 0) {
                return FlatType.THREE_ROOM;
            } else {
                return FlatType.TWO_ROOM;
            }
        }
    }

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
            // For now, we'll assume they're applying for 3-Room if available
            if (project.hasFlatType(FlatType.THREE_ROOM)) {
                return FlatType.THREE_ROOM;
            } else if (project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        }
        
        return null;
    }
    
    
    
    /**
     * Create a new project
     * @param manager the manager creating the project
     * @param details map of project details
     * @return the created project, or null if creation failed
     */
    public Project createProject(HDBManager manager, Map<String, Object> details) {
        // Create the project
        return manager.createProject(details);
    }
    
    /**
     * Edit an existing project
     * @param manager the manager editing the project
     * @param project the project to edit
     * @param details map of updated details
     * @return true if edit was successful, false otherwise
     */
    public boolean editProject(HDBManager manager, Project project, Map<String, Object> details) {
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Edit the project
        return manager.editProject(project, details);
    }
    
    /**
     * Delete a project
     * @param manager the manager deleting the project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(HDBManager manager, Project project) {
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Delete the project
        return manager.deleteProject(project);
    }
    
    /**
     * Toggle project visibility
     * @param manager the manager toggling visibility
     * @param project the project
     * @param visible the new visibility
     * @return true if toggle was successful, false otherwise
     */
    public boolean toggleProjectVisibility(HDBManager manager, Project project, boolean visible) {
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Toggle visibility
        return manager.toggleVisibility(project, visible);
    }
    
    /**
     * Get projects created by this manager
     * @param manager the manager
     * @return list of projects
     */
    public List<Project> getProjectsByManager(HDBManager manager) {
        return projectControl.getProjectsByManager(manager);
    }
    
    /**
     * Get all projects in the system
     * @return list of projects
     */
    public List<Project> getAllProjects() {
        return projectControl.getAllProjects();
    }
}