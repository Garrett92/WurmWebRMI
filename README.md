# WurmWebRMI 
Web based HTML interface for Wurm Unlimited RMI commands.

## Installation
* Extract contents of zip file into a folder on the system
* Edit the included WurmWebRMI.ini file
* Windows: Run startWurmWebRMI.bat
* Linux: Run 'java -jar WurmWebRMI.jar' from the installation directory

## Usage
This isn't really meant to be a stand-alone tool, just an interface for web based control so it can be accessed from other scripts or a web server. The webpage output string will need to be parsed by a separate tool or script in order to make the data useful.

Try to load the webpage by going to [http://127.0.0.1:8080](http://127.0.0.1:8080)

Examples: 
* http://127.0.0.1:8080/shutdown?60&Server will shutdown in 60 seconds
* ddd

**Commands:**

```
broadcast?[message]
	Sends a message visible to all players on the server
shutdown?[time in seconds]&[message]
	Tells the server to shutdown in the specified amount of time along with a reason for the shutdown
cancelShutdown
	If a shutdown has been initiated, this will cancel it
isRunning
	Returns 'true' if running
	
getWurmTime
	Returns current time in wurm (as if you ran the /time command in-game)
getUpTime
	Returns the amount of time the server has been online in days, hours, and minutes
getAllServers
	Returns list of servers linked together?
	
getPlayerCount
	Returns the amount of players currently online.
getAllPlayers
	Returns list of all characters registered on the server in the format NAME=PLAYERID
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
	
getTileSummary?[TileX]&[TileY]&[Boolean surfaceTile]
	Returns information about the tile (surfaceTile argument must be 'true' or 'false')
getStructureSummary?[structureID]
	Returns information about the structure
getItemSummary?[itemID]
	Returns information about the item

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
	Returns list of players skills in the format (SKILL NAME=SKILL LEVEL)
getMoney?[playerID]&[playerName]
	Returns players banked money in iron. (20000 would be 2 silver)
addMoneyToBank?[playerName]&[playerID]&[amount in iron]
	Adds money to players bank and returns total updated amount in iron
chargeMoney?[playerName]&[amount in iron]
	Removed money from players bank and returns total updated amount in iron
```

## Extra Info
If the Wurm Unlimited server is running on a remote server, you will need to edit the LaunchConfig.ini file located in your Wurm Unlimited server folder. Add the following code to the end of the file and change the IP address to the external IP (if the remote server is external) or the LAN IP (if the server is local).

```
JvmParam1=-Djava.rmi.server.hostname=127.0.0.1
```
