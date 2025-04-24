package utils.filters;

import entity.Application;
import entity.ApplicationStatus;
import entity.FlatType;
import entity.MaritalStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application filter implementing only the interfaces it needs
 * Demonstrates Interface Segregation Principle by implementing
 * only the specific filtering interfaces relevant for applications
 */
public class ApplicationFilter implements Filterable<Application>,
                                          MaritalStatusFilterable<Application>,
                                          ApplicationStatusFilterable<Application>,
                                          FlatTypeFilterable<Application> {
    
    /**
     * Apply filters to a list of applications
     * @param applications the list of applications to filter
     * @param filters a map of filter criteria
     * @return filtered list of applications
     */
    @Override
    public List<Application> applyFilters(List<Application> applications, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(applications);
        }
        
        List<Application> filteredApplications = new ArrayList<>(applications);
        
        // Filter by marital status
        if (filters.containsKey("maritalStatus")) {
            String maritalStatusStr = (String) filters.get("maritalStatus");
            if (maritalStatusStr != null && !maritalStatusStr.isEmpty()) {
                MaritalStatus status = MaritalStatus.fromDisplayValue(maritalStatusStr);
                if (status != null) {
                    filteredApplications = filterByMaritalStatus(filteredApplications, status);
                }
            }
        }
        
        // Filter by application status
        if (filters.containsKey("status")) {
            String statusStr = (String) filters.get("status");
            if (statusStr != null && !statusStr.isEmpty()) {
                ApplicationStatus status = ApplicationStatus.fromDisplayValue(statusStr);
                if (status != null) {
                    filteredApplications = filterByStatus(filteredApplications, status);
                }
            }
        }
        
        // Filter by flat type (for booked applications)
        if (filters.containsKey("flatType")) {
            String flatTypeStr = (String) filters.get("flatType");
            if (flatTypeStr != null && !flatTypeStr.isEmpty()) {
                FlatType flatType = FlatType.fromDisplayValue(flatTypeStr);
                if (flatType != null) {
                    filteredApplications = filterByFlatType(filteredApplications, flatType);
                }
            }
        }
        
        return filteredApplications;
    }
    
    /**
     * Filter applications by marital status
     * @param applications the list of applications to filter
     * @param maritalStatus the marital status to filter by
     * @return filtered list of applications
     */
    @Override
    public List<Application> filterByMaritalStatus(List<Application> applications, MaritalStatus maritalStatus) {
        List<Application> filtered = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getApplicant().getMaritalStatus() == maritalStatus) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter applications by status
     * @param applications the list of applications to filter
     * @param status the status to filter by
     * @return filtered list of applications
     */
    @Override
    public List<Application> filterByStatus(List<Application> applications, ApplicationStatus status) {
        List<Application> filtered = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getStatus() == status) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter applications by flat type
     * @param applications the list of applications to filter
     * @param flatType the flat type to filter by
     * @return filtered list of applications
     */
    @Override
    public List<Application> filterByFlatType(List<Application> applications, FlatType flatType) {
        List<Application> filtered = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getStatus() == ApplicationStatus.BOOKED && 
                app.getBookedFlat() != null && 
                app.getBookedFlat().getType() == flatType) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
}