package control;

import entity.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controls operations related to Enquiries in the BTO system.
 * Enhanced to provide better enquiry management features.
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
     * Get all enquiries
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
        // Validate inputs
        if (applicant == null || project == null || content == null || content.trim().isEmpty()) {
            return null;
        }
        
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
        // Validate inputs
        if (enquiry == null || newContent == null || newContent.trim().isEmpty()) {
            return false;
        }
        
        // Check if enquiry exists
        int index = -1;
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).getEnquiryID().equals(enquiry.getEnquiryID())) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            return false;
        }
        
        // Get the enquiry from the list
        Enquiry existingEnquiry = enquiries.get(index);
        
        // Check if enquiry has replies (can't edit if it does)
        if (existingEnquiry.getReplies() != null && !existingEnquiry.getReplies().isEmpty()) {
            return false;
        }
        
        // Update content
        existingEnquiry.setContent(newContent);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Delete an enquiry
     * @param enquiry the enquiry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEnquiry(Enquiry enquiry) {
        // Validate input
        if (enquiry == null) {
            return false;
        }
        
        // Check if enquiry exists
        int index = -1;
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).getEnquiryID().equals(enquiry.getEnquiryID())) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            return false;
        }
        
        // Get the enquiry from the list
        Enquiry existingEnquiry = enquiries.get(index);
        
        // Check if enquiry has replies (can't delete if it does)
        if (existingEnquiry.getReplies() != null && !existingEnquiry.getReplies().isEmpty()) {
            return false;
        }
        
        // Remove from list
        enquiries.remove(index);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Get enquiry by ID
     * @param enquiryID the enquiry ID
     * @return the enquiry, or null if not found
     */
    public Enquiry getEnquiryByID(String enquiryID) {
        // Validate input
        if (enquiryID == null || enquiryID.trim().isEmpty()) {
            return null;
        }
        
        // Find enquiry
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getEnquiryID().equals(enquiryID)) {
                return enquiry;
            }
        }
        
        return null;
    }
    
    /**
     * Add a reply to an enquiry
     * @param enquiry the enquiry
     * @param reply the reply content
     * @param responder the user responding
     * @return true if reply was added successfully, false otherwise
     */
    public boolean addReply(Enquiry enquiry, String reply, User responder) {
        // Validate inputs
        if (enquiry == null || reply == null || reply.trim().isEmpty() || responder == null) {
            return false;
        }
        
        // Check if enquiry exists
        int index = -1;
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).getEnquiryID().equals(enquiry.getEnquiryID())) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            return false;
        }
        
        // Get the enquiry from the list
        Enquiry existingEnquiry = enquiries.get(index);
        
        // Format reply with responder info and timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String formattedReply = "[" + dateFormat.format(new Date()) + "] " + 
                               responder.getName() + " (" + responder.getRole() + "): " + reply;
        
        // Initialize replies list if null
        if (existingEnquiry.getReplies() == null) {
            existingEnquiry.setReplies(new ArrayList<>());
        }
        
        // Add reply
        existingEnquiry.getReplies().add(formattedReply);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Check if an enquiry has been replied to
     * @param enquiry the enquiry to check
     * @return true if replied, false otherwise
     */
    public boolean hasBeenReplied(Enquiry enquiry) {
        // Validate input
        if (enquiry == null) {
            return false;
        }
        
        // Check if enquiry exists
        for (Enquiry e : enquiries) {
            if (e.getEnquiryID().equals(enquiry.getEnquiryID())) {
                return e.getReplies() != null && !e.getReplies().isEmpty();
            }
        }
        
        return false;
    }
    
    /**
     * Get pending (unreplied) enquiries for a project
     * @param project the project
     * @return list of pending enquiries
     */
    public List<Enquiry> getPendingEnquiriesForProject(Project project) {
        // Validate input
        if (project == null) {
            return new ArrayList<>();
        }
        
        // Get project enquiries
        List<Enquiry> projectEnquiries = getEnquiriesForProject(project);
        List<Enquiry> pendingEnquiries = new ArrayList<>();
        
        // Filter out replied enquiries
        for (Enquiry enquiry : projectEnquiries) {
            if (enquiry.getReplies() == null || enquiry.getReplies().isEmpty()) {
                pendingEnquiries.add(enquiry);
            }
        }
        
        return pendingEnquiries;
    }
    
    /**
     * Generate an enquiry ID
     * @param applicant the applicant
     * @param project the project
     * @return a unique enquiry ID
     */
    private String generateEnquiryID(Applicant applicant, Project project) {
        // Simple algorithm to generate an enquiry ID
        // Format: ENQ-{ApplicantNRIC(4chars)}-{ProjectID(3chars)}-{Timestamp(last 4 digits)}
        String nricPart = applicant.getNRIC().substring(1, 5);
        String projectPart = project.getProjectID().substring(0, Math.min(3, project.getProjectID().length()));
        String timestampPart = String.valueOf(System.currentTimeMillis() % 10000);
        
        return "ENQ-" + nricPart + "-" + projectPart + "-" + timestampPart;
    }
    
    /**
     * Load enquiries from file
     * @return list of enquiries
     */
    private List<Enquiry> loadEnquiries() {
        List<Enquiry> loadedEnquiries = new ArrayList<>();
        ProjectControl projectControl = new ProjectControl();
        
        try {
            File file = new File(ENQUIRIES_FILE);
            
            // Create file if it doesn't exist
            if (!file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("EnquiryID,ApplicantNRIC,ProjectID,SubmissionDate,Content,Replies");
                }
                return loadedEnquiries;
            }
            
            try (Scanner fileScanner = new Scanner(file)) {
                // Skip header if exists
                if (fileScanner.hasNextLine()) {
                    fileScanner.nextLine();
                }
                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    
                    // Use a more robust parsing approach to handle commas in content
                    int firstComma = line.indexOf(',');
                    int secondComma = line.indexOf(',', firstComma + 1);
                    int thirdComma = line.indexOf(',', secondComma + 1);
                    int fourthComma = line.indexOf(',', thirdComma + 1);
                    
                    if (firstComma == -1 || secondComma == -1 || thirdComma == -1 || fourthComma == -1) {
                        System.err.println("Invalid enquiry record format: " + line);
                        continue;
                    }
                    
                    try {
                        String enquiryID = line.substring(0, firstComma).trim();
                        String applicantNRIC = line.substring(firstComma + 1, secondComma).trim();
                        String projectID = line.substring(secondComma + 1, thirdComma).trim();
                        String dateStr = line.substring(thirdComma + 1, fourthComma).trim();
                        
                        // Extract content - any remaining text until last comma or end of line
                        String remainingText = line.substring(fourthComma + 1);
                        String content;
                        List<String> replies = new ArrayList<>();
                        
                        int lastComma = remainingText.lastIndexOf(',');
                        if (lastComma != -1) {
                            // Has replies
                            content = remainingText.substring(0, lastComma).trim();
                            String repliesStr = remainingText.substring(lastComma + 1).trim();
                            
                            if (!repliesStr.isEmpty()) {
                                String[] repliesArr = repliesStr.split("\\|");
                                for (String reply : repliesArr) {
                                    replies.add(reply.trim());
                                }
                            }
                        } else {
                            // No replies
                            content = remainingText.trim();
                        }
                        
                        // Parse date
                        long submissionDate = Long.parseLong(dateStr);
                        
                        // Find projects - using ProjectControl
                        List<Project> allProjects = projectControl.getAllProjects();
                        Project matchingProject = null;
                        
                        for (Project project : allProjects) {
                            if (project.getProjectID().equals(projectID)) {
                                matchingProject = project;
                                break;
                            }
                        }
                        
                        if (matchingProject == null) {
                            System.err.println("Project not found for ID: " + projectID);
                            continue;
                        }
                        
                        // Create applicant placeholder - in a real system this would come from a repository
                        Applicant applicant = new Applicant(
                            "Applicant " + applicantNRIC, 
                            applicantNRIC,
                            "password",
                            30, // Placeholder age
                            "Single", // Placeholder marital status
                            "Applicant"
                        );
                        
                        // Create enquiry
                        Enquiry enquiry = new Enquiry();
                        enquiry.setEnquiryID(enquiryID);
                        enquiry.setApplicant(applicant);
                        enquiry.setProject(matchingProject);
                        enquiry.setContent(content);
                        enquiry.setSubmissionDate(new Date(submissionDate));
                        enquiry.setReplies(replies);
                        
                        // Add to list
                        loadedEnquiries.add(enquiry);
                        
                    } catch (Exception e) {
                        System.err.println("Error parsing enquiry data: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Enquiries file not found. Starting with empty list.");
        } catch (IOException e) {
            System.err.println("Error reading enquiries file: " + e.getMessage());
        }
        
        return loadedEnquiries;
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
                    writer.print(
                        enquiry.getEnquiryID() + "," +
                        enquiry.getApplicant().getNRIC() + "," +
                        enquiry.getProject().getProjectID() + "," +
                        enquiry.getSubmissionDate().getTime() + "," +
                        enquiry.getContent()
                    );
                    
                    // Add replies if any
                    if (enquiry.getReplies() != null && !enquiry.getReplies().isEmpty()) {
                        writer.print(",");
                        for (int i = 0; i < enquiry.getReplies().size(); i++) {
                            if (i > 0) writer.print("|");
                            writer.print(enquiry.getReplies().get(i));
                        }
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
}