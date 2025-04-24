

package utils.filters;
import entity.HDBManager;
import java.util.List;
/**
 * Interface for filtering by manager
 * Demonstrates ISP by having a focused interface for manager filtering
 */
public interface ManagerFilterable<T> {
    /**
     * Filter items by manager
     * @param items the items to filter
     * @param manager the manager to filter by
     * @return filtered list of items
     */
    List<T> filterByManager(List<T> items, HDBManager manager);
}