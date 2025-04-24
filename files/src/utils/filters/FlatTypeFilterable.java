package utils.filters;
import entity.FlatType;
import java.util.List;
/**
 * Interface for filtering by flat type
 * Demonstrates ISP by having a focused interface for flat type filtering
 */
public interface FlatTypeFilterable<T> {
    List<T> filterByFlatType(List<T> items, FlatType flatType);
}
