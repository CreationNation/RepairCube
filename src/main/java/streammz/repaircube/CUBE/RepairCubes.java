package streammz.repaircube.CUBE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import streammz.repaircube.Core;

public class RepairCubes {
	private static File file = new File("plugins/repaircubes/data.txt");
	private static File folder = new File("plugins/repaircubes/");

	//returns true if loaded all he could find
	//returns false if errors occured
	public static boolean Load() {
		if (!checkFile(file, false)) { return true; }
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String s;
			
			while ((s = in.readLine()) != null) {
				try {
					if (s.startsWith("#")) continue;
					
					String[] split = s.split(":");
					String owner = split[0];
					String world = split[1];
					String xs = split[2];
					String ys = split[3];
					String zs = split[4];
					String axe = split[5];
					String pick = split[6];
					String hoe = split[7];
					String sword = split[8];
					String spade = split[9];
					String helms = split[10];
					String bodys = split[11];
					String legss = split[12];
					String feets = split[13];
					String mats = split[14];
					// get block out of world/x/y/z
					World w = Bukkit.getServer().getWorld(world);
					int x = Integer.parseInt(xs);
					int y = Integer.parseInt(ys);
					int z = Integer.parseInt(zs);
					Block b = w.getBlockAt(x,y,z);
					// translate string price into double price
					double axePrice = Double.parseDouble(axe);
					double pickPrice = Double.parseDouble(pick);
					double hoePrice = Double.parseDouble(hoe);
					double swordPrice = Double.parseDouble(sword);
					double spadePrice = Double.parseDouble(spade);
					double helm = Double.parseDouble(helms);
					double body = Double.parseDouble(bodys);
					double legs = Double.parseDouble(legss);
					double feet = Double.parseDouble(feets);
					//get current mats
					int CurrentMats = Integer.parseInt(mats);
					// add repair cube
					Core.cubes.add(new RepairCube(owner,b,axePrice,pickPrice,hoePrice,swordPrice,spadePrice, body, helm, legs, feet, CurrentMats));
					
					
				} catch (NumberFormatException e) {
					continue;
				} catch (NullPointerException e) {
					continue;
				}
			}
			
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	public static boolean Save() {
		if (!checkFile(file, true)) { return false; }
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (int i=0; i<Core.cubes.size(); i++) {
				RepairCube b = Core.cubes.get(i);
				write(out, b.toString());
			}
			
			out.close();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	private static boolean checkFile(File file, boolean shouldCreate) {
		if (!file.exists()) {
			if (shouldCreate) {
				try {
					folder.mkdirs();
					file.createNewFile();
					return true;
				} catch (IOException e) {
					return false;
				}
			}
		}
		return true;
	}
	private static void write(BufferedWriter out, String s) throws IOException {
		out.write(s);
		out.newLine();
	}
}
