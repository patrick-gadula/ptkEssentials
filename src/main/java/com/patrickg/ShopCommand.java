package com.patrickg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopCommand implements CommandExecutor, Listener {

    private final Plugin plugin;

    public ShopCommand(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /shop.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("set")) {
                handleSetCommand(player, args);
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                handleRemoveCommand(player, args);
                return true;
            }
        }

        openMainMenu(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_GREEN + "Shop")) {
            event.setCancelled(true);

            Material clicked = event.getCurrentItem().getType();
            if (clicked == Material.GREEN_STAINED_GLASS_PANE) {
                openBuyerShop(player, 1);
            } else if (clicked == Material.RED_STAINED_GLASS_PANE) {
                openSellerShop(player, 1);
            }
            return;
        }

        if (title.contains(ChatColor.DARK_GREEN + "Seller Shop")) {
            event.setCancelled(true);

            if (event.getSlot() == 44 && title.contains("Page 1")) {
                player.closeInventory();
                openSellerShop(player, 2);
                return;
            }
    
            if (event.getSlot() == 36 && title.contains("Page 2")) {
                player.closeInventory();
                openSellerShop(player, 1);
                return;
            }

            Material clickedMaterial = event.getCurrentItem().getType();

            ConfigurationSection page = plugin.shopData.getConfigurationSection("page1");
            if (page != null) {
                for (String slotKey : page.getKeys(false)) {
                    String materialName = page.getString(slotKey + ".material");
                    int minCount = plugin.shopData.getInt("page1." + slotKey + ".min_count");
                    double sellPrice = plugin.shopData.getDouble("page1." + slotKey + ".sell_price");

                    if (materialName != null && Material.getMaterial(materialName) == clickedMaterial) {
                        int playerItemCount = countItem(player, clickedMaterial);

                        if (event.isLeftClick()) {
                            if (playerItemCount >= minCount) {
                                player.getInventory().removeItem(new ItemStack(clickedMaterial, minCount));
                                plugin.getBalanceManager().addBalance(player.getUniqueId(), sellPrice);
                                player.sendMessage(ChatColor.GREEN + "Sold " + minCount + " " + clickedMaterial.name() + " for $" + sellPrice + "!");
                                plugin.getBalanceManager().saveAllBalances();
                            } else {
                                player.sendMessage(ChatColor.RED + "You need at least " + minCount + " " + clickedMaterial.name() + " to sell!");
                            }
                        } else if (event.isRightClick()) {
                            if (playerItemCount >= minCount) {
                                int batches = playerItemCount / minCount;
                                int totalItemsSold = batches * minCount;
                                double totalEarned = batches * sellPrice;

                                if (totalItemsSold > 0) {
                                    player.getInventory().removeItem(new ItemStack(clickedMaterial, totalItemsSold));
                                    plugin.getBalanceManager().addBalance(player.getUniqueId(), totalEarned);
                                    player.sendMessage(ChatColor.GREEN + "Sold " + totalItemsSold + " " + clickedMaterial.name() + " for $" + totalEarned + "!");
                                    plugin.getBalanceManager().saveAllBalances();
                                } else {
                                    player.sendMessage(ChatColor.RED + "You don't have enough " + clickedMaterial.name() + " to sell!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have enough " + clickedMaterial.name() + " to sell!");
                            }
                        }

                        player.closeInventory();
                        return;
                    }
                }
            }
            return;
        }

        if (title.contains(ChatColor.DARK_GREEN + "Buyer Shop")) {
            event.setCancelled(true);

            if (event.getSlot() == 44 && title.contains("Page 1")) {
                player.closeInventory();
                openBuyerShop(player, 2);
                return;
            }
    
            if (event.getSlot() == 36 && title.contains("Page 2")) {
                player.closeInventory();
                openBuyerShop(player, 1);
                return;
            }

            Material clickedMaterial = event.getCurrentItem().getType();

            ConfigurationSection page = plugin.shopData.getConfigurationSection("page1");
            if (page != null) {
                for (String slotKey : page.getKeys(false)) {
                    String materialName = page.getString(slotKey + ".material");
                    double buyPrice = plugin.shopData.getDouble("page1." + slotKey + ".buy_price");

                    if (materialName != null && Material.getMaterial(materialName) == clickedMaterial) {
                        if (event.isLeftClick()) {
                            double balance = plugin.getBalanceManager().getBalance(player.getUniqueId());
                            if (balance >= buyPrice) {
                                plugin.getBalanceManager().subtractBalance(player.getUniqueId(), buyPrice);
                                player.getInventory().addItem(new ItemStack(clickedMaterial, 1));
                                player.sendMessage(ChatColor.GREEN + "Bought 1 " + clickedMaterial.name() + " for $" + buyPrice + "!");
                                plugin.getBalanceManager().saveAllBalances();
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have enough money to buy this!");
                            }
                        } else if (event.isRightClick()){
                            double balance = plugin.getBalanceManager().getBalance(player.getUniqueId());
                            if (balance >= buyPrice * 64) {
                                plugin.getBalanceManager().subtractBalance(player.getUniqueId(), buyPrice * 64);
                                player.getInventory().addItem(new ItemStack(clickedMaterial, 64));
                                player.sendMessage(ChatColor.GREEN + "Bought 64 " + clickedMaterial.name() + " for $" + buyPrice * 64 + "!");
                                plugin.getBalanceManager().saveAllBalances();
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have enough money to buy this!");
                            }
                        }

                        player.closeInventory();
                        return;
                    }
                }
            }
        }
    }



    private int countItem(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("ptk.shopset")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to set shop items.");
            return;
        }
    
        if (args.length != 7) {
            player.sendMessage(ChatColor.RED + "Usage: /shop set <page> <slot> <item> <min_count> <sell_price> <buy_price>");
            return;
        }
    
        int page, slot, minCount;
        Double sellPrice = null;
        Double buyPrice = null;
        Material material;
    
        try {
            page = Integer.parseInt(args[1]);
            slot = Integer.parseInt(args[2]);
            material = Material.valueOf(args[3].toUpperCase());
            minCount = Integer.parseInt(args[4]);
    
            // Check if admin wrote "null" for sell or buy
            if (!args[5].equalsIgnoreCase("null")) {
                sellPrice = Double.parseDouble(args[5]);
            }
    
            if (!args[6].equalsIgnoreCase("null")) {
                buyPrice = Double.parseDouble(args[6]);
            }
    
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Invalid arguments. Please check numbers and item name.");
            return;
        }
    
        String path = "page" + page + ".slot" + slot;
        plugin.shopData.set(path + ".material", material.name());
        plugin.shopData.set(path + ".min_count", minCount);
    
        if (sellPrice != null) {
            plugin.shopData.set(path + ".sell_price", sellPrice);
        }
    
        if (buyPrice != null) {
            plugin.shopData.set(path + ".buy_price", buyPrice);
        }
    
        try {
            plugin.shopData.save(new File(plugin.getDataFolder(), "shop.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        // Build a smart confirmation message
        String sellInfo = (sellPrice != null) ? ("Sell $" + sellPrice) : "No Sell";
        String buyInfo = (buyPrice != null) ? ("Buy $" + buyPrice) : "No Buy";
    
        player.sendMessage(ChatColor.GREEN + "Shop item set: " + material.name() +
                " (Page " + page + ", Slot " + slot + ") | " + sellInfo + " | " + buyInfo);
    }
    
    

    private void handleRemoveCommand(Player player, String[] args) {
        if (!player.hasPermission("ptk.shopremove")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to remove shop items.");
            return;
        }
    
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /shop remove <page> <slot>");
            return;
        }
    
        int page, slot;
        try {
            page = Integer.parseInt(args[1]);
            slot = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid page or slot number.");
            return;
        }
    
        String path = "page" + page + ".slot" + slot;
        if (plugin.shopData.contains(path)) {
            plugin.shopData.set(path, null);
            try {
                plugin.shopData.save(new File(plugin.getDataFolder(), "shop.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.sendMessage(ChatColor.GREEN + "Removed shop item at Page " + page + ", Slot " + slot + "!");
        } else {
            player.sendMessage(ChatColor.RED + "No shop item found at Page " + page + ", Slot " + slot + ".");
        }
    }
    
    private void openMainMenu(Player player) {
        Inventory mainMenu = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Shop");
    
        // Green Glass (Buy Shop)
        ItemStack buyItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta buyMeta = buyItem.getItemMeta();
        buyMeta.setDisplayName(ChatColor.GREEN + "Open Buyer Shop");
        buyItem.setItemMeta(buyMeta);
    
        // Red Glass (Sell Shop)
        ItemStack sellItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta sellMeta = sellItem.getItemMeta();
        sellMeta.setDisplayName(ChatColor.RED + "Open Seller Shop");
        sellItem.setItemMeta(sellMeta);
    
        mainMenu.setItem(11, buyItem);
        mainMenu.setItem(15, sellItem);
    
        player.openInventory(mainMenu);
    }

    private void openBuyerShop(Player player, int pageNumber) {
        Inventory buyerShop = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Buyer Shop - Page " + pageNumber);

        String pagePath = "page" + pageNumber;
        ConfigurationSection page = plugin.shopData.getConfigurationSection(pagePath);
        if (page != null) {
            for (String slotKey : page.getKeys(false)) {
                int slot = Integer.parseInt(slotKey.replace("slot", ""));
                String materialName = page.getString(slotKey + ".material");

                if (!plugin.shopData.contains(pagePath + "." + slotKey + ".buy_price")) {
                    continue; // No buy price = not for buyer shop
                }

                double buyPrice = plugin.shopData.getDouble(pagePath + "." + slotKey + ".buy_price"); // FIXED small mistake here too
    
                Material material = Material.getMaterial(materialName);
                if (material != null) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.AQUA + "Buy " + material.name());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Left-click to buy 1 for $" + buyPrice);
                    lore.add(ChatColor.GRAY + "Right-click to buy 64 for $" + buyPrice * 64);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
    
                    buyerShop.setItem(slot, item);
                }
            }
        }

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 44; i++) {
            if (buyerShop.getItem(i) == null) {
                buyerShop.setItem(i, filler);
            }
        }

        if (pageNumber == 1) {
            // Page 1: Only Next Page
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.GOLD + "Next Page");
            nextPage.setItemMeta(nextMeta);
            buyerShop.setItem(44, nextPage);
        } else {
            // Page 2+: Back button
            ItemStack backPage = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backPage.getItemMeta();
            backMeta.setDisplayName(ChatColor.RED + "Back Page");
            backPage.setItemMeta(backMeta);
            buyerShop.setItem(36, backPage); // 1st slot of bottom row
        }
    
        player.openInventory(buyerShop);
    }
    
    private void openSellerShop(Player player, int pageNumber) {
        Inventory sellerShop = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Seller Shop - Page " + pageNumber);
    
        String pagePath = "page" + pageNumber;
        ConfigurationSection page = plugin.shopData.getConfigurationSection(pagePath);
        if (page != null) {
            for (String slotKey : page.getKeys(false)) {
                int slot = Integer.parseInt(slotKey.replace("slot", ""));
                String materialName = page.getString(slotKey + ".material");
                if (!plugin.shopData.contains(pagePath + "." + slotKey + ".sell_price")) {
                    continue; // No sell price = not for seller shop
                }
        
                double sellPrice = plugin.shopData.getDouble(pagePath + "." + slotKey + ".sell_price");
                int minCount = plugin.shopData.getInt(pagePath + "." + slotKey + ".min_count");

    
                Material material = Material.getMaterial(materialName);
                if (material != null) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.AQUA + "Sell " + material.name());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Left-click sell " + minCount + " for $" + sellPrice);
                    lore.add(ChatColor.GRAY + "Right-click sell all you have!");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
    
                    sellerShop.setItem(slot, item);
                }
            }
        }

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 44; i++) {
            if (sellerShop.getItem(i) == null) {
                sellerShop.setItem(i, filler);
            }
        }

        if (pageNumber == 1) {
            // Page 1: Only Next Page
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.GOLD + "Next Page");
            nextPage.setItemMeta(nextMeta);
            sellerShop.setItem(44, nextPage);
        } else {
            // Page 2+: Back button
            ItemStack backPage = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backPage.getItemMeta();
            backMeta.setDisplayName(ChatColor.RED + "Back Page");
            backPage.setItemMeta(backMeta);
            sellerShop.setItem(36, backPage); // 1st slot of bottom row
        }
    
        player.openInventory(sellerShop);
    }
    
}