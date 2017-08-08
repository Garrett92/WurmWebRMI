package com.imraginbro.wurm.webrmi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.wurmonline.server.webinterface.WebInterface;

public final class WebRMI {
	
	static final String newLine = System.lineSeparator();
	static WebInterface iface;
	
	static String correctUsage = "";
	
	static boolean debug = false;
	
	static String bindSocket = "";
	static String addr = "127.0.0.1";
	static int webport = 8080;
	static int rmiport = 7220;
	static String pass = "";
	static String limitConnection = "*";
	static boolean allowAll = true;
	static String[] allowedIP;

	private static final int fNumberOfThreads = 100;
	private static final Executor fThreadPool = Executors.newFixedThreadPool(fNumberOfThreads);

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException
	{
		if (!loadPropValues()) {
			return;
		}
		
		System.getSecurityManager();
		ServerSocket socket;
		if (bindSocket.equals("")) {
			socket = new ServerSocket(webport);
		} else {
			socket = new ServerSocket(webport, 0, InetAddress.getByName(bindSocket));
		}
		
		System.out.println("Starting server... listening from " + socket.getLocalSocketAddress());
		while (true)
		{
			final Socket connection = socket.accept();
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						connection.setSoTimeout(2000);
						final String ip = connection.getInetAddress().getHostAddress();
						if (debug) {
							System.out.println("[DEBUG-CONNECTION] connection attempt from " + ip);
						}
						if (!allowAll) {
							boolean success = false;
							for (int i = 0; i < allowedIP.length; i++) {
								if (ip.contains(allowedIP[i])) {
									success = true;
									break;
								}
							}
							if (!success) {
								System.out.println("[SECURITY] Blocked connection attempt from " + ip);
								connection.close();
								return;
							}
						}
						HandleRequest(connection);
					} catch (Exception e) {
						if (debug) {
							System.out.println("[DEBUG-ERROR] " + e.getMessage());
						}
					}
				}
			};
			fThreadPool.execute(task);
		}
	}
	
	private static String processCommand(String cmd, String[] args) throws Exception {
		switch (cmd.toLowerCase()) {
		case "messageplayerpopup":
			correctUsage = "USAGE=messagePlayerPopup?[wurmID]&[windowTitle]&[message]";
			return iface.CPplayerSendPopup(pass, Long.parseLong(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8"), java.net.URLDecoder.decode(args[2], "UTF-8"));
		case "messageplayerpm":
			correctUsage = "USAGE=messagePlayerPM?[wurmID]&[senderName]&[windowTitle]&[message]";
			return iface.CPmessagePlayerPM(pass, Long.parseLong(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8"), java.net.URLDecoder.decode(args[2], "UTF-8"), java.net.URLDecoder.decode(args[3], "UTF-8"));
		case "messageplayersystempm":
			correctUsage = "USAGE=messagePlayerSystemPM?[wurmID]&[windowTitle]&[message]";
			return iface.CPmessagePlayerWarnPM(pass, Long.parseLong(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8"), java.net.URLDecoder.decode(args[2], "UTF-8"));
		case "messageplayercustom":
			correctUsage = "USAGE=messagePlayerCustom?[wurmID]&[customWindowTitle]&[senderName]&[message]&[0-255red]&[0-255green]&[0-255blue]";
			return iface.CPmessagePlayerCustom(pass, Long.parseLong(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8"), java.net.URLDecoder.decode(args[2], "UTF-8"), java.net.URLDecoder.decode(args[3], "UTF-8"), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
		case "getallguardtowers":
			correctUsage = "USAGE=getAllGuardTowers";
			return buildOutput(iface.CustomgetAllGuardTowers(pass));
		case "getallsteamids":
			correctUsage = "USAGE=getAllSteamIDs";
			return buildOutput(iface.CPgetSteamDBInfo(pass));
		case "isplayeronline":
			correctUsage = "USAGE=isPlayerOnline?[wurmID]";
			return Boolean.toString(iface.CPisPlayerOnline(pass, Long.parseLong(args[0])));
		case "getallstructures":
			correctUsage = "USAGE=getAllStructures";
			return buildOutput(iface.CPgetAllStructures(pass));
		case "kickplayer":
			correctUsage = "USAGE=kickPlayer?[wurmID]&[message]";
			return iface.CPkickPlayer(pass, Long.parseLong(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8"));
		case "getitemtemplates":
			correctUsage = "USAGE=getItemTemplates";
			return getItemTemplates();
		case "changepower":
			correctUsage = "USAGE=changePower?[wurmID]&[powerLevel]";
			return iface.CPchangePower(pass, Long.parseLong(args[0]), Byte.parseByte(args[1]));
		case "giveitem":
			correctUsage = "USAGE=giveItem?[wurmID]&[itemID]&[itemQuality]&[itemRarity]&[creator]&[itemAmount]";
			return iface.CPgiveItem(pass, Long.parseLong(args[0]), Integer.parseInt(args[1]), Float.parseFloat(args[2]), Byte.parseByte(args[3]), java.net.URLDecoder.decode(args[4], "UTF-8"), Integer.parseInt(args[5]));
		case "messageplayer":
			correctUsage = "USAGE=messagePlayer?[wurmID]&[messageType]&[message]";
			return iface.CPmessagePlayer(pass, Long.parseLong(args[0]), Byte.parseByte(args[1]), java.net.URLDecoder.decode(args[2], "UTF-8"));
		case "removebannedip":
			correctUsage = "USAGE=removeBannedIP?[IP]";
			return iface.removeBannedIp(pass, args[0]);
		case "getbannedplayers":
			correctUsage = "USAGE=getBannedPlayers";
			return buildOutput(iface.getNameBans(pass));
		case "getbannedip":
			correctUsage = "USAGE=getBannedIP";
			return buildOutput(iface.getIPBans(pass));
		case "addbannedip":
			correctUsage = "USAGE=addBannedIP?[IP]&[Reason]&[Days]";
			return iface.addBannedIp(pass, args[0], args[1], Integer.parseInt(args[2]));
		case "setweather":
			correctUsage = "USAGE=setWeather?[windRotation]&[windPower]&[windDir]";
			iface.setWeather(pass, Float.parseFloat(args[0]), Float.parseFloat(args[1]), Float.parseFloat(args[2]));
			return "RETURN=OK";
		case "pardonban":
			correctUsage = "USAGE=pardonBan?[playerName]";
			return iface.pardonban(pass, args[0]);
		case "banplayer":
			correctUsage = "USAGE=banPlayer?[playerName]&[reason]&[days]";
			return iface.ban(pass, args[0], args[1], Integer.parseInt(args[2]));
		case "getrecentplayers":
			correctUsage = "USAGE=getRecentPlayers?[timeInSeconds]";
			return buildOutput(getRecentPlayers(Long.parseLong(args[0])));
		case "changepassword":
			correctUsage = "USAGE=changePassword?[playerName]&[steamID64]";
			return changePassword(args[0], args[1]);
		case "changeemail":
			correctUsage = "USAGE=changeEmail?[playerName]&[oldEmail]&[newEmail]";
			return changeEmail(args[0], args[1], args[2]);
		case "renamecharacter":
			correctUsage = "USAGE=renameCharacter?[oldName]&[newName]&[steamID64]";
			return renameChracter(args[0], args[1], args[2]);
		case "getkingdominfluence":
			correctUsage = "USAGE=getKingdomInfluence";
			return buildOutput(iface.getKingdomInfluence(pass));
		case "createplayer":
			correctUsage = "USAGE=createPlayer?[playerName]&[steamID64]&[kingdomID]&[gender]&[power]";
			return createPlayer(args[0], args[1], Byte.valueOf(args[2]), Byte.valueOf(args[3]), Byte.valueOf(args[4]));
		case "findsteamidpower":
			correctUsage = "USAGE=findSteamIDPower?[steamID64]";
			return getHighestPowerForSteamID(args[0]);
		case "checkuserpass":
			correctUsage = "USAGE=checkUserPass?[playerName]&[steamID64]";
			return buildOutput(checkUserPass(args[0], args[1]));
		case "findplayerswithsteamid":
			correctUsage = "USAGE=findPlayersWithSteamID?[steamID64]";
			return buildOutput(findPlayersWithSteamID(args[0]));
		case "genpassword":
			correctUsage = "USAGE=genPassword?[playerName]&[steamID64]";
			return passwordEncrypt(args[0], args[1]);
		case "getonlineplayers":
			correctUsage = "USAGE=getOnlinePlayers";
			return buildOutput(getOnlinePlayers());
		case "getplayerstates":
			correctUsage = "USAGE=getPlayerStates?[playerID]&[playerID]&...";
			long[] ret = new long[args.length];
			for (int i=0; i<(args.length); i++) {
				ret[i] = Long.parseLong(args[i]);
			}
			return buildOutput(iface.getPlayerStates(pass, ret));
		case "getallserverinternaladdresses":
			correctUsage = "USAGE=getAllServerInternalAddresses";
			return buildOutput(iface.getAllServerInternalAddresses(pass));
		case "getserverstatus":
			correctUsage = "USAGE=getServerStatus";
			return iface.getServerStatus(pass);
		case "getkingdoms":
			correctUsage = "USAGE=getKingdoms";
			return buildOutput(iface.getKingdoms(pass));
		case "getallplayers":
			correctUsage = "USAGE=getAllPlayers";
			return buildOutput(getAllPlayers());
		case "getbattleranks":
			correctUsage = "USAGE=getBattleRanks?[Amount]";
			return buildOutput(iface.getBattleRanks(pass, Integer.parseInt(args[0])));
		case "getplayersforkingdom":
			correctUsage = "USAGE=getPlayersForKingdom?[KingdomID]";
			return buildOutput(iface.getPlayersForKingdom(pass, Integer.parseInt(args[0])));
		case "chargemoney":
			correctUsage = "USAGE=chargeMoney?[playerName]&[amountInIron]";
			return ""+iface.chargeMoney(pass, args[0], Long.parseLong(args[1]));
		case "addmoneytobank":
			correctUsage = "USAGE=addMoneyToBank?[playerName]&[playerID]&[amountInIron]";
			return buildOutput(iface.addMoneyToBank(pass, args[0], Long.parseLong(args[1]), Long.parseLong(args[2]), "[WurmWebRMI]", true));
		case "getstructuresummary":
			correctUsage = "USAGE=getStructureSummary?[structureID]";
			return buildOutput(iface.getStructureSummary(pass, Long.parseLong(args[0])));
		case "gettilesummary":
			correctUsage = "USAGE=getTileSummary?[TileX]&[TileY]&[(Boolean)surfaceTile]";
			return buildOutput(iface.getTileSummary(pass, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Boolean.valueOf(args[2])));
		case "getitemsummary":
			correctUsage = "USAGE=getItemSummary?[itemID]";
			return buildOutput(iface.getItemSummary(pass, Long.parseLong(args[0])));
		case "getplayersummary":
			correctUsage = "USAGE=getPlayerSummary?[playerID]";
			return buildOutput(iface.getPlayerSummary(pass, Long.parseLong(args[0])));
		case "getfriends":
			correctUsage = "USAGE=getFriends?[playerID]";
			return buildOutput(iface.getFriends(pass, Long.parseLong(args[0])));
		case "getinventory":
			correctUsage = "USAGE=getInventory?[playerID]";
			return buildOutput(iface.getInventory(pass, Long.parseLong(args[0])));
		case "getpower":
			correctUsage = "USAGE=getPower?[playerID]";
			return ""+iface.getPower(pass, Long.parseLong(args[0]));
		case "getmoney":
			correctUsage = "USAGE=getMoney?[playerID]&[playerName]";
			return ""+iface.getMoney(pass, Long.parseLong(args[0]), args[1]);
		case "getbankaccount":
			correctUsage = "USAGE=getBankAccount?[playerID]";
			return buildOutput(iface.getBankAccount(pass, Long.parseLong(args[0])));
		case "getareahistory":
			correctUsage = "USAGE=getAreaHistory?[Amount]";
			return String.join(newLine,(iface.getAreaHistory(pass, Integer.parseInt(args[0]))));
		case "getplayeripaddresses":
			correctUsage = "USAGE=getPlayerIpAddresses";
			return buildOutput(iface.getPlayerIPAddresses(pass));
		case "getalliesfordeed":
			correctUsage = "USAGE=getAlliesForDeed?[villageID]";
			return buildOutput(iface.getAlliesForDeed(pass, Integer.parseInt(args[0])));
		case "gethistoryfordeed":
			correctUsage = "USAGE=getHistoryForDeed?[villageID]&[Amount]";
			return String.join(newLine,(iface.getHistoryForDeed(pass, Integer.parseInt(args[0]), Integer.parseInt(args[1]))));
		case "getplayersfordeed":
			correctUsage = "USAGE=getPlayersForDeed?[villageID]";
			return buildOutput(iface.getPlayersForDeed(pass, Integer.parseInt(args[0])));
		case "getdeedsummary":
			correctUsage = "USAGE=getDeedSummary?[villageID]";
			return buildOutput(iface.getDeedSummary(pass, Integer.parseInt(args[0])));
		case "getbodyitems":
			correctUsage = "USAGE=getBodyItems?[playerID]";
			return buildOutput(iface.getBodyItems(pass, Long.parseLong(args[0])));
		case "getdeeds":
			correctUsage = "USAGE=getDeeds";
			return buildOutput(iface.getDeeds(pass));
		case "getplayerid":
			correctUsage = "USAGE=getPlayerID?[playerName]";
			return Long.toString((iface.getPlayerId(pass, args[0])));
		case "doesplayerexist":
			correctUsage = "USAGE=doesPlayerExist?[playerName]";
			return buildOutput(iface.doesPlayerExist(pass, args[0]));
		case "getskills":
			correctUsage = "USAGE=getSkills";
			return buildOutput(iface.getSkills(pass));
		case "getskillsforplayer":
			correctUsage = "USAGE=getSkillsForPlayer?[playerID]";
			return buildOutput(iface.getSkills(pass, Long.parseLong(args[0])));
		case "getskillstats":
			correctUsage = "USAGE=getSkillStats?[SkillID]";
			return buildOutput(iface.getSkillStats(pass, Integer.parseInt(args[0])));
		case "getallservers":
			correctUsage = "USAGE=getAllServers";
			return buildOutput(iface.getAllServers(pass));
		case "getwurmtime":
			correctUsage = "USAGE=getWurmTime";
			return ""+iface.getWurmTime(pass);
		case "getuptime":
			correctUsage = "USAGE=getUpTime";
			return ""+iface.getUptime(pass);
		case "isrunning":
			correctUsage = "USAGE=isRunning";
			return ""+iface.isRunning(pass);
		case "getplayercount":
			correctUsage = "USAGE=getPlayerCount";
			return ""+iface.getPlayerCount(pass);
		case "broadcast":
			correctUsage = "USAGE=broadcast?[message]";
			iface.broadcastMessage(pass, java.net.URLDecoder.decode(args[0], "UTF-8"));
			return "RETURN=OK";
		case "shutdown":
			correctUsage = "USAGE=shutdown?[time in seconds]&[message]";
			iface.startShutdown(pass, "[WurmWebRMI]", Integer.parseInt(args[0]), java.net.URLDecoder.decode(args[1], "UTF-8"));
			return "RETURN=OK";
		case "cancelshutdown":
			correctUsage = "USAGE=cancelShutdown";
			iface.startShutdown(pass, "[WurmWebRMI]", -1, "Shutdown canceled.");
			return "RETURN=OK";
		case "reloadsettings":
			correctUsage = "USAGE=reloadSettings";
			loadPropValues();
			return "RETURN=OK";
		default:
			return "ERROR=Unknown command: " + cmd;
		}
	}

	private static String getItemTemplates() {
		StringBuffer sb = new StringBuffer();
		ClassLoader classLoader = WebRMI.class.getClassLoader();
		try {
			Class<?> cls = classLoader.loadClass("com.wurmonline.server.items.ItemList");
			Field[] fields = cls.getFields();
			for(Field field : fields) {
	            String name = field.getName();
	            Object value = field.getInt(field);
	            sb.append(value.toString() + "=" + name + newLine);
	        }
		} catch (Exception e) { }
		return sb.toString();
	}
	
	private static Long wurmDateToUnix(Date date) throws ParseException {
		return (date.getTime()/1000);
	}

	private static Long calcTimeDifference(Long time) throws ParseException {
		Long now = new Date().getTime()/1000;  
		return (now - time);
	}

	private static Map<Long, String[]> getRecentPlayers(Long timeInSeconds) throws ParseException, RemoteException, NotBoundException {
		//SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		Map<String, Long> allPlayers = getAllPlayers();
		Map<Long, String[]> toReturn = new HashMap<Long, String[]>();
		for(Map.Entry<String, Long> player : allPlayers.entrySet()) {
			Map<String, ?> playerSummary = iface.getPlayerSummary(pass, player.getValue());
			Long calc = calcTimeDifference(wurmDateToUnix((Date) playerSummary.get("Last logout")));
			if (calc <= timeInSeconds || playerSummary.containsKey("Coord x")) {
				ArrayList<String> finalPlayers = new ArrayList<String>();
				finalPlayers.add((String) playerSummary.get("Name"));
				if (playerSummary.containsKey("Coord x")) {
					finalPlayers.add(Long.toString(calcTimeDifference(wurmDateToUnix((Date) playerSummary.get("Last login")))));
					finalPlayers.add("TRUE");
				} else {
					finalPlayers.add(Long.toString(calcTimeDifference(wurmDateToUnix((Date) playerSummary.get("Last logout")))));
					finalPlayers.add("FALSE");
				}
				String[] ready = new String[finalPlayers.size()];
				ready = finalPlayers.toArray(ready);
				toReturn.put(player.getValue(), ready);
			}
		}
		return toReturn;
	}

	private static Map<Long, String[]> getOnlinePlayers() throws Exception {
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
					finalPlayers.add(Long.toString(calcTimeDifference(wurmDateToUnix((Date) playerSummary.get("Last login")))));
					String[] ready = new String[finalPlayers.size()];
					ready = finalPlayers.toArray(ready);
					toReturn.put(playerStates.getKey(), ready);
				}
			}
		}
		return toReturn;
	}

	private static String changePassword(String playerName, String steamID) throws RemoteException {
		return buildOutput(iface.changePassword(pass, raiseFirstLetter(playerName), raiseFirstLetter(playerName)+"@test.com", steamID));
	}

	private static final String raiseFirstLetter(String oldString)
	{
		if (oldString.length() == 0) {
			return oldString;
		}
		String lOldString = oldString.toLowerCase();
		String firstLetter = lOldString.substring(0, 1).toUpperCase();
		String newString = firstLetter + lOldString.substring(1, lOldString.length());
		return newString;
	}

	private static String changeEmail(String name, String oldEmail, String newEmail) throws RemoteException {
		return buildOutput(iface.changeEmail(pass, name, oldEmail, newEmail));
	}

	private static String renameChracter(String oldname, String newname, String steamID) throws RemoteException {
		changeEmail(raiseFirstLetter(oldname), raiseFirstLetter(oldname)+"@test.com", raiseFirstLetter(newname)+"@test.com");
		return iface.rename(pass, raiseFirstLetter(oldname), raiseFirstLetter(newname), steamID, 5);
	}

	private static String createPlayer(String playerName, String steamID, byte kingdomID, byte gender, byte power) throws RemoteException {
		return buildOutput(iface.createPlayer(pass, raiseFirstLetter(playerName), steamID, "What is your mother's maiden name?", "Sawyer", raiseFirstLetter(playerName)+"@test.com", kingdomID, power, 8263186381637L, gender));
	}

	private static String getHighestPowerForSteamID(String steamID) throws Exception {
		Map<Long, String[]> players = findPlayersWithSteamID(steamID);
		int powerReturn = 0;
		for(Entry<Long, String[]> player : players.entrySet()) {
			if (Integer.parseInt(player.getValue()[1]) > powerReturn) {
				powerReturn = Integer.parseInt(player.getValue()[1]);
			}
		}
		return Integer.toString(powerReturn);
	}

	private static Map<Long, String[]> findPlayersWithSteamID(String steamID) throws Exception {
		Map<String, Long> allPlayers = getAllPlayers();
		Map<Long, String[]> toReturn = new HashMap<Long, String[]>();
		for(Map.Entry<String, Long> player : allPlayers.entrySet()) {
			String name = player.getKey();
			if (checkUserPass(name, steamID).containsKey("PlayerID0")) {
				int power = iface.getPower(pass, player.getValue());
				ArrayList<String> finalPlayers = new ArrayList<String>();
				finalPlayers.add(name);
				finalPlayers.add(Integer.toString(power));
				String[] ready = new String[finalPlayers.size()];
				ready = finalPlayers.toArray(ready);
				toReturn.put(player.getValue(), ready);
			}
		}
		return toReturn;
	}

	private static Map<String, ?> checkUserPass(String name, String steamID) throws Exception {
		String playerPass = passwordEncrypt(raiseFirstLetter(name), steamID);
		return iface.authenticateUser(pass, raiseFirstLetter(name), raiseFirstLetter(name)+"@test.com", playerPass);
	}

	private static String encrypt(String plaintext) throws Exception
	{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA");
		} catch (Exception e) { }
		try {
			md.update(plaintext.getBytes("UTF-8"));
		} catch (Exception e) { }
		byte[] raw = md.digest();
		String hash = Base64.getEncoder().encodeToString(raw);
		return hash;
	}

	private static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		char[] passwordChars = password.toCharArray();
		byte[] saltBytes = salt.getBytes();

		PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, 1000, 192);

		SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hashedPassword = key.generateSecret(spec).getEncoded();
		return String.format("%x", new Object[] { new BigInteger(hashedPassword) });
	}

	private static String passwordEncrypt(String name, String steamID64) throws Exception {
		String enc = encrypt(raiseFirstLetter(name));
		return hashPassword(steamID64, enc);
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
			return ("ERROR=" + e.getMessage());
		}
	}

	private static WebInterface setupConnection(String host, int port) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		return (WebInterface) registry.lookup("wuinterface");
	}
	
	public static void copy(InputStream source , String destination) {
        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }
	
	private static void copyPropertiesFile() {
		try {
			InputStream in = WebRMI.class.getResourceAsStream("/resources/WurmWebRMI.properties");
			copy(in, "WurmWebRMI.properties");
			in.close();
		} catch(Exception e) {
			System.out.println("Error copying properties file from jar - " + e.getMessage());
		}
	}
	
	private static boolean loadPropValues() {
		System.out.println("Loading WurmWebRMI.properties file!");
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("WurmWebRMI.properties");
		} catch (Exception e) {
			System.out.println("[ERROR] problem loading properties FileInputStream - " + e.getMessage());
			System.out.println("Copying properties file from jar... please configure and restart program.");
			copyPropertiesFile();
			return false;
		}
		if (input != null) {
			try {
				prop.load(input);
			} catch (Exception e) {
				System.out.println("Error loading properties file - " + e.getMessage());
				return false;
			}
		}
		
		webport = Integer.parseInt(prop.getProperty("HttpWebPort", Integer.toString(webport)));
		addr = prop.getProperty("WurmServerIP", addr);
		rmiport = Integer.parseInt(prop.getProperty("WurmRMIPort", Integer.toString(rmiport)));
		pass = prop.getProperty("WurmRMIPassword", pass);
		debug = Boolean.parseBoolean(prop.getProperty("DebugMode", Boolean.toString(debug)));
		bindSocket = prop.getProperty("bindSocket", bindSocket);
		limitConnection = prop.getProperty("limitConnection", limitConnection);
		
		System.out.println("[INFO] Web Port: " + webport);
		System.out.println("[INFO] Debug: " + debug);
		System.out.println("[INFO] Wurm IP: " + addr);
		System.out.println("[INFO] Wurm RMI Port: " + rmiport);
		System.out.println("[INFO] Wurm RMI Pass: " + pass);
		
		if (bindSocket.equals("")) {
			System.out.println("[INFO] Binding socket to all local IP's");
		} else {
			System.out.println("[INFO] Binding socket to: " + bindSocket);
		}
		
		if (!limitConnection.equals("*")) {
			allowAll = false;
			allowedIP = limitConnection.split(",");
			for (int i = 0; i < allowedIP.length; i++) {
				allowedIP[i] = allowedIP[i].trim();
				System.out.println("[INFO] ("+(i+1)+") Allowing connections from " + allowedIP[i]);
				allowedIP[i] = allowedIP[i].replace("*", "");
			}
		} else {
			System.out.println("[INFO] Allowing all connections");
		}

		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private static void HandleRequest(Socket s) throws UnsupportedEncodingException {
		BufferedReader in;
		PrintWriter out;
		String request;

		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			request = in.readLine();
			request = request.substring(request.indexOf("/")+1, request.indexOf("HTTP")-1);
			if (debug) {
				System.out.println("[DEBUG-REQUEST] ("+s.getInetAddress().getHostAddress()+") " + request);
			}
			String[] commands = request.split("\\?");
			if (commands.length > 1) {
				request = commands[0];
				commands = commands[1].split("&");
			}
			String response;
			try {
				if (!request.contains("reloadsettings"))
					iface = setupConnection(addr, rmiport); 
				response = processCommand(request, commands);
			} catch (Exception e) {
				response = "ERROR=" + e.toString() + newLine + correctUsage;
			}
			out = new PrintWriter(s.getOutputStream(), true);
			out.println("HTTP/1.0 200");
			out.println("Content-type: text/plain");
			out.println("Server-name: WurmRMI");
			out.println("Content-length: " + response.length());
			out.println("");
			out.println(response);
			out.flush();
			out.close();
			s.close();
			in.close();
		}
		catch (Exception e) {
			if (debug) {
				System.out.println("[DEBUG-REQUEST] Connection error: " + e.getMessage());
			}
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