# WurmWebRMI 
Web based HTML interface for Wurm Unlimited RMI commands.

## Installation
* Extract contents of zip file into a folder on the system
* Edit the included WurmWebRMI.ini file
* Windows: Run startWurmWebRMI.bat
* Linux: Run 'java -jar WurmWebRMI.jar' from the installation directory

## WurmWebRMI.ini
* Web Port = This is the port the webserver will be hosted on
* Server IP = Your Wurm Unlimited server IP
* RMI Port = Your Wurm Unlimited RMI port
* RMI Password = Your Wurm Unlimited RMI password

## Usage
This isn't really meant to be a stand-alone tool, just an interface for web based control so it can be accessed from other scripts or a web server. The webpage output string will need to be parsed by a separate tool or script in order to make the data useful.

Try to load the webpage by going to [http://127.0.0.1:8080](http://127.0.0.1:8080)

* 127.0.0.1:8080/shutdown?60&Server will shutdown in 60 seconds
* 127.0.0.1:8080/getAllPlayers
* 127.0.0.1:8080/addMoneyToBank?Admin&1234567890&10000 

**Commands (MODDED RMI):**

If you would like to use the modded RMI commands, you will need the CustomRMI mod running on the server.

[Wurm Custom RMI Mod](https://github.com/Garrett92/WurmRMImod/releases/latest)

```
messagePlayerCustom?[wurmID]&[customWindowTitle]&[senderName]&[message]&[red]&[green]&[blue]
	Sends the player a message - this command gives full control over the message
	Red, Green, and Blue will be the color of the text and should be a value of 0-255 for each.
	customWindowTitle will be the target tab. (You can set ':Event' for event log, or ':Local' or make your own)
	sender name will show up in the message as '<senderName> message'
messagePlayerPopup?[wurmID]&[windowTitle]&[message]
	Sends the player a popup window
messagePlayerPM?[wurmID]&[senderName]&[windowTitle]&[message]
	Sends the player a message through 'PM: windowTitle' <senderName> message
messagePlayerSystemPM?[wurmID]&[windowTitle]&[message]
	Sends the player a message through 'PM: windowTitle' <System> message
getItemTemplates
	Returns a list of all item template ID's and names (used for giveItem command)
isPlayerOnline?[wurmID]
	Returns true or false if the player is online or not
giveItem?[wurmID]&[itemTemplateID]&[itemQuality]&[itemRarity]&[creator]&[itemAmount]
	Gives the specified player items (must be online)
messagePlayer?[wurmID]&[messageType]&[message]
	Sends a message to the player through the event log (must be online)
changePower?[wurmID]&[powerLevel]
	Sets the players admin power level (must be online)
kickPlayer?[wurmID]&[message]
	Kicks the player and gives them a reason
getAllStructures
	Returns a list of all structures(houses/bridges) in the format STRUCTUREID=NAME
getAllGuardTowers
	Returns a list of all guard towers format: STRUCTUREID=[X,Y,CREATORID,CREATORNAME,QL,DMG]
getAllSteamIDs
	Returns a list of SteamID's stored in the DB in the format PLAYERID=[NAME,STEAMID64]
```

**Commands (DEFAULT RMI):**

```
broadcast?[message]
	Sends a message visible to all players on the server
shutdown?[time in seconds]&[message]
	Tells the server to shutdown in the specified amount of time along with a reason for the shutdown
cancelShutdown
	If a shutdown has been initiated, this will cancel it
isRunning
	Returns 'true' if running
	
getServerStatus
	Returns status of server - (If it is being shutdown or running)
getWurmTime
	Returns current time in wurm (as if you ran the /time command in-game)
getUpTime
	Returns the amount of time the server has been online in days, hours, and minutes
getAllServers
	Returns list of servers?
getAllServerInternalAddresses
	Returns list of servers?
	
getPlayerCount
	Returns the amount of players currently online
getOnlinePlayers
	Returns list of connected players in the format ID=[NAME,X-COORD,Y-COORD,(SEC)TIME-ONLINE]
getRecentPlayers?[timeInSeconds]
	Returns list of players that have been online within the specified time in the format
	ID=[NAME,(SECONDS)LAST-ONLINE,(BOOL)CURRENTLY-ONLINE].
	Note: If they are online, it will be (SECONDS)TIME-ONLINE
getAllPlayers
	Returns list of all characters registered on the server in the format NAME=PLAYERID
getPlayerStates?[playerID]&[playerID]&...
	Returns array of bytes of player states (unknown)
getPlayerIpAddresses
	Returns list of all players currently online along with their IP
doesPlayerExist?[playerName]
	Returns information on if the player exists or not
getSkills
	Returns list of the skill names and skill IDs
getSkillStats?[SkillID]
	Returns list of players with high levels in the specified skill
getBattleRanks?[Amount]
	Returns list of players and their battle ranks - limited by the amount specified
getPlayersForKingdom?[KingdomID]
	Returns list of players online in the specified kingdom
getAreaHistory?[Amount]
	Returns list of all history for the server limited by the amount specified
getKingdoms
	Returns list of kingdoms/IDs on the server
getKingdomInfluence
	Returns information about kingdom influence
setWeather?[windRotation]&[windPower]&[windDir]
	Unknown command - all vars requested are floats
	
getTileSummary?[TileX]&[TileY]&[Boolean surfaceTile]
	Returns information about the tile (surfaceTile argument must be 'true' or 'false')
getStructureSummary?[structureID]
	Returns information about the structure
getItemSummary?[itemID]
	Returns information about the item

getBannedIP
	Returns list of banned IP addresses
removeBannedIP?[IP]
	Removes specified banned IP address
addBannedIP?[IP]&[Reason]&[Days]
	Bans specified IP with reason and time length in days
getBannedPlayers
	Returns list of banned players
pardonBan?[playerName]
	Removes specified banned player from the banned list
banPlayer?[playerName]&[reason]&[days]
	Bans specified player with reason and time length in days

getDeeds
	Returns a list of active deeds on the server along with their villageID
getDeedSummary?[villageID]
	Returns information about the deed
getPlayersForDeed?[villageID]
	Returns list of players that are a part of the village
getHistoryForDeed?[villageID]&[Amount]
	Returns list of village history - limited by the amount specified
getAlliesForDeed?[villageID]
	Returns list of allies for the village

getPlayerID?[playerName]
	Returns players playerID
getPlayerSummary?[playerID]
	Returns information about the player
getFriends?[playerID]
	Returns characters on the players friends list
getPower?[playerID]
	Returns integer value of players administration power
getInventory?[playerID]
	Returns list of players inventory contents (must be online)
getBodyItems?[playerID]
	Returns list of the players body contents (must be online)
getBankAccount?[playerID]
	Returns list of players bank contents
getSkillsForPlayer?[playerID]
	Returns list of players skills in the format SKILL NAME=SKILL LEVEL
getMoney?[playerID]&[playerName]
	Returns players banked money in iron. (20000 would be 2 silver)
addMoneyToBank?[playerName]&[playerID]&[amount in iron]
	Adds money to players bank
chargeMoney?[playerName]&[amount in iron]
	Removes money from players bank
	
findPlayersWithSteamID?[steamID64]
	Matches any players with the supplied steamID returns PLAYERID=[PLAYERNAME,POWER]
findSteamIDPower?[steamID64]
	Returns the integer value of the highest power player from the findPlayersWithSteamID list
genPassword?[playerName]&[steamID64]
	Returns the encrypted password so you can match it with the database
checkUserPass?[playerName]&[steamID64]
	Returns some information the player if the name/password is valid
	
createPlayer?[playerName]&[steamID64]&[kingdomID]&[gender]&[power]
	Creates a player on the Wurm Unlimited server
renameCharacter?[oldName]&[newName]&[steamID64]
	Renames the player
changePassword?[playerName]&[steamID64]
	Changes the players password
changeEmail?[playerName]&[oldEmail]&[newEmail]
	Changes the email assigned to the player account
```

## Extra Info
It is not recommended to open your RMI ports to the outside of your local network. This tool is developed to be ran within the same local network as your Wurm Unlimited server.

If the Wurm Unlimited server is running on a remote server, you will need to edit the LaunchConfig.ini file located in your Wurm Unlimited server folder. Add the following code to the end of the file and change the IP address to the external IP (if the remote server is external) or the LAN IP (if the server is local).

```
JvmParam1=-Djava.rmi.server.hostname=127.0.0.1
```
