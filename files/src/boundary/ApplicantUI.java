package boundary;

import control.ApplicationControl;
import control.EnquiryControl;
import control.HDBOfficerControl;
import control.ProjectControl;
import control.UserControl;
import entity.*;
import java.text.SimpleDateFormat;
import java.util.*;
import utils.ScreenUtil;

/**
 * UI class for Applicant operations in the BTO Management System.
 */
public class ApplicantUI {
    private Applicant currentUser;
    private Scanner sc;
    private ProjectControl projectControl;
    private ApplicationControl applicationControl;
    private EnquiryControl enquiryControl;
    private HDBOfficerControl officerControl;
    
    // Store user's filter preferences
    private Map<String, Object> filterPreferences;
    
    // Project sorting options
    private static final String SORT_BY_FLAT_TYPE = "1";
    private static final String SORT_BY_NEIGHBORHOOD = "2";
    private static final String SORT_BY_PRICE_RANGE = "3";
    private static final String SORT_BY_CLOSING_DATE = "4";
    private static final String SORT_BY_ALPHABETICAL = "5";
    private static final String SORT_BY_AVAILABILITY = "6";
    
    /**
     * Constructor for ApplicantUI
     */
    public ApplicantUI(Applicant user) {
        this.currentUser = user;
        this.sc = new Scanner(System.in);
        this.projectControl = new ProjectControl();
        this.applicationControl = new ApplicationControl();
        this.enquiryControl = new EnquiryControl();
        this.officerControl = new HDBOfficerControl();
        this.filterPreferences = new HashMap<>();
        
        // Set default sort preference
        filterPreferences.put("sortBy", SORT_BY_ALPHABETICAL);
    }
    
    /**
     * Display the main menu for Applicants
     */
    /**
 * Updated displayMenu method for ApplicantUI class
 */

public void displayMenu() {
    boolean exit = false;
    
    while (!exit) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Applicant Menu =====");
        System.out.println("Welcome, " + currentUser.getName() + "!");
        System.out.println("1. View Available Projects");
        System.out.println("2. View My Applications");
        System.out.println("3. Enquiry Management");  // New option
        System.out.println("4. View My Profile");
        System.out.println("5. Change Password");
        System.out.println("6. Sign Out");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                viewAvailableProjects();
                break;
            case "2":
                viewMyApplications();
                break;
            case "3":
                displayEnquiryMenu();  // New method
                break;
            case "4":
                viewProfile();
                break;
            case "5":
                changePassword();
                break;
            case "6":
                exit = true;
                System.out.println("Signing out...");
                break;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
        }
    }
}
    
    private void viewAvailableProjects() {
    boolean exitToMain = false;
    
    while (!exitToMain) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Available Projects =====");
        
        // Get projects based on applicant's eligibility
        List<Project> eligibleProjects = projectControl.getEligibleProjects(currentUser);
        
        if (eligibleProjects.isEmpty()) {
            System.out.println("No eligible projects available for you at this time.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Pre-filter to only show projects that are open for application
        Date currentDate = new Date();
        eligibleProjects.removeIf(p -> !p.isOpenForApplication());
        
        if (eligibleProjects.isEmpty()) {
            System.out.println("No projects are currently open for application.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Always start with alphabetical sorting by default
        // This ensures projects are displayed immediately in alphabetical order
        if (!filterPreferences.containsKey("sortBy")) {
            filterPreferences.put("sortBy", SORT_BY_ALPHABETICAL);
        }
        
        // Apply current filters and sorting
        List<Project> filteredProjects = applyFiltersAndSort(eligibleProjects);
        
        if (filteredProjects.isEmpty()) {
            System.out.println("\nNo projects match your filter criteria.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display projects
        displayProjects(filteredProjects);
        
        // Project selection submenu
        System.out.println("\nOptions:");
        System.out.println("1. View Project Details");
        System.out.println("2. Filter and Sort Projects");
        System.out.println("3. Return to Main Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                System.out.print("Enter project number to view details: ");
                try {
                    int projectIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (projectIndex >= 0 && projectIndex < filteredProjects.size()) {
                        viewProjectDetails(filteredProjects.get(projectIndex));
                    } else {
                        System.out.println("Invalid project number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "2":
                // Show filtering options only when user selects this option
                displayFilterOptions(eligibleProjects);
                break;
            case "3":
                exitToMain = true;
                break;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
        }
    }
}
    
private void displayFilterOptions(List<Project> eligibleProjects) {
    System.out.println("\n----- Filter and Sort Options -----");
    System.out.println("Sort by:");
    System.out.println("1. Flat Types");
    System.out.println("2. Neighborhood");
    System.out.println("3. Closing Date (Earliest first)");
    System.out.println("4. Alphabetical (Default)");
    System.out.println("5. Availability (Most units first)");
    
    System.out.print("\nEnter sort option (or press Enter to use previous/default): ");
    String sortOption = sc.nextLine();
    
    if (!sortOption.trim().isEmpty()) {
        filterPreferences.put("sortBy", sortOption);
    }
    
    // Get the current sort method
    String currentSortMethod = (String) filterPreferences.getOrDefault("sortBy", SORT_BY_ALPHABETICAL);
    
    // Neighborhood filtering for Neighborhood sort method
    if (currentSortMethod.equals(SORT_BY_NEIGHBORHOOD)) {
        Set<String> availableNeighborhoods = new HashSet<>();
        for (Project p : eligibleProjects) {
            availableNeighborhoods.add(p.getNeighborhood());
        }
        
        System.out.println("\nAvailable Neighborhoods:");
        int index = 1;
        Map<Integer, String> neighborhoodMap = new HashMap<>();
        
        for (String neighborhood : availableNeighborhoods) {
            System.out.println(index + ". " + neighborhood);
            neighborhoodMap.put(index, neighborhood);
            index++;
        }
        
        System.out.print("\nSelect a neighborhood number (0 for all): ");
        int neighborhoodChoice = 0;
        try {
            String input = sc.nextLine();
            if (!input.trim().isEmpty()) {
                neighborhoodChoice = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Showing all neighborhoods.");
        }
        
        if (neighborhoodChoice > 0 && neighborhoodChoice < index) {
            String selectedNeighborhood = neighborhoodMap.get(neighborhoodChoice);
            filterPreferences.put("neighborhood", selectedNeighborhood);
            System.out.println("Filtering by: " + selectedNeighborhood);
        } else {
            filterPreferences.remove("neighborhood");
        }
    }
    
    // Flat type filtering for Flat Type and Availability sort methods
    if (currentSortMethod.equals(SORT_BY_FLAT_TYPE) || 
        currentSortMethod.equals(SORT_BY_AVAILABILITY)) {
        System.out.println("\nSelect Flat Type:");
        System.out.println("1. 2-Room");
        System.out.println("2. 3-Room");
        System.out.println("0. All Types");
        
        System.out.print("\nSelect a flat type (0 for all): ");
        int flatTypeChoice = 0;
        try {
            String input = sc.nextLine();
            if (!input.trim().isEmpty()) {
                flatTypeChoice = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Showing all flat types.");
        }
        
        if (flatTypeChoice == 1) {
            filterPreferences.put("flatType", "2-Room");
            System.out.println("Filtering by: 2-Room flats");
        } else if (flatTypeChoice == 2) {
            filterPreferences.put("flatType", "3-Room");
            System.out.println("Filtering by: 3-Room flats");
        } else {
            filterPreferences.remove("flatType");
        }
    }
}
    
    // First, let's restore and update the applyFiltersAndSort method in ApplicantUI.java

    private List<Project> applyFiltersAndSort(List<Project> projects) {
        List<Project> filteredProjects = new ArrayList<>(projects);
        
        // Filter for current date - only show projects that are currently open for application
        Date currentDate = new Date(); // Today's date
        filteredProjects.removeIf(p -> !p.isOpenForApplication());
        
        // Apply sorting
        String sortBy = (String) filterPreferences.getOrDefault("sortBy", SORT_BY_ALPHABETICAL);
        
        switch (sortBy) {
            case SORT_BY_NEIGHBORHOOD:
                // Apply neighborhood filter
                if (filterPreferences.containsKey("neighborhood")) {
                    String neighborhood = (String) filterPreferences.get("neighborhood");
                    if (neighborhood != null && !neighborhood.trim().isEmpty()) {
                        filteredProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(neighborhood));
                    }
                }
                // Sort by neighborhood
                filteredProjects.sort(Comparator.comparing(Project::getNeighborhood));
                break;
            
            case SORT_BY_CLOSING_DATE:
                // Sort by closing date (earliest first)
                filteredProjects.sort(Comparator.comparing(Project::getApplicationCloseDate));
                break;
            
            case SORT_BY_FLAT_TYPE:
            case SORT_BY_AVAILABILITY:
                // Apply flat type filter
                if (filterPreferences.containsKey("flatType")) {
                    String flatTypeStr = (String) filterPreferences.get("flatType");
                    FlatType type = null;
                    
                    if (flatTypeStr.equalsIgnoreCase("2-Room")) {
                        type = FlatType.TWO_ROOM;
                    } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
                        type = FlatType.THREE_ROOM;
                    }
                    
                    if (type != null) {
                        final FlatType finalType = type;
                        // Filter projects that have this flat type AND have available units
                        filteredProjects.removeIf(p -> !p.hasFlatType(finalType) || p.getAvailableUnitsByType(finalType) <= 0);
                    }
                }
                
                // Sort by total available units (descending) if SORT_BY_AVAILABILITY
                if (sortBy.equals(SORT_BY_AVAILABILITY)) {
                    filteredProjects.sort((p1, p2) -> {
                        int p1Available = calculateTotalAvailableUnits(p1);
                        int p2Available = calculateTotalAvailableUnits(p2);
                        return Integer.compare(p2Available, p1Available); // Descending
                    });
                } else {
                    // Sort by number of distinct flat types
                    filteredProjects.sort(Comparator.comparing(p -> p.getFlatTypes().size()));
                }
                break;
            
            case SORT_BY_ALPHABETICAL:
            default:
                // Optionally apply neighborhood filter if set
                if (filterPreferences.containsKey("neighborhood")) {
                    String neighborhood = (String) filterPreferences.get("neighborhood");
                    if (neighborhood != null && !neighborhood.trim().isEmpty()) {
                        filteredProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(neighborhood));
                    }
                }
                
                // Optionally apply flat type filter if set
                if (filterPreferences.containsKey("flatType")) {
                    String flatTypeStr = (String) filterPreferences.get("flatType");
                    FlatType type = null;
                    
                    if (flatTypeStr.equalsIgnoreCase("2-Room")) {
                        type = FlatType.TWO_ROOM;
                    } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
                        type = FlatType.THREE_ROOM;
                    }
                    
                    if (type != null) {
                        final FlatType finalType = type;
                        // Filter projects that have this flat type AND have available units
                        filteredProjects.removeIf(p -> !p.hasFlatType(finalType) || p.getAvailableUnitsByType(finalType) <= 0);
                    }
                }
                
                // Sort by project name
                filteredProjects.sort(Comparator.comparing(Project::getProjectName));
                break;
        }
        
        return filteredProjects;
    }
    
    /**
     * Calculate total available units across all flat types
     */
    private int calculateTotalAvailableUnits(Project project) {
        int total = 0;
        for (FlatType type : project.getFlatTypes()) {
            total += project.getAvailableUnitsByType(type);
        }
        return total;
    }
    
    private void displayProjects(List<Project> projects) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = new Date();
        
        System.out.println("\n----- Available Projects -----");
        System.out.printf("%-5s %-20s %-15s %-20s %-20s %-15s\n", 
                         "No.", "Project Name", "Neighborhood", "Closing Date", "Days Remaining", "Available Units");
        System.out.println("------------------------------------------------------------------------------------------------");
        
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            int totalAvailable = calculateTotalAvailableUnits(p);
            
            // Calculate days remaining until closing date
            long diffInMillies = p.getApplicationCloseDate().getTime() - currentDate.getTime();
            long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
            
            System.out.printf("%-5d %-20s %-15s %-20s %-20d %-15d\n", 
                            (i + 1),
                            truncateString(p.getProjectName(), 20),
                            truncateString(p.getNeighborhood(), 15),
                            dateFormat.format(p.getApplicationCloseDate()),
                            diffInDays,
                            totalAvailable);
        }
        
        // Only show flat type availability when sorting by flat type
        String currentSortMethod = (String) filterPreferences.getOrDefault("sortBy", SORT_BY_ALPHABETICAL);
        if (currentSortMethod.equals(SORT_BY_FLAT_TYPE)) {
            System.out.println("\nFlat Type Availability:");
            System.out.printf("%-5s %-20s %-20s %-20s\n", 
                             "No.", "Project Name", "2-Room Available", "3-Room Available");
            System.out.println("--------------------------------------------------------------------------");
            
            for (int i = 0; i < projects.size(); i++) {
                Project p = projects.get(i);
                int twoRoomAvailable = p.getAvailableUnitsByType(FlatType.TWO_ROOM);
                int threeRoomAvailable = p.getAvailableUnitsByType(FlatType.THREE_ROOM);
                
                System.out.printf("%-5d %-20s %-20d %-20d\n", 
                                (i + 1),
                                truncateString(p.getProjectName(), 20),
                                twoRoomAvailable,
                                threeRoomAvailable);
            }
        }
    }

/**
 * Display the enquiry menu
 */
private void displayEnquiryMenu() {
    ScreenUtil.clearScreen();
    System.out.println("\n===== Enquiry Menu =====");
    System.out.println("1. Make New Enquiry");
    System.out.println("2. View My Enquiries");
    System.out.println("3. Return to Main Menu");
    
    System.out.print("\nEnter your choice: ");
    String choice = sc.nextLine();
    
    switch (choice) {
        case "1":
            makeEnquiry();
            break;
        case "2":
            viewEnquiries();
            break;
        case "3":
            return;
        default:
            System.out.println("Invalid choice. Press Enter to continue.");
            sc.nextLine();
    }
}

/**
 * Make a new enquiry
 */
private void makeEnquiry() {
    ScreenUtil.clearScreen();
    System.out.println("\n===== Make New Enquiry =====");
    
    // Get visible projects for the applicant
    List<Project> visibleProjects = projectControl.getVisibleProjectsForUser(currentUser, filterPreferences);
    
    if (visibleProjects.isEmpty()) {
        System.out.println("No projects available for enquiry at this time.");
        System.out.println("Press Enter to continue...");
        sc.nextLine();
        return;
    }
    
    // Display the list of projects
    System.out.println("Select a project to enquire about:");
    for (int i = 0; i < visibleProjects.size(); i++) {
        System.out.println((i + 1) + ". " + visibleProjects.get(i).getProjectName());
    }
    
    System.out.print("\nEnter project number (0 to cancel): ");
    int projectChoice;
    try {
        projectChoice = Integer.parseInt(sc.nextLine());
        if (projectChoice == 0) {
            return;
        }
        
        if (projectChoice < 1 || projectChoice > visibleProjects.size()) {
            System.out.println("Invalid project number. Press Enter to continue.");
            sc.nextLine();
            return;
        }
    } catch (NumberFormatException e) {
        System.out.println("Invalid input. Press Enter to continue.");
        sc.nextLine();
        return;
    }
    
    Project selectedProject = visibleProjects.get(projectChoice - 1);
    
    // Get enquiry content
    System.out.println("\nEnter your enquiry about " + selectedProject.getProjectName() + ":");
    String content = sc.nextLine();
    
    if (content.trim().isEmpty()) {
        System.out.println("Enquiry cannot be empty. Press Enter to continue.");
        sc.nextLine();
        return;
    }
    
    // Create and submit the enquiry
    EnquiryControl enquiryControl = new EnquiryControl();
    Enquiry enquiry = enquiryControl.submitEnquiry(currentUser, selectedProject, content);
    
    if (enquiry != null) {
        System.out.println("\nEnquiry submitted successfully!");
        System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
    } else {
        System.out.println("\nFailed to submit enquiry. Please try again later.");
    }
    
    System.out.println("Press Enter to continue...");
    sc.nextLine();
}

/**
 * View all enquiries made by the applicant
 */
private void viewEnquiries() {
    ScreenUtil.clearScreen();
    System.out.println("\n===== My Enquiries =====");
    
    // Get all enquiries for the current applicant
    EnquiryControl enquiryControl = new EnquiryControl();
    List<Enquiry> myEnquiries = enquiryControl.getEnquiriesByApplicant(currentUser);
    
    if (myEnquiries.isEmpty()) {
        System.out.println("You have not made any enquiries yet.");
        System.out.println("Press Enter to continue...");
        sc.nextLine();
        return;
    }
    
    // Display the list of enquiries
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    
    System.out.printf("%-5s %-20s %-20s %-30s %-15s\n", 
                     "No.", "Project", "Date", "Content", "Status");
    System.out.println("--------------------------------------------------------------------------------");
    
    for (int i = 0; i < myEnquiries.size(); i++) {
        Enquiry enquiry = myEnquiries.get(i);
        String status = (enquiry.getReplies() != null && !enquiry.getReplies().isEmpty()) ? 
                         "Replied" : "Pending";
        
        System.out.printf("%-5d %-20s %-20s %-30s %-15s\n", 
                        (i + 1),
                        truncateString(enquiry.getProject().getProjectName(), 20),
                        dateFormat.format(enquiry.getSubmissionDate()),
                        truncateString(enquiry.getContent(), 30),
                        status);
    }
    
    // Options submenu
    System.out.println("\nOptions:");
    System.out.println("1. View Enquiry Details");
    System.out.println("2. Edit Enquiry");
    System.out.println("3. Delete Enquiry");
    System.out.println("4. Return to Enquiry Menu");
    
    System.out.print("\nEnter your choice: ");
    String choice = sc.nextLine();
    
    switch (choice) {
        case "1":
            viewEnquiryDetails(myEnquiries);
            break;
        case "2":
            editEnquiry(myEnquiries);
            break;
        case "3":
            deleteEnquiry(myEnquiries);
            break;
        case "4":
            return;
        default:
            System.out.println("Invalid choice. Press Enter to continue.");
            sc.nextLine();
    }
}

/**
 * View details of a specific enquiry
 * @param enquiries list of enquiries to choose from
 */
private void viewEnquiryDetails(List<Enquiry> enquiries) {
    System.out.print("\nEnter enquiry number to view details: ");
    try {
        int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
        if (enquiryIndex < 0 || enquiryIndex >= enquiries.size()) {
            System.out.println("Invalid enquiry number. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        Enquiry selectedEnquiry = enquiries.get(enquiryIndex);
        displayEnquiryDetails(selectedEnquiry);
    } catch (NumberFormatException e) {
        System.out.println("Invalid input. Press Enter to continue.");
        sc.nextLine();
    }
}

/**
 * Display details of an enquiry
 * @param enquiry the enquiry to display
 */
private void displayEnquiryDetails(Enquiry enquiry) {
    ScreenUtil.clearScreen();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    
    System.out.println("\n===== Enquiry Details =====");
    System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
    System.out.println("Project: " + enquiry.getProject().getProjectName());
    System.out.println("Date: " + dateFormat.format(enquiry.getSubmissionDate()));
    System.out.println("\nContent:");
    System.out.println(enquiry.getContent());
    
    if (enquiry.getReplies() != null && !enquiry.getReplies().isEmpty()) {
        System.out.println("\nReplies:");
        for (int i = 0; i < enquiry.getReplies().size(); i++) {
            System.out.println((i + 1) + ". " + enquiry.getReplies().get(i));
        }
    } else {
        System.out.println("\nNo replies yet.");
    }
    
    System.out.println("\nPress Enter to continue...");
    sc.nextLine();
}

/**
 * Edit an enquiry
 * @param enquiries list of enquiries to choose from
 */
private void editEnquiry(List<Enquiry> enquiries) {
    System.out.print("\nEnter enquiry number to edit: ");
    try {
        int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
        if (enquiryIndex < 0 || enquiryIndex >= enquiries.size()) {
            System.out.println("Invalid enquiry number. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        Enquiry selectedEnquiry = enquiries.get(enquiryIndex);
        
        // Check if enquiry has been replied to
        EnquiryControl enquiryControl = new EnquiryControl();
        if (enquiryControl.hasBeenReplied(selectedEnquiry)) {
            System.out.println("Cannot edit an enquiry that has been replied to. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        // Display current content
        ScreenUtil.clearScreen();
        System.out.println("\n===== Edit Enquiry =====");
        System.out.println("Project: " + selectedEnquiry.getProject().getProjectName());
        System.out.println("\nCurrent content:");
        System.out.println(selectedEnquiry.getContent());
        
        // Get new content
        System.out.println("\nEnter new content (or press Enter to cancel):");
        String newContent = sc.nextLine();
        
        if (newContent.trim().isEmpty()) {
            System.out.println("Edit cancelled. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        // Update the enquiry
        boolean success = enquiryControl.editEnquiry(selectedEnquiry, newContent);
        
        if (success) {
            System.out.println("\nEnquiry updated successfully!");
        } else {
            System.out.println("\nFailed to update enquiry. Please try again later.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    } catch (NumberFormatException e) {
        System.out.println("Invalid input. Press Enter to continue.");
        sc.nextLine();
    }
}

/**
 * Delete an enquiry
 * @param enquiries list of enquiries to choose from
 */
private void deleteEnquiry(List<Enquiry> enquiries) {
    System.out.print("\nEnter enquiry number to delete: ");
    try {
        int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
        if (enquiryIndex < 0 || enquiryIndex >= enquiries.size()) {
            System.out.println("Invalid enquiry number. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        Enquiry selectedEnquiry = enquiries.get(enquiryIndex);
        
        // Check if enquiry has been replied to
        EnquiryControl enquiryControl = new EnquiryControl();
        if (enquiryControl.hasBeenReplied(selectedEnquiry)) {
            System.out.println("Cannot delete an enquiry that has been replied to. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        // Confirm deletion
        System.out.print("\nAre you sure you want to delete this enquiry? (Y/N): ");
        String confirmation = sc.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            // Delete the enquiry
            boolean success = enquiryControl.deleteEnquiry(selectedEnquiry);
            
            if (success) {
                System.out.println("\nEnquiry deleted successfully!");
            } else {
                System.out.println("\nFailed to delete enquiry. Please try again later.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    } catch (NumberFormatException e) {
        System.out.println("Invalid input. Press Enter to continue.");
        sc.nextLine();
    }
}

    /**
     * Truncate a string to a maximum length
     * @param str the string to truncate
     * @param maxLength the maximum length
     * @return truncated string
     */
    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    private void viewProjectDetails(Project project) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = new Date();
        
        System.out.println("\n===== Project Details =====");
        System.out.println("Project ID: " + project.getProjectID());
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        
        System.out.println("\nApplication Period:");
        System.out.println("Opening Date: " + dateFormat.format(project.getApplicationOpenDate()));
        System.out.println("Closing Date: " + dateFormat.format(project.getApplicationCloseDate()));
        
        // Calculate days remaining until closing
        long diffInMillies = project.getApplicationCloseDate().getTime() - currentDate.getTime();
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        System.out.println("Days Remaining: " + diffInDays);
        
        // Verify application is open
        if (project.isOpenForApplication()) {
            System.out.println("Status: OPEN FOR APPLICATION");
        } else if (currentDate.before(project.getApplicationOpenDate())) {
            System.out.println("Status: NOT YET OPEN");
        } else {
            System.out.println("Status: CLOSED");
        }
        
        System.out.println("\nFlat Types Available:");
        
        // Show flat types based on eligibility
        if (currentUser.getMaritalStatus() == MaritalStatus.SINGLE) {
            if (currentUser.getAge() >= 35) {
                // Singles 35 and above can only apply for 2-Room
                if (project.hasFlatType(FlatType.TWO_ROOM)) {
                    System.out.println("2-Room: " + 
                                 project.getAvailableUnitsByType(FlatType.TWO_ROOM) + " units available out of " + 
                                 project.getTotalUnitsByType(FlatType.TWO_ROOM));
                } else {
                    System.out.println("No eligible flat types for single applicants.");
                }
            } else {
                System.out.println("Single applicants must be 35 years or older to apply.");
            }
        } else {
            // For married couples, show all flat types
            if (project.hasFlatType(FlatType.TWO_ROOM)) {
                System.out.println("2-Room: " + 
                             project.getAvailableUnitsByType(FlatType.TWO_ROOM) + " units available out of " + 
                             project.getTotalUnitsByType(FlatType.TWO_ROOM));
            }
            
            if (project.hasFlatType(FlatType.THREE_ROOM)) {
                System.out.println("3-Room: " + 
                             project.getAvailableUnitsByType(FlatType.THREE_ROOM) + " units available out of " + 
                             project.getTotalUnitsByType(FlatType.THREE_ROOM));
            }
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. Apply for this Project");
        System.out.println("2. Register as HDB Officer for this Project");
        System.out.println("3. Submit an Enquiry");
        System.out.println("4. Return to Project List");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                applyForProject(project);
                break;
            case "2":
                registerAsOfficer(project);
                break;
            case "3":
                submitEnquiry(project);
                break;
            case "4":
                return;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
        }
    }
    
    /**
     * Apply for a project
     * @param project the project to apply for
     */
    private void applyForProject(Project project) {
        // Check if already has an active application
        if (currentUser.hasActiveApplications()) {
            System.out.println("You already have an active application. Cannot apply for multiple projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Check eligibility
        if (!project.checkEligibility(currentUser)) {
            System.out.println("You are not eligible for this project. Please check requirements.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Confirm application
        System.out.print("Confirm application for " + project.getProjectName() + "? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = applicationControl.submitApplication(currentUser, project);
            
            if (success) {
                System.out.println("Application submitted successfully! Your application is pending approval.");
            } else {
                System.out.println("Failed to submit application. Please ensure you meet all requirements.");
            }
        } else {
            System.out.println("Application cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Register as an HDB Officer for a project
     * @param project the project to register for
     */
    private void registerAsOfficer(Project project) {
        // Check if there are available officer slots
        if (project.getAvailableOfficerSlots() <= 0) {
            System.out.println("No available officer slots for this project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Check if already registered or applied for this project
        for (Application app : currentUser.getApplications()) {
            if (app.getProject().equals(project)) {
                System.out.println("You have already applied for this project as an Applicant.");
                System.out.println("You cannot be both an Applicant and an Officer for the same project.");
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return;
            }
        }
        
        // Confirm registration
        System.out.print("Confirm registration as HDB Officer for " + project.getProjectName() + "? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            // Create a temporary HDBOfficer
            HDBOfficer tempOfficer = new HDBOfficer(
                currentUser.getName(), 
                currentUser.getNRIC(), 
                currentUser.getPassword(), 
                currentUser.getAge(), 
                currentUser.getMaritalStatus(), 
                "HDBOfficer"
            );
            
            boolean success = officerControl.registerOfficer(tempOfficer, project);
            
            if (success) {
                System.out.println("Registration submitted successfully! Your registration is pending approval.");
                System.out.println("You can check your officer registration status in your profile.");
            } else {
                System.out.println("Failed to register. You may already be an officer for another project in the same period.");
            }
        } else {
            System.out.println("Registration cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Submit an enquiry about a project
     * @param project the project
     */
    private void submitEnquiry(Project project) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Submit Enquiry =====");
        System.out.println("Project: " + project.getProjectName());
        
        System.out.println("\nPlease select an enquiry category:");
        System.out.println("1. General Enquiry");
        System.out.println("2. Application Process");
        System.out.println("3. Flat Types and Availability");
        System.out.println("4. Eligibility Criteria");
        System.out.println("5. Other");
        
        System.out.print("Enter category number: ");
        int categoryNum = 0;
        try {
            categoryNum = Integer.parseInt(sc.nextLine());
            if (categoryNum < 1 || categoryNum > 5) {
                System.out.println("Invalid category. Defaulting to 'General Enquiry'");
                categoryNum = 1;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Defaulting to 'General Enquiry'");
            categoryNum = 1;
        }
        
        String category;
        switch (categoryNum) {
            case 2: category = "Application Process"; break;
            case 3: category = "Flat Types and Availability"; break;
            case 4: category = "Eligibility Criteria"; break;
            case 5: category = "Other"; break;
            default: category = "General Enquiry"; break;
        }
        
        System.out.println("\nEnter your enquiry (250 characters max):");
        String enquiryContent = sc.nextLine();
        
        if (enquiryContent.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty. Operation cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Trim if too long
        if (enquiryContent.length() > 250) {
            enquiryContent = enquiryContent.substring(0, 250);
            System.out.println("Note: Your enquiry has been trimmed to 250 characters.");
        }
        
        // Format enquiry with category
        String formattedEnquiry = "[" + category + "] " + enquiryContent;
        
        // Submit enquiry
        Enquiry enquiry = enquiryControl.submitEnquiry(currentUser, project, formattedEnquiry);
        
        if (enquiry != null) {
            System.out.println("\nEnquiry submitted successfully!");
            System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
        } else {
            System.out.println("\nFailed to submit enquiry. Please try again later.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View all applications made by the applicant
     */
    private void viewMyApplications() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Applications =====");
        
        List<Application> applications = currentUser.getApplications();
        
        if (applications.isEmpty()) {
            System.out.println("You have not applied for any projects yet.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.printf("%-5s %-20s %-15s %-15s %-15s\n", 
                         "No.", "Project Name", "Application Date", "Status", "Last Updated");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            
            System.out.printf("%-5d %-20s %-15s %-15s %-15s\n", 
                            (i + 1),
                            truncateString(app.getProject().getProjectName(), 20),
                            dateFormat.format(app.getApplicationDate()),
                            app.getStatus().getDisplayValue(),
                            dateFormat.format(app.getStatusUpdateDate()));
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. View Application Details");
        System.out.println("2. Request Withdrawal");
        System.out.println("3. Return to Main Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                System.out.print("Enter application number to view details: ");
                try {
                    int appIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (appIndex >= 0 && appIndex < applications.size()) {
                        viewApplicationDetails(applications.get(appIndex));
                    } else {
                        System.out.println("Invalid application number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "2":
                System.out.print("Enter application number to request withdrawal: ");
                try {
                    int appIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (appIndex >= 0 && appIndex < applications.size()) {
                        requestWithdrawal(applications.get(appIndex));
                    } else {
                        System.out.println("Invalid application number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
        }
    }
    
    /**
     * View details of a specific application
     * @param application the application to view
     */
    private void viewApplicationDetails(Application application) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n===== Application Details =====");
        System.out.println("Application ID: " + application.getApplicationID());
        System.out.println("Project: " + application.getProject().getProjectName());
        System.out.println("Application Date: " + dateFormat.format(application.getApplicationDate()));
        System.out.println("Status: " + application.getStatus().getDisplayValue());
        System.out.println("Last Updated: " + dateFormat.format(application.getStatusUpdateDate()));
        
        if (application.getStatus() == ApplicationStatus.BOOKED) {
            Flat bookedFlat = application.getBookedFlat();
            if (bookedFlat != null) {
                System.out.println("\nBooked Flat Details:");
                System.out.println("Flat ID: " + bookedFlat.getFlatID());
                System.out.println("Flat Type: " + bookedFlat.getType().getDisplayValue());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Request withdrawal of an application
     * @param application the application to withdraw
     */
    private void requestWithdrawal(Application application) {
        if (!application.canWithdraw()) {
            System.out.println("This application cannot be withdrawn at this stage.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Confirm withdrawal of application for " + 
                        application.getProject().getProjectName() + "? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = applicationControl.withdrawApplication(application);
            
            if (success) {
                System.out.println("Withdrawal request submitted successfully!");
                System.out.println("Your request will be processed by the HDB Manager.");
            } else {
                System.out.println("Failed to submit withdrawal request.");
            }
        } else {
            System.out.println("Withdrawal cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Manage enquiries (submit, view, edit, delete)
     */
    private void enquiryManagement() {
        boolean done = false;
        
        while (!done) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Enquiry Management =====");
            System.out.println("1. View My Enquiries");
            System.out.println("2. Submit New Enquiry");
            System.out.println("3. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    viewMyEnquiries();
                    break;
                case "2":
                    submitNewEnquiry();
                    break;
                case "3":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
            }
        }
    }
    
    /**
     * View all enquiries made by the applicant
     */
    private void viewMyEnquiries() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Enquiries =====");
        
        List<Enquiry> enquiries = enquiryControl.getEnquiriesByApplicant(currentUser);
        
        if (enquiries.isEmpty()) {
            System.out.println("You have not submitted any enquiries yet.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        System.out.printf("%-5s %-20s %-20s %-30s %-10s\n", 
                         "No.", "Project", "Date", "Content", "Replies");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry enquiry = enquiries.get(i);
            
            System.out.printf("%-5d %-20s %-20s %-30s %-10d\n", 
                            (i + 1),
                            truncateString(enquiry.getProject().getProjectName(), 20),
                            dateFormat.format(enquiry.getSubmissionDate()),
                            truncateString(enquiry.getContent(), 30),
                            enquiry.getReplies().size());
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. View Enquiry Details");
        System.out.println("2. Edit Enquiry");
        System.out.println("3. Delete Enquiry");
        System.out.println("4. Return to Enquiry Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                System.out.print("Enter enquiry number to view details: ");
                try {
                    int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (enquiryIndex >= 0 && enquiryIndex < enquiries.size()) {
                        viewEnquiryDetails(enquiries.get(enquiryIndex));
                    } else {
                        System.out.println("Invalid enquiry number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "2":
                System.out.print("Enter enquiry number to edit: ");
                try {
                    int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (enquiryIndex >= 0 && enquiryIndex < enquiries.size()) {
                        editEnquiry(enquiries.get(enquiryIndex));
                    } else {
                        System.out.println("Invalid enquiry number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "3":
                System.out.print("Enter enquiry number to delete: ");
                try {
                    int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (enquiryIndex >= 0 && enquiryIndex < enquiries.size()) {
                        deleteEnquiry(enquiries.get(enquiryIndex));
                    } else {
                        System.out.println("Invalid enquiry number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "4":
                return;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
        }
    }
    
    /**
     * View details of a specific enquiry
     * @param enquiry the enquiry to view
     */
    private void viewEnquiryDetails(Enquiry enquiry) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        System.out.println("\n===== Enquiry Details =====");
        System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
        System.out.println("Project: " + enquiry.getProject().getProjectName());
        System.out.println("Submission Date: " + dateFormat.format(enquiry.getSubmissionDate()));
        System.out.println("\nContent:");
        System.out.println(enquiry.getContent());
        
        List<String> replies = enquiry.getReplies();
        if (!replies.isEmpty()) {
            System.out.println("\nReplies:");
            for (int i = 0; i < replies.size(); i++) {
                System.out.println((i + 1) + ". " + replies.get(i));
            }
        } else {
            System.out.println("\nNo replies yet.");
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Edit an existing enquiry
     * @param enquiry the enquiry to edit
     */
    private void editEnquiry(Enquiry enquiry) {
        // Check if enquiry has replies (can't edit if it does)
        if (!enquiry.getReplies().isEmpty()) {
            System.out.println("Cannot edit an enquiry that has already been replied to.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        ScreenUtil.clearScreen();
        System.out.println("\n===== Edit Enquiry =====");
        System.out.println("Current Content:");
        System.out.println(enquiry.getContent());
        
        System.out.println("\nEnter new content (250 characters max):");
        String newContent = sc.nextLine();
        
        if (newContent.trim().isEmpty()) {
            System.out.println("Content cannot be empty. Edit cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Trim if too long
        if (newContent.length() > 250) {
            newContent = newContent.substring(0, 250);
            System.out.println("Note: Your content has been trimmed to 250 characters.");
        }
        
        // Preserve category if present
        String category = "";
        if (enquiry.getContent().startsWith("[") && enquiry.getContent().contains("]")) {
            category = enquiry.getContent().substring(0, enquiry.getContent().indexOf("]") + 1) + " ";
            newContent = category + newContent;
        }
        
        boolean success = enquiryControl.editEnquiry(enquiry, newContent);
        
        if (success) {
            System.out.println("Enquiry updated successfully!");
        } else {
            System.out.println("Failed to update enquiry.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Delete an enquiry
     * @param enquiry the enquiry to delete
     */
    private void deleteEnquiry(Enquiry enquiry) {
        // Check if enquiry has replies (can't delete if it does)
        if (!enquiry.getReplies().isEmpty()) {
            System.out.println("Cannot delete an enquiry that has already been replied to.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Are you sure you want to delete this enquiry? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = enquiryControl.deleteEnquiry(enquiry);
            
            if (success) {
                System.out.println("Enquiry deleted successfully!");
            } else {
                System.out.println("Failed to delete enquiry.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Submit a new enquiry
     */
    private void submitNewEnquiry() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Submit New Enquiry =====");
        
        // Get eligible projects
        List<Project> eligibleProjects = projectControl.getEligibleProjects(currentUser);
        
        if (eligibleProjects.isEmpty()) {
            System.out.println("No eligible projects available for enquiry at this time.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display projects
        System.out.println("Select a project to enquire about:");
        for (int i = 0; i < eligibleProjects.size(); i++) {
            System.out.println((i + 1) + ". " + eligibleProjects.get(i).getProjectName());
        }
        
        System.out.print("\nEnter project number (0 to cancel): ");
        int projectIndex;
        try {
            projectIndex = Integer.parseInt(sc.nextLine()) - 1;
            if (projectIndex < 0 || projectIndex >= eligibleProjects.size()) {
                if (projectIndex == -1) {
                    System.out.println("Operation cancelled.");
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                } else {
                    System.out.println("Invalid project number. Press Enter to continue.");
                    sc.nextLine();
                }
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        Project selectedProject = eligibleProjects.get(projectIndex);
        
        // Select category
        System.out.println("\nPlease select an enquiry category:");
        System.out.println("1. General Enquiry");
        System.out.println("2. Application Process");
        System.out.println("3. Flat Types and Availability");
        System.out.println("4. Eligibility Criteria");
        System.out.println("5. Other");
        
        System.out.print("Enter category number: ");
        int categoryNum = 0;
        try {
            categoryNum = Integer.parseInt(sc.nextLine());
            if (categoryNum < 1 || categoryNum > 5) {
                System.out.println("Invalid category. Defaulting to 'General Enquiry'");
                categoryNum = 1;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Defaulting to 'General Enquiry'");
            categoryNum = 1;
        }
        
        String category;
        switch (categoryNum) {
            case 2: category = "Application Process"; break;
            case 3: category = "Flat Types and Availability"; break;
            case 4: category = "Eligibility Criteria"; break;
            case 5: category = "Other"; break;
            default: category = "General Enquiry"; break;
        }
        
        System.out.println("\nEnter your enquiry (250 characters max):");
        String enquiryContent = sc.nextLine();
        
        if (enquiryContent.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty. Operation cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Trim if too long
        if (enquiryContent.length() > 250) {
            enquiryContent = enquiryContent.substring(0, 250);
            System.out.println("Note: Your enquiry has been trimmed to 250 characters.");
        }
        
        // Format enquiry with category
        String formattedEnquiry = "[" + category + "] " + enquiryContent;
        
        // Submit enquiry
        Enquiry enquiry = enquiryControl.submitEnquiry(currentUser, selectedProject, formattedEnquiry);
        
        if (enquiry != null) {
            System.out.println("\nEnquiry submitted successfully!");
            System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
        } else {
            System.out.println("\nFailed to submit enquiry. Please try again later.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View applicant's profile
     */
    private void viewProfile() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Profile =====");
        System.out.println("Name: " + currentUser.getName());
        System.out.println("NRIC: " + currentUser.getNRIC());
        System.out.println("Age: " + currentUser.getAge());
        System.out.println("Marital Status: " + currentUser.getMaritalStatusDisplayValue());
        System.out.println("Role: " + currentUser.getRole());
        
        // Show booked flat if any
        Flat bookedFlat = currentUser.getBookedFlat();
        if (bookedFlat != null) {
            System.out.println("\nBooked Flat Information:");
            System.out.println("Project: " + bookedFlat.getProject().getProjectName());
            System.out.println("Flat Type: " + bookedFlat.getType().getDisplayValue());
            System.out.println("Flat ID: " + bookedFlat.getFlatID());
        } else {
            System.out.println("\nNo flats booked yet.");
        }
        
        // Show officer registrations if any
        List<Map<String, Object>> officerRegistrations = officerControl.getOfficerRegistrations(
            new HDBOfficer(
                currentUser.getName(),
                currentUser.getNRIC(),
                currentUser.getPassword(),
                currentUser.getAge(),
                currentUser.getMaritalStatus(),
                "HDBOfficer"
            )
        );
        
        if (!officerRegistrations.isEmpty()) {
            System.out.println("\nOfficer Registration Status:");
            for (Map<String, Object> reg : officerRegistrations) {
                Project project = (Project) reg.get("project");
                RegistrationStatus status = (RegistrationStatus) reg.get("status");
                System.out.println("- " + project.getProjectName() + ": " + status.getDisplayValue());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Change password
     */
    private void changePassword() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Change Password =====");
        System.out.print("Enter current password: ");
        String currentPassword = sc.nextLine();
        
        if (!currentUser.getPassword().equals(currentPassword)) {
            System.out.println("Incorrect password. Password change cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();
        
        if (newPassword.length() < 8) {
            System.out.println("Password must be at least 8 characters long. Password change cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Confirm new password: ");
        String confirmPassword = sc.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Password change cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
                // Change password
                // After setting the password in memory
        currentUser.setPassword(newPassword);

        // Update the password in the CSV file
        UserControl userControl = new UserControl();
        boolean saved = userControl.updateUserPassword(currentUser);

        if (saved) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Password changed in memory but could not be saved to file.");
            System.out.println("Changes may be lost when you restart the application.");
        }
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}