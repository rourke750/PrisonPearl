package com.untamedears.PrisonPearl;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PrisonPearl {
	private final short id;
	private final String imprisonedname;
    private String motd = "";
	private Player player;
	private Item item;
	private Location blocklocation;
	private boolean pearlOnCursor = false;
	private long lastMoved = 0;
	
	public PrisonPearl(short id, String imprisonedname, Player holderplayer) {
		this.id = id;
		this.imprisonedname = imprisonedname;
		this.player = holderplayer;
	}
	
    public PrisonPearl(short id, String imprisonedname, Location blocklocation) {
		this.id = id;
		this.imprisonedname = imprisonedname;
		this.blocklocation = blocklocation;
	}
	
	public static PrisonPearl makeFromLocation(short id, String imprisonedname, Location loc) {
		if (imprisonedname == null || loc == null)
			return null;
		BlockState bs = loc.getBlock().getState();
		if (bs instanceof InventoryHolder)
			return new PrisonPearl(id, imprisonedname, loc);
		else
			return null;
	}
	
	public short getID() {
		return id;
	}
	
	public String getImprisonedName() {
		return imprisonedname;
	}
	
	public Player getImprisonedPlayer() {
		return Bukkit.getPlayerExact(imprisonedname);
	}

	public OfflinePlayer getImprisonedOfflinePlayer() {
		return Bukkit.getOfflinePlayer(imprisonedname);
	}

	public Player getHolderPlayer() {
		return player;
	}
	
	public BlockState getHolderBlockState() {
		if (blocklocation != null)
			return blocklocation.getBlock().getState();
		else
			return null;
	}
	
	public Item getHolderItem() {
		return item;
	}
	
    public String getHolderName() {
		if (player != null) {
			return player.getName();
		} else if (item != null) {
			return "nobody";
		} else if (blocklocation != null) {
			switch (getHolderBlockState().getType()) {
			case CHEST:
			case TRAPPED_CHEST:
				return "a chest";
			case FURNACE:
				return "a furnace";
			case BREWING_STAND:
				return "a brewing stand";
			case DISPENSER:
				return "a dispenser";
			case ITEM_FRAME:
				return "a wall frame";
			case DROPPER:
				return "a dropper";
			case HOPPER:
				return "a hopper";
			default:
				PrisonPearlPlugin.info("PrisonPearl " + id + " is inside an unknown block " + getHolderBlockState().getType().toString());
				return "an unknown block"; 
			}
		} else {
			PrisonPearlPlugin.info("PrisonPearl " + id + " has no player, item, nor location");
			return "unknown"; 
		}
	}
	
	public Location getLocation() {
		if (player != null) {
			return player.getLocation().add(0, -.5, 0);
		} else if (item != null) {
			return item.getLocation();
		} else if (blocklocation != null) {
			return blocklocation;
		} else {
			throw new RuntimeException("PrisonPearl " + id + " has no player, item, nor location");
		}
	}
	
	public String describeLocation() {
		Location loc = getLocation();
		Vector vec = loc.toVector();
		String str = loc.getWorld().getName() + " " + vec.getBlockX() + " " + vec.getBlockY() + " " + vec.getBlockZ();
		
		return "held by " + getHolderName() + " at " + str;
	}

    public boolean verifyLocation() {
		// Return true if the pearl exists in a valid location
		if (System.currentTimeMillis() - this.lastMoved < 2000) {
			// The pearl was recently moved. Due to a race condition, this exists to
			//  prevent players from spamming /ppl to get free when a pearl is moved.
			return true;
		}
		if (item != null) {
			Chunk chunk = item.getLocation().getChunk();
			for (Entity entity : chunk.getEntities()) {
				if (entity == item)
					return true;
			}
			PrisonPearlPlugin.info(String.format(
				"PP (%d, %s) failed verification: On ground not in chunk",
				id, imprisonedname));
			return false;
		} else {
			Inventory inv;
			if (player != null) {
				if (!player.isOnline()) {
					PrisonPearlPlugin.info(String.format(
						"PP (%d, %s) failed verification: Jailor %s not online",
						id, imprisonedname, player.getName()));
					return false;
				}
				if (pearlOnCursor) {
					return true;
				}
				ItemStack cursoritem = player.getItemOnCursor();
				if (cursoritem.getType() == Material.ENDER_PEARL && cursoritem.getDurability() == id)
					return true;
				inv = player.getInventory();
			} else if (blocklocation != null) {
				BlockState bs = getHolderBlockState();
				if (bs == null) {
					PrisonPearlPlugin.info(String.format(
						"PP (%d, %s) failed verification: BlockState is null",
						id, imprisonedname));
					return false;
				}
				if (!(bs instanceof InventoryHolder)) {
					Location bsLoc = bs.getLocation();
					PrisonPearlPlugin.info(String.format(
						"PP (%d, %s) failed verification: %s not inventory at (%d,%d,%d)",
						id, imprisonedname, bs.getType().toString(),
						bsLoc.getBlockX(), bsLoc.getBlockY(), bsLoc.getBlockZ()));
					return false;
				}
				inv = ((InventoryHolder)bs).getInventory();
				for (HumanEntity viewer : inv.getViewers()) {
					ItemStack cursoritem = viewer.getItemOnCursor();
					if (cursoritem.getType() == Material.ENDER_PEARL && cursoritem.getDurability() == id)
						return true;
				}
			} else {
				PrisonPearlPlugin.info(String.format(
					"PP (%d, %s) failed verification: Has no player, item, nor location",
					id, imprisonedname));
				return false;
			}
			for (ItemStack item : inv.all(Material.ENDER_PEARL).values()) {
				if (item.getDurability() == id)
					return true;
			}
			PrisonPearlPlugin.info(String.format(
				"PP (%d, %s) failed verification: Not in inventory",
				id, imprisonedname));
			return false;
		}
	}

	public void setHolder(Player player) {
		this.player = player;
		item = null;
		blocklocation = null;
		pearlOnCursor = false;
	}

	public void setCursorHolder(Player player) {
		this.player = player;
		item = null;
		blocklocation = null;
		pearlOnCursor = true;
	}

	public <ItemBlock extends BlockState & InventoryHolder> void setHolder(ItemBlock blockstate) {
		player = null;
		item = null;
		blocklocation = blockstate.getLocation();
		pearlOnCursor = false;
	}
	
	public void setHolder(Item item) {
		player = null;
		this.item = item;
		blocklocation = null;
		pearlOnCursor = false;
	}

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public void markMove() {
        this.lastMoved = System.currentTimeMillis();
    }
}
