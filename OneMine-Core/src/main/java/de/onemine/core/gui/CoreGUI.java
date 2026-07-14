package de.onemine.core.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * CoreGUI: Helper class for building unified 54-slot Java GUIs.
 * All OneMine GUIs use this: black glass borders, wood themes, custom click sounds.
 */
public class CoreGUI implements InventoryHolder {
    private Inventory inventory;
    private final String title;

    public CoreGUI(String title) {
        this.title = title;
        this.inventory = createInventory();
    }

    /**
     * Create a 54-slot inventory with black border.
     */
    private Inventory createInventory() {
        Inventory inv = org.bukkit.Bukkit.createInventory(this, 54, title);
        
        // Add black stained glass border (slots 0-8, 45-53, and column edges)
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS, " ");
        
        // Top border (row 0: slots 0-8)
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass);
        }
        
        // Bottom border (row 5: slots 45-53)
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, blackGlass);
        }
        
        // Side borders (column 0 and 8)
        for (int i = 1; i < 5; i++) {
            inv.setItem(i * 9, blackGlass);      // Left column
            inv.setItem(i * 9 + 8, blackGlass);  // Right column
        }
        
        return inv;
    }

    /**
     * Create a display item with name and lore.
     */
    public ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line);
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Add a back button (slot 45, bottom-left).
     */
    public void addBackButton() {
        ItemStack backButton = createItem(Material.OAK_DOOR, "§6← Back");
        inventory.setItem(45, backButton);
    }

    /**
     * Add an info button (slot 49, center-bottom).
     */
    public void addInfoButton(String infoTitle) {
        ItemStack infoButton = createItem(Material.OAK_SIGN, "§6ℹ Info", "§7" + infoTitle);
        inventory.setItem(49, infoButton);
    }

    /**
     * Get the underlying inventory.
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Open GUI for a player.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /**
     * Set an item at a specific slot.
     */
    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    /**
     * Get item at a specific slot.
     */
    public ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }

    /**
     * Get title of the GUI.
     */
    public String getTitle() {
        return title;
    }
}
