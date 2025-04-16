package control;

import entity.*;
import java.util.*;

/**
 * Controls operations related to Report generation in the BTO system.
 */
public class ReportControl {
    
    /**
     * Generate an application report
     * @param project the project
     * @param applications the applications to include
     * @param filters the filters to apply
     * @return the generated report
     */
    public Report generateApplicationReport(Project project, List<Application> applications, Map<String, Object> filters) {
        // Apply filters to applications
        List<Application> filteredApplications = applyFilters(applications, filters);
        
        // Generate report ID
        String reportID = "RPT-APP-" + project.getProjectID() + "-" + System.currentTimeMillis() % 10000;
        
        // Create report
        Report report = new Report();
        report.setReportID(reportID);
        report.setApplications(filteredApplications);
        report.setGenerationDate(new Date());
        report.setCriteria(new HashMap<>(filters)); // Clone filters
        
        return report;
    }
    
    /**
     * Generate a booking report
     * @param project the project
     * @param applications the booked applications to include
     * @param filters the filters to apply
     * @return the generated report
     */
    public Report generateBookingReport(Project project, List<Application> applications, Map<String, Object> filters) {
        // Ensure only booked applications are included
        List<Application> bookedApplications = new ArrayList<>();
        for (Application app : applications) {
            if (app.getStatus() == ApplicationStatus.BOOKED && app.getBookedFlat() != null) {
                bookedApplications.add(app);
            }
        }
        
        // Apply filters to applications
        List<Application> filteredApplications = applyFilters(bookedApplications, filters);
        
        // Generate report ID
        String reportID = "RPT-BOOK-" + project.getProjectID() + "-" + System.currentTimeMillis() % 10000;
        
        // Create report
        Report report = new Report();
        report.setReportID(reportID);
        report.setApplications(filteredApplications);
        report.setGenerationDate(new Date());
        report.setCriteria(new HashMap<>(filters)); // Clone filters
        
        return report;
    }
    
    /**
     * Apply filters to applications
     * @param applications the applications to filter
     * @param filters the filters to apply
     * @return the filtered list of applications
     */
    private List<Application> applyFilters(List<Application> applications, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(applications); // No filters, return all
        }
        
        List<Application> filteredList = new ArrayList<>(applications);
        
        // Apply marital status filter
        if (filters.containsKey("maritalStatus")) {
            String maritalStatusStr = (String) filters.get("maritalStatus");
            MaritalStatus maritalStatus = null;
            
            // Try to convert to enum
            try {
                if (maritalStatusStr.equalsIgnoreCase("Single")) {
                    maritalStatus = MaritalStatus.SINGLE;
                } else if (maritalStatusStr.equalsIgnoreCase("Married")) {
                    maritalStatus = MaritalStatus.MARRIED;
                }
                
                if (maritalStatus != null) {
                    final MaritalStatus finalStatus = maritalStatus;
                    filteredList.removeIf(app -> app.getApplicant().getMaritalStatus() != finalStatus);
                }
            } catch (Exception e) {
                // Invalid value, ignore filter
            }
        }
        
        // Apply min age filter
        if (filters.containsKey("minAge")) {
            int minAge = (int) filters.get("minAge");
            filteredList.removeIf(app -> app.getApplicant().getAge() < minAge);
        }
        
        // Apply max age filter
        if (filters.containsKey("maxAge")) {
            int maxAge = (int) filters.get("maxAge");
            filteredList.removeIf(app -> app.getApplicant().getAge() > maxAge);
        }
        
        // Apply status filter
        if (filters.containsKey("status")) {
            String statusStr = (String) filters.get("status");
            ApplicationStatus status = null;
            
            // Try to convert to enum
            try {
                if (statusStr.equalsIgnoreCase("Pending")) {
                    status = ApplicationStatus.PENDING;
                } else if (statusStr.equalsIgnoreCase("Successful")) {
                    status = ApplicationStatus.SUCCESSFUL;
                } else if (statusStr.equalsIgnoreCase("Unsuccessful")) {
                    status = ApplicationStatus.UNSUCCESSFUL;
                } else if (statusStr.equalsIgnoreCase("Booked")) {
                    status = ApplicationStatus.BOOKED;
                }
                
                if (status != null) {
                    final ApplicationStatus finalStatus = status;
                    filteredList.removeIf(app -> app.getStatus() != finalStatus);
                }
            } catch (Exception e) {
                // Invalid value, ignore filter
            }
        }
        
        // Apply flat type filter (for booked applications)
        if (filters.containsKey("flatType")) {
            String flatTypeStr = (String) filters.get("flatType");
            FlatType flatType = null;
            
            // Try to convert to enum
            try {
                if (flatTypeStr.equalsIgnoreCase("2-Room")) {
                    flatType = FlatType.TWO_ROOM;
                } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
                    flatType = FlatType.THREE_ROOM;
                }
                
                if (flatType != null) {
                    final FlatType finalType = flatType;
                    filteredList.removeIf(app -> {
                        Flat bookedFlat = app.getBookedFlat();
                        return bookedFlat == null || bookedFlat.getType() != finalType;
                    });
                }
            } catch (Exception e) {
                // Invalid value, ignore filter
            }
        }
        
        return filteredList;
    }
    
    /**
     * Create a summary of applications by status
     * @param applications list of applications
     * @return map of status to count
     */
    public Map<ApplicationStatus, Integer> summarizeByStatus(List<Application> applications) {
        Map<ApplicationStatus, Integer> summary = new HashMap<>();
        
        for (Application app : applications) {
            ApplicationStatus status = app.getStatus();
            summary.put(status, summary.getOrDefault(status, 0) + 1);
        }
        
        return summary;
    }
    
    /**
     * Create a summary of applications by marital status
     * @param applications list of applications
     * @return map of marital status to count
     */
    public Map<MaritalStatus, Integer> summarizeByMaritalStatus(List<Application> applications) {
        Map<MaritalStatus, Integer> summary = new HashMap<>();
        
        for (Application app : applications) {
            MaritalStatus status = app.getApplicant().getMaritalStatus();
            summary.put(status, summary.getOrDefault(status, 0) + 1);
        }
        
        return summary;
    }
    
    /**
     * Create a summary of applications by flat type (for booked applications)
     * @param applications list of applications
     * @return map of flat type to count
     */
    public Map<FlatType, Integer> summarizeByFlatType(List<Application> applications) {
        Map<FlatType, Integer> summary = new HashMap<>();
        
        for (Application app : applications) {
            Flat bookedFlat = app.getBookedFlat();
            if (bookedFlat != null) {
                FlatType type = bookedFlat.getType();
                summary.put(type, summary.getOrDefault(type, 0) + 1);
            }
        }
        
        return summary;
    }
    
    /**
     * Create a summary of applications by age range
     * @param applications list of applications
     * @param ageRanges array of age ranges (e.g., [21, 30, 40, 50])
     * @return map of age range to count
     */
    public Map<String, Integer> summarizeByAgeRange(List<Application> applications, int[] ageRanges) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        
        // Initialize the ranges
        for (int i = 0; i < ageRanges.length; i++) {
            String rangeStr;
            if (i == 0) {
                rangeStr = "Under " + ageRanges[i];
            } else if (i == ageRanges.length - 1) {
                rangeStr = ageRanges[i] + " and above";
            } else {
                rangeStr = ageRanges[i-1] + "-" + (ageRanges[i] - 1);
            }
            summary.put(rangeStr, 0);
        }
        
        // Count applications by age range
        for (Application app : applications) {
            int age = app.getApplicant().getAge();
            String rangeStr = null;
            
            // Find the appropriate range
            for (int i = 0; i < ageRanges.length; i++) {
                if (i == 0 && age < ageRanges[i]) {
                    rangeStr = "Under " + ageRanges[i];
                    break;
                } else if (i == ageRanges.length - 1 && age >= ageRanges[i]) {
                    rangeStr = ageRanges[i] + " and above";
                    break;
                } else if (i > 0 && age >= ageRanges[i-1] && age < ageRanges[i]) {
                    rangeStr = ageRanges[i-1] + "-" + (ageRanges[i] - 1);
                    break;
                }
            }
            
            if (rangeStr != null) {
                summary.put(rangeStr, summary.getOrDefault(rangeStr, 0) + 1);
            }
        }
        
        return summary;
    }
}