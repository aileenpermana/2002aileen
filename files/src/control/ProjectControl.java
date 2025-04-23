package control;

import entity.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import utils.ProjectFileManager;

/**
 * Control class for managing Project operations in the BTO system.
 * Demonstrates the use of the Controller pattern in MVC architecture.
 */
public class ProjectControl {
    private ProjectFileManager fileManager;
    
    /**
     * Constructor for ProjectControl
     */
    public ProjectControl() {
        this.fileManager = ProjectFileManager.getInstance();
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
            if (project.isVisible() && project.checkEligibility(user)) {
                eligibleProjects.add(project);
            }
        }
        
        return eligibleProjects;
    }

    /**
 * Find or create a project by ID with proper manager data lookup
 * This method should be added to the ProjectControl class
 * 
 * @param projectID the project ID
 * @return the project object with actual manager data
 */
private Project findOrCreateProject(String projectID) {
    // First try to find the project in existing projects
    List<Project> allProjects = getAllProjects();
    
    for (Project p : allProjects) {
        if (p.getProjectID().equals(projectID)) {
            return p;
        }
    }
    
    // If not found, try to retrieve details from the project file
    try (Scanner scanner = new Scanner(new File("files/resources/ProjectList.csv"))) {
        // Skip header
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            
            String[] values = line.split(",");
            if (values.length < 12) continue;
            
            String projID = values[0].trim();
            if (projID.equals(projectID)) {
                // Found the project, now get proper details
                String projectName = values[1].trim();
                String neighborhood = values[2].trim();
                String managerNRIC = values[11].trim();
                
                // Parse flat types and units
                Map<FlatType, Integer> totalUnits = new HashMap<>();
                
                if (values[3].contains("2-Room") && !values[4].isEmpty()) {
                    try {
                        totalUnits.put(FlatType.TWO_ROOM, Integer.parseInt(values[4]));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing 2-Room units for project " + projectID);
                    }
                }
                
                if (values[6].contains("3-Room") && !values[7].isEmpty()) {
                    try {
                        totalUnits.put(FlatType.THREE_ROOM, Integer.parseInt(values[7]));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing 3-Room units for project " + projectID);
                    }
                }
                
                // Parse dates
                Date openDate, closeDate;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    openDate = dateFormat.parse(values[9]);
                    closeDate = dateFormat.parse(values[10]);
                } catch (ParseException e) {
                    // Use current dates as fallback
                    System.err.println("Error parsing dates for project " + projectID);
                    openDate = new Date();
                    closeDate = new Date();
                }
                
                // Get manager with actual data
                HDBManager manager;
                User foundUser = UserDataLookup.findUserByNRICAndType(managerNRIC, "HDBManager");
                
                if (foundUser != null && foundUser instanceof HDBManager) {
                    manager = (HDBManager) foundUser;
                } else {
                    manager = (HDBManager) UserDataLookup.createFallbackUser(managerNRIC, "HDBManager");
                }
                
                // Parse officer slots
                int officerSlots = 5; // Default
                try {
                    if (values.length > 12) {
                        officerSlots = Integer.parseInt(values[12]);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing officer slots for project " + projectID);
                }
                
                // Create the project with actual data
                Project project = new Project(
                    projectID,
                    projectName,
                    neighborhood,
                    totalUnits,
                    openDate,
                    closeDate,
                    manager,
                    officerSlots
                );
                
                // Set visibility (default to true)
                project.setVisible(true);
                
                return project;
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading ProjectList.csv: " + e.getMessage());
    }
    
    // If still not found, create a placeholder project
    Map<FlatType, Integer> defaultUnits = new HashMap<>();
    defaultUnits.put(FlatType.TWO_ROOM, 10);
    defaultUnits.put(FlatType.THREE_ROOM, 10);
    
    // Create with a placeholder manager using UserDataLookup to ensure consistent approach
    HDBManager placeholderManager = (HDBManager) UserDataLookup.createFallbackUser("S0000000A", "HDBManager");
    
    return new Project(
        projectID,
        "Project " + projectID, // Placeholder name
        "Unknown Neighborhood", // Placeholder neighborhood
        defaultUnits,
        new Date(), // Current date for open date
        new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // 30 days later for close date
        placeholderManager,
        5 // Default officer slots
    );
}
    
    /**
     * Get visible projects for a user
     * @param user the user
     * @param filters optional filters
     * @return list of visible projects
     */
    public List<Project> getVisibleProjectsForUser(User user, Map<String, Object> filters) {
        List<Project> allProjects = getAllProjects();
        List<Project> visibleProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // Managers can see all projects
            if (user instanceof HDBManager) {
                visibleProjects.add(project);
                continue;
            }
            
            // Officers can see projects they are handling, regardless of visibility
            if (user instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) user;
                if (officer.isHandlingProject(project)) {
                    visibleProjects.add(project);
                    continue;
                }
            }
            
            // For all users, check visibility and eligibility
            if (project.isVisible() && project.checkEligibility(user)) {
                visibleProjects.add(project);
            }
        }
        
        // Apply filters if provided
        if (filters != null && !filters.isEmpty()) {
            visibleProjects = applyFilters(visibleProjects, filters);
        }
        
        return visibleProjects;
    }
    
    /**
     * Apply filters to a list of projects
     * @param projects the projects to filter
     * @param filters the filters to apply
     * @return filtered list of projects
     */
    private List<Project> applyFilters(List<Project> projects, Map<String, Object> filters) {
        List<Project> filteredProjects = new ArrayList<>(projects);
        
        // Neighborhood filter
        if (filters.containsKey("neighborhood")) {
            String neighborhood = (String) filters.get("neighborhood");
            filteredProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(neighborhood));
        }
        
        // Flat type filter
        if (filters.containsKey("flatType")) {
            FlatType flatType = (FlatType) filters.get("flatType");
            filteredProjects.removeIf(p -> !p.getFlatTypes().contains(flatType));
        }
        
        // Add more filters as needed
        
        return filteredProjects;
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
     * @param visible the new visibility
     * @return true if toggle was successful, false otherwise
     */
    public boolean toggleVisibility(Project project, boolean visible) {
        project.setVisible(visible);
        return updateProject(project);
    }

    /**
 * Filter projects by neighborhood
 * @param projects the list of projects to filter
 * @param neighborhood the neighborhood to filter by
 * @return filtered list of projects
 */
public List<Project> filterByNeighborhood(List<Project> projects, String neighborhood) {
    List<Project> filtered = new ArrayList<>();
    
    for (Project project : projects) {
        if (project.getNeighborhood().equalsIgnoreCase(neighborhood)) {
            filtered.add(project);
        }
    }
    
    return filtered;
}

/**
 * Filter projects by flat type
 * @param projects the list of projects to filter
 * @param flatType the flat type to filter by
 * @return filtered list of projects
 */
public List<Project> filterByFlatType(List<Project> projects, FlatType flatType) {
    List<Project> filtered = new ArrayList<>();
    
    for (Project project : projects) {
        if (project.hasFlatType(flatType)) {
            filtered.add(project);
        }
    }
    
    return filtered;
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

/**
 * Filter projects based on various criteria
 * @param projects the list of projects to filter
 * @param filters a map of filter criteria
 * @return filtered list of projects
 */
public List<Project> filterProjects(List<Project> projects, Map<String, Object> filters) {
    if (filters == null || filters.isEmpty()) {
        return new ArrayList<>(projects);
    }
    
    List<Project> filteredProjects = new ArrayList<>(projects);
    
    // Filter by neighborhood
    if (filters.containsKey("neighborhood")) {
        String neighborhood = (String) filters.get("neighborhood");
        if (neighborhood != null && !neighborhood.isEmpty()) {
            filteredProjects = filterByNeighborhood(filteredProjects, neighborhood);
        }
    }
    
    // Filter by flat type
    if (filters.containsKey("flatType")) {
        String flatTypeStr = (String) filters.get("flatType");
        if (flatTypeStr != null && !flatTypeStr.isEmpty()) {
            FlatType flatType = FlatType.fromDisplayValue(flatTypeStr);
            if (flatType != null) {
                filteredProjects = filterByFlatType(filteredProjects, flatType);
            }
        }
    }
    
    // Filter by application period
    if (filters.containsKey("startDate")) {
        Date startDate = (Date) filters.get("startDate");
        if (startDate != null) {
            filteredProjects = filterByStartDate(filteredProjects, startDate);
        }
    }
    
    if (filters.containsKey("endDate")) {
        Date endDate = (Date) filters.get("endDate");
        if (endDate != null) {
            filteredProjects = filterByEndDate(filteredProjects, endDate);
        }
    }
    
    // Filter by availability
    if (filters.containsKey("minAvailability")) {
        Integer minAvailability = (Integer) filters.get("minAvailability");
        if (minAvailability != null && minAvailability > 0) {
            filteredProjects = filterByMinAvailability(filteredProjects, minAvailability);
        }
    }
    
    // Filter by manager
    if (filters.containsKey("manager")) {
        HDBManager manager = (HDBManager) filters.get("manager");
        if (manager != null) {
            filteredProjects = filterByManager(filteredProjects, manager);
        }
    }
    
    // Sort projects if sortBy is specified
    if (filters.containsKey("sortBy")) {
        String sortBy = (String) filters.get("sortBy");
        if (sortBy != null && !sortBy.isEmpty()) {
            filteredProjects = sortProjects(filteredProjects, sortBy);
        }
    }
    
    return filteredProjects;
}

// In ProjectControl.java, add this method:

/**
 * Sync managers with their projects
 * This ensures all managers are properly linked to their projects after loading from files
 */
public void syncManagerProjects() {
    List<Project> allProjects = getAllProjects();
    
    // Group projects by manager NRIC
    Map<String, List<Project>> projectsByManager = new HashMap<>();
    
    for (Project project : allProjects) {
        HDBManager manager = project.getManagerInCharge();
        String managerNRIC = manager.getNRIC();
        
        if (!projectsByManager.containsKey(managerNRIC)) {
            projectsByManager.put(managerNRIC, new ArrayList<>());
        }
        
        projectsByManager.get(managerNRIC).add(project);
    }
    
    // Now ensure each manager has all their projects
    for (Map.Entry<String, List<Project>> entry : projectsByManager.entrySet()) {
        String managerNRIC = entry.getKey();
        List<Project> managerProjects = entry.getValue();
        
        try {
            // Load manager details
            File file = new File("files/resources/ManagerList.csv");
            if (file.exists()) {
                try (Scanner scanner = new Scanner(file)) {
                    // Skip header
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }
                    
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.isEmpty()) continue;
                        
                        String[] fields = line.split(",");
                        if (fields.length < 2) continue;
                        
                        if (fields[1].trim().equalsIgnoreCase(managerNRIC)) {
                            // Found the manager in ManagerList.csv
                            // Now update the manager in each project to ensure bidirectional link
                            for (Project project : managerProjects) {
                                HDBManager manager = project.getManagerInCharge();
                                // Update manager's list
                                manager.addManagedProject(project);
                                
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing manager projects: " + e.getMessage());
        }
    }
}
}