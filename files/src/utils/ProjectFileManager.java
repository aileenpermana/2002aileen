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
                fw.write("ProjectID,Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
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
                fw.write("ProjectID,Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                         "Type 2,Number of units for Type 2,Selling price for Type 2," +
                         "Application opening date,Application closing date,Manager,Officer Slot,Officer\n");
                
                // Add a sample project if needed
                fw.write("SUN001,Sunrise Heights,Yishun,2-Room,100,350000,3-Room,150,450000,01/05/2024,01/08/2024,S1234567A,5,\n");
            } catch (IOException e) {
                System.out.println("Error creating project file: " + e.getMessage());
            }
        }
        
        // Read projects
        List<Project> projects = readAllProjects();
        System.out.println("Loaded " + projects.size() + " projects from file.");
    }

        /**
     * Find or create an officer by NRIC
     * @param nric the officer's NRIC
     * @return HDBOfficer object
     */
    private HDBOfficer findOrCreateOfficer(String nric) {
        try (Scanner scanner = new Scanner(new File("files/resources/OfficerList.csv"))) {
            // Skip header
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] fields = line.split(",");
                if (fields.length < 5) continue;
                
                String officerNRIC = fields[1].trim();
                if (officerNRIC.equalsIgnoreCase(nric)) {
                    // Create and return the real officer
                    return new HDBOfficer(
                        fields[0].trim(), // name
                        officerNRIC,
                        fields[4].trim(), // password
                        Integer.parseInt(fields[2].trim()), // age
                        fields[3].trim(), // marital status
                        "HDBOfficer"
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding officer: " + e.getMessage());
        }
        
        // If not found, create a placeholder
        return new HDBOfficer(
            "Officer", 
            nric, 
            "password", 
            30, // placeholder age
            "Single", // placeholder marital status
            "HDBOfficer"
        );
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
            
            if (fields.length < 13) {
                System.out.println("Invalid project record (not enough fields): " + line);
                return null;
            }
            
            // Extract project ID (first field)
            String projectID = fields[0].trim();
            
            // Extract basic project details
            String projectName = fields[1].trim();
            String neighborhood = fields[2].trim();
            
            // Parse flat types and units
            Map<FlatType, Integer> totalUnits = new HashMap<>();
            if (fields[3].equals("2-Room") && !fields[4].isEmpty()) {
                try {
                    totalUnits.put(FlatType.TWO_ROOM, Integer.parseInt(fields[4]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for 2-Room units: " + fields[4]);
                }
            }
            
            if (fields[6].equals("3-Room") && !fields[7].isEmpty()) {
                try {
                    totalUnits.put(FlatType.THREE_ROOM, Integer.parseInt(fields[7]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for 3-Room units: " + fields[7]);
                }
            }
            
            // Parse dates with error handling
            Date openDate;
            Date closeDate;
            try {
                openDate = DATE_FORMAT.parse(fields[9]);
                closeDate = DATE_FORMAT.parse(fields[10]);
            } catch (ParseException e) {
                System.out.println("Error parsing dates in project record: " + e.getMessage());
                return null;
            }
            
            // Find manager by NRIC
            String managerNRIC = fields[11].trim();
            HDBManager manager = findManagerByNRIC(managerNRIC);
            if (manager == null) {
                // If not found, create temporary placeholder
                manager = new HDBManager("Manager", managerNRIC, "password", 30, "MARRIED", "HDBManager");
            }
            
            // Parse officer slots with error handling
            int officerSlots;
            try {
                officerSlots = Integer.parseInt(fields[12]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for officer slots: " + fields[12]);
                officerSlots = 5; // Default value
            }

            // Parse officer NRICs (last column)

            
            // Use the provided project ID instead of generating a new one
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
            
            if (fields.length > 13 && !fields[13].trim().isEmpty()) {
                String[] officerNRICs = fields[13].trim().split(";");
                for (String nric : officerNRICs) {
                    HDBOfficer officer = findOrCreateOfficer(nric.trim());
                    project.addOfficer(officer);
                }
            }

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
            fw.write("ProjectID,Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
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
        
        // Project ID
        sb.append(project.getProjectID()).append(",");
        
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
        
        // Officers 
        List<String> officerNRICs = new ArrayList<>();
        for (HDBOfficer officer : project.getOfficers()) {
            officerNRICs.add(officer.getNRIC());
        }
        sb.append(String.join(";", officerNRICs));
        
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