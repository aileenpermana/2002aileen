import boundary.ApplicantUI;
import boundary.LoginUI;
import boundary.ManagerUI;
import boundary.OfficerUI;
import control.HDBOfficerControl;
import entity.Applicant;
import entity.HDBManager;
import entity.HDBOfficer;
import entity.User;
import java.util.Scanner;
import utils.InitializationUtil;
import utils.ScreenUtil;

/**
 * Main application class for the BTO Management System.
 */
public class App {
    /**
     * Initialize application data
     */
    public static void initializeData() {
        try {
            System.out.println("Initializing application data...");
            // Initialize application data
            InitializationUtil.initializeDefaultData();
            System.out.println("Data initialization complete.");
        } catch (Exception e) {
            System.out.println("Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

        
    
    public static void main(String[] args) {
        // Display welcome message
        ScreenUtil.clearScreen();
        System.out.println("====================================================");
        System.out.println("           BTO MANAGEMENT SYSTEM                    ");
        System.out.println("====================================================");
        System.out.println("Welcome to the BTO Management System");
        System.out.println("Developed for SC/CE/CZ2002 Object-Oriented Design & Programming");
        System.out.println("====================================================");
        System.out.println();
        
        // Initialize application data
        initializeData();
        
        boolean exitApplication = false;
        
        // Application loop - continues until user explicitly exits
        while (!exitApplication) {
            // Initialize login UI
            LoginUI loginUI = new LoginUI();
            User currentUser = null;
            Object currentUI = null;
    
            // Login loop
            while (currentUser == null) {
                boolean shouldContinue = loginUI.displayLoginMenu();
                if (!shouldContinue) {
                    currentUser = loginUI.getCurrentUser();
                    if (currentUser == null) {
                        System.out.println("Login failed. Press Enter to try again or type 'exit' to quit.");
                        Scanner sc = new Scanner(System.in);
                        String input = sc.nextLine();
                        if (input.equalsIgnoreCase("exit")) {
                            exitApplication = true;
                            break;
                        }
                    } else {
                        // Determine initial UI based on user type
                        if (currentUser instanceof HDBManager) {
                            currentUI = new ManagerUI((HDBManager) currentUser);
                        } else if (currentUser instanceof HDBOfficer) {
                            currentUI = new OfficerUI((HDBOfficer) currentUser);
                        } else if (currentUser instanceof Applicant) {
                            currentUI = new ApplicantUI((Applicant) currentUser);
                        }
                    }
                }
            }
            
            // If user chose to exit from login, break out of the main loop
            if (exitApplication) {
                break;
            }
            
            // Main application loop - runs until user signs out
            boolean signedOut = false;
            while (currentUI != null && !signedOut) {
                if (currentUI instanceof ManagerUI) {
                    signedOut = ((ManagerUI) currentUI).displayMenu();
                } else if (currentUI instanceof OfficerUI) {
                    signedOut = ((OfficerUI) currentUI).displayMenu();
                    
                    // Switch to Applicant UI if not signed out
                    if (!signedOut && currentUser instanceof HDBOfficer) {
                        Applicant applicant = new Applicant(
                            currentUser.getName(), 
                            currentUser.getNRIC(), 
                            currentUser.getPassword(), 
                            currentUser.getAge(), 
                            currentUser.getMaritalStatus(), 
                            "Applicant"
                        );
                        currentUser = applicant;
                        currentUI = new ApplicantUI(applicant);
                    }
                } else if (currentUI instanceof ApplicantUI) {
                    signedOut = ((ApplicantUI) currentUI).displayMenu();
                    
                    // Switch to Officer UI if the user is an officer and chose to switch
                    if (!signedOut && currentUser instanceof Applicant) {
                        HDBOfficerControl officerControl = new HDBOfficerControl();
                        if (officerControl.isOfficer(currentUser.getNRIC())) {
                            HDBOfficer officer = new HDBOfficer(
                                currentUser.getName(), 
                                currentUser.getNRIC(), 
                                currentUser.getPassword(), 
                                currentUser.getAge(), 
                                currentUser.getMaritalStatus(), 
                                "HDBOfficer"
                            );
                            currentUser = officer;
                            currentUI = new OfficerUI(officer);
                        }
                    }
                } else {
                    signedOut = true;
                }
                
                // If signed out, break UI loop to return to login
                if (signedOut) {
                    System.out.println("Signing out...");
                    System.out.println("Press Enter to return to login screen...");
                    Scanner sc = new Scanner(System.in);
                    sc.nextLine();
                    break;
                }
            }
        }
        
        // Display exit message
        ScreenUtil.clearScreen();
        System.out.println("====================================================");
        System.out.println("Thank you for using BTO Management System. Goodbye!");
        System.out.println("====================================================");
    }
}