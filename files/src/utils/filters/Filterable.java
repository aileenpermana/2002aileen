package utils.filters;

import entity.ApplicationStatus;
import entity.MaritalStatus;
import java.util.List;
import java.util.Map;

/**
 * Base interface for any filterable component
 * Demonstrates Interface Segregation Principle by having a minimal base interface
 */
public interface Filterable<T> {
    /**
     * Apply a map of filters to a list of items
     * @param items the items to filter
     * @param filters the filter criteria
     * @return filtered list of items
     */
    List<T> applyFilters(List<T> items, Map<String, Object> filters);
}








interface MaritalStatusFilterable<T> {
    /**
     * Filter items by marital status
     * @param items the items to filter
     * @param maritalStatus the marital status to filter by
     * @return filtered list of items
     */
    List<T> filterByMaritalStatus(List<T> items, MaritalStatus maritalStatus);
}

/**
 * Interface for filtering by application status
 * Demonstrates ISP by having a focused interface for application status filtering
 */
interface ApplicationStatusFilterable<T> {
    /**
     * Filter items by application status
     * @param items the items to filter
     * @param status the application status to filter by
     * @return filtered list of items
     */
    List<T> filterByStatus(List<T> items, ApplicationStatus status);
}