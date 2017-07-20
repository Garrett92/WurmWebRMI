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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

	private static final int fNumberOfThreads = 5;
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

	private static String processCommand(String cmd, String[] args) throws RemoteException, NotBoundException, UnsupportedEncodingException {
		switch (cmd.toLowerCase()) {
		case "getplayerstates":
			long[] ret = new long[args.length];
			for (int i=0; i<(args.length); i++) {
				ret[i] = safeLong(args[i]);
			}
			try {
				return buildReturnSpecial(iface.getPlayerStates(pass, ret));
			} catch (WurmServerException e) {
				e.printStackTrace();
			}
			return "";
		case "getallserverinternaladdresses":
			return buildReturn(iface.getAllServerInternalAddresses(pass));
		case "getserverstatus":
			return iface.getServerStatus(pass);
		case "getkingdoms":
			return buildReturn(iface.getKingdoms(pass));
		case "getallplayers": // getallplayers
			return buildReturn(getAllPlayers(iface.getBattleRanks(pass, 999999)));
		case "getbattleranks": // getbattleranks?5 --args (int)limit)
			return buildReturn(iface.getBattleRanks(pass, safeInt(args[0])));
		case "getplayersforkingdom": // getplayersforkingdom?4 --args (int)kingdomID
			return buildReturn(iface.getPlayersForKingdom(pass, safeInt(args[0])));
		case "chargemoney": // chargemoney?Admin&500 --args (str)playerName,(int)amountInIron
			return ""+iface.chargeMoney(pass, args[0], safeLong(args[1]));
		case "addmoneytobank": // addmoneytobank?Admin&-1&500 --args (str)playerName,(int)playerID,(int)amountInIron ---- note: use -1 playerID if searching by name
			return buildReturn(iface.addMoneyToBank(pass, args[0], safeLong(args[1]), safeLong(args[2]), "[WurmWebRMI]", true));
		case "getstructuresummary": // getstructuresummary?1234567890 --args (int)structureID
			return buildReturn(iface.getStructureSummary(pass, safeLong(args[0])));
		case "gettilesummary": // gettilesummary?1000&1500&true --args (int)tileX,(int)tileY,(boolean)surfaceTile
			return buildReturn(iface.getTileSummary(pass, safeInt(args[0]), safeInt(args[1]), Boolean.valueOf(args[2])));
		case "getitemsummary": // getitemsummary?1234567890 --args (int)itemID
			return buildReturn(iface.getItemSummary(pass, safeLong(args[0])));
		case "getplayersummary": // getplayersummary?1234567890 --args (int)playerID
			return buildReturn(iface.getPlayerSummary(pass, safeLong(args[0])));
		case "getfriends": // getfriends?1234567890 --args (int)playerID
			return buildReturn(iface.getFriends(pass, safeLong(args[0])));
		case "getinventory": // getinventory?1234567890 --args (int)playerID
			return buildReturn(iface.getInventory(pass, safeLong(args[0])));
		case "getpower": // getpower?1234567890 --args (int)playerID
			return ""+iface.getPower(pass, safeLong(args[0]));
		case "getmoney": // getmoney?1234567890&Admin --args (int)playerID,(str)playerName
			return ""+iface.getMoney(pass, safeLong(args[0]), args[1]);
		case "getbankaccount": // getbankaccount?1234567890 --args (int)playerID
			return buildReturn(iface.getBankAccount(pass, safeLong(args[0])));
		case "getareahistory": // getareahistory?5 --args (int)limit)
			return ""+String.join("<br>",(iface.getAreaHistory(pass, safeInt(args[0]))));
		case "getplayeripaddresses": // getplayeripaddresses
			return buildReturn(iface.getPlayerIPAddresses(pass));
		case "getalliesfordeed": // getalliesfordeed?1 --args (int)villageID
			return buildReturn(iface.getAlliesForDeed(pass, safeInt(args[0])));
		case "gethistoryfordeed": // gethistoryfordeed?1&5 --args (int)villageID,(int)limit
			return ""+String.join("<br>",(iface.getHistoryForDeed(pass, safeInt(args[0]), safeInt(args[1]))));
		case "getplayersfordeed": // getplayersfordeed?1 --args (int)villageID
			return buildReturn(iface.getPlayersForDeed(pass, safeInt(args[0])));
		case "getdeedsummary": // getdeedsummary?1 --args (int)villageID
			return buildReturn(iface.getDeedSummary(pass, safeInt(args[0])));
		case "getbodyitems": // getbodyitems?1234567890 --args (int)playerID
			return buildReturn(iface.getBodyItems(pass, safeLong(args[0])));
		case "getdeeds": // getdeeds
			return buildReturn(iface.getDeeds(pass));
		case "getplayerid": // getplayerid?Admin --args (str)playerName
			return ""+iface.getPlayerId(pass, args[0]);
		case "doesplayerexist": // doesplayerexist?Admin --args (str)playerName
			return buildReturn(iface.doesPlayerExist(pass, args[0]));
		case "getskills": // getskills
			return buildReturn(iface.getSkills(pass));
		case "getskillsforplayer": // getskillsforplayer?1234567890 --args (int)playerID
			return buildReturn(iface.getSkills(pass, safeLong(args[0])));
		case "getskillstats": // getskillstats?1008 --args (int)skillID
			return buildReturn(iface.getSkillStats(pass, safeInt(args[0])));
		case "getallservers": // getallservers
			return buildReturn(iface.getAllServers(pass));
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

	private static Map<String, Long> getAllPlayers(Map<String, Integer> map) throws RemoteException, NotBoundException {
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
	
	private static String buildReturnSpecial(Map<?, byte[]> map) {
		StringBuilder stringBuilder = new StringBuilder();
		map.forEach((k,v)->stringBuilder.append(k + "=" + Arrays.toString(v) + "<br>"));
		return stringBuilder.toString();
	}

	private static String buildReturn(Map<?, ?> map) {
		StringBuilder stringBuilder = new StringBuilder();
		map.forEach((k,v)->stringBuilder.append(k + "=" + v + "<br>"));
		return stringBuilder.toString();
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
			} catch (RemoteException | NotBoundException | UnsupportedEncodingException e) {
				response = "[ERROR]" + e.toString();
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