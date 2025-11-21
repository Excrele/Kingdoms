package com.excrele.kingdoms.model;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a library for storing and sharing books/enchantments
 */
public class KingdomLibrary {
    private String name;
    private String kingdomName;
    private Chunk chunk;
    private Location location;
    private List<ItemStack> books; // Stored books
    private List<String> enchantments; // Available enchantments
    private int maxBooks;
    private boolean isPublic; // Can all members access
    
    public KingdomLibrary(String name, String kingdomName, Chunk chunk, Location location) {
        this.name = name;
        this.kingdomName = kingdomName;
        this.chunk = chunk;
        this.location = location;
        this.books = new ArrayList<>();
        this.enchantments = new ArrayList<>();
        this.maxBooks = 50; // Default max
        this.isPublic = true;
    }
    
    public String getName() { return name; }
    public String getKingdomName() { return kingdomName; }
    public Chunk getChunk() { return chunk; }
    public Location getLocation() { return location; }
    public List<ItemStack> getBooks() { return books; }
    public boolean addBook(ItemStack book) {
        if (books.size() >= maxBooks) return false;
        books.add(book);
        return true;
    }
    public boolean removeBook(ItemStack book) {
        return books.remove(book);
    }
    public List<String> getEnchantments() { return enchantments; }
    public void addEnchantment(String enchantment) {
        if (!enchantments.contains(enchantment)) {
            enchantments.add(enchantment);
        }
    }
    public int getMaxBooks() { return maxBooks; }
    public void setMaxBooks(int maxBooks) { this.maxBooks = maxBooks; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}

