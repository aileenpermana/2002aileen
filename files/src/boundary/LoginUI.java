package boundary;

import control.LoginControl;
import control.RegistrationControl;
import entity.User;
import java.util.Scanner;
import utils.ScreenUtil; // Make sure to import User

public class LoginUI {
    private Scanner sc;
    private LoginControl loginControl;
    private RegistrationControl registrationControl;
    private User currentUser; // Add this to store the current user
    
    public LoginUI() {
        sc = new Scanner(System.in);
        loginControl = new LoginControl();
    }
    
    // Add a getter for the current user
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean displayLoginMenu() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== BTO Management System =====");
        System.out.println("Welcome! Please login to continue.");
        
        // Ask for NRIC and password
        System.out.print("Enter your NRIC: ");
        String nric = sc.nextLine();
        nric = nric.toUpperCase();
        
        System.out.print("Enter your password (if you are a new user, use default password 'password'):");
        String password = sc.nextLine();
        
        // Check if user exists
        if (!loginControl.validateInitial(nric, password)) {
            System.out.println("Please try again. Click ENTER to restart.");
            sc.nextLine();
            return true;
        } else {
            // Get the user from login control
            User user = loginControl.checkUserCredentials(nric, password);
            
            if (user != null) {
                // Successful login - set the current user
                currentUser = user;
                System.out.println("\nLogin successful!");
                System.out.println("Welcome, " + user.getName() + "! ");
                return false; // Continue to main menu
            } else {
                // User not found, ask if they want to register
                System.out.println("\nUser not found or password is incorrect.");
                System.out.print("Do you want to register as a new user? (Y/N): ");
                String response = sc.nextLine();
                
                if (response.equalsIgnoreCase("Y")) {
                    return registerNewUser(nric);
                } else {
                    System.out.println("Login cancelled. Click ENTER to restart.");
                    sc.nextLine();
                    return true;
                }
            }
        }
    }
    
    private boolean registerNewUser(String nric) {
        // Collect user information
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        
        System.out.print("Enter your age: ");
        int age = 0;
        try {
            age = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age format. Registration cancelled. click ENTER to restart.");
            sc.nextLine();
            return true;
        }
        
        System.out.print("Enter your marital status (Single/Married): ");
        String maritalStatus = sc.nextLine();
        
        String password = "password";
        
        // Create registration control and register
        registrationControl = new RegistrationControl(name, nric, password, age, maritalStatus);
        
        if (registrationControl.validateCredentials()) {
            // Register and get the user object
            User newUser = registrationControl.registerUser();
            if (newUser != null) {
                // Set the current user
                currentUser = newUser;
                System.out.println("\nPlease change default password upon logging in.");
                System.out.println("Welcome, " + name + "! ");
                return false; // Continue to main menu
            } else {
                System.out.println("\nRegistration failed. Please try again later. click ENTER to restart.");
                sc.nextLine();
                return true;
            }
        } else {
            System.out.println("\nRegistration failed. Please try again later. click ENTER to restart.");
            sc.nextLine();
            return true;
        }
    }
    
    // Method to close scanner
    public void close() {
        if (sc != null) {
            sc.close();
        }
    }

    public Object getScanner() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScanner'");
    }
}