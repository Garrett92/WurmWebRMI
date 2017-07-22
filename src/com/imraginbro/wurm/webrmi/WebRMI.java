package com.imraginbro.wurm.webrmi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.shared.exceptions.WurmServerException;

public final class WebRMI {

	static WebInterface iface;
	static String addr = "127.0.0.1";
	static int webport = 8080;
	static int rmiport = 7220;
	static String pass = "";
	static final String newLine = System.lineSeparator();

	private static final int fNumberOfThreads = 100;
	private static final Executor fThreadPool = Executors.newFixedThreadPool(fNumberOfThreads);

	public static void main(String[] args) throws IOException
	{
		loadSettings();
		System.out.println("Starting webserver on port " + webport);
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(webport);
		while (true)
		{
			final Socket connection = socket.accept();
			Runnable task = new Runnable()
			{
				@Override
				public void run()
				{
					HandleRequest(connection);
				}
			};
			fThreadPool.execute(task);
		}
	}

	private static String processCommand(String cmd, String[] args) throws RemoteException, NotBoundException, UnsupportedEncodingException, WurmServerException {
		switch (cmd.toLowerCase()) {
		case "getonlineplayers": //getOnlinePlayers?true --args (boolean)moreInfo
			return buildOutput(getOnlinePlayers());
		case "getplayerstates": //getPlayerStates?01234567890&01234567890&01234567890.... (long)playerID
			long[] ret = new long[args.length];
			for (int i=0; i<(args.length); i++) {
				ret[i] = safeLong(args[i]);
			}
			return buildOutput(iface.getPlayerStates(pass, ret));
		case "getallserverinternaladdresses":
			return buildOutput(iface.getAllServerInternalAddresses(pass));
		case "getserverstatus":
			return iface.getServerStatus(pass);
		case "getkingdoms":
			return buildOutput(iface.getKingdoms(pass));
		case "getallplayers": // getallplayers
			return buildOutput(getAllPlayers());
		case "getbattleranks": // getbattleranks?5 --args (int)limit)
			return buildOutput(iface.getBattleRanks(pass, safeInt(args[0])));
		case "getplayersforkingdom": // getplayersforkingdom?4 --args (int)kingdomID
			return buildOutput(iface.getPlayersForKingdom(pass, safeInt(args[0])));
		case "chargemoney": // chargemoney?Admin&500 --args (str)playerName,(int)amountInIron
			return ""+iface.chargeMoney(pass, args[0], safeLong(args[1]));
		case "addmoneytobank": // addmoneytobank?Admin&-1&500 --args (str)playerName,(int)playerID,(int)amountInIron ---- note: use -1 playerID if searching by name
			return buildOutput(iface.addMoneyToBank(pass, args[0], safeLong(args[1]), safeLong(args[2]), "[WurmWebRMI]", true));
		case "getstructuresummary": // getstructuresummary?1234567890 --args (int)structureID
			return buildOutput(iface.getStructureSummary(pass, safeLong(args[0])));
		case "gettilesummary": // gettilesummary?1000&1500&true --args (int)tileX,(int)tileY,(boolean)surfaceTile
			return buildOutput(iface.getTileSummary(pass, safeInt(args[0]), safeInt(args[1]), Boolean.valueOf(args[2])));
		case "getitemsummary": // getitemsummary?1234567890 --args (int)itemID
			return buildOutput(iface.getItemSummary(pass, safeLong(args[0])));
		case "getplayersummary": // getplayersummary?1234567890 --args (int)playerID
			return buildOutput(iface.getPlayerSummary(pass, safeLong(args[0])));
		case "getfriends": // getfriends?1234567890 --args (int)playerID
			return buildOutput(iface.getFriends(pass, safeLong(args[0])));
		case "getinventory": // getinventory?1234567890 --args (int)playerID
			return buildOutput(iface.getInventory(pass, safeLong(args[0])));
		case "getpower": // getpower?1234567890 --args (int)playerID
			return ""+iface.getPower(pass, safeLong(args[0]));
		case "getmoney": // getmoney?1234567890&Admin --args (int)playerID,(str)playerName
			return ""+iface.getMoney(pass, safeLong(args[0]), args[1]);
		case "getbankaccount": // getbankaccount?1234567890 --args (int)playerID
			return buildOutput(iface.getBankAccount(pass, safeLong(args[0])));
		case "getareahistory": // getareahistory?5 --args (int)limit)
			return ""+String.join(newLine,(iface.getAreaHistory(pass, safeInt(args[0]))));
		case "getplayeripaddresses": // getplayeripaddresses
			return buildOutput(iface.getPlayerIPAddresses(pass));
		case "getalliesfordeed": // getalliesfordeed?1 --args (int)villageID
			return buildOutput(iface.getAlliesForDeed(pass, safeInt(args[0])));
		case "gethistoryfordeed": // gethistoryfordeed?1&5 --args (int)villageID,(int)limit
			return ""+String.join(newLine,(iface.getHistoryForDeed(pass, safeInt(args[0]), safeInt(args[1]))));
		case "getplayersfordeed": // getplayersfordeed?1 --args (int)villageID
			return buildOutput(iface.getPlayersForDeed(pass, safeInt(args[0])));
		case "getdeedsummary": // getdeedsummary?1 --args (int)villageID
			return buildOutput(iface.getDeedSummary(pass, safeInt(args[0])));
		case "getbodyitems": // getbodyitems?1234567890 --args (int)playerID
			return buildOutput(iface.getBodyItems(pass, safeLong(args[0])));
		case "getdeeds": // getdeeds
			return buildOutput(iface.getDeeds(pass));
		case "getplayerid": // getplayerid?Admin --args (str)playerName
			return ""+iface.getPlayerId(pass, args[0]);
		case "doesplayerexist": // doesplayerexist?Admin --args (str)playerName
			return buildOutput(iface.doesPlayerExist(pass, args[0]));
		case "getskills": // getskills
			return buildOutput(iface.getSkills(pass));
		case "getskillsforplayer": // getskillsforplayer?1234567890 --args (int)playerID
			return buildOutput(iface.getSkills(pass, safeLong(args[0])));
		case "getskillstats": // getskillstats?1008 --args (int)skillID
			return buildOutput(iface.getSkillStats(pass, safeInt(args[0])));
		case "getallservers": // getallservers
			return buildOutput(iface.getAllServers(pass));
		case "getwurmtime": // getwurmtime
			return ""+iface.getWurmTime(pass);
		case "getuptime": // getuptime
			return ""+iface.getUptime(pass);
		case "isrunning": // isrunning
			return ""+iface.isRunning(pass);
		case "getplayercount": // getplayercount
			return ""+iface.getPlayerCount(pass);
		case "broadcast": // broadcast?send a message --args (str)message
			iface.broadcastMessage(pass, java.net.URLDecoder.decode(args[0], "UTF-8"));
			return "broadcast";
		case "shutdown": // shutdown?60&send a message --args (int)timeInSeconds(str)message
			iface.startShutdown(pass, "[WurmWebRMI]", safeInt(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8")); //-1 to cancel
			return "shutdown";
		case "cancelshutdown": // cancelshutdown
			iface.startShutdown(pass, "[WurmWebRMI]", -1, "Shutdown canceled.");
			return "shutdown canceled.";
		case "reloadsettings": //reloadsettings
			loadSettings();
			return "reloaded settings (changes to web port require full restart)";
		default:
			return "[ERROR] Unknown command: " + cmd;
		}
	}
	
	private static Map<Long, String[]> getOnlinePlayers() throws RemoteException, NotBoundException, WurmServerException {
		Map<String, Long> allPlayers = getAllPlayers();
		Map<Long, String[]> toReturn = new HashMap<Long, String[]>();
		List<Long> playerIDs = new ArrayList<Long>();
		for(Map.Entry<String, Long> player : allPlayers.entrySet()) {
			playerIDs.add(player.getValue());
		}
		long[] arrayID = playerIDs.stream().mapToLong(l -> l).toArray();
		Map<Long, byte[]> states = iface.getPlayerStates(pass, arrayID);
		for(Entry<Long, byte[]> playerStates : states.entrySet()) {
			byte lastValue = playerStates.getValue()[playerStates.getValue().length-1];
			if (lastValue > 0) {
				Map<String, ?> playerSummary = iface.getPlayerSummary(pass, playerStates.getKey());
				if (playerSummary.containsKey("Coord x")) {
					ArrayList<String> finalPlayers = new ArrayList<String>();
					finalPlayers.add((String) playerSummary.get("Name"));
					finalPlayers.add(Integer.toString((int) playerSummary.get("Coord x")));
					finalPlayers.add(Integer.toString((int) playerSummary.get("Coord y")));
					String[] ready = new String[finalPlayers.size()];
					ready = finalPlayers.toArray(ready);
					toReturn.put(playerStates.getKey(), ready);
				}
			}
		}
		return toReturn;
	}

	private static Map<String, Long> getAllPlayers() throws RemoteException, NotBoundException {
		Map<String, Integer> map = iface.getBattleRanks(pass, 999999);
		Map<String, Long> toReturn = new HashMap<String, Long>();
		map.forEach((k,v)->{
			try {
				toReturn.put(k, iface.getPlayerId(pass, k));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
		return toReturn;
	}

	private static String arrayToString(byte[] v, String delimiter) {
		StringBuilder string = new StringBuilder();
		string.append("[");
		for (int i = 0; i < v.length; i++) {
			if (i > 0) {
				string.append(delimiter);
			}
			byte item = v[i];
			string.append(item);
		}
		string.append("]");
		return string.toString();
	}
	
	private static String arrayToString(String[] v, String delimiter) {
		StringBuilder string = new StringBuilder();
		string.append("[");
		for (int i = 0; i < v.length; i++) {
			if (i > 0) {
				string.append(delimiter);
			}
			String item = v[i];
			string.append(item);
		}
		string.append("]");
		return string.toString();
	}

	private static String buildOutput(Map<?, ?> map) {
		try {
			StringBuilder string = new StringBuilder();
			int mapSize = map.size();
			int c = 0;
			for(Map.Entry<?, ?> entry : map.entrySet()) {
				c++;
				Object k = entry.getKey();
				Object v = entry.getValue();
				if (v instanceof byte[])
					string.append(k + "=" + arrayToString((byte[]) v, ","));
				else if (v instanceof String[])
					string.append(k + "=" + arrayToString((String[]) v, ","));
				else
					string.append(k + "=" + v);
				if (c < mapSize)
					string.append(newLine);
			}
			return string.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return ("[ERROR] " + e.getMessage());
		}
	}

	private static WebInterface setupConnection(String host, int port) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		return (WebInterface) registry.lookup("wuinterface");
	}

	private static int safeInt(String s) {
		try {
			return Integer.parseInt(s, 10);
		} catch (NumberFormatException e) {
			System.out.println("Parse error: '" + s + "' is not a valid integer");
			return 0;
		}
	}

	private static long safeLong(String s) {
		try {
			return Long.parseLong(s, 10);
		} catch (NumberFormatException e) {
			System.out.println("Parse error: '" + s + "' is not a valid long");
			return 0;
		}
	}

	private static void loadSettings() {
		//URL url = WebRMI.class.getProtectionDomain().getCodeSource().getLocation();
		//System.out.println(url);
		File settings = new File("WurmWebRMI.ini");
		if (!settings.exists()) {
			try {
				settings.createNewFile();
			} catch (IOException e) {
				System.out.println("Settings file not found and java cannot create one...");
				System.out.println("Error creating settings file: " + e.getMessage());
			}
		}
		try {
			Ini ini = new Ini(settings);
			if (!ini.containsKey("Main"))
				ini.add("Main");
			Section section = ini.get("Main");
			if (!section.containsKey("Server IP"))
				ini.put("Main", "Server IP", "127.0.0.1");
			if (!section.containsKey("Web Port"))
				ini.put("Main", "Web Port", "8080");
			if (!section.containsKey("RMI Port"))
				ini.put("Main", "RMI Port", "7220");
			if (!section.containsKey("RMI Password"))
				ini.put("Main", "RMI Password", "changeme");
			ini.store();
			addr = ini.get("Main", "Server IP");
			webport = safeInt(ini.get("Main", "Web Port"));
			rmiport = safeInt(ini.get("Main", "RMI Port"));
			pass = ini.get("Main", "RMI Password");
		} catch (IOException e) {
			System.out.println("Error loading settings file: " + e.getMessage());
		}
	}

	private static void HandleRequest(Socket s) {
		BufferedReader in;
		PrintWriter out;
		String request;

		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			request = in.readLine();
			//System.out.println(request);
			request = request.substring(request.indexOf("/")+1, request.indexOf("HTTP")-1);
			String[] commands = request.split("\\?");
			if (commands.length > 1) {
				request = commands[0];
				commands = commands[1].split("&");
			}
			//System.out.println(Arrays.toString(commands));
			//System.out.println("---" + request);
			String response;
			try {
				if (!request.contains("reloadsettings"))
					iface = setupConnection(addr, rmiport); 
				response = processCommand(request, commands);
			} catch (RemoteException | NotBoundException | UnsupportedEncodingException | WurmServerException e) {
				response = "[ERROR] " + e.toString();
			}
			out = new PrintWriter(s.getOutputStream(), true);
			out.println("HTTP/1.0 200");
			out.println("Content-type: text/html");
			out.println("Server-name: WurmRMI");
			out.println("Content-length: " + response.length());
			out.println("");
			out.println(response);
			out.flush();
			out.close();
			s.close();
		}
		catch (IOException e) {
			System.out.println("Failed respond to client request: " + e.getMessage());
		}
		finally {
			if (s != null) {
				try {
					s.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return;
	}

}