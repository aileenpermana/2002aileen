// Enhanced Report.java file
package entity;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a report generated in the BTO system.
 * Enhanced to support more comprehensive report generation features.
 */
public class Report {
    private String reportID;
    private List<Application> applications;
    private Date generationDate;
    private Map<String, Object> criteria;
    private String reportTitle;
    private String reportType; // "APPLICATION" or "BOOKING"
    private Project project;
    
    /**
     * Default constructor
     */
    public Report() {
        this.criteria = new HashMap<>();
        this.applications = new ArrayList<>();
        this.generationDate = new Date();
    }
    
    /**
     * Constructor with parameters
     * @param reportID unique identifier for the report
     * @param reportTitle title of the report
     * @param reportType type of report (APPLICATION or BOOKING)
     * @param project the project this report is for
     */
    public Report(String reportID, String reportTitle, String reportType, Project project) {
        this.reportID = reportID;
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.project = project;
        this.applications = new ArrayList<>();
        this.criteria = new HashMap<>();
        this.generationDate = new Date();
    }
    
    /**
     * Get the report ID
     * @return report ID
     */
    public String getReportID() {
        return reportID;
    }
    
    /**
     * Set the report ID
     * @param reportID the report ID
     */
    public void setReportID(String reportID) {
        this.reportID = reportID;
    }
    
    /**
     * Get the report title
     * @return report title
     */
    public String getReportTitle() {
        return reportTitle;
    }
    
    /**
     * Set the report title
     * @param reportTitle the report title
     */
    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }
    
    /**
     * Get the report type
     * @return report type
     */
    public String getReportType() {
        return reportType;
    }
    
    /**
     * Set the report type
     * @param reportType the report type
     */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    /**
     * Get the project
     * @return the project
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Set the project
     * @param project the project
     */
    public void setProject(Project project) {
        this.project = project;
    }
    
    /**
     * Get the list of applications
     * @return list of applications
     */
    public List<Application> getApplications() {
        return applications;
    }
    
    /**
     * Set the list of applications
     * @param applications the applications
     */
    public void setApplications(List<Application> applications) {
        this.applications = applications;
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
     * Get the filter criteria
     * @return map of filter criteria
     */
    public Map<String, Object> getCriteria() {
        return criteria;
    }
    
    /**
     * Set the filter criteria
     * @param criteria the filter criteria
     */
    public void setCriteria(Map<String, Object> criteria) {
        this.criteria = criteria;
    }
    
    /**
     * Add a criterion to the filter criteria
     * @param key the criterion key
     * @param value the criterion value
     */
    public void addCriterion(String key, Object value) {
        this.criteria.put(key, value);
    }
    
    /**
     * Filter applications by marital status
     * @param maritalStatus the marital status to filter by
     * @return filtered list of applications
     */
    public List<Application> filterByMaritalStatus(String maritalStatus) {
        if (maritalStatus == null || maritalStatus.isEmpty()) {
            return new ArrayList<>(applications);
        }
        
        MaritalStatus status = MaritalStatus.fromDisplayValue(maritalStatus);
        if (status == null) {
            return new ArrayList<>(applications);
        }
        
        List<Application> filtered = new ArrayList<>();
        for (Application app : applications) {
            if (app.getApplicant().getMaritalStatus() == status) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter applications by flat type
     * @param flatType the flat type to filter by
     * @return filtered list of applications
     */
    public List<Application> filterByFlatType(FlatType flatType) {
        if (flatType == null) {
            return new ArrayList<>(applications);
        }
        
        List<Application> filtered = new ArrayList<>();
        for (Application app : applications) {
            if (app.getBookedFlat() != null && app.getBookedFlat().getType() == flatType) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter applications by neighborhood
     * @param neighborhood the neighborhood to filter by
     * @return filtered list of applications
     */
    public List<Application> filterByNeighborhood(String neighborhood) {
        if (neighborhood == null || neighborhood.isEmpty()) {
            return new ArrayList<>(applications);
        }
        
        List<Application> filtered = new ArrayList<>();
        for (Application app : applications) {
            if (app.getProject().getNeighborhood().equalsIgnoreCase(neighborhood)) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter applications by age range
     * @param minAge minimum age
     * @param maxAge maximum age
     * @return filtered list of applications
     */
    public List<Application> filterByAgeRange(int minAge, int maxAge) {
        if (minAge <= 0 && maxAge <= 0) {
            return new ArrayList<>(applications);
        }
        
        List<Application> filtered = new ArrayList<>();
        for (Application app : applications) {
            int age = app.getApplicant().getAge();
            if ((minAge <= 0 || age >= minAge) && (maxAge <= 0 || age <= maxAge)) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter applications by application status
     * @param status the status to filter by
     * @return filtered list of applications
     */
    public List<Application> filterByStatus(ApplicationStatus status) {
        if (status == null) {
            return new ArrayList<>(applications);
        }
        
        List<Application> filtered = new ArrayList<>();
        for (Application app : applications) {
            if (app.getStatus() == status) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Apply all filters based on the criteria
     * @return filtered list of applications
     */
    public List<Application> applyAllFilters() {
        List<Application> filtered = new ArrayList<>(applications);
        
        // Apply marital status filter
        if (criteria.containsKey("maritalStatus")) {
            String maritalStatus = (String) criteria.get("maritalStatus");
            List<Application> maritalFiltered = filterByMaritalStatus(maritalStatus);
            filtered.retainAll(maritalFiltered);
        }
        
        // Apply flat type filter
        if (criteria.containsKey("flatType")) {
            String flatTypeStr = (String) criteria.get("flatType");
            FlatType flatType = FlatType.fromDisplayValue(flatTypeStr);
            if (flatType != null) {
                List<Application> flatTypeFiltered = filterByFlatType(flatType);
                filtered.retainAll(flatTypeFiltered);
            }
        }
        
        // Apply neighborhood filter
        if (criteria.containsKey("neighborhood")) {
            String neighborhood = (String) criteria.get("neighborhood");
            List<Application> neighborhoodFiltered = filterByNeighborhood(neighborhood);
            filtered.retainAll(neighborhoodFiltered);
        }
        
        // Apply age range filter
        int minAge = 0;
        int maxAge = 0;
        if (criteria.containsKey("minAge")) {
            minAge = (int) criteria.get("minAge");
        }
        if (criteria.containsKey("maxAge")) {
            maxAge = (int) criteria.get("maxAge");
        }
        if (minAge > 0 || maxAge > 0) {
            List<Application> ageFiltered = filterByAgeRange(minAge, maxAge);
            filtered.retainAll(ageFiltered);
        }
        
        // Apply status filter
        if (criteria.containsKey("status")) {
            String statusStr = (String) criteria.get("status");
            try {
                ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
                List<Application> statusFiltered = filterByStatus(status);
                filtered.retainAll(statusFiltered);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        return filtered;
    }
    
    /**
     * Generate a formatted report as a string
     * @return the formatted report
     */
    public String generateFormattedReport() {
        StringBuilder report = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        report.append("============================================================\n");
        report.append("                ").append(reportTitle).append("\n");
        report.append("============================================================\n");
        report.append("Report ID: ").append(reportID).append("\n");
        report.append("Project: ").append(project.getProjectName()).append("\n");
        report.append("Neighborhood: ").append(project.getNeighborhood()).append("\n");
        report.append("Generated on: ").append(dateFormat.format(generationDate)).append("\n\n");
        
        // Add filter criteria
        report.append("Filter Criteria:\n");
        if (criteria.isEmpty()) {
            report.append("- No filters applied\n");
        } else {
            for (Map.Entry<String, Object> entry : criteria.entrySet()) {
                report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        // Apply filters
        List<Application> filteredApps = applyAllFilters();
        
        // Add summary statistics
        report.append("\nSummary Statistics:\n");
        report.append("Total Applications: ").append(filteredApps.size()).append("\n\n");
        
        // Add status breakdown
        Map<ApplicationStatus, Integer> statusCounts = new HashMap<>();
        for (Application app : filteredApps) {
            ApplicationStatus status = app.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        
        report.append("Status Breakdown:\n");
        for (Map.Entry<ApplicationStatus, Integer> entry : statusCounts.entrySet()) {
            report.append("- ").append(entry.getKey().getDisplayValue())
                  .append(": ").append(entry.getValue()).append("\n");
        }
        
        // Add marital status breakdown
        Map<MaritalStatus, Integer> maritalCounts = new HashMap<>();
        for (Application app : filteredApps) {
            MaritalStatus status = app.getApplicant().getMaritalStatus();
            maritalCounts.put(status, maritalCounts.getOrDefault(status, 0) + 1);
        }
        
        report.append("\nMarital Status Breakdown:\n");
        for (Map.Entry<MaritalStatus, Integer> entry : maritalCounts.entrySet()) {
            report.append("- ").append(entry.getKey().getDisplayValue())
                  .append(": ").append(entry.getValue()).append("\n");
        }
        
        // Add flat type breakdown for booking reports
        if ("BOOKING".equals(reportType)) {
            Map<FlatType, Integer> flatTypeCounts = new HashMap<>();
            for (Application app : filteredApps) {
                if (app.getBookedFlat() != null) {
                    FlatType type = app.getBookedFlat().getType();
                    flatTypeCounts.put(type, flatTypeCounts.getOrDefault(type, 0) + 1);
                }
            }
            
            report.append("\nFlat Type Breakdown:\n");
            for (Map.Entry<FlatType, Integer> entry : flatTypeCounts.entrySet()) {
                report.append("- ").append(entry.getKey().getDisplayValue())
                      .append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        // Add detailed application list
        report.append("\nDetailed Application List:\n");
        report.append(String.format("%-5s %-20s %-10s %-15s %-15s %-15s\n", 
                                "No.", "Applicant Name", "Age", "Marital Status", "Status", 
                                "BOOKING".equals(reportType) ? "Flat Type" : "Application Date"));
        report.append("-------------------------------------------------------------------------------\n");
        
        for (int i = 0; i < filteredApps.size(); i++) {
            Application app = filteredApps.get(i);
            Applicant applicant = app.getApplicant();
            
            String lastColumn;
            if ("BOOKING".equals(reportType) && app.getBookedFlat() != null) {
                lastColumn = app.getBookedFlat().getType().getDisplayValue();
            } else {
                lastColumn = dateFormat.format(app.getApplicationDate());
            }
            
            report.append(String.format("%-5d %-20s %-10d %-15s %-15s %-15s\n", 
                                      (i + 1),
                                      truncateString(applicant.getName(), 20),
                                      applicant.getAge(),
                                      applicant.getMaritalStatusDisplayValue(),
                                      app.getStatus().getDisplayValue(),
                                      lastColumn));
        }
        
        report.append("\n============================================================\n");
        report.append("                End of Report                              \n");
        report.append("============================================================\n");
        
        return report.toString();
    }
    
    /**
     * Truncate a string to a maximum length
     * @param str the string to truncate
     * @param maxLength the maximum length
     * @return truncated string
     */
    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Print the report
     */
    public void printReport() {
        System.out.println(generateFormattedReport());
    }
}