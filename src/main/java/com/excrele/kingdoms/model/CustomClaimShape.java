package com.excrele.kingdoms.model;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a custom claim shape (polygon, circle, etc.)
 */
public class CustomClaimShape {
    public enum ShapeType {
        RECTANGULAR, // Default chunk-based
        POLYGON,    // Custom polygon from WorldEdit selection
        CIRCULAR,   // Circular claim
        MULTI_LEVEL // Vertical stacking
    }
    
    private final String id;
    private final String kingdomName;
    private final ShapeType shapeType;
    private final World world;
    private final List<Location> points; // For polygon
    private final Location center; // For circular
    private final double radius; // For circular
    private final int minY; // For multi-level
    private final int maxY; // For multi-level
    private final List<Chunk> affectedChunks; // Chunks affected by this shape
    
    public CustomClaimShape(String id, String kingdomName, ShapeType shapeType, World world) {
        this.id = id;
        this.kingdomName = kingdomName;
        this.shapeType = shapeType;
        this.world = world;
        this.points = new ArrayList<>();
        this.center = null;
        this.radius = 0;
        this.minY = world.getMinHeight();
        this.maxY = world.getMaxHeight();
        this.affectedChunks = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getKingdomName() {
        return kingdomName;
    }
    
    public ShapeType getShapeType() {
        return shapeType;
    }
    
    public World getWorld() {
        return world;
    }
    
    public List<Location> getPoints() {
        return points;
    }
    
    public void addPoint(Location point) {
        points.add(point);
    }
    
    public Location getCenter() {
        return center;
    }
    
    public void setCenter(Location center) {
        // This would be set in constructor for circular shapes
    }
    
    public double getRadius() {
        return radius;
    }
    
    public void setRadius(double radius) {
        // This would be set in constructor for circular shapes
    }
    
    public int getMinY() {
        return minY;
    }
    
    public int getMaxY() {
        return maxY;
    }
    
    public List<Chunk> getAffectedChunks() {
        return affectedChunks;
    }
    
    /**
     * Check if a location is within this claim shape
     */
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        switch (shapeType) {
            case RECTANGULAR:
                // Check if location is in any affected chunk
                Chunk locChunk = location.getChunk();
                return affectedChunks.contains(locChunk);
                
            case CIRCULAR:
                if (center == null) return false;
                double distance = location.distance(center);
                return distance <= radius && location.getY() >= minY && location.getY() <= maxY;
                
            case POLYGON:
                return isPointInPolygon(location);
                
            case MULTI_LEVEL:
                Chunk chunk = location.getChunk();
                return affectedChunks.contains(chunk) && 
                       location.getY() >= minY && location.getY() <= maxY;
                
            default:
                return false;
        }
    }
    
    /**
     * Check if a point is inside a polygon using ray casting algorithm
     */
    private boolean isPointInPolygon(Location point) {
        if (points.size() < 3) {
            return false;
        }
        
        double x = point.getX();
        double z = point.getZ();
        boolean inside = false;
        
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            double xi = points.get(i).getX();
            double zi = points.get(i).getZ();
            double xj = points.get(j).getX();
            double zj = points.get(j).getZ();
            
            boolean intersect = ((zi > z) != (zj > z)) &&
                               (x < (xj - xi) * (z - zi) / (zj - zi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }
        
        return inside && point.getY() >= minY && point.getY() <= maxY;
    }
    
    /**
     * Calculate affected chunks for this shape
     */
    public void calculateAffectedChunks() {
        affectedChunks.clear();
        
        switch (shapeType) {
            case RECTANGULAR:
                // Already set by claim system
                break;
                
            case CIRCULAR:
                if (center != null) {
                    int centerChunkX = (int) Math.floor(center.getX() / 16);
                    int centerChunkZ = (int) Math.floor(center.getZ() / 16);
                    int radiusChunks = (int) Math.ceil(radius / 16) + 1;
                    
                    for (int x = centerChunkX - radiusChunks; x <= centerChunkX + radiusChunks; x++) {
                        for (int z = centerChunkZ - radiusChunks; z <= centerChunkZ + radiusChunks; z++) {
                            Chunk chunk = world.getChunkAt(x, z);
                            Location chunkCenter = chunk.getBlock(8, (int) center.getY(), 8).getLocation();
                            if (chunkCenter.distance(center) <= radius) {
                                affectedChunks.add(chunk);
                            }
                        }
                    }
                }
                break;
                
            case POLYGON:
                // Find bounding box
                double minX = points.stream().mapToDouble(Location::getX).min().orElse(0);
                double maxX = points.stream().mapToDouble(Location::getX).max().orElse(0);
                double minZ = points.stream().mapToDouble(Location::getZ).min().orElse(0);
                double maxZ = points.stream().mapToDouble(Location::getZ).max().orElse(0);
                
                int minChunkX = (int) Math.floor(minX / 16);
                int maxChunkX = (int) Math.ceil(maxX / 16);
                int minChunkZ = (int) Math.floor(minZ / 16);
                int maxChunkZ = (int) Math.ceil(maxZ / 16);
                
                for (int x = minChunkX; x <= maxChunkX; x++) {
                    for (int z = minChunkZ; z <= maxChunkZ; z++) {
                        Chunk chunk = world.getChunkAt(x, z);
                        Location chunkCenter = chunk.getBlock(8, (minY + maxY) / 2, 8).getLocation();
                        if (isPointInPolygon(chunkCenter)) {
                            affectedChunks.add(chunk);
                        }
                    }
                }
                break;
                
            case MULTI_LEVEL:
                // Already set by claim system, just verify Y bounds
                break;
        }
    }
}

