package com.untamedears.PrisonPearl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

class AltsList {
	private HashMap<UUID, UUID[]> altsHash;
	private boolean initialised = false;
	
	public AltsList() {
	}
	
	public void load(File file) {
		try {
			loadAlts(file);
			initialised = true;
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.getLogger().info("Failed to load file!");
			initialised = false;
		}
	}
	
	private void loadAlts(File file) throws IOException {
		altsHash = new HashMap<UUID, UUID[]>();
		FileInputStream fis;
		fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.length() > 1) {
				String parts[] = line.split(" ");
				UUID uuids[] = new UUID[parts.length];
				//String[] newString = new String[parts.length];
                //System.arraycopy(parts, 0, newString, 0, parts.length);
				for(int i = 0; i<parts.length; i++)
				{
					uuids[i] = UUID.fromString(parts[i]);
				}
                for (UUID uuid: uuids) {
                    altsHash.put(uuid, uuids);
                }
			}
		}
		
		br.close();
	}
	
	public UUID[] getAltsArray(UUID uuid){
		if (initialised && altsHash.containsKey(uuid)) {
			UUID[] uuids = altsHash.get(uuid);
			UUID[] alts = new UUID[uuids.length-1];
			for (int i = 0, j = 0; i < uuids.length; i++) {
				if (!uuids[i].equals(uuid)) {
					alts[j] = uuids[i];
					j++;
				}
			}
			return alts;
		}
		return new UUID[0];
	}
	
	public Set<UUID> getAllNames() {
		return altsHash.keySet();
	}
}
