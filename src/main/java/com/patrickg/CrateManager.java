package com.patrickg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CrateManager implements Listener {

    private final Plugin plugin;
    private final Random random = new Random();

    // Each prize: either item or money
    private final List<CratePrize> prizes = new ArrayList<>();

    public CrateManager(Plugin plugin) {
        this.plugin = plugin;
        setupDefaultPrizes();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupDefaultPrizes() {

        // --- Sharpness IV Book ---
        ItemStack sharpnessBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta sharpMeta = (EnchantmentStorageMeta) sharpnessBook.getItemMeta();
        sharpMeta.addStoredEnchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 4, true);
        sharpnessBook.setItemMeta(sharpMeta);
        prizes.add(new CratePrize(sharpnessBook, true)); // rare
        
        // --- Protection IV Book ---
        ItemStack protectionBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta protMeta = (EnchantmentStorageMeta) protectionBook.getItemMeta();
        protMeta.addStoredEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        protectionBook.setItemMeta(protMeta);
        prizes.add(new CratePrize(protectionBook, true)); // rare

        // --- Fortune III Book ---
        ItemStack fortuneBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta fortuneMeta = (EnchantmentStorageMeta) fortuneBook.getItemMeta();
        fortuneMeta.addStoredEnchant(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS, 3, true);
        fortuneBook.setItemMeta(fortuneMeta);
        prizes.add(new CratePrize(fortuneBook, true)); // rare

        // --- Feather Falling IV Book ---
        ItemStack featherBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta featherMeta = (EnchantmentStorageMeta) featherBook.getItemMeta();
        featherMeta.addStoredEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_FALL, 4, true);
        featherBook.setItemMeta(featherMeta);
        prizes.add(new CratePrize(featherBook, true)); // rare

        
        prizes.add(new CratePrize(sharpnessBook, true)); // rare prize
        // RARE PRIZES
        prizes.add(new CratePrize(new ItemStack(Material.DIAMOND_BLOCK, 2), true));

        prizes.add(new CratePrize(new ItemStack(Material.ELYTRA, 1), true));


        prizes.add(new CratePrize(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), true));
        prizes.add(new CratePrize(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2), true));

        prizes.add(new CratePrize(50000, true)); // $50,000 cash
        prizes.add(new CratePrize(100000, true)); // $100,000 cash
        
        prizes.add(new CratePrize(new ItemStack(Material.BEACON, 1), true));
        prizes.add(new CratePrize(new ItemStack(Material.TOTEM_OF_UNDYING, 3), true));
        prizes.add(new CratePrize(new ItemStack(Material.TOTEM_OF_UNDYING, 1), true));
        prizes.add(new CratePrize(new ItemStack(Material.TOTEM_OF_UNDYING, 2), true));

    
        // COMMON PRIZES
        for (int i = 0; i < 10; i++) {
            prizes.add(new CratePrize(new ItemStack(Material.IRON_INGOT, 32), false));
            prizes.add(new CratePrize(new ItemStack(Material.EMERALD, 8), false));
            prizes.add(new CratePrize(200, false)); // $200 cash
            prizes.add(new CratePrize(500, false)); // $500 cash
        }
    
        // Shuffle the list (optional but makes it less predictable)
        java.util.Collections.shuffle(prizes);
    }
    

    public void openCrateSpinGUI(Player player, int cost) {
        if (plugin.getBalanceManager().getBalance(player.getUniqueId()) < cost) {
            player.sendMessage(ChatColor.RED + "You don't have enough money to spin ($" + cost + ")!");
            return;
        }

        plugin.getBalanceManager().subtractBalance(player.getUniqueId(), cost);
        plugin.getBalanceManager().saveAllBalances();

        Inventory spinInventory = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Crate Spinning...");

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 9; i++) {
            if (i < 3 || i > 5) {
                spinInventory.setItem(i, filler);
            }
        }

        player.openInventory(spinInventory);

        startSpin(player, spinInventory, 0, 60 + random.nextInt(20), 2);
    }

    private void startSpin(Player player, Inventory spinInventory, int spins, int totalSpins, int currentDelay) {
        new BukkitRunnable() {
            @Override
            public void run() {

                int newSpins = spins + 1;

                // Shift left
                spinInventory.setItem(3, spinInventory.getItem(4));
                spinInventory.setItem(4, spinInventory.getItem(5));

                // Add new random item at slot 5
                CratePrize prize = prizes.get(random.nextInt(prizes.size()));
                if (prize.isMoneyPrize()) {
                    ItemStack moneyItem = new ItemStack(Material.PAPER);
                    ItemMeta meta = moneyItem.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "$" + prize.getMoneyAmount());
                    moneyItem.setItemMeta(meta);
                    spinInventory.setItem(5, moneyItem);
                } else {
                    spinInventory.setItem(5, prize.getItemStack());
                }


                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);

                // Slow down logic
                int newDelay = currentDelay;
                if (newSpins > totalSpins * 0.6) newDelay = 4;
                if (newSpins > totalSpins * 0.8) newDelay = 6;
                if (newSpins > totalSpins * 0.9) newDelay = 10;

                if (newSpins >= totalSpins) {
                    // Final prize
                    ItemStack finalItem = spinInventory.getItem(4);
                    CratePrize wonPrize = null;

                    if (finalItem != null && finalItem.getType() == Material.PAPER) {
                        // Landed on a money note -> read amount from paper name
                        String displayName = ChatColor.stripColor(finalItem.getItemMeta().getDisplayName());
                        if (displayName.startsWith("$")) {
                            try {
                                int amount = Integer.parseInt(displayName.substring(1)); // skip the '$'
                    
                                // ✅ Find the original prize matching amount
                                for (CratePrize p : prizes) {
                                    if (p.isMoneyPrize() && p.getMoneyAmount() == amount) {
                                        wonPrize = p;
                                        break;
                                    }
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    } else {
                        // Landed on item prize
                        for (CratePrize p : prizes) {
                            if (!p.isMoneyPrize() && p.getItemStack().getType() == finalItem.getType()) {
                                wonPrize = p;
                                break;
                            }
                        }
                    }

                    if (wonPrize == null) {
                        wonPrize = new CratePrize(10000, false); // fallback
                    }

                    final CratePrize finalPrizeCopy = wonPrize;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (finalPrizeCopy.isMoneyPrize()) {
                                plugin.getBalanceManager().addBalance(player.getUniqueId(), finalPrizeCopy.getMoneyAmount());
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "You won $" + finalPrizeCopy.getMoneyAmount() + "!");
                            } else {
                                player.getInventory().addItem(finalPrizeCopy.getItemStack());
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "You won " + finalPrizeCopy.getItemStack().getAmount() + "x " + finalPrizeCopy.getItemStack().getType().name() + "!");
                            }
                    
                            if (finalPrizeCopy.isRare()) {
                                triggerRareEffects(player);
                            }
                            plugin.getBalanceManager().saveAllBalances();
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            player.closeInventory();
                        }
                    }.runTaskLater(plugin, 40L); // 2 second delay
                } else {
                    // Continue spinning with updated delay
                    startSpin(player, spinInventory, newSpins, totalSpins, newDelay);
                }
            }
        }.runTaskLater(plugin, currentDelay);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Crate Spinning...")) {
            event.setCancelled(true);
        }
    }
    
    private void triggerRareEffects(Player player) {
        org.bukkit.entity.Firework firework = player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class);
        org.bukkit.inventory.meta.FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
                .withColor(org.bukkit.Color.PURPLE, org.bukkit.Color.AQUA)
                .withFade(org.bukkit.Color.LIME)
                .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build());
        fireworkMeta.setPower(2);
        firework.setFireworkMeta(fireworkMeta);
    
        player.sendTitle(ChatColor.LIGHT_PURPLE + "RARE WIN!", ChatColor.AQUA + "You hit the jackpot!", 10, 70, 20);
    
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
    }
    
}
