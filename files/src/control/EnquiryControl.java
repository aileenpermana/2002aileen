package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to Enquiries in the BTO system.
 */
public class EnquiryControl {
    private static final String ENQUIRIES_FILE = "files/resources/EnquiryList.csv";
    private List<Enquiry> enquiries;
    
    /**
     * Constructor initializes the enquiries list from storage
     */
    public EnquiryControl() {
        this.enquiries = loadEnquiries();
    }
    
    /**
     * Get all enquiries in the system
     * @return list of all enquiries
     */
    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiries);
    }
    
    /**
     * Get enquiries for a project
     * @param project the project
     * @return list of enquiries for the project
     */
    public List<Enquiry> getEnquiriesForProject(Project project) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getProject().getProjectID().equals(project.getProjectID())) {
                projectEnquiries.add(enquiry);
            }
        }
        
        return projectEnquiries;
    }
    
    /**
     * Get enquiries submitted by an applicant
     * @param applicant the applicant
     * @return list of enquiries by the applicant
     */
    public List<Enquiry> getEnquiriesByApplicant(Applicant applicant) {
        List<Enquiry> applicantEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantEnquiries.add(enquiry);
            }
        }
        
        return applicantEnquiries;
    }
    
    /**
     * Submit a new enquiry
     * @param applicant the applicant
     * @param project the project
     * @param content the enquiry content
     * @return the created enquiry, or null if submission failed
     */
    public Enquiry submitEnquiry(Applicant applicant, Project project, String content) {
        // Create new enquiry
        Enquiry enquiry = new Enquiry();
        
        // Set basic properties
        enquiry.setEnquiryID(generateEnquiryID(applicant, project));
        enquiry.setApplicant(applicant);
        enquiry.setProject(project);
        enquiry.setContent(content);
        enquiry.setSubmissionDate(new Date());
        enquiry.setReplies(new ArrayList<>());
        
        // Add to list
        enquiries.add(enquiry);
        
        // Save to file
        if (saveEnquiries()) {
            return enquiry;
        }
        
        return null;
    }
    
    /**
     * Edit an existing enquiry
     * @param enquiry the enquiry to edit
     * @param newContent the new content
     * @return true if edit was successful, false otherwise
     */
    public boolean editEnquiry(Enquiry enquiry, String newContent) {
        // Find the enquiry
        Enquiry foundEnquiry = null;
        for (Enquiry e : enquiries) {
            if (e.getEnquiryID().equals(enquiry.getEnquiryID())) {
                foundEnquiry = e;
                break;
            }
        }
        
        if (foundEnquiry == null) {
            return false;
        }
        
        // Check if enquiry has replies (can't edit if it does)
        if (!foundEnquiry.getReplies().isEmpty()) {
            return false;
        }
        
        // Update content
        foundEnquiry.setContent(newContent);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Delete an enquiry
     * @param enquiry the enquiry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEnquiry(Enquiry enquiry) {
        // Find the enquiry
        Enquiry foundEnquiry = null;
        for (Enquiry e : enquiries) {
            if (e.getEnquiryID().equals(enquiry.getEnquiryID())) {
                foundEnquiry = e;
                break;
            }
        }
        
        if (foundEnquiry == null) {
            return false;
        }
        
        // Check if enquiry has replies (can't delete if it does)
        if (!foundEnquiry.getReplies().isEmpty()) {
            return false;
        }
        
        // Remove from list
        enquiries.remove(foundEnquiry);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Add a reply to an enquiry
     * @param enquiry the enquiry
     * @param reply the reply content
     * @param responder the user responding
     * @return true if reply was added successfully, false otherwise
     */
    public boolean addReply(Enquiry enquiry, String reply, User responder) {
        // Find the enquiry
        Enquiry foundEnquiry = null;
        for (Enquiry e : enquiries) {
            if (e.getEnquiryID().equals(enquiry.getEnquiryID())) {
                foundEnquiry = e;
                break;
            }
        }
        
        if (foundEnquiry == null) {
            return false;
        }
        
        // Add reply
        if (foundEnquiry.getReplies() == null) {
            foundEnquiry.setReplies(new ArrayList<>());
        }
        
        foundEnquiry.getReplies().add(reply);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Generate an enquiry ID
     * @param applicant the applicant
     * @param project the project
     * @return a unique enquiry ID
     */
    private String generateEnquiryID(Applicant applicant, Project project) {
        // Format: ENQ-{NRIC suffix}-{ProjectID prefix}-{timestamp}
        return "ENQ-" + applicant.getNRIC().substring(1, 5) + "-" + 
               project.getProjectID().substring(0, 3) + "-" + 
               System.currentTimeMillis() % 10000;
    }
    
    /**
     * Load enquiries from file
     * @return list of enquiries
     */
    private List<Enquiry> loadEnquiries() {
        List<Enquiry> loadedEnquiries = new ArrayList<>();
        
        try {
            File enquiriesFile = new File(ENQUIRIES_FILE);
            
            // If file doesn't exist, create it with header
            if (!enquiriesFile.exists()) {
                File parentDir = enquiriesFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(enquiriesFile))) {
                    writer.println("EnquiryID,ApplicantNRIC,ProjectID,SubmissionDate,Content,Replies");
                }
                return loadedEnquiries;
            }
            
            try (Scanner fileScanner = new Scanner(enquiriesFile)) {
                // Skip header if exists
                if (fileScanner.hasNextLine()) {
                    fileScanner.nextLine();
                }
                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    // Split by commas, but keep content and replies intact
                    String[] parts = splitCSVLine(line);
                    if (parts.length < 5) continue; // Invalid line
                    
                    try {
                        String enquiryID = parts[0].trim();
                        String applicantNRIC = parts[1].trim();
                        String projectID = parts[2].trim();
                        long submissionDate = Long.parseLong(parts[3].trim());
                        String content = parts[4].trim();
                        
                        // Parse replies (if any)
                        List<String> replies = new ArrayList<>();
                        if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                            String[] repliesArr = parts[5].split("\\|");
                            for (String reply : repliesArr) {
                                replies.add(reply.trim());
                            }
                        }
                        
                        // Find or create applicant
                        Applicant applicant = findOrCreateApplicant(applicantNRIC);
                        
                        // Find or create project
                        Project project = findOrCreateProject(projectID);
                        
                        // Create enquiry
                        Enquiry enquiry = new Enquiry();
                        enquiry.setEnquiryID(enquiryID);
                        enquiry.setApplicant(applicant);
                        enquiry.setProject(project);
                        enquiry.setContent(content);
                        enquiry.setSubmissionDate(new Date(submissionDate));
                        enquiry.setReplies(replies);
                        
                        // Add to list
                        loadedEnquiries.add(enquiry);
                        
                    } catch (Exception e) {
                        System.err.println("Error parsing enquiry data: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading enquiries: " + e.getMessage());
        }
        
        return loadedEnquiries;
    }
    
    /**
     * Split a CSV line while preserving commas in content and replies fields
     * @param line the CSV line
     * @return array of fields
     */
    private String[] splitCSVLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == ',' && !inQuotes) {
                parts.add(currentPart.toString());
                currentPart = new StringBuilder();
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else {
                currentPart.append(c);
            }
        }
        
        // Add the last part
        parts.add(currentPart.toString());
        
        return parts.toArray(new String[0]);
    }
    
    /**
     * Save enquiries to file
     * @return true if successful, false otherwise
     */
    private boolean saveEnquiries() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(ENQUIRIES_FILE))) {
                // Write header
                writer.println("EnquiryID,ApplicantNRIC,ProjectID,SubmissionDate,Content,Replies");
                
                // Write enquiries
                for (Enquiry enquiry : enquiries) {
                    // Format content with quotes to handle commas
                    String formattedContent = "\"" + enquiry.getContent().replace("\"", "\"\"") + "\"";
                    
                    writer.print(
                        enquiry.getEnquiryID() + "," +
                        enquiry.getApplicant().getNRIC() + "," +
                        enquiry.getProject().getProjectID() + "," +
                        enquiry.getSubmissionDate().getTime() + "," +
                        formattedContent
                    );
                    
                    // Add replies if any
                    List<String> replies = enquiry.getReplies();
                    if (replies != null && !replies.isEmpty()) {
                        writer.print(",\"");
                        for (int i = 0; i < replies.size(); i++) {
                            if (i > 0) writer.print("|");
                            writer.print(replies.get(i).replace("\"", "\"\""));
                        }
                        writer.print("\"");
                    }
                    
                    writer.println();
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving enquiries: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to find or create an applicant by NRIC
     * @param nric the applicant's NRIC
     * @return the applicant object
     */
    private Applicant findOrCreateApplicant(String nric) {
        // In a real system, this would check a database or repository
        // For now, create a placeholder applicant
        return new Applicant(
            "Applicant", // Placeholder name
            nric,
            "password",
            30, // Placeholder age
            "Married", // Placeholder marital status
            "Applicant"
        );
    }
    
    /**
     * Helper method to find or create a project by ID
     * @param projectID the project ID
     * @return the project object
     */
    private Project findOrCreateProject(String projectID) {
        // Get the project from project control if possible
        ProjectControl projectControl = new ProjectControl();
        List<Project> allProjects = projectControl.getAllProjects();
        
        for (Project p : allProjects) {
            if (p.getProjectID().equals(projectID)) {
                return p;
            }
        }
        
        // If not found, create a placeholder
        return new Project(
            projectID,
            "Project", // Placeholder name
            "Neighborhood", // Placeholder neighborhood
            new HashMap<>(), // Placeholder units
            new Date(), // Placeholder open date
            new Date(), // Placeholder close date
            null, // Placeholder manager
            5 // Placeholder officer slots
        );
    }
    
    /**
     * Get an enquiry by ID
     * @param enquiryID the enquiry ID
     * @return the enquiry, or null if not found
     */
    public Enquiry getEnquiryByID(String enquiryID) {
        for (Enquiry e : enquiries) {
            if (e.getEnquiryID().equals(enquiryID)) {
                return e;
            }
        }
        return null;
    }
    
    /**
     * Count the number of enquiries for a project
     * @param project the project
     * @return the count of enquiries
     */
    public int countEnquiriesForProject(Project project) {
        int count = 0;
        for (Enquiry e : enquiries) {
            if (e.getProject().getProjectID().equals(project.getProjectID())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Count the number of unanswered enquiries for a project
     * @param project the project
     * @return the count of unanswered enquiries
     */
    public int countUnansweredEnquiriesForProject(Project project) {
        int count = 0;
        for (Enquiry e : enquiries) {
            if (e.getProject().getProjectID().equals(project.getProjectID()) &&
                (e.getReplies() == null || e.getReplies().isEmpty())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get unanswered enquiries for a project
     * @param project the project
     * @return list of unanswered enquiries
     */
    public List<Enquiry> getUnansweredEnquiriesForProject(Project project) {
        List<Enquiry> unanswered = new ArrayList<>();
        for (Enquiry e : enquiries) {
            if (e.getProject().getProjectID().equals(project.getProjectID()) &&
                (e.getReplies() == null || e.getReplies().isEmpty())) {
                unanswered.add(e);
            }
        }
        return unanswered;
    }
}