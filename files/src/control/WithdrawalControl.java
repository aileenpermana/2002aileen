package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Enhanced methods for application withdrawal functionality
 */
public class WithdrawalControl {
    private static final String WITHDRAWALS_FILE = "files/resources/WithdrawalList.csv";
    private List<Map<String, Object>> withdrawalRequests;
    
    /**
     * Constructor initializes the withdrawal requests list from storage
     */
    public WithdrawalControl() {
        this.withdrawalRequests = loadWithdrawalRequests();
    }
    
    /**
     * Get withdrawal requests for a project
     * @param project the project
     * @return list of withdrawal requests
     */
    public List<Map<String, Object>> getWithdrawalRequestsForProject(Project project) {
        List<Map<String, Object>> projectWithdrawals = new ArrayList<>();
        
        for (Map<String, Object> request : withdrawalRequests) {
            Application app = (Application) request.get("application");
            if (app.getProject().getProjectID().equals(project.getProjectID())) {
                projectWithdrawals.add(request);
            }
        }
        
        return projectWithdrawals;
    }
    
    /**
     * Get withdrawal requests by an applicant
     * @param applicant the applicant
     * @return list of withdrawal requests
     */
    public List<Map<String, Object>> getWithdrawalRequestsByApplicant(Applicant applicant) {
        List<Map<String, Object>> applicantWithdrawals = new ArrayList<>();
        
        for (Map<String, Object> request : withdrawalRequests) {
            Application app = (Application) request.get("application");
            if (app.getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantWithdrawals.add(request);
            }
        }
        
        return applicantWithdrawals;
    }
    
    /**
     * Submit a withdrawal request
     * @param application the application to withdraw
     * @param reason the reason for withdrawal
     * @return true if request was successful, false otherwise
     */
    public boolean submitWithdrawalRequest(Application application, String reason) {
        // Check if application can be withdrawn
        if (!application.canWithdraw()) {
            return false;
        }
        
        // Check if there's already a pending withdrawal request for this application
        for (Map<String, Object> request : withdrawalRequests) {
            Application app = (Application) request.get("application");
            Boolean isProcessed = (Boolean) request.get("isProcessed");
            
            if (app.getApplicationID().equals(application.getApplicationID()) && !isProcessed) {
                return false; // Already has a pending request
            }
        }
        
        // Create withdrawal request
        Map<String, Object> request = new HashMap<>();
        request.put("application", application);
        request.put("reason", reason);
        request.put("requestDate", new Date());
        request.put("isProcessed", false);
        request.put("isApproved", false);
        request.put("processedDate", null);
        request.put("processedBy", null);
        
        // Add to list
        withdrawalRequests.add(request);
        
        // Save to file
        return saveWithdrawalRequests();
    }
    
    /**
     * Process a withdrawal request
     * @param application the application
     * @param approve true to approve, false to reject
     * @param processor the user processing the request
     * @return true if processing was successful, false otherwise
     */
    public boolean processWithdrawalRequest(Application application, boolean approve, User processor) {
        // Find the request
        Map<String, Object> request = null;
        for (Map<String, Object> req : withdrawalRequests) {
            Application app = (Application) req.get("application");
            Boolean isProcessed = (Boolean) req.get("isProcessed");
            
            if (app.getApplicationID().equals(application.getApplicationID()) && !isProcessed) {
                request = req;
                break;
            }
        }
        
        if (request == null) {
            return false; // Request not found or already processed
        }
        
        // Update request
        request.put("isProcessed", true);
        request.put("isApproved", approve);
        request.put("processedDate", new Date());
        request.put("processedBy", processor);
        
        // If approved, update application status
        if (approve) {
            ApplicationStatus currentStatus = application.getStatus();
            
            // Reset the application status to allow new applications
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            
            // If the application was successful or booked, increment available units
            if (currentStatus == ApplicationStatus.SUCCESSFUL || currentStatus == ApplicationStatus.BOOKED) {
                // Get the project
                Project project = application.getProject();
                
                // If flat was booked, free it
                if (currentStatus == ApplicationStatus.BOOKED) {
                    Flat bookedFlat = application.getBookedFlat();
                    if (bookedFlat != null) {
                        FlatType flatType = bookedFlat.getType();
                        project.incrementAvailableUnits(flatType);
                        bookedFlat.setBookedByApplication(null);
                        application.setBookedFlat(null);
                        
                        // Also update the applicant
                        Applicant applicant = application.getApplicant();
                        applicant.setBookedFlat(null);
                    }
                } else if (currentStatus == ApplicationStatus.SUCCESSFUL) {
                    // For successful applications, we need to determine the flat type
                    // This would typically be stored in the application, but for simplicity
                    // we'll determine it based on the applicant's eligibility
                    FlatType flatType = determineEligibleFlatType(application);
                    if (flatType != null) {
                        project.incrementAvailableUnits(flatType);
                    }
                }
                
                // Update the application in the system
                ApplicationControl applicationControl = new ApplicationControl();
                applicationControl.updateApplication(application);
                
                // Update the project in the system
                ProjectControl projectControl = new ProjectControl();
                projectControl.updateProject(project);
            }
        }
        
        // Save to file
        return saveWithdrawalRequests();
    }
    
    /**
     * Determine eligible flat type for an application
     * @param application the application
     * @return the eligible flat type
     */
    private FlatType determineEligibleFlatType(Application application) {
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            if (project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        } else {
            // For married couples, check what's available
            // Preference: 3-Room > 2-Room
            if (project.hasFlatType(FlatType.THREE_ROOM)) {
                return FlatType.THREE_ROOM;
            } else if (project.hasFlatType(FlatType.TWO_ROOM)) {
                return FlatType.TWO_ROOM;
            }
        }
        
        return null; // No eligible flat type found
    }
    
    /**
     * Get pending withdrawal requests
     * @return list of pending withdrawal requests
     */
    public List<Map<String, Object>> getPendingWithdrawalRequests() {
        List<Map<String, Object>> pendingRequests = new ArrayList<>();
        
        for (Map<String, Object> request : withdrawalRequests) {
            Boolean isProcessed = (Boolean) request.get("isProcessed");
            
            if (!isProcessed) {
                pendingRequests.add(request);
            }
        }
        
        return pendingRequests;
    }
    
    /**
     * Check if an application has a pending withdrawal request
     * @param application the application
     * @return true if has pending request, false otherwise
     */
    public boolean hasPendingWithdrawalRequest(Application application) {
        for (Map<String, Object> request : withdrawalRequests) {
            Application app = (Application) request.get("application");
            Boolean isProcessed = (Boolean) request.get("isProcessed");
            
            if (app.getApplicationID().equals(application.getApplicationID()) && !isProcessed) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get withdrawal request status for an application
     * @param application the application
     * @return status map (null if no request found)
     */
    public Map<String, Object> getWithdrawalRequestStatus(Application application) {
        for (Map<String, Object> request : withdrawalRequests) {
            Application app = (Application) request.get("application");
            
            if (app.getApplicationID().equals(application.getApplicationID())) {
                return new HashMap<>(request); // Return a copy
            }
        }
        
        return null;
    }
    
    /**
     * Load withdrawal requests from file
     * @return list of withdrawal requests
     */
    private List<Map<String, Object>> loadWithdrawalRequests() {
        List<Map<String, Object>> loadedRequests = new ArrayList<>();
        
        try {
            File file = new File("files/resources/WithdrawalRequests.csv");
            
            // Create file if it doesn't exist
            if (!file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("ApplicationID,Reason,RequestDate,IsProcessed,IsApproved,ProcessedDate,ProcessedBy");
                }
                return loadedRequests;
            }
            
            try (Scanner fileScanner = new Scanner(file)) {
                // Skip header if exists
                if (fileScanner.hasNextLine()) {
                    fileScanner.nextLine();
                }
                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    try {
                        // Parse the line
                        String[] values = line.split(",", 7);
                        
                        if (values.length < 7) {
                            System.err.println("Invalid withdrawal record format: " + line);
                            continue;
                        }
                        
                        String applicationID = values[0].trim();
                        String reason = values[1].trim();
                        long requestDate = Long.parseLong(values[2].trim());
                        boolean isProcessed = Boolean.parseBoolean(values[3].trim());
                        boolean isApproved = Boolean.parseBoolean(values[4].trim());
                        
                        // Parse processed date and processor
                        Long processedDate = null;
                        String processedBy = null;
                        
                        if (!values[5].trim().equals("null")) {
                            processedDate = Long.parseLong(values[5].trim());
                        }
                        
                        if (!values[6].trim().equals("null")) {
                            processedBy = values[6].trim();
                        }
                        
                        // Find the application (placeholder for now)
                        ApplicationControl applicationControl = new ApplicationControl();
                        Application application = applicationControl.getApplicationByID(applicationID);
                        
                        if (application == null) {
                            System.err.println("Application not found for ID: " + applicationID);
                            continue;
                        }
                        
                        // Create withdrawal request
                        Map<String, Object> request = new HashMap<>();
                        request.put("application", application);
                        request.put("reason", reason);
                        request.put("requestDate", new Date(requestDate));
                        request.put("isProcessed", isProcessed);
                        request.put("isApproved", isApproved);
                        
                        if (processedDate != null) {
                            request.put("processedDate", new Date(processedDate));
                        } else {
                            request.put("processedDate", null);
                        }
                        
                        request.put("processedBy", processedBy);
                        
                        // Add to list
                        loadedRequests.add(request);
                        
                    } catch (Exception e) {
                        System.err.println("Error parsing withdrawal data: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading withdrawals file: " + e.getMessage());
        }
        
        return loadedRequests;
    }
    
    /**
     * Save withdrawal requests to file
     * @return true if successful, false otherwise
     */
    private boolean saveWithdrawalRequests() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter("files/resources/WithdrawalRequests.csv"))) {
                // Write header
                writer.println("ApplicationID,Reason,RequestDate,IsProcessed,IsApproved,ProcessedDate,ProcessedBy");
                
                // Write withdrawal requests
                for (Map<String, Object> request : withdrawalRequests) {
                    Application app = (Application) request.get("application");
                    String reason = (String) request.get("reason");
                    Date requestDate = (Date) request.get("requestDate");
                    Boolean isProcessed = (Boolean) request.get("isProcessed");
                    Boolean isApproved = (Boolean) request.get("isApproved");
                    Date processedDate = (Date) request.get("processedDate");
                    String processedBy = (String) request.get("processedBy");
                    
                    writer.print(
                        app.getApplicationID() + "," +
                        reason + "," +
                        requestDate.getTime() + "," +
                        isProcessed + "," +
                        isApproved + ","
                    );
                    
                    // Add processed date and processor
                    if (processedDate != null) {
                        writer.print(processedDate.getTime());
                    } else {
                        writer.print("null");
                    }
                    
                    writer.print(",");
                    
                    if (processedBy != null) {
                        writer.print(processedBy);
                    } else {
                        writer.print("null");
                    }
                    
                    writer.println();
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving withdrawal requests: " + e.getMessage());
            return false;
        }
    }
}