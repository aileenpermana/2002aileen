package utils.filters;

import entity.FlatType;
import entity.HDBManager;
import entity.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Project filter implementing only the interfaces it needs
 * Demonstrates Interface Segregation Principle by implementing
 * only the specific filtering interfaces relevant for projects
 */
public class ProjectFilter implements Filterable<Project>, 
                                     NeighborhoodFilterable<Project>,
                                     FlatTypeFilterable<Project>,
                                     ManagerFilterable<Project> {
    
    /**
     * Apply filters to a list of projects
     * @param projects the list of projects to filter
     * @param filters a map of filter criteria
     * @return filtered list of projects
     */
    @Override
    public List<Project> applyFilters(List<Project> projects, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(projects);
        }
        
        List<Project> filteredProjects = new ArrayList<>(projects);
        
        // Filter by neighborhood
        if (filters.containsKey("neighborhood")) {
            String neighborhood = (String) filters.get("neighborhood");
            if (neighborhood != null && !neighborhood.isEmpty()) {
                filteredProjects = filterByNeighborhood(filteredProjects, neighborhood);
            }
        }
        
        // Filter by flat type
        if (filters.containsKey("flatType")) {
            String flatTypeStr = (String) filters.get("flatType");
            if (flatTypeStr != null && !flatTypeStr.isEmpty()) {
                FlatType flatType = FlatType.fromDisplayValue(flatTypeStr);
                if (flatType != null) {
                    filteredProjects = filterByFlatType(filteredProjects, flatType);
                }
            }
        }
        
        // Filter by manager
        if (filters.containsKey("manager")) {
            HDBManager manager = (HDBManager) filters.get("manager");
            if (manager != null) {
                filteredProjects = filterByManager(filteredProjects, manager);
            }
        }
        
        return filteredProjects;
    }
    
    /**
     * Filter projects by neighborhood
     * @param projects the list of projects to filter
     * @param neighborhood the neighborhood to filter by
     * @return filtered list of projects
     */
    @Override
    public List<Project> filterByNeighborhood(List<Project> projects, String neighborhood) {
        List<Project> filtered = new ArrayList<>();
        
        for (Project project : projects) {
            if (project.getNeighborhood().equalsIgnoreCase(neighborhood)) {
                filtered.add(project);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter projects by flat type
     * @param projects the list of projects to filter
     * @param flatType the flat type to filter by
     * @return filtered list of projects
     */
    @Override
    public List<Project> filterByFlatType(List<Project> projects, FlatType flatType) {
        List<Project> filtered = new ArrayList<>();
        
        for (Project project : projects) {
            if (project.hasFlatType(flatType)) {
                filtered.add(project);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter projects by manager
     * @param projects the list of projects to filter
     * @param manager the manager to filter by
     * @return filtered list of projects
     */
    @Override
    public List<Project> filterByManager(List<Project> projects, HDBManager manager) {
        List<Project> filtered = new ArrayList<>();
        
        for (Project project : projects) {
            if (project.getManagerInCharge().getNRIC().equals(manager.getNRIC())) {
                filtered.add(project);
            }
        }
        
        return filtered;
    }
}