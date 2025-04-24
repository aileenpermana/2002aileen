package utils.filters;


import java.util.List;

/**
 * Interface for filtering by location-based criteria
 * Demonstrates ISP by having a focused interface for neighborhood filtering
 */
public interface NeighborhoodFilterable<T> {
    List<T> filterByNeighborhood(List<T> items, String neighborhood);
}