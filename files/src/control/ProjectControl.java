package control;

import entity.*;
import java.util.*;
import utils.ProjectFileManager;
import utils.filters.*;

/**
 * Control class for managing Project operations in the BTO system.
 * Demonstrates the use of the Controller pattern in MVC architecture.
 */
public class ProjectControl {
    private ProjectFileManager fileManager;
    
    private NeighborhoodFilterable<Project> neighborhoodFilter;
    private FlatTypeFilterable<Project> flatTypeFilter;
    private ManagerFilterable<Project> managerFilter;

    /**
     * Constructor for ProjectControl
     */
    public ProjectControl() {
        this.fileManager = ProjectFileManager.getInstance();
        ProjectFilter filter = new ProjectFilter();
        this.neighborhoodFilter = filter;
        this.flatTypeFilter = filter;
        this.managerFilter = filter;
    }
    
    /**
     * Get all projects in the system
     * @return list of all projects
     */
    public List<Project> getAllProjects() {
        return fileManager.readAllProjects();
    }
    
    /**
     * Get eligible projects for a user
     * @param user the user
     * @return list of projects the user is eligible for
     */
    public List<Project> getEligibleProjects(User user) {
        List<Project> allProjects = getAllProjects();
        List<Project> eligibleProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            if (project.isVisible() && project.checkEligibility(user,project.getProjectID())) {
                eligibleProjects.add(project);
            }
        }
        
        return eligibleProjects;
    }
    
    /**
 * Get visible and eligible projects for a user
 * @param user the user
 * @return list of projects the user is eligible for and are visible
 */
public List<Project> getVisibleEligibleProjects(User user) {
    List<Project> allProjects = getAllProjects();
    List<Project> visibleEligibleProjects = new ArrayList<>();
    
    for (Project project : allProjects) {
        // Make sure the project is visible AND the user is eligible
        if (project.isVisible() && project.checkEligibility(user, project.getProjectID())) {
            visibleEligibleProjects.add(project);
        }
    }
    
    return visibleEligibleProjects;
}
    
    /**
     * Get projects created by a specific manager
     * @param manager the manager
     * @return list of projects created by the manager
     */
    public List<Project> getProjectsByManager(HDBManager manager) {
        List<Project> allProjects = getAllProjects();
        List<Project> managerProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            if (project.getManagerInCharge().getNRIC().equals(manager.getNRIC())) {
                managerProjects.add(project);
            }
        }
        
        return managerProjects;
    }

    public boolean updateProjectUnitsAfterBooking(Project project, FlatType flatType) {
        try {
            // Log the operation to help debugging
            System.out.println("Updating project units after booking:");
            System.out.println("Project: " + project.getProjectName());
            System.out.println("Flat type: " + flatType.getDisplayValue());
            System.out.println("Available units before update: " + project.getAvailableUnitsByType(flatType));
            
            // We don't need to decrement here because it's already done in HDBOfficerControl.bookFlatForApplicant
            // Just save the updated project to the file
            boolean saveSuccess = fileManager.updateProject(project);
            
            System.out.println("Available units after update: " + project.getAvailableUnitsByType(flatType));
            System.out.println("Save success: " + saveSuccess);
            
            return saveSuccess;
        } catch (Exception e) {
            System.err.println("Error updating project units: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

        /**
     * Get projects handled by a specific officer
     * @param officer the HDB officer
     * @return list of projects the officer is handling
     */
    public List<Project> getProjectsByOfficer(HDBOfficer officer) {
        List<Project> allProjects = getAllProjects();
        List<Project> officerProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // Use the existing officers list to check if the officer is handling the project
            for (HDBOfficer projectOfficer : project.getOfficers()) {
                if (projectOfficer.getNRIC().equalsIgnoreCase(officer.getNRIC())) {
                    officerProjects.add(project);
                    break;
                }
            }
        }
        
        return officerProjects;
    }

    
    
    /**
     * Add a new project to the system
     * @param project the project to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addProject(Project project) {
        return fileManager.addProject(project);
    }
    
    /**
     * Update an existing project
     * @param project the updated project
     * @return true if update was successful, false otherwise
     */
    public boolean updateProject(Project project) {
        return fileManager.updateProject(project);
    }
    
    /**
     * Delete a project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        return fileManager.deleteProject(project);
    }
    
    /**
 * Toggle the visibility of a project
 * @param project the project
 * @param visible the new visibility state
 * @return true if toggle was successful, false otherwise
 */
public boolean toggleVisibility(Project project, boolean visible) {
    project.setVisible(visible);
    return updateProject(project);
}



/**
 * Filter projects by start date
 * @param projects the list of projects to filter
 * @param startDate the minimum start date
 * @return filtered list of projects
 */
public List<Project> filterByStartDate(List<Project> projects, Date startDate) {
    List<Project> filtered = new ArrayList<>();
    
    for (Project project : projects) {
        if (!project.getApplicationOpenDate().before(startDate)) {
            filtered.add(project);
        }
    }
    
    return filtered;
}

/**
 * Filter projects by end date
 * @param projects the list of projects to filter
 * @param endDate the maximum end date
 * @return filtered list of projects
 */
public List<Project> filterByEndDate(List<Project> projects, Date endDate) {
    List<Project> filtered = new ArrayList<>();
    
    for (Project project : projects) {
        if (!project.getApplicationCloseDate().after(endDate)) {
            filtered.add(project);
        }
    }
    
    return filtered;
}

/**
 * Filter projects by minimum availability
 * @param projects the list of projects to filter
 * @param minAvailability the minimum number of available units
 * @return filtered list of projects
 */
public List<Project> filterByMinAvailability(List<Project> projects, int minAvailability) {
    List<Project> filtered = new ArrayList<>();
    
    for (Project project : projects) {
        int totalAvailable = 0;
        for (FlatType type : project.getFlatTypes()) {
            totalAvailable += project.getAvailableUnitsByType(type);
        }
        
        if (totalAvailable >= minAvailability) {
            filtered.add(project);
        }
    }
    
    return filtered;
}

/**
 * Filter projects by manager
 * @param projects the list of projects to filter
 * @param manager the manager to filter by
 * @return filtered list of projects
 */
public List<Project> filterByManager(List<Project> projects, HDBManager manager) {
    List<Project> filtered = new ArrayList<>();
    
    for (Project project : projects) {
        if (project.getManagerInCharge().getNRIC().equals(manager.getNRIC())) {
            filtered.add(project);
        }
    }
    
    return filtered;
}

/**
 * Sort projects by different criteria
 * @param projects the list of projects to sort
 * @param sortBy the sort option
 * @return sorted list of projects
 */
public List<Project> sortProjects(List<Project> projects, String sortBy) {
    List<Project> sortedProjects = new ArrayList<>(projects);
    
    switch (sortBy) {
        case "flatType":
            // Sort by number of flat types
            sortedProjects.sort(Comparator.comparing(p -> p.getFlatTypes().size()));
            break;
        case "neighborhood":
            sortedProjects.sort(Comparator.comparing(Project::getNeighborhood));
            break;
        case "openDate":
            sortedProjects.sort(Comparator.comparing(Project::getApplicationOpenDate));
            break;
        case "closeDate":
            sortedProjects.sort(Comparator.comparing(Project::getApplicationCloseDate));
            break;
        case "availabilityDesc":
            // Sort by total available units (descending)
            sortedProjects.sort((p1, p2) -> {
                int p1Available = calculateTotalAvailableUnits(p1);
                int p2Available = calculateTotalAvailableUnits(p2);
                return Integer.compare(p2Available, p1Available); // Descending
            });
            break;
        case "availability":
            // Sort by total available units (ascending)
            sortedProjects.sort(Comparator.comparing(this::calculateTotalAvailableUnits));
            break;
        case "name":
        default:
            sortedProjects.sort(Comparator.comparing(Project::getProjectName));
            break;
    }
    
    return sortedProjects;
}

/**
 * Calculate total available units for a project
 * @param project the project
 * @return total available units
 */
private int calculateTotalAvailableUnits(Project project) {
    int total = 0;
    for (FlatType type : project.getFlatTypes()) {
        total += project.getAvailableUnitsByType(type);
    }
    return total;
}/**
 * Enhanced project filtering methods for ProjectControl
 */

 

public List<Project> filterByNeighborhood(List<Project> projects, String neighborhood) {
    return neighborhoodFilter.filterByNeighborhood(projects, neighborhood);
}

public List<Project> filterByFlatType(List<Project> projects, FlatType flatType) {
    return flatTypeFilter.filterByFlatType(projects, flatType);
}

public List<Project> filterProjectsByManager(List<Project> projects, HDBManager manager) {
    // Cast to access the specific interface method
    return (managerFilter.filterByManager(projects, manager));
}



}