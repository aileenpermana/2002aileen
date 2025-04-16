package entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a receipt for a flat booking in the BTO system.
 */
public class Receipt {
    private String receiptID;
    private Application application;
    private Date generationDate;
    private HDBOfficer generatedBy;
    
    /**
     * Default constructor
     */
    public Receipt() {
        this.generationDate = new Date();
    }
    
    /**
     * Constructor with parameters
     * @param receiptID unique identifier for the receipt
     * @param application the application for which the receipt is generated
     * @param generatedBy the HDB Officer who generated the receipt
     */
    public Receipt(String receiptID, Application application, HDBOfficer generatedBy) {
        this.receiptID = receiptID;
        this.application = application;
        this.generationDate = new Date();
        this.generatedBy = generatedBy;
    }
    
    /**
     * Get the receipt ID
     * @return receipt ID
     */
    public String getReceiptID() {
        return receiptID;
    }
    
    /**
     * Set the receipt ID
     * @param receiptID the receipt ID
     */
    public void setReceiptID(String receiptID) {
        this.receiptID = receiptID;
    }
    
    /**
     * Get the application
     * @return the application
     */
    public Application getApplication() {
        return application;
    }
    
    /**
     * Set the application
     * @param application the application
     */
    public void setApplication(Application application) {
        this.application = application;
    }
    
    /**
     * Get the generation date
     * @return generation date
     */
    public Date getGenerationDate() {
        return generationDate;
    }
    
    /**
     * Set the generation date
     * @param generationDate the generation date
     */
    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }
    
    /**
     * Get the HDB Officer who generated the receipt
     * @return the HDB Officer
     */
    public HDBOfficer getGeneratedBy() {
        return generatedBy;
    }
    
    /**
     * Set the HDB Officer who generated the receipt
     * @param generatedBy the HDB Officer
     */
    public void setGeneratedBy(HDBOfficer generatedBy) {
        this.generatedBy = generatedBy;
    }
    
    /**
     * Generate a formatted receipt as a string
     * @return the formatted receipt
     */
    public String generateFormattedReceipt() {
        if (application == null || application.getBookedFlat() == null) {
            return "Cannot generate receipt: Invalid application or no flat booked.";
        }
        
        StringBuilder receipt = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        Flat flat = application.getBookedFlat();
        
        receipt.append("====================================================\n");
        receipt.append("                BOOKING RECEIPT                     \n");
        receipt.append("====================================================\n");
        receipt.append("Receipt ID: ").append(receiptID).append("\n");
        receipt.append("Date: ").append(dateFormat.format(generationDate)).append("\n\n");
        
        receipt.append("APPLICANT DETAILS:\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("NRIC: ").append(applicant.getNRIC()).append("\n");
        receipt.append("Age: ").append(applicant.getAge()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatusDisplayValue()).append("\n\n");
        
        receipt.append("PROJECT DETAILS:\n");
        receipt.append("Project Name: ").append(project.getProjectName()).append("\n");
        receipt.append("Neighborhood: ").append(project.getNeighborhood()).append("\n\n");
        
        receipt.append("FLAT DETAILS:\n");
        receipt.append("Flat ID: ").append(flat.getFlatID()).append("\n");
        receipt.append("Flat Type: ").append(flat.getType().getDisplayValue()).append("\n\n");
        
        receipt.append("APPLICATION DETAILS:\n");
        receipt.append("Application ID: ").append(application.getApplicationID()).append("\n");
        receipt.append("Application Date: ").append(dateFormat.format(application.getApplicationDate())).append("\n");
        receipt.append("Status: ").append(application.getStatus().getDisplayValue()).append("\n\n");
        
        receipt.append("OFFICER DETAILS:\n");
        receipt.append("Officer Name: ").append(generatedBy.getName()).append("\n");
        receipt.append("Officer ID: ").append(generatedBy.getOfficerID()).append("\n\n");
        
        receipt.append("This receipt confirms the booking of the flat.\n");
        receipt.append("Please keep this receipt for your records.\n");
        receipt.append("====================================================\n");
        
        return receipt.toString();
    }
    
    /**
     * Print the receipt
     */
    public void printReceipt() {
        System.out.println(generateFormattedReceipt());
    }
    
    /**
     * Save the receipt to a file
     * @param filePath the file path to save to
     * @return true if successful, false otherwise
     */
    public boolean saveReceiptToFile(String filePath) {
        try {
            java.io.File directory = new java.io.File(filePath).getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
                writer.print(generateFormattedReceipt());
            }
            
            return true;
        } catch (java.io.IOException e) {
            System.err.println("Error saving receipt to file: " + e.getMessage());
            return false;
        }
    }
}