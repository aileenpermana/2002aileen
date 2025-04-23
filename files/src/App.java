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
        
        // Main application loop
    while (currentUI != null) {
        if (currentUI instanceof ManagerUI) {
            ((ManagerUI) currentUI).displayMenu();
        } else if (currentUI instanceof OfficerUI) {
            ((OfficerUI) currentUI).displayMenu();
            
            // Switch to Applicant UI
            if (currentUser instanceof HDBOfficer) {
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
            ((ApplicantUI) currentUI).displayMenu();
            
            // Switch to Officer UI if the user is an officer and chose to switch
            if (currentUser instanceof Applicant) {
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
            break;
        }
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