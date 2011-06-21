package streammz.repaircube.CUBE;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class RepairCube {
	public String owner;
	public Block block;
	
	public double axePrice = 100.00;
	public double pickPrice = 100.00;
	public double hoePrice = 100.00;
	public double swordPrice = 100.00;
	public double spadePrice = 100.00;
	public double bodyPrice = 100.00;
	public double helmPrice = 100.00;
	public double legsPrice = 100.00;
	public double feetPrice = 100.00;
	
	public Material expectedType;
	
	public int currentMats = 0;
	
	public RepairCube(String player, Block block) {
		this.owner = player;
		this.block = block;
		this.expectedType = block.getType();
	}
	
	public RepairCube(String player, Block block, double axe, double pick, double hoe, double sword, double spade, double body, double helm, double legs, double feet, int mats) {
		this.owner = player;
		this.block = block;
		this.axePrice = axe;
		this.pickPrice = pick;
		this.hoePrice = hoe;
		this.swordPrice = sword;
		this.spadePrice = spade;
		this.currentMats = mats;
		this.bodyPrice = body;
		this.helmPrice = helm;
		this.legsPrice = legs;
		this.feetPrice = feet;
		this.expectedType = block.getType();
	}
	
	public boolean check() {
		if (block.getType() != this.expectedType) return false;
		return true;
	}
	
	@Override
	public String toString() {

		//owner:world:x:y:z:axe:pick:hoe:sword:spade:helm:body:legs:feet:mats
		return owner + ":" + block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + axePrice + ":" + pickPrice + ":" + hoePrice + ":" + swordPrice + ":" + spadePrice
		 + ":" + helmPrice + ":" + bodyPrice + ":" + legsPrice + ":" + feetPrice + ":" + currentMats;
	}
}
