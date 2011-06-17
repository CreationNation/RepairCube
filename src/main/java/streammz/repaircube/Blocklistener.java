package streammz.repaircube;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
import com.iConomy.iConomy;
import com.iConomy.system.Account;

import streammz.repaircube.CUBE.RepairCube;
import streammz.repaircube.CUBE.RepairCubes;

public class Blocklistener extends BlockListener {
	Core plugin;
	//constants
	int[] tools = {256, 257, 258, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 283, 284, 285, 286, 290, 291, 292, 293, 294, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 346};
	int[] woodtools = {268, 269, 270, 271, 290};
	int[] stonetools = {272, 273, 274, 275, 291};
	int[] irontools = {256, 257, 258, 267, 292, 306, 307, 308, 309};
	int[] goldtools = {283, 284, 285, 286, 294, 314, 315, 316, 317};
	int[] diatools = {276, 277, 278, 279, 293, 310, 311, 312, 313};
	int[] axes = {258, 271, 275, 279, 286};
	int[] picks = {257, 270, 274, 278, 285};
	int[] spades = {256, 269, 273, 277, 284};
	int[] swords = {267, 268, 272, 276, 283};
	int[] hoes = {290, 291, 292, 293, 294};
	int[] armor = {306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317};
	int[] chestplate = {307, 311, 315};
	int[] helmets = {306, 310, 314};
	int[] legs = {308, 312, 316};
	int[] boots = {309, 313, 317};
	
	public Blocklistener(Core plugin) {
		this.plugin = plugin;
	}
	
	private int getMaterialAmount(int id) {
		if (contains(axes,id)) return 2;
		if (contains(picks,id)) return 2;
		if (contains(spades,id)) return 1;
		if (contains(swords,id)) return 1;
		if (contains(hoes,id)) return 1;
		if (contains(chestplate,id)) return 6;
		if (contains(helmets,id)) return 3;
		if (contains(legs,id)) return 5;
		if (contains(boots,id)) return 2;
		return 1;
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		RepairCube c = Core.getRepairCube(event.getBlock());
		if (c == null) return;
		else {
			event.setCancelled(true);
			Player p = event.getPlayer();
			ItemStack hand = p.getItemInHand();
			Account money = this.getAccount(p.getName());

			int percent = getDurabilityPercent(hand);
			int mats = getMaterialAmount(hand.getTypeId());
			
			
			if (!contains(tools, hand.getTypeId())) {
				p.sendMessage("You can't repair that item");
				if (p.getName().equals(c.owner)) { p.sendMessage("If you meant to destroy the cube, use /rc remove"); } 
				return;
			}
			
			
			if (hand.getDurability() == 0) { p.sendMessage("That tool already is repaired"); return; }
			
			//get price of repair
			double price = 0;
			if (contains(axes, hand.getTypeId())) price = c.axePrice;
			if (contains(picks, hand.getTypeId())) price = c.pickPrice;
			if (contains(hoes, hand.getTypeId())) price = c.hoePrice;
			if (contains(swords, hand.getTypeId())) price = c.swordPrice;
			if (contains(spades, hand.getTypeId())) price = c.spadePrice;
			if (contains(chestplate, hand.getTypeId())) price = c.bodyPrice;
			if (contains(helmets, hand.getTypeId())) price = c.helmPrice;
			if (contains(legs, hand.getTypeId())) price = c.legsPrice;
			if (contains(boots, hand.getTypeId())) price = c.feetPrice;
			
			if (money.getHoldings().hasUnder(price)) {
				p.sendMessage("You don't have " + iConomy.format(price) + " to repair your tool");
			}
			if (c.currentMats < mats) { p.sendMessage("The owner is out of materials"); return; }
			
			
			if (event.getBlock().getTypeId() == 5) {
				if (contains(woodtools, hand.getTypeId())) {
					hand.setDurability((short)0);
					c.currentMats -= mats;
				} else {
					p.sendMessage("You cant repair this tool with this RepairCube");
					return;
				}
			}
			if (event.getBlock().getTypeId() == 4) {
				if (contains(stonetools, hand.getTypeId())) {
					hand.setDurability((short)0);
					c.currentMats -= mats;
				} else {
					p.sendMessage("You cant repair this tool with this RepairCube");
					return;
				}
			}
			if (event.getBlock().getTypeId() == 42) {
				if (contains(irontools, hand.getTypeId())) {
					hand.setDurability((short)0);
					c.currentMats -= mats;
				} else {
					p.sendMessage("You cant repair this tool with this RepairCube");
					return;
				}
			}
			if (event.getBlock().getTypeId() == 41) {
				if (contains(goldtools, hand.getTypeId())) {
					hand.setDurability((short)0);
					c.currentMats -= mats;
				} else {
					p.sendMessage("You cant repair this tool with this RepairCube");
					return;
				}
			}
			if (event.getBlock().getTypeId() == 57) {
				if (contains(diatools, hand.getTypeId())) {
					hand.setDurability((short)0);
					c.currentMats -= mats;
				} else {
					p.sendMessage("You cant repair this tool with this RepairCube");
					return;
				}
			}
			
			this.getAccount(p.getName()).getHoldings().subtract(price);
			
			p.sendMessage(ChatColor.AQUA + "You've repaired your " + hand.getType().toString() + " for " + iConomy.format(price));
			p.sendMessage(ChatColor.AQUA + "It costed " + mats + " materials for the owner");
			RepairCubes.Save();
		}
		
		
	}
	
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled()) return;
		RepairCube c = Core.getRepairCube(event.getBlock());
		if (c == null) { return; }
		else {
			Player p = event.getPlayer();
			ItemStack hand = p.getItemInHand();
			
			if (!contains(tools, hand.getTypeId())) { p.sendMessage("Impossible to repair that tool"); return; }
			

			double price = 0;
			if (contains(axes, hand.getTypeId())) price = c.axePrice;
			if (contains(picks, hand.getTypeId())) price = c.pickPrice;
			if (contains(hoes, hand.getTypeId())) price = c.hoePrice;
			if (contains(swords, hand.getTypeId())) price = c.swordPrice;
			if (contains(spades, hand.getTypeId())) price = c.spadePrice;
			if (contains(chestplate, hand.getTypeId())) price = c.bodyPrice;
			if (contains(helmets, hand.getTypeId())) price = c.helmPrice;
			if (contains(legs, hand.getTypeId())) price = c.legsPrice;
			if (contains(boots, hand.getTypeId())) price = c.feetPrice;
			
			p.sendMessage(ChatColor.GREEN + "Are you sure that you want to repair");
			p.sendMessage(ChatColor.GREEN + "your " + hand.getType().toString() + " for " + iConomy.format(price) + "?");
		}
		
	}
	
	private boolean contains(int[] list, int check) {
		for (int i : list) {
			if (i == check) { return true; }
		}
		return false;
	}
	private int getDurabilityPercent(ItemStack s) {
		int realdura = s.getType().getMaxDurability() - s.getDurability();
		System.out.println("realdura = " + realdura + "  max = " + s.getType().getMaxDurability());
		int percent = (int) (100D / (double)s.getType().getMaxDurability() * (double)realdura);
		System.out.println("percent = " + percent);
		return percent;
	}
	private Account getAccount(String name) {
		return iConomy.getAccount(name);
	}
}
