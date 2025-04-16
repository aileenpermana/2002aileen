package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents criteria for filtering applications in a report.
 */
public class ReportCriteria {
    private String maritalStatus;
    private FlatType flatType;
    private String neighborhood;
    private int minAge;
    private int maxAge;
    private ApplicationStatus status;
    
    /**
     * Default constructor
     */
    public ReportCriteria() {
        this.minAge = 0;
        this.maxAge = 0;
    }
    
    /**
     * Constructor with parameters
     * @param maritalStatus marital status filter
     * @param flatType flat type filter
     * @param neighborhood neighborhood filter
     * @param minAge minimum age filter
     * @param maxAge maximum age filter
     * @param status application status filter
     */
    public ReportCriteria(String maritalStatus, FlatType flatType, String neighborhood, 
                          int minAge, int maxAge, ApplicationStatus status) {
        this.maritalStatus = maritalStatus;
        this.flatType = flatType;
        this.neighborhood = neighborhood;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.status = status;
    }
    
    /**
     * Get the marital status filter
     * @return marital status
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }
    
    /**
     * Set the marital status filter
     * @param maritalStatus the marital status
     */
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
    
    /**
     * Get the flat type filter
     * @return flat type
     */
    public FlatType getFlatType() {
        return flatType;
    }
    
    /**
     * Set the flat type filter
     * @param flatType the flat type
     */
    public void setFlatType(FlatType flatType) {
        this.flatType = flatType;
    }
    
    /**
     * Get the neighborhood filter
     * @return neighborhood
     */
    public String getNeighborhood() {
        return neighborhood;
    }
    
    /**
     * Set the neighborhood filter
     * @param neighborhood the neighborhood
     */
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    
    /**
     * Get the minimum age filter
     * @return minimum age
     */
    public int getMinAge() {
        return minAge;
    }
    
    /**
     * Set the minimum age filter
     * @param minAge the minimum age
     */
    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }
    
    /**
     * Get the maximum age filter
     * @return maximum age
     */
    public int getMaxAge() {
        return maxAge;
    }
    
    /**
     * Set the maximum age filter
     * @param maxAge the maximum age
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
    
    /**
     * Get the application status filter
     * @return application status
     */
    public ApplicationStatus getStatus() {
        return status;
    }
    
    /**
     * Set the application status filter
     * @param status the application status
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    /**
     * Apply filters to a list of applications
     * @param applications the applications to filter
     * @return filtered list of applications
     */
    public List<Application> applyFilters(List<Application> applications) {
        List<Application> filteredApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (matchesCriteria(app)) {
                filteredApplications.add(app);
            }
        }
        
        return filteredApplications;
    }
    
    /**
     * Check if an application matches the criteria
     * @param application the application to check
     * @return true if matches, false otherwise
     */
    private boolean matchesCriteria(Application application) {
        // Check marital status
        if (maritalStatus != null && !maritalStatus.isEmpty()) {
            MaritalStatus appStatus = application.getApplicant().getMaritalStatus();
            MaritalStatus criteriaStatus = MaritalStatus.fromDisplayValue(maritalStatus);
            
            if (criteriaStatus != null && appStatus != criteriaStatus) {
                return false;
            }
        }
        
        // Check flat type (for booked applications)
        if (flatType != null && application.getBookedFlat() != null) {
            if (application.getBookedFlat().getType() != flatType) {
                return false;
            }
        }
        
        // Check neighborhood
        if (neighborhood != null && !neighborhood.isEmpty()) {
            if (!application.getProject().getNeighborhood().equalsIgnoreCase(neighborhood)) {
                return false;
            }
        }
        
        // Check age range
        int age = application.getApplicant().getAge();
        if (minAge > 0 && age < minAge) {
            return false;
        }
        if (maxAge > 0 && age > maxAge) {
            return false;
        }
        
        // Check application status
        if (status != null && application.getStatus() != status) {
            return false;
        }
        
        // All checks passed
        return true;
    }
    
    /**
     * Convert to a map of criteria
     * @return map of criteria
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        
        if (maritalStatus != null && !maritalStatus.isEmpty()) {
            map.put("maritalStatus", maritalStatus);
        }
        
        if (flatType != null) {
            map.put("flatType", flatType.getDisplayValue());
        }
        
        if (neighborhood != null && !neighborhood.isEmpty()) {
            map.put("neighborhood", neighborhood);
        }
        
        if (minAge > 0) {
            map.put("minAge", minAge);
        }
        
        if (maxAge > 0) {
            map.put("maxAge", maxAge);
        }
        
        if (status != null) {
            map.put("status", status.getDisplayValue());
        }
        
        return map;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ReportCriteria{");
        
        if (maritalStatus != null && !maritalStatus.isEmpty()) {
            sb.append("maritalStatus='").append(maritalStatus).append("', ");
        }
        
        if (flatType != null) {
            sb.append("flatType=").append(flatType.getDisplayValue()).append(", ");
        }
        
        if (neighborhood != null && !neighborhood.isEmpty()) {
            sb.append("neighborhood='").append(neighborhood).append("', ");
        }
        
        if (minAge > 0) {
            sb.append("minAge=").append(minAge).append(", ");
        }
        
        if (maxAge > 0) {
            sb.append("maxAge=").append(maxAge).append(", ");
        }
        
        if (status != null) {
            sb.append("status=").append(status.getDisplayValue());
        }
        
        sb.append("}");
        return sb.toString();
    }
}