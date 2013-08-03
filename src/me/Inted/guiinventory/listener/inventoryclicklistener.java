package me.Inted.guiinventory.listener;


import java.io.File;
import java.io.IOException;

import me.Inted.guiinventory.guiinventory;
import me.Inted.guiinventory.main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;



public class inventoryclicklistener implements Listener{
	
	 main plugin;
	 File file;
	 YamlConfiguration cfg;
	
	
	public inventoryclicklistener(main plugin){
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {	
		guiinventory Inventar = null;
		for(guiinventory inv : plugin.inventories){
			if(inv.getInventory().getTitle().equalsIgnoreCase(event.getInventory().getTitle())){
				Inventar = inv;
			}
		}
		if(Inventar == null){
			return;
		}
		if(event.getCurrentItem() == null){
			return;
		}
		if(event.getCurrentItem().getType() == Material.AIR)
        {
        	return;
        }
		
		Inventory inv = Inventar.getInventory();
		if(!inv.contains(event.getCurrentItem())){
			return;
		}
		event.setCancelled(true);
		file = new File(plugin.getDataFolder() + "/inventories",Inventar.getName() + ".yml");
		cfg = YamlConfiguration.loadConfiguration(file);
		if(!file.exists())
		{
		cfg.addDefault(Inventar.getName() + ".slot1.item", "");
		cfg.addDefault(Inventar.getName() + ".slot1.action", "");
		cfg.options().copyDefaults(true);
		saveConfig();
		}
		if(cfg.getString(Inventar.getName() + ".slot" + event.getSlot() + ".action").equalsIgnoreCase(""))
		{
			return;
		}
		
		String[] todo = cfg.getString(Inventar.getName() + ".slot" + event.getSlot() + ".action").split(";");
		for(String task : todo)
		{
			String[] action = task.split(":");
			if(action.length <2){
			return;
			}
		
			if(action[0].equalsIgnoreCase("command")){
			Player p = ((Player)event.getWhoClicked());
			plugin.getServer().dispatchCommand((Player)event.getWhoClicked(), action[1].replaceAll("@p",p.getName()).trim().substring(1));
			}else if(action[0].equalsIgnoreCase("console")){
				Player p = ((Player)event.getWhoClicked());
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action[1].replaceAll("@p",p.getName()).trim().substring(1));
				}
			
			else if(action[0].equalsIgnoreCase("openinv")){
				for(final guiinventory gi : plugin.inventories){
					if(gi.getName().equalsIgnoreCase(action[1].trim())){
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run() {
							event.getWhoClicked().openInventory(gi.getInventory());
							}
							}, 1L);
					}
				}
				
			}else if(action[0].equalsIgnoreCase("msg")){
				Player p = ((Player)event.getWhoClicked());
				String text = action[1].replaceAll("@p",p.getName()).trim();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
			}else if(action[0].equalsIgnoreCase("broadcast")){
				plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', replace(action[1].trim(),(Player)event.getWhoClicked())));
			}
			else{
			event.getWhoClicked().closeInventory();
			}
		}
	}
	
	public void saveConfig(){
		try {
			cfg.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String replace (String text, Player p){
		return ChatColor.translateAlternateColorCodes('&', text.replaceAll("@p", p.getName()));
	}
	
	
	
}
