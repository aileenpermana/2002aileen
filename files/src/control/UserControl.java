package control;

import entity.*;
import java.io.*;
import java.util.*;

public class UserControl {
    private static final String APPLICANT_FILE = "files/resources/ApplicantList.csv";
    private static final String OFFICER_FILE = "files/resources/OfficerList.csv";
    private static final String MANAGER_FILE = "files/resources/ManagerList.csv";
    
    /**
     * Update user password in the appropriate CSV file
     * @param user the user whose password is being updated
     * @return true if update was successful, false otherwise
     */
    public boolean updateUserPassword(User user) {
        String filePath;
        
        // Determine which file to update based on user type
        if (user instanceof Applicant) {
            filePath = APPLICANT_FILE;
        } else if (user instanceof HDBOfficer) {
            filePath = OFFICER_FILE;
        } else if (user instanceof HDBManager) {
            filePath = MANAGER_FILE;
        } else {
            return false; // Unknown user type
        }
        
        return updatePasswordInFile(user, filePath);
    }
    
    /**
     * Update a user's password in the specified file
     * @param user the user
     * @param filePath the file path
     * @return true if successful, false otherwise
     */
    private boolean updatePasswordInFile(User user, String filePath) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        try {
            // Read the existing file
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
            
            try (Scanner fileScanner = new Scanner(file)) {
                // Read header
                if (fileScanner.hasNextLine()) {
                    lines.add(fileScanner.nextLine());
                }
                
                // Process each line
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] values = line.split(",");
                    
                    // Check if this is the user we're looking for (by NRIC)
                    if (values.length >= 2 && values[1].trim().equalsIgnoreCase(user.getNRIC())) {
                        // Found the user, update password (assuming password is the 5th column)
                        if (values.length >= 5) {
                            values[4] = user.getPassword();
                            line = String.join(",", values);
                            found = true;
                        }
                    }
                    
                    lines.add(line);
                }
            }
            
            // If user was found, write the updated content back to the file
            if (found) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    for (String line : lines) {
                        writer.println(line);
                    }
                }
                return true;
            }
            
            return false; // User not found
            
        } catch (IOException e) {
            System.err.println("Error updating password in file: " + e.getMessage());
            return false;
        }
    }
}