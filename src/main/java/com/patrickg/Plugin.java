package com.patrickg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.patrickg.Bosses.BossCommand;
import com.patrickg.Bosses.BossManager;
import com.patrickg.Bosses.CursedItemListener;
import com.patrickg.Clans.Clan;
import com.patrickg.Clans.ClanChatListener;
import com.patrickg.Clans.ClanCommand;
import com.patrickg.Clans.ClanManager;
import com.patrickg.Clans.ClanPrefixManager;
import com.patrickg.Clans.ClanPvpListener;
import com.patrickg.Clans.ClanStorage;

/*
 * [ptkEssentials] Plugin
 */
public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("ptkEssentials");

  public File logFile;

  public FileConfiguration balanceData;
  public FileConfiguration shopData;
  private File balanceFile;
  private File shopFile;
  private BalanceManager balanceManager;
  private ChallengeManager challengeManager;
  private InsuranceManager insuranceManager;
  private CrateManager crateManager;
  private ClanManager clanManager;
  private ClanPrefixManager clanPrefixManager;
  private ClanStorage clanStorage;
  private BossManager bossManager;


  
  public void onEnable()
  {
    // Console Boot
    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[ptkEssentials] Enabled");

    // File Starters
    createBalanceFile();
    createShopFile();

    // Bal Manager
    balanceManager = new BalanceManager(this);
    challengeManager = new ChallengeManager(this);
    new ChallengeListener(this);
    insuranceManager = new InsuranceManager(this);
    new InsuranceListener(this);
    crateManager = new CrateManager(this);
    new CrateCommand(this);
    new RouletteCommand(this);
    new SupplyDropManager(this);
    new MoneyNoteListener(this);

    //Clans
    clanStorage = new ClanStorage(getDataFolder());
    clanManager = new ClanManager(clanStorage);
    clanPrefixManager = new ClanPrefixManager();

    //Bosses
    bossManager = new BossManager(clanManager);
    getServer().getPluginManager().registerEvents(bossManager, this);
    this.getCommand("boss").setExecutor(new BossCommand(bossManager));


    Map<String, Clan> loadedClans = clanStorage.loadClans();
    for (Clan clan : loadedClans.values()) {
        clanManager.registerLoadedClan(clan); // You'll add this method below
    }

    getServer().getPluginManager().registerEvents(new ClanPvpListener(clanManager), this);
    getServer().getPluginManager().registerEvents(new ClanChatListener(clanManager, clanPrefixManager), this);
    getServer().getPluginManager().registerEvents(new CursedItemListener(), this);



    // Logging File
    logFile = new File(getDataFolder(), "donationLog.log");
    if (!logFile.exists()) {
        try {
            getDataFolder().mkdirs();
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Commands
    this.getCommand("balance").setExecutor(new BalCommand(this));
    this.getCommand("baltop").setExecutor(new BalCommand(this));
    this.getCommand("donate").setExecutor(new BalCommand(this));
    this.getCommand("pay").setExecutor(new BalCommand(this));

    this.getCommand("shop").setExecutor(new ShopCommand(this));

    //Challenges
    this.getCommand("challenge").setExecutor(new ChallengeCommand(this));

    //Insurance
    this.getCommand("insurance").setExecutor(new InsuranceCommand(this));

    this.getCommand("clan").setExecutor(new ClanCommand(clanManager, clanPrefixManager));

    
  }

  public void onDisable()
  {
    getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ptkEssentials] Disabled");

    // Save Balance
    getLogger().info("Saving balances...");
    balanceManager.saveAllBalances();
    clanStorage.saveClans(clanManager.getClans());
  }

  public ChallengeManager getChallengeManager() {
    return challengeManager;
  }

  public BalanceManager getBalanceManager(){
    return balanceManager;
  }

  public InsuranceManager getInsuranceManager() {
    return insuranceManager;
  }

  public CrateManager getCrateManager() {
    return crateManager;
  }



  public void logToFile(String message) {
    try (FileWriter fw = new FileWriter(logFile, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
        out.println(LocalDateTime.now() + " - " + message);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

  public void createBalanceFile() {
    balanceFile = new File(getDataFolder(), "balances.yml");

    if (!balanceFile.exists()) {
      getDataFolder().mkdirs();
      try {
        balanceFile.createNewFile();
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    balanceData = YamlConfiguration.loadConfiguration(balanceFile);
  }

  public void createShopFile() {
    shopFile = new File(getDataFolder(), "shop.yml");

    if (!shopFile.exists()) {
        getDataFolder().mkdirs();
        try {
            shopFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    shopData = YamlConfiguration.loadConfiguration(shopFile);
  }
}
