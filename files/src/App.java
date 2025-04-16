import boundary.ApplicantUI;
import boundary.LoginUI;
import boundary.ManagerUI;
import boundary.OfficerUI;
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
    
    /**
     * Main method to start the application.
     * @param args command line arguments
     */
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
        
        // Initialize login UI
        LoginUI loginUI = new LoginUI();
        User currentUser = null;
        
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
                        break;
                    }
                }
            }
        }
        
        // Route to appropriate UI based on user role
        if (currentUser instanceof HDBManager) {
            ManagerUI managerUI = new ManagerUI((HDBManager) currentUser);
            managerUI.displayMenu();
        } else if (currentUser instanceof HDBOfficer) {
            OfficerUI officerUI = new OfficerUI((HDBOfficer) currentUser);
            officerUI.displayMenu();
        } else if (currentUser instanceof Applicant) {
            ApplicantUI applicantUI = new ApplicantUI((Applicant) currentUser);
            applicantUI.displayMenu();
        } else {
            System.out.println("Unknown user type. Please contact system administrator.");
        }
        
        // Close resources
        loginUI.close();
        
        // Display exit message
        ScreenUtil.clearScreen();
        System.out.println("====================================================");
        System.out.println("Thank you for using BTO Management System. Goodbye!");
        System.out.println("====================================================");
    }
}