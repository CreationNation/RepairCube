package streammz.repaircube;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.iConomy.*;

import streammz.repaircube.CUBE.RepairCube;
import streammz.repaircube.CUBE.RepairCubes;

public class Core extends JavaPlugin {
	public static ArrayList<RepairCube> cubes = new ArrayList<RepairCube>();
	
	public static PermissionHandler permissionHandler; //permissions
	
	BlockListener blockListener = new Blocklistener(this);
	
	public iConomy iConomy = null;
	
	public void onDisable() {
		RepairCubes.Save();
	}

	public void onEnable() {
		RepairCubes.Load();
		setupPermissions();
		
		//Listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Low, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
		//iConomy
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, new Serverlistener(this), Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, new Serverlistener(this), Event.Priority.Monitor, this);
		
		PluginDescriptionFile pdfFile = getDescription();
	    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName().toLowerCase();
		Player p = (Player)sender;
		
		if (cmd.equals("rc")) {
			//  /rc      = /rc info
			//  /rc info = info about the cube
			//  /rc create = creates cube
			//  /rc remove = removes cube if its yours, or got permissions for it
			//  /rc price <p(ick) / a(xe) / h(oe) / sw(ord) / s(pade)> price
			if (args.length == 0) { return false; }
			if (args[0].equalsIgnoreCase("create")) {
				if (getMaximumCubes(p) == 0) { p.sendMessage("You can't create repair cubes."); }
				Block target = p.getTargetBlock(null, 5);
				int id = target.getTypeId();
				if (id == 4 || id == 5 || id == 41 || id == 42 || id == 57) {
					if (getTotalCubes(p)+1 > getMaximumCubes(p)) { p.sendMessage("You've reached your limit of repair cubes"); return true; }
					cubes.add(new RepairCube(p.getName(), target));
					p.sendMessage("Repair cube created");
					RepairCubes.Save();
				} else {
					p.sendMessage("That block can't be used as an repair cube.");
				}
			}
			else if (args[0].equalsIgnoreCase("info")) {
				Block target = p.getTargetBlock(null, 5);
				RepairCube c = getRepairCube(target);
				int id = target.getTypeId();
				if (c == null) { p.sendMessage("That block isn't an Repair cube."); return true; }
				p.sendMessage(ChatColor.GREEN + "Owner: " + ChatColor.GOLD + c.owner);
				p.sendMessage(ChatColor.GREEN + "Material left: " + ChatColor.GOLD + c.currentMats);
				p.sendMessage(ChatColor.GREEN + "===========" + ChatColor.GOLD + "Prices" + ChatColor.GREEN +  "=================");
				p.sendMessage(ChatColor.GREEN + "Axe: " + ChatColor.GOLD + c.axePrice + ChatColor.GREEN + "   Pickaxe: " + ChatColor.GOLD + c.pickPrice + ChatColor.GREEN + "   Spade: " + ChatColor.GOLD + c.spadePrice);
				p.sendMessage(ChatColor.GREEN + "Sword: " + ChatColor.GOLD + c.swordPrice + ChatColor.GREEN + "   Hoe: " + ChatColor.GOLD + c.hoePrice);
				if (id == 41 || id == 42 || id == 57) { 
					p.sendMessage(ChatColor.GREEN + "ChestPlate: " + ChatColor.GOLD + c.bodyPrice + ChatColor.GREEN + "   Helmet: " + ChatColor.GOLD + c.helmPrice);
					p.sendMessage(ChatColor.GREEN + "Legs: " + ChatColor.GOLD + c.legsPrice + ChatColor.GREEN + "   Boots: " + ChatColor.GOLD + c.feetPrice);
				}
			}
			else if (args[0].equalsIgnoreCase("remove")) {
				Block target = p.getTargetBlock(null, 5);
				RepairCube c = getRepairCube(target);

				if (c == null) { p.sendMessage("That block isn't an Repair cube."); return true; }
				
				if (!c.owner.equals(p.getName()) && !hasPermission(p, "repaircube.remove")) {
					p.sendMessage("That isn't your repair cube"); 
					return true;
				}

				Material mat = Material.ARROW;
				if (target.getTypeId() == 5) mat = Material.WOOD;
				if (target.getTypeId() == 4) mat = Material.COBBLESTONE;
				if (target.getTypeId() == 42) mat = Material.IRON_INGOT;
				if (target.getTypeId() == 41) mat = Material.GOLD_INGOT;
				if (target.getTypeId() == 57) mat = Material.DIAMOND;
				
				while (c.currentMats > 0) {
					int cur = c.currentMats;
					int newnum = 0;
					
					if (cur > 64) {
						newnum = 64;
					} else {
						newnum = cur;
					}
					if (newnum > 10) newnum = 10;
					
					c.block.getWorld().dropItemNaturally(c.block.getLocation(), new ItemStack(mat, newnum));
					c.currentMats -= newnum;
				}
				cubes.remove(c);
				p.sendMessage("Cube succesfully removed");
				RepairCubes.Save();
			}
			else if (args[0].equalsIgnoreCase("fill")) {
				Block target = p.getTargetBlock(null, 5);
				RepairCube c = getRepairCube(target);

				if (c == null) { p.sendMessage("That block isn't an Repair cube."); return true; }
				if (!c.owner.equals(p.getName())) { p.sendMessage("You aren't the owner of that cube"); return true; }
				
				if (args.length < 2) {
					p.sendMessage("Please specify how many you want to add");
					return true;
				}
				int i = 0;
				try {
					i = Integer.parseInt(args[1]);
				} catch (Exception e) {
					p.sendMessage("That isn't an prober number >:(");
					return true;
				}
				if (i < 0) { 
					p.sendMessage("That isn't an prober number >:(");
					return true;
				}
				
				Material mat = Material.AIR;
				if (target.getTypeId() == 5) mat = Material.WOOD;
				if (target.getTypeId() == 4) mat = Material.COBBLESTONE;
				if (target.getTypeId() == 42) mat = Material.IRON_INGOT;
				if (target.getTypeId() == 41) mat = Material.GOLD_INGOT;
				if (target.getTypeId() == 57) mat = Material.DIAMOND;
				
				int max = getMaximumFill(p);
				if (c.currentMats + i > max) {
					i = max-c.currentMats;
					p.sendMessage("More materials then limited, rounded to " + i);
				}
				
				Inventory inv = p.getInventory();
				
				if (!inv.contains(mat, i)) { 
					int amount = 0;
					for (ItemStack s : inv.getContents()) {
						if (s == null) continue;
						if (s.getType() != mat) continue;
						amount += s.getAmount();
					}
					i = amount;
					p.sendMessage("More materials then avaible in inventory, rounded to " + amount);
				}
				
				c.currentMats += i;
				p.sendMessage("Added " + i + " " + mat.toString() + "'s to the Repair Cube");
				while (i > 0) {
					ItemStack rem = inv.getItem(inv.first(mat));
					if (rem.getAmount() <= i) { i -= rem.getAmount(); inv.clear(inv.first(mat)); }
					else { rem.setAmount(rem.getAmount() - i); i = 0; }
				}
				
				RepairCubes.Save();
				
				
			}
			else if (args[0].equalsIgnoreCase("price")) {
				if (args.length < 3) {
					p.sendMessage("/rc price <item shortcut> <amount>");
					p.sendMessage(ChatColor.GREEN + "===========================");
					p.sendMessage(ChatColor.GREEN + "=" + ChatColor.WHITE + " (a)xe (p)ickaxe (sp)ade      " + ChatColor.GREEN + "=");
					p.sendMessage(ChatColor.GREEN + "=" + ChatColor.WHITE + "  (sw)ord (h)oe                  " + ChatColor.GREEN + "=");
					p.sendMessage(ChatColor.GREEN + "= = = = = = = = == = = = = = = =");
					p.sendMessage(ChatColor.GREEN + "=" + ChatColor.WHITE + "  (ch)estplate (he)lmet         " + ChatColor.GREEN + "=");
					p.sendMessage(ChatColor.GREEN + "=" + ChatColor.WHITE + "  (l)egs (b)oots                 " + ChatColor.GREEN + "=");
					p.sendMessage(ChatColor.GREEN + "===========================");
				} else {
					Block target = p.getTargetBlock(null, 5);
					RepairCube c = getRepairCube(target);

					if (c == null) { p.sendMessage("That block isn't an Repair cube."); return true; }
					if (!c.owner.equals(p.getName())) { p.sendMessage("You aren't the owner of this cube."); return true; }
					
					double amount = 0;
					try {
						amount = Double.parseDouble(args[2]);
					} catch (NumberFormatException e) { p.sendMessage("Not an number"); return true; }
					if (amount < 0) { p.sendMessage("Can't be negative"); return true; }
					
					if (args[1].startsWith("a")) {
						c.axePrice = amount;
						p.sendMessage("Changed the price of axes to " + amount);
					} else if (args[1].startsWith("p")) {
						c.pickPrice = amount;
						p.sendMessage("Changed the price of pickaxes to " + amount);
					} else if (args[1].startsWith("sp")) {
						c.spadePrice = amount;
						p.sendMessage("Changed the price of spades to " + amount);
					} else if (args[1].startsWith("sw")) {
						c.swordPrice = amount;
						p.sendMessage("Changed the price of swords to " + amount);
					} else if (args[1].startsWith("h")) {
						c.hoePrice = amount;
						p.sendMessage("Changed the price of hoes to " + amount);
					} else if (args[1].startsWith("ch")) {
						c.bodyPrice = amount;
						p.sendMessage("Changed the price of chestplates to " + amount);
					} else if (args[1].startsWith("he")) {
						c.helmPrice = amount;
						p.sendMessage("Changed the price of helmets to " + amount);
					} else if (args[1].startsWith("l")) {
						c.legsPrice = amount;
						p.sendMessage("Changed the price of legs to " + amount);
					} else if (args[1].startsWith("b")) {
						c.feetPrice = amount;
						p.sendMessage("Changed the price of boots to " + amount);
					}
					RepairCubes.Save();
				}
			}
		}
		
		
		return true;
	}
	
	
	private void setupPermissions() {
	    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (permissionHandler == null) {
	    	if (permissionsPlugin != null) {
	    		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	    		System.out.println("[Tetris] Permission found");
         	} else {
	     		System.out.println("[Tetris] No permissions found");
	     	}
	     }
	}
	public static boolean hasPermission(Player p, String node) {
		if (p.isOp()) { return true; }
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Permissions")) { return false; }
		if (permissionHandler.has(p, node)) { return true; }
		return false;
	}
	
	public static RepairCube getRepairCube(Block b) {
		for (int x=0; x<cubes.size(); x++) {
			RepairCube c = cubes.get(x);
			if (c == null) { break; }
			if (c.block.getX() == b.getX() &&
				c.block.getY() == b.getY() && 
				c.block.getZ() == b.getZ() &&
				c.block.getWorld() == b.getWorld()) {
				return c;
			}
		}
		return null;
	}
	
	public static int getMaximumFill(Player p) {
		if (hasPermission(p, "repaircube.level2")) { return 100; }
		if (hasPermission(p, "repaircube.level1")) { return 50; }
		return 0;
	}
	public static int getMaximumCubes(Player p) {
		if (hasPermission(p, "repaircube.level2")) { return 4; }
		if (hasPermission(p, "repaircube.level1")) { return 2; }
		return 0;
	}
	
	public static int getTotalCubes(Player p) {
		int total = 0;
		for (int x=0; x<cubes.size(); x++) {
			RepairCube c = cubes.get(x);
			if (c.owner.equals(p.getName())) total++;
		}
		return total;
	}
	


}