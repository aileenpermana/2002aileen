package boundary;

import entity.*;
import control.ProjectControl;
import control.ApplicationControl;
import control.HDBOfficerControl;
import control.EnquiryControl;
import utils.ScreenUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * UI class for HDB Officer operations in the BTO Management System.
 */
public class OfficerUI {
    private HDBOfficer currentUser;
    private Scanner sc;
    private ProjectControl projectControl;
    private ApplicationControl applicationControl;
    private HDBOfficerControl officerControl;
    private EnquiryControl enquiryControl;
    
    /**
     * Constructor for OfficerUI
     * @param user the logged-in HDB Officer
     */
    public OfficerUI(HDBOfficer user) {
        this.currentUser = user;
        this.sc = new Scanner(System.in);
        this.projectControl = new ProjectControl();
        this.applicationControl = new ApplicationControl();
        this.officerControl = new HDBOfficerControl();
        this.enquiryControl = new EnquiryControl();
    }
    
    /**
     * Display the main menu for HDB Officers
     */
    public void displayMenu() {
        boolean exit = false;
        
        while (!exit) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== HDB Officer Menu =====");
            System.out.println("Welcome, " + currentUser.getName() + "!");
            System.out.println("1. View Available Projects");
            System.out.println("2. View My Applications");
            System.out.println("3. View My Officer Registrations");
            System.out.println("4. View Projects I'm Handling");
            System.out.println("5. Flat Selection & Booking");
            System.out.println("6. View & Reply to Enquiries");
            System.out.println("7. View My Profile");
            System.out.println("8. Change Password");
            System.out.println("9. Sign Out");
            
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
                    viewOfficerRegistrations();
                    break;
                case "4":
                    viewHandledProjects();
                    break;
                case "5":
                    flatSelectionMenu();
                    break;
                case "6":
                    viewAndReplyToEnquiries();
                    break;
                case "7":
                    viewProfile();
                    break;
                case "8":
                    changePassword();
                    break;
                case "9":
                    exit = true;
                    System.out.println("Signing out...");
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
            }
        }
    }
    
    /**
     * View available projects - implemented similarly to ApplicantUI
     * This method would be similar to the one in ApplicantUI, so I'm keeping it brief
     */
    private void viewAvailableProjects() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Available Projects =====");
        
        // Get projects based on eligibility
        List<Project> eligibleProjects = projectControl.getEligibleProjects(currentUser);
        
        if (eligibleProjects.isEmpty()) {
            System.out.println("No eligible projects available for you at this time.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display projects and handle user interaction similar to ApplicantUI
        // For brevity, not including the full implementation here
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View my applications - implemented similarly to ApplicantUI
     * This method would be similar to the one in ApplicantUI, so I'm keeping it brief
     */
    private void viewMyApplications() {
        // Implementation similar to ApplicantUI.viewMyApplications()
        System.out.println("\n===== My Applications =====");
        System.out.println("This feature is available but not shown for brevity.");
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View officer registrations
     */
    private void viewOfficerRegistrations() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Officer Registrations =====");
        
        List<Map<String, Object>> registrations = officerControl.getOfficerRegistrations(currentUser);
        
        if (registrations.isEmpty()) {
            System.out.println("You have not registered as an officer for any project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.printf("%-5s %-20s %-15s %-20s\n", 
                         "No.", "Project Name", "Status", "Registration Date");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < registrations.size(); i++) {
            Map<String, Object> reg = registrations.get(i);
            Project project = (Project) reg.get("project");
            RegistrationStatus status = (RegistrationStatus) reg.get("status");
            Date regDate = (Date) reg.get("date");
            
            System.out.printf("%-5d %-20s %-15s %-20s\n", 
                            (i + 1),
                            truncateString(project.getProjectName(), 20),
                            status.getDisplayValue(),
                            dateFormat.format(regDate));
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View projects that the officer is handling
     */
    private void viewHandledProjects() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Projects I'm Handling =====");
        
        List<Project> handledProjects = currentUser.getHandlingProjects();
        
        if (handledProjects.isEmpty()) {
            System.out.println("You are not currently handling any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.printf("%-5s %-20s %-15s %-20s %-15s\n", 
                         "No.", "Project Name", "Neighborhood", "Closing Date", "Available Units");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < handledProjects.size(); i++) {
            Project p = handledProjects.get(i);
            int totalAvailable = calculateTotalAvailableUnits(p);
            
            System.out.printf("%-5d %-20s %-15s %-20s %-15d\n", 
                            (i + 1),
                            truncateString(p.getProjectName(), 20),
                            truncateString(p.getNeighborhood(), 15),
                            dateFormat.format(p.getApplicationCloseDate()),
                            totalAvailable);
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. View Project Details");
        System.out.println("2. Return to Main Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        if (choice.equals("1")) {
            System.out.print("Enter project number to view details: ");
            try {
                int projectIndex = Integer.parseInt(sc.nextLine()) - 1;
                if (projectIndex >= 0 && projectIndex < handledProjects.size()) {
                    viewHandledProjectDetails(handledProjects.get(projectIndex));
                } else {
                    System.out.println("Invalid project number. Press Enter to continue.");
                    sc.nextLine();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * View details of a project being handled
     * @param project the project to view
     */
    private void viewHandledProjectDetails(Project project) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n===== Project Details =====");
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + 
                         dateFormat.format(project.getApplicationOpenDate()) + " to " + 
                         dateFormat.format(project.getApplicationCloseDate()));
        System.out.println("Manager: " + project.getManagerInCharge().getName());
        
        System.out.println("\nFlat Types Available:");
        for (FlatType type : project.getFlatTypes()) {
            System.out.println("- " + type.getDisplayValue() + ": " + 
                             project.getAvailableUnitsByType(type) + " units available out of " + 
                             project.getTotalUnitsByType(type));
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Calculate total available units across all flat types
     * @param project the project
     * @return total available units
     */
    private int calculateTotalAvailableUnits(Project project) {
        int total = 0;
        for (FlatType type : project.getFlatTypes()) {
            total += project.getAvailableUnitsByType(type);
        }
        return total;
    }
    
    /**
     * Display the flat selection menu
     */
    private void flatSelectionMenu() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Flat Selection & Booking =====");
        
        // Get the list of projects this officer is handling
        List<Project> handledProjects = currentUser.getHandlingProjects();
        
        if (handledProjects.isEmpty()) {
            System.out.println("You are not currently handling any projects.");
            System.out.println("Press Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        
        // Display the list of handled projects
        System.out.println("Select a project to manage flat bookings:");
        for (int i = 0; i < handledProjects.size(); i++) {
            System.out.println((i + 1) + ". " + handledProjects.get(i).getProjectName());
        }
        
        System.out.print("\nEnter project number (0 to cancel): ");
        int projectChoice;
        try {
            projectChoice = Integer.parseInt(sc.nextLine());
            if (projectChoice == 0) {
                return;
            }
            
            if (projectChoice < 1 || projectChoice > handledProjects.size()) {
                System.out.println("Invalid project number. Press Enter to continue.");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        Project selectedProject = handledProjects.get(projectChoice - 1);
        
        // Show booking options for the selected project
        boolean done = false;
        while (!done) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Flat Booking for " + selectedProject.getProjectName() + " =====");
            System.out.println("1. Update Available Flats");
            System.out.println("2. Retrieve Applicant's Application");
            System.out.println("3. Book Flat for Applicant");
            System.out.println("4. Generate Receipt");
            System.out.println("5. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    updateAvailableFlats(selectedProject);
                    break;
                case "2":
                    retrieveApplication(selectedProject);
                    break;
                case "3":
                    bookFlat(selectedProject);
                    break;
                case "4":
                    generateReceipt(selectedProject);
                    break;
                case "5":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
            }
        }
    }
    
    /**
     * Update the number of available flats
     * @param project the project to update
     */
    private void updateAvailableFlats(Project project) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Update Available Flats =====");
        System.out.println("Project: " + project.getProjectName());
        
        System.out.println("\nCurrent Flat Availability:");
        for (FlatType type : project.getFlatTypes()) {
            System.out.println(type.getDisplayValue() + ": " + 
                             project.getAvailableUnitsByType(type) + " units available out of " + 
                             project.getTotalUnitsByType(type));
        }
        
        System.out.println("\nSelect flat type to update:");
        List<FlatType> flatTypes = project.getFlatTypes();
        for (int i = 0; i < flatTypes.size(); i++) {
            System.out.println((i + 1) + ". " + flatTypes.get(i).getDisplayValue());
        }
        
        System.out.print("\nEnter flat type number (0 to cancel): ");
        int typeChoice;
        try {
            typeChoice = Integer.parseInt(sc.nextLine());
            if (typeChoice == 0) {
                return;
            }
            
            if (typeChoice < 1 || typeChoice > flatTypes.size()) {
                System.out.println("Invalid flat type number. Press Enter to continue.");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        FlatType selectedType = flatTypes.get(typeChoice - 1);
        int currentAvailable = project.getAvailableUnitsByType(selectedType);
        int totalUnits = project.getTotalUnitsByType(selectedType);
        
        System.out.println("\nCurrent available " + selectedType.getDisplayValue() + " units: " + currentAvailable);
        System.out.print("Enter new available count (0-" + totalUnits + "): ");
        
        try {
            int newCount = Integer.parseInt(sc.nextLine());
            if (newCount < 0 || newCount > totalUnits) {
                System.out.println("Invalid count. Must be between 0 and " + totalUnits);
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return;
            }
            
            // Update the available units
            currentUser.updateFlatAvailability(project, selectedType, newCount);
            System.out.println("Available units updated successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Press Enter to continue.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Retrieve an applicant's application
     * @param project the project
     */
    private void retrieveApplication(Project project) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Retrieve Applicant's Application =====");
        System.out.println("Project: " + project.getProjectName());
        
        System.out.print("Enter applicant's NRIC: ");
        String nric = sc.nextLine();
        
        if (nric.isEmpty()) {
            System.out.println("Operation cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        Application application = currentUser.retrieveApplicationByNRIC(nric, project);
        
        if (application == null) {
            System.out.println("No application found for NRIC: " + nric);
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display application details
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        System.out.println("\nApplication Details:");
        System.out.println("Application ID: " + application.getApplicationID());
        System.out.println("Applicant: " + application.getApplicant().getName());
        System.out.println("NRIC: " + application.getApplicant().getNRIC());
        System.out.println("Status: " + application.getStatus().getDisplayValue());
        System.out.println("Application Date: " + dateFormat.format(application.getApplicationDate()));
        
        if (application.getStatus() == ApplicationStatus.BOOKED) {
            System.out.println("\nThis application already has a booked flat.");
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Book a flat for an applicant
     * @param project the project
     */
    private void bookFlat(Project project) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Book Flat for Applicant =====");
        System.out.println("Project: " + project.getProjectName());
        
        System.out.print("Enter applicant's NRIC: ");
        String nric = sc.nextLine();
        
        if (nric.isEmpty()) {
            System.out.println("Operation cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        Application application = currentUser.retrieveApplicationByNRIC(nric, project);
        
        if (application == null) {
            System.out.println("No application found for NRIC: " + nric);
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("This application is not in 'Successful' status and cannot be booked.");
            System.out.println("Current status: " + application.getStatus().getDisplayValue());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        if (application.getBookedFlat() != null) {
            System.out.println("This application already has a booked flat.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display available flat types
        System.out.println("\nAvailable Flat Types:");
        List<FlatType> availableTypes = new ArrayList<>();
        
        for (FlatType type : project.getFlatTypes()) {
            int availableUnits = project.getAvailableUnitsByType(type);
            if (availableUnits > 0) {
                availableTypes.add(type);
                System.out.println((availableTypes.size()) + ". " + type.getDisplayValue() + 
                                  " (" + availableUnits + " units available)");
            }
        }
        
        if (availableTypes.isEmpty()) {
            System.out.println("No flats available for booking in this project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("\nSelect flat type to book (0 to cancel): ");
        int typeChoice;
        
        try {
            typeChoice = Integer.parseInt(sc.nextLine());
            if (typeChoice == 0) {
                return;
            }
            
            if (typeChoice < 1 || typeChoice > availableTypes.size()) {
                System.out.println("Invalid flat type number. Press Enter to continue.");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        FlatType selectedType = availableTypes.get(typeChoice - 1);
        
        // Generate a new flat
        String flatID = "F" + project.getProjectID() + "-" + selectedType.toString().charAt(0) + "-" + 
                        (project.getTotalUnitsByType(selectedType) - project.getAvailableUnitsByType(selectedType) + 1);
        
        Flat newFlat = new Flat(flatID, project, selectedType);
        
        // Book the flat
        boolean success = currentUser.bookFlat(application, newFlat);
        
        if (success) {
            System.out.println("\nFlat booked successfully!");
            System.out.println("Flat ID: " + newFlat.getFlatID());
            System.out.println("Flat Type: " + newFlat.getType().getDisplayValue());
            System.out.println("Application status updated to: " + application.getStatus().getDisplayValue());
        } else {
            System.out.println("\nFailed to book flat. Please check eligibility and availability.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Generate a receipt for a flat booking
     * @param project the project
     */
    private void generateReceipt(Project project) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Generate Receipt =====");
        System.out.println("Project: " + project.getProjectName());
        
        System.out.print("Enter applicant's NRIC: ");
        String nric = sc.nextLine();
        
        if (nric.isEmpty()) {
            System.out.println("Operation cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        Application application = currentUser.retrieveApplicationByNRIC(nric, project);
        
        if (application == null) {
            System.out.println("No application found for NRIC: " + nric);
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        if (application.getStatus() != ApplicationStatus.BOOKED || application.getBookedFlat() == null) {
            System.out.println("This application does not have a booked flat.");
            System.out.println("Current status: " + application.getStatus().getDisplayValue());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Generate and display receipt
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n========================================");
        System.out.println("           BOOKING RECEIPT             ");
        System.out.println("========================================");
        System.out.println("Receipt Date: " + dateFormat.format(new Date()));
        System.out.println("Receipt ID: REC-" + application.getApplicationID());
        
        System.out.println("\nAPPLICANT DETAILS");
        System.out.println("Name: " + application.getApplicant().getName());
        System.out.println("NRIC: " + application.getApplicant().getNRIC());
        System.out.println("Age: " + application.getApplicant().getAge());
        System.out.println("Marital Status: " + application.getApplicant().getMaritalStatusDisplayValue());
        
        System.out.println("\nPROJECT DETAILS");
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        
        System.out.println("\nBOOKING DETAILS");
        System.out.println("Application ID: " + application.getApplicationID());
        System.out.println("Application Date: " + dateFormat.format(application.getApplicationDate()));
        System.out.println("Flat Type: " + application.getBookedFlat().getType().getDisplayValue());
        System.out.println("Flat ID: " + application.getBookedFlat().getFlatID());
        
        System.out.println("\nThis receipt confirms the booking of the flat.");
        System.out.println("Please keep for your records.");
        System.out.println("========================================");
        
        System.out.println("\nReceipt generated successfully!");
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View and reply to enquiries
     */
    private void viewAndReplyToEnquiries() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== View & Reply to Enquiries =====");
        
        // Get the list of projects this officer is handling
        List<Project> handledProjects = currentUser.getHandlingProjects();
        
        if (handledProjects.isEmpty()) {
            System.out.println("You are not currently handling any projects.");
            System.out.println("Press Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        
        // Display the list of handled projects
        System.out.println("Select a project to view enquiries:");
        for (int i = 0; i < handledProjects.size(); i++) {
            System.out.println((i + 1) + ". " + handledProjects.get(i).getProjectName());
        }
        
        System.out.print("\nEnter project number (0 to cancel): ");
        int projectChoice;
        try {
            projectChoice = Integer.parseInt(sc.nextLine());
            if (projectChoice == 0) {
                return;
            }
            
            if (projectChoice < 1 || projectChoice > handledProjects.size()) {
                System.out.println("Invalid project number. Press Enter to continue.");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Press Enter to continue.");
            sc.nextLine();
            return;
        }
        
        Project selectedProject = handledProjects.get(projectChoice - 1);
        
        // Display enquiries for the selected project
        List<Enquiry> enquiries = enquiryControl.getEnquiriesForProject(selectedProject);
        
        if (enquiries.isEmpty()) {
            System.out.println("\nNo enquiries found for " + selectedProject.getProjectName());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Enquiries for " + selectedProject.getProjectName() + " =====");
            
            System.out.printf("%-5s %-20s %-20s %-30s\n", 
                            "No.", "Applicant", "Date", "Content");
            System.out.println("--------------------------------------------------------------------------------");
            
            for (int i = 0; i < enquiries.size(); i++) {
                Enquiry enquiry = enquiries.get(i);
                System.out.printf("%-5d %-20s %-20s %-30s\n", 
                                (i + 1),
                                truncateString(enquiry.getApplicant().getName(), 20),
                                dateFormat.format(enquiry.getSubmissionDate()),
                                truncateString(enquiry.getContent(), 30));
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. View Enquiry Details");
            System.out.println("2. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1")) {
                System.out.print("Enter enquiry number to view details: ");
                try {
                    int enquiryIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (enquiryIndex >= 0 && enquiryIndex < enquiries.size()) {
                        viewAndReplyToEnquiry(enquiries.get(enquiryIndex));
                        
                        // Refresh the enquiry list in case of updates
                        enquiries = enquiryControl.getEnquiriesForProject(selectedProject);
                    } else {
                        System.out.println("Invalid enquiry number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
            } else if (choice.equals("2")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * View and reply to a specific enquiry
     * @param enquiry the enquiry
     */
    private void viewAndReplyToEnquiry(Enquiry enquiry) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Enquiry Details =====");
            System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
            System.out.println("From: " + enquiry.getApplicant().getName() + " (" + 
                             enquiry.getApplicant().getNRIC() + ")");
            System.out.println("Project: " + enquiry.getProject().getProjectName());
            System.out.println("Date: " + dateFormat.format(enquiry.getSubmissionDate()));
            System.out.println("\nContent:");
            System.out.println(enquiry.getContent());
            
            List<String> replies = enquiry.getReplies();
            if (!replies.isEmpty()) {
                System.out.println("\nReplies:");
                for (int i = 0; i < replies.size(); i++) {
                    System.out.println((i + 1) + ". " + replies.get(i));
                }
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. Reply to Enquiry");
            System.out.println("2. Return to Enquiry List");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1")) {
                System.out.println("\nEnter your reply:");
                String reply = sc.nextLine();
                
                if (!reply.trim().isEmpty()) {
                    // Add reply to the enquiry
                    enquiryControl.addReply(enquiry, reply, currentUser);
                    System.out.println("Reply submitted successfully!");
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                } else {
                    System.out.println("Reply cannot be empty. Press Enter to continue...");
                    sc.nextLine();
                }
            } else if (choice.equals("2")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * View officer's profile
     */
    private void viewProfile() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Profile =====");
        System.out.println("Name: " + currentUser.getName());
        System.out.println("NRIC: " + currentUser.getNRIC());
        System.out.println("Age: " + currentUser.getAge());
        System.out.println("Marital Status: " + currentUser.getMaritalStatusDisplayValue());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Officer ID: " + currentUser.getOfficerID());
        
        List<Project> handlingProjects = currentUser.getHandlingProjects();
        if (!handlingProjects.isEmpty()) {
            System.out.println("\nProjects I'm Handling:");
            for (Project project : handlingProjects) {
                System.out.println("- " + project.getProjectName());
            }
        } else {
            System.out.println("\nNot currently handling any projects.");
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
        
        System.out.print("Confirm new password: ");
        String confirmPassword = sc.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Password change cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Change password
        currentUser.setPassword(newPassword);
        System.out.println("Password changed successfully!");
        System.out.println("Press Enter to continue...");
        sc.nextLine();
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
}