package utils;

import entity.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for managing Project data in CSV files
 * Demonstrates the use of the Singleton pattern for file operations
 */
public class ProjectFileManager {
    private static ProjectFileManager instance;
    private static final String FILE_PATH = "files/resources/ProjectList.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    // Private constructor - Singleton pattern
    private ProjectFileManager() {
        // Make sure the directory exists
        File resourceDir = new File("files/resources");
        if (!resourceDir.exists()) {
            resourceDir.mkdirs();
        }
        
        // Check if the file exists
        File projectFile = new File(FILE_PATH);
        if (!projectFile.exists()) {
            try {
                projectFile.createNewFile();
                // Write header
                FileWriter fw = new FileWriter(projectFile);
                fw.write("Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                         "Type 2,Number of units for Type 2,Selling price for Type 2," +
                         "Application opening date,Application closing date,Manager,Officer Slot,Officer\n");
                fw.close();
            } catch (IOException e) {
                System.out.println("Error creating project file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Load initial project data from CSV file
     * Should be called when the application starts
     */
    public void loadInitialProjectData() {
        System.out.println("Loading initial project data from: " + FILE_PATH);
        
        // Check if file exists
        File projectFile = new File(FILE_PATH);
        if (!projectFile.exists()) {
            System.out.println("Project file not found. Creating new file at: " + FILE_PATH);
            // Create parent directories if they don't exist
            File parentDir = projectFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Create the file with header
            try (FileWriter fw = new FileWriter(projectFile)) {
                fw.write("Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                         "Type 2,Number of units for Type 2,Selling price for Type 2," +
                         "Application opening date,Application closing date,Manager,Officer Slot,Officer\n");
                
                // Add a sample project if needed
                fw.write("Sunrise Heights,Yishun,2-Room,100,350000,3-Room,150,450000,01/05/2024,01/08/2024,S1234567A,5,\n");
            } catch (IOException e) {
                System.out.println("Error creating project file: " + e.getMessage());
            }
        }
        
        // Read projects
        List<Project> projects = readAllProjects();
        System.out.println("Loaded " + projects.size() + " projects from file.");
    }
    
    /**
     * Get the singleton instance
     * @return the ProjectFileManager instance
     */
    public static ProjectFileManager getInstance() {
        if (instance == null) {
            instance = new ProjectFileManager();
        }
        return instance;
    }
    
    /**
     * Read all projects from CSV file
     * @return list of projects
     */
    public List<Project> readAllProjects() {
        List<Project> projects = new ArrayList<>();
        File projectFile = new File(FILE_PATH);
        
        if (!projectFile.exists()) {
            System.out.println("Project file not found at: " + FILE_PATH);
            return projects;
        }
        
        // Check if file is empty
        if (projectFile.length() == 0) {
            System.out.println("Project file is empty: " + FILE_PATH);
            return projects;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(projectFile))) {
            String line;
            boolean isHeader = true;
            
            // Read and process each line
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    Project project = parseProjectFromCSV(line);
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading project file: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Parse a project from a CSV line
     * @param line the CSV line
     * @return the parsed Project object
     */
    private Project parseProjectFromCSV(String line) {
        try {
            String[] fields = line.split(",");
            
            if (fields.length < 12) {
                System.out.println("Invalid project record (not enough fields): " + line);
                return null;
            }
            
            // Extract basic project details
            String projectName = fields[0];
            String neighborhood = fields[1];
            
            // Parse flat types and units
            Map<FlatType, Integer> totalUnits = new HashMap<>();
            if (fields[2].equals("2-Room") && !fields[3].isEmpty()) {
                try {
                    totalUnits.put(FlatType.TWO_ROOM, Integer.parseInt(fields[3]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for 2-Room units: " + fields[3]);
                }
            }
            
            if (fields[5].equals("3-Room") && !fields[6].isEmpty()) {
                try {
                    totalUnits.put(FlatType.THREE_ROOM, Integer.parseInt(fields[6]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for 3-Room units: " + fields[6]);
                }
            }
            
            // Parse dates with error handling
            Date openDate;
            Date closeDate;
            try {
                openDate = DATE_FORMAT.parse(fields[8]);
                closeDate = DATE_FORMAT.parse(fields[9]);
            } catch (ParseException e) {
                System.out.println("Error parsing dates in project record: " + e.getMessage());
                return null;
            }
            
            // Create a temporary manager object - will be replaced later
            String managerNRIC = fields[10].trim();
        
        // CHANGE THIS PART: Find actual manager from ManagerList.csv instead of creating a placeholder
            HDBManager manager = findManagerByNRIC(managerNRIC);
            if (manager == null) {
                // If not found, create temporary placeholder (keep as fallback)
                manager = new HDBManager("Manager", managerNRIC, "password", 30, "MARRIED", "HDBManager");
            }
            // Parse officer slots with error handling
            int officerSlots;
            try {
                officerSlots = Integer.parseInt(fields[11]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for officer slots: " + fields[11]);
                officerSlots = 5; // Default value
            }
            
            String projectID = generateProjectID(projectName);
            Project project = new Project(
                projectID,
                projectName,
                neighborhood,
                totalUnits,
                openDate,
                closeDate,
                manager,
                officerSlots
            );
            
            // Set project visibility (default true)
            project.setVisible(true);
            
            return project;
            
        } catch (Exception e) {
            System.out.println("Error parsing project record: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private HDBManager findManagerByNRIC(String nric) {
        try (Scanner scanner = new Scanner(new File("files/resources/ManagerList.csv"))) {
            // Skip header
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] fields = line.split(",");
                if (fields.length < 5) continue;
                
                String managerNRIC = fields[1].trim();
                if (managerNRIC.equalsIgnoreCase(nric)) {
                    // Create and return the real manager
                    return new HDBManager(
                        fields[0].trim(), // name
                        managerNRIC,
                        fields[4].trim(), // password
                        Integer.parseInt(fields[2].trim()), // age
                        fields[3].trim(), // marital status
                        "HDBManager"
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding manager: " + e.getMessage());
        }
        return null;
    }
    /**
     * Generate a simple project ID based on the project name
     * @param projectName the project name
     * @return a unique project ID
     */
    private String generateProjectID(String projectName) {
        // Simple algorithm to generate a project ID
        String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        return prefix + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Save a list of projects to the CSV file
     * @param projects the list of projects to save
     * @return true if save was successful, false otherwise
     */
    public boolean saveAllProjects(List<Project> projects) {
        // Ensure directory exists
        File resourceDir = new File("files/resources");
        if (!resourceDir.exists()) {
            resourceDir.mkdirs();
        }
        
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            // Write header
            fw.write("Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                     "Type 2,Number of units for Type 2,Selling price for Type 2," +
                     "Application opening date,Application closing date,Manager,Officer Slot,Officer\n");
            
            // Write projects
            for (Project project : projects) {
                fw.write(formatProjectForCSV(project) + "\n");
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving projects: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Format a project as a CSV line
     * @param project the project to format
     * @return formatted CSV line
     */
    private String formatProjectForCSV(Project project) {
        StringBuilder sb = new StringBuilder();
        
        // Project Name
        sb.append(project.getProjectName()).append(",");
        
        // Neighborhood
        sb.append(project.getNeighborhood()).append(",");
        
        // Flat Types
        // Type 1 (2-Room)
        sb.append("2-Room").append(",");
        sb.append(project.getTotalUnitsByType(FlatType.TWO_ROOM)).append(",");
        sb.append("0").append(","); // Placeholder for selling price
        
        // Type 2 (3-Room)
        sb.append("3-Room").append(",");
        sb.append(project.getTotalUnitsByType(FlatType.THREE_ROOM)).append(",");
        sb.append("0").append(","); // Placeholder for selling price
        
        // Dates
        sb.append(DATE_FORMAT.format(project.getApplicationOpenDate())).append(",");
        sb.append(DATE_FORMAT.format(project.getApplicationCloseDate())).append(",");
        
        // Manager
        sb.append(project.getManagerInCharge().getNRIC()).append(",");
        
        // Officer Slots
        sb.append(project.getAvailableOfficerSlots()).append(",");
        
        // Officers - placeholder for now
        sb.append("");
        
        return sb.toString();
    }
    
    /**
     * Add a new project to the CSV file
     * @param project the project to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addProject(Project project) {
        List<Project> projects = readAllProjects();
        projects.add(project);
        return saveAllProjects(projects);
    }
    
    /**
     * Update an existing project in the CSV file
     * @param updatedProject the updated project
     * @return true if update was successful, false otherwise
     */
    public boolean updateProject(Project updatedProject) {
        List<Project> projects = readAllProjects();
        
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectID().equals(updatedProject.getProjectID())) {
                projects.set(i, updatedProject);
                return saveAllProjects(projects);
            }
        }
        
        // Project not found
        return false;
    }
    
    /**
     * Delete a project from the CSV file
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        List<Project> projects = readAllProjects();
        
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectID().equals(project.getProjectID())) {
                projects.remove(i);
                return saveAllProjects(projects);
            }
        }
        
        // Project not found
        return false;
    }
}