package com.gmail.ddmytriw.RegenPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class Regenerator implements Runnable, Listener {	
	private List<Block> regenBlockList = new ArrayList<Block>();
	
	private String worldName;
	private ArchetypeWorld archetype_world;
	private PlayerPlacedWorld pp_world;

	static String BLOCK_LIST_FILE_FOLDER = System.getProperty("user.dir") + "\\plugins\\";
	static String BLOCK_LIST_FILE = BLOCK_LIST_FILE_FOLDER + "blockLists.xml";
	
	public Regenerator(String world_name) {
		super();
		worldName = world_name;
	}

	@Override
	public void run() {
		regenTask();
	}

	public void onEnable()
	{
		RegenPlugin.get().getLogger().info("Regenerator.onEnable()");
		
		RegenPlugin.get().getServer().getPluginManager().registerEvents((Listener) this, RegenPlugin.get());
		
		archetype_world = new ArchetypeWorld(worldName);
		pp_world = new PlayerPlacedWorld(worldName);
		
		loadBlockList();
	}

	public void onDisable() 
	{
		RegenPlugin.get().getLogger().info("Regenerator.onDisable()");
		
		HandlerList.unregisterAll((Listener)this);
		
		saveBlockList();
		
	}

	public void regenTask()
	{
		//getLogger().info(this.getName() + ".RegenTask()");

		if(!regenBlockList.isEmpty())
		{
			//sort by Y, this will move lowest blocks in terrain to front of regen 'queue'
			Collections.sort(regenBlockList, new lowestFirstComparator());

			ListIterator<Block> iter = regenBlockList.listIterator();
			while (iter.hasNext()) {
				Block block = (Block) iter.next();
				if(!pp_world.isBlockPlacedByPlayer(block)){
					regenBlockList.remove(block);
					regenBlock(block);
					break;					
				}
			}	
		}
	}
	
	public void saveBlockList()
	{
		RegenPlugin.get().getLogger().info(RegenPlugin.get().getName() + ".SaveBlockList()");
		
		//TODO: Save block list into SQL DB
		
		//We need a Document
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
        Document doc = docBuilder.newDocument();

        ////////////////////////
        //Creating the XML tree

        //create the root element and add it to the document
        Element root = doc.createElement("regenBlockList");
        doc.appendChild(root);

        ListIterator<Block> iter = regenBlockList.listIterator();
		while (iter.hasNext()) {
			Block block = (Block) iter.next();

	        //create child element, add an attribute, and add to root
	        Element block_element = doc.createElement("block");
	        block_element.setAttribute("x", String.valueOf(block.getX()));
	        block_element.setAttribute("y", String.valueOf(block.getY()));
	        block_element.setAttribute("z", String.valueOf(block.getZ()));
	        root.appendChild(block_element);			
		}

        /////////////////
        //Output the XML
        //set up a transformer
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans;
		try {
			trans = transfac.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return;
		}
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        //create string from xml tree
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        try {
			trans.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
			return;
		}
        String xmlString = sw.toString();

        //print xml
        System.out.println("Here's the xml:\n\n" + xmlString);
        
        //write to .xml file		
		File file = new File(BLOCK_LIST_FILE);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(BLOCK_LIST_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			fos.write(xmlString.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}	
		
	}
	
	public void loadBlockList()
	{
		RegenPlugin.get().getLogger().info(RegenPlugin.get().getName() + ".LoadBlockList() fileName: " + BLOCK_LIST_FILE);
		
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		
		File file = new File(BLOCK_LIST_FILE);
		if(!file.exists()){
			return;
		}
		
		FileInputStream fis;
		try {
			fis = new FileInputStream(BLOCK_LIST_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		Document document;
		try {
			document = docBuilder.parse(fis);
			
			Element regen_block_list_element = document.getDocumentElement();
			NodeList regen_block_list = regen_block_list_element.getChildNodes();
			for(int index = 0; index < regen_block_list.getLength(); index++)
			{
				Node node = regen_block_list.item(index); 
				if(node instanceof Element)
				{
					//a child element to process
					Element block_element = (Element) node;
					World world = RegenPlugin.get().getServer().getWorld(worldName);
					Block block = world.getBlockAt(Integer.parseInt(block_element.getAttribute("x"))
													,Integer.parseInt(block_element.getAttribute("y"))
													,Integer.parseInt(block_element.getAttribute("z")));
					
				    this.addBlockToRegenList(block);
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	    
		file.delete();	    
	}

	//lowest block first
	public class lowestFirstComparator implements Comparator<Block>{
		@Override
	    public int compare(Block block1, Block block2) {
	        return block1.getY() - block2.getY();
	    }
	}

	public void onBlockRemoved(Block block){
		RegenPlugin.get().getLogger().info("onBlockRemoved: " + block.getLocation().toString());
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(pp_world.isBlockPlacedByPlayer(block)){
			pp_world.onBlockRemoved(block);
		}
		else
		{	
			addBlockToRegenList(block);
		}
	}
	
	public void onBlockRemoved(List<Block> block_list){
		ListIterator<Block> iter = block_list.listIterator();
		while (iter.hasNext()) {
			Block block = (Block) iter.next();
			onBlockRemoved(block);
		}		
	}

	private void addBlockToRegenList(Block block) {
		RegenPlugin.get().getLogger().info("addBlockToRegenList - " + block.getLocation().toString());
		regenBlockList.add(block);	
	}
	
	private void regenBlock(Block block)
	{
		//plugin.getLogger().info("regenBlock: " + block.getLocation().toString() + " id:" + block.getTypeId());
		Block archetype_block = archetype_world.getBlockAt(block.getLocation());
		//plugin.getLogger().info("archetype_block: " + archetype_block.getLocation().toString() + " id:" + archetype_block.getTypeId());
		block.setTypeId(archetype_block.getTypeId());
	}	
	
	
	public void onBlockPlacedByPlayer(Block block)
	{
		RegenPlugin.get().getLogger().info("onBlockPlacedByPlayer - " + block.getLocation().toString());

		if(block.getTypeId() != archetype_world.getBlockAt(block.getLocation()).getTypeId()){
			pp_world.onBlockPlacedByPlayer(block);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld().getName().compareTo(worldName) != 0) return;
        
		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();
		getLogger().info(event.getEventName() + ": " + player.getDisplayName() + " placed a block id: " + block.getTypeId() + " at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		
		onBlockPlacedByPlayer(block);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockBreakEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld().getName().compareTo(worldName) != 0) return;
        
		Block block = event.getBlock();
		getLogger().info(event.getEventName() + ": "
		+ event.getPlayer().getDisplayName()
		+ " broke a block id: "
		+ block.getTypeId()
		+ " at x:" + block.getX()
		+ " y:" + block.getY()
		+ " z:" + block.getZ()
		+ " with:" + event.getPlayer().getItemInHand().toString());
		
		onBlockRemoved(block);
		
		block.breakNaturally(event.getPlayer().getItemInHand());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(EntityExplodeEvent event){
        if (event.isCancelled()) return;
        if (event.getEntity().getWorld().getName().compareTo(worldName) != 0) return;

		List<Block> block_list = event.blockList();
		if(block_list.size() > 0)
		{
			getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());
			onBlockRemoved(block_list);
		}

		event.setYield(0); //TODO: remove this!
	}
	
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(BlockDamageEvent event){
//        if (event.isCancelled()) return;
//    if (event.getBlock().getWorld().getName().compareTo(worldName) != 0) return;
//        
//		getLogger().info(event.getEventName());
//	}
//		
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(BlockFadeEvent event){
//        if (event.isCancelled()) return;
//    if (event.getBlock().getWorld().getName().compareTo(worldName) != 0) return;
//        
//		getLogger().info(event.getEventName());
//		event.getNewState().
//	}
//		
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(EntityChangeBlockEvent event){
//        if (event.isCancelled()) return;
//    if (event.getBlock().getWorld().getName().compareTo(worldName) != 0) return;
//        
//		getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());
//	}
//
	private Logger getLogger() {
		return RegenPlugin.get().getLogger();
	}

	public void regenAll() {
		RegenPlugin.get().getLogger().info("Regenerator.regenAll()");

		if(!regenBlockList.isEmpty()){
			ListIterator<Block> iter = regenBlockList.listIterator();
			
			while (iter.hasNext()) {
				Block block = (Block) iter.next();
				regenBlock(block);	
			}
			regenBlockList.clear();
		}
	}
}
