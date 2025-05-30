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
 * UI class for HDB Officer operations in the BTO Management System.
 */
public class OfficerUI {
    private final HDBOfficer currentUser;
    private final Scanner sc;
    private final ProjectControl projectControl;
    private final ApplicationControl applicationControl;
    private final HDBOfficerControl officerControl;
    private final EnquiryControl enquiryControl;
    
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
    public boolean displayMenu() {
        boolean exit = false;
        List<Project> myProjects = currentUser.getHandlingProjects();
        if (myProjects == null || myProjects.isEmpty()) {
            myProjects = projectControl.getProjectsByOfficer(currentUser);
            currentUser.setHandlingProjects(myProjects);
        }
        
        while (!exit) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== HDB Officer Menu =====");
            System.out.println("Welcome, " + currentUser.getName() + "!");
            System.out.println("1. View My Officer Registrations");
            System.out.println("2. View Projects I'm Handling");
            System.out.println("3. Flat Selection & Booking");
            System.out.println("4. View & Reply to Enquiries");
            System.out.println("5. View My Profile");
            System.out.println("6. Switch role to Applicant");
            System.out.println("7. Change Password");
            System.out.println("8. Sign Out");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1" -> viewOfficerRegistrations();
                case "2" -> viewHandledProjects(myProjects);
                case "3" -> flatSelectionMenu();
                case "4" -> viewAndReplyToEnquiries();
                case "5" -> viewProfile();
                case "6" -> {
                    System.out.println("Switching to Applicant role...");
                    return false;
                }
                case "7" -> changePassword();
                case "8" -> {
                    System.out.println("Signing out...");
                    return true;
                }
                default -> {
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
                }
            }
        }
        return false;
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
    private void viewHandledProjects(List<Project> handledProjects) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Projects I'm Handling =====");
        
        
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
            System.out.println("1. Book Flat for Applicant");
            System.out.println("2. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1" -> bookFlat(selectedProject);
                case "2" -> done = true;
                default -> {
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
                }
            }
        }
    }
    
    
    private void bookFlat(Project project) {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Book Flat for Applicant =====");
        System.out.println("Project: " + project.getProjectName());
        
        // Step 1: Get the applicant's NRIC
        System.out.print("Enter applicant's NRIC: ");
        String nric = sc.nextLine().trim().toUpperCase();
        
        if (nric.isEmpty()) {
            System.out.println("Operation cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Validate NRIC format
        if (!nric.matches("[STst]\\d{7}[A-Za-z]")) {
            System.out.println("Invalid NRIC format. It should start with S or T, followed by 7 digits, and end with a letter.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Step 2: Find the successful application
        ApplicationControl applicationControl = new ApplicationControl();
        Application application = applicationControl.findApplicationByNRICAndProject(nric, project);
        
        if (application == null) {
            System.out.println("No successful application found for NRIC: " + nric + " in project " + project.getProjectName());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Step 3: Validate application status
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("This application is not in 'Successful' status and cannot be booked.");
            System.out.println("Current status: " + application.getStatus().getDisplayValue());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Step 4: Verify applicant eligibility and determine eligible flat types
        Applicant applicant = application.getApplicant();
        List<FlatType> availableFlatTypes = new ArrayList<>();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room at 35 or above
            if (applicant.getAge() >= 35 && 
                project.hasFlatType(FlatType.TWO_ROOM) && 
                project.getAvailableUnitsByType(FlatType.TWO_ROOM) > 0) {
                availableFlatTypes.add(FlatType.TWO_ROOM);
            }
        } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
            // Married applicants can apply for any available flat type from 21 or above
            if (applicant.getAge() >= 21) {
                if (project.hasFlatType(FlatType.TWO_ROOM) && 
                    project.getAvailableUnitsByType(FlatType.TWO_ROOM) > 0) {
                    availableFlatTypes.add(FlatType.TWO_ROOM);
                }
                if (project.hasFlatType(FlatType.THREE_ROOM) && 
                    project.getAvailableUnitsByType(FlatType.THREE_ROOM) > 0) {
                    availableFlatTypes.add(FlatType.THREE_ROOM);
                }
            }
        }
        
        if (availableFlatTypes.isEmpty()) {
            System.out.println("No eligible flat types available for this applicant.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Step 5: Display available flat types
        System.out.println("\nAvailable Flat Types:");
        for (int i = 0; i < availableFlatTypes.size(); i++) {
            FlatType type = availableFlatTypes.get(i);
            System.out.println((i + 1) + ". " + type.getDisplayValue() + 
                             " (" + project.getAvailableUnitsByType(type) + " units available)");
        }
        
        // Step 6: Applicant selects flat type
        System.out.print("\nSelect flat type (0 to cancel): ");
        int typeChoice;
        try {
            typeChoice = Integer.parseInt(sc.nextLine());
            if (typeChoice == 0) {
                System.out.println("Booking cancelled.");
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return;
            }
            
            if (typeChoice < 1 || typeChoice > availableFlatTypes.size()) {
                System.out.println("Invalid choice. Booking cancelled.");
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Booking cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        FlatType selectedType = availableFlatTypes.get(typeChoice - 1);
        
        // Step 7: Book the flat
        HDBOfficerControl officerControl = new HDBOfficerControl();
        boolean success = officerControl.bookFlatForApplicant(currentUser, application, selectedType);
        
        if (success) {
            System.out.println("\nFlat booked successfully!");
            
            // Display booking details
            Flat bookedFlat = application.getBookedFlat();
            System.out.println("Project: " + project.getProjectName());
            System.out.println("Applicant: " + applicant.getName());
            System.out.println("Flat Type: " + selectedType.getDisplayValue());
            System.out.println("Flat ID: " + bookedFlat.getFlatID());
            
            // Generate and display receipt
            Receipt receipt = new Receipt("REC-" + application.getApplicationID(), application, currentUser);
            System.out.println("\nReceipt Generated:");
            System.out.println(receipt.generateFormattedReceipt());
            
        } else {
            System.out.println("\nFailed to book flat. Please check eligibility and availability.");
        }
        
        System.out.println("\nPress Enter to continue...");
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
        
        OUTER:
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
            switch (choice) {
                case "1" -> {
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
                }
                case "2" -> {
                    break OUTER;
                }
                default -> {
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
                }
            }
        }
    }
    
    /**
     * View and reply to a specific enquiry
     * @param enquiry the enquiry
     */
    private void viewAndReplyToEnquiry(Enquiry enquiry) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        OUTER:
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
            switch (choice) {
                case "1" -> {
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
                }
                case "2" -> {
                    break OUTER;
                }
                default -> {
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
                }
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