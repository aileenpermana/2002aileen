package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Utility class to look up user data from CSV files
 */
public class UserDataLookup {
    private static final String APPLICANT_FILE = "files/resources/ApplicantList.csv";
    private static final String OFFICER_FILE = "files/resources/OfficerList.csv";
    private static final String MANAGER_FILE = "files/resources/ManagerList.csv";
    
    /**
     * Find a user by NRIC across all user types
     * @param nric the user's NRIC
     * @return a User object with actual data, or null if not found
     */
    public static User findUserByNRIC(String nric) {
        // Try to find in managers first
        User user = findUserInFile(MANAGER_FILE, nric, "HDBManager");
        if (user != null) {
            return user;
        }
        
        // Try to find in officers
        user = findUserInFile(OFFICER_FILE, nric, "HDBOfficer");
        if (user != null) {
            return user;
        }
        
        // Try to find in applicants
        user = findUserInFile(APPLICANT_FILE, nric, "Applicant");
        if (user != null) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Find a specific user type by NRIC
     * @param nric the user's NRIC
     * @param userType the type of user to return ("Applicant", "HDBOfficer", or "HDBManager")
     * @return a User object of the specified type with actual data, or null if not found
     */
    public static User findUserByNRICAndType(String nric, String userType) {
        String filePath;
        
        switch (userType) {
            case "Applicant":
                filePath = APPLICANT_FILE;
                break;
            case "HDBOfficer":
                filePath = OFFICER_FILE;
                break;
            case "HDBManager":
                filePath = MANAGER_FILE;
                break;
            default:
                return null;
        }
        
        return findUserInFile(filePath, nric, userType);
    }
    
    /**
     * Find a user in a specific CSV file
     * @param filePath the path to the CSV file
     * @param nric the user's NRIC
     * @param userType the type of user to create
     * @return a User object with actual data, or null if not found
     */
    private static User findUserInFile(String filePath, String nric, String userType) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            // Skip header
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 5) continue;
                
                String userNRIC = values[1].trim();
                if (userNRIC.equalsIgnoreCase(nric)) {
                    // Found the user data
                    String name = values[0].trim();
                    int age = Integer.parseInt(values[2].trim());
                    String maritalStatus = values[3].trim();
                    String password = values[4].trim();
                    
                    // Create the appropriate user type
                    switch (userType) {
                        case "Applicant":
                            return new Applicant(name, nric, password, age, maritalStatus, userType);
                        case "HDBOfficer":
                            return new HDBOfficer(name, nric, password, age, maritalStatus, userType);
                        case "HDBManager":
                            return new HDBManager(name, nric, password, age, maritalStatus, userType);
                        default:
                            return null;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding user in " + filePath + ": " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Create a fallback user if real data is not found
     * @param nric the user's NRIC
     * @param userType the type of user to create
     * @return a User object with placeholder data
     */
    public static User createFallbackUser(String nric, String userType) {
        switch (userType) {
            case "Applicant":
                return new Applicant(
                    "Applicant " + nric, // Fallback name
                    nric,
                    "password",
                    30, // Fallback age
                    "Single", // Fallback marital status
                    userType
                );
            case "HDBOfficer":
                return new HDBOfficer(
                    "Officer " + nric, // Fallback name
                    nric,
                    "password",
                    30, // Fallback age
                    "Single", // Fallback marital status
                    userType
                );
            case "HDBManager":
                return new HDBManager(
                    "Manager " + nric, // Fallback name
                    nric,
                    "password",
                    30, // Fallback age
                    "Single", // Fallback marital status
                    userType
                );
            default:
                return null;
        }
    }
}