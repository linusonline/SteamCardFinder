# SteamCardFinder
This is a command line utility that helps you trade full sets of steam trading cards quickly and cheaply.

Please support [Steam Card Exchange](http://www.steamcardexchange.net/index.php "Steam Card Exchange")!

**Note:** This program does **not** consider the Steam Market value of cards, only the Steam Card Exchange credit value.

What it does:
- Downloads and analyzes the Steam Card Exchange trading bot inventory
- Prints a list of the cheapest full sets of cards available
- Optionally summarizes the credit value of your own cards, warning about overstocked cards

## Example usage
Print the 20 cheapest games with more than one full set available:

```
SteamCardFinder -l 20
SteamCardFinder --limit 20
```

As above, plus print summary of own cards current credit value (card list read from mycards.txt), plus 15 credits saved up in your SCE profile:
```
SteamCardFinder -l 20 -c -r 15
SteamCardFinder --limit 20 --cards --credits 15
```

## Usage
```
usage: SteamCardFinder [-c] [-d <file>] [-f] [-h] [-l <nr>] [-n <file>]
       [-r <credit-amount>] [-s] [-v]
 -c,--cards                     Read and summarize list of owned game
                                cards. Each line of the list file should
                                be on the form
                                '<card-amount>:<drop-amount>:<game-name>'.
                                Game name must match the name in the
                                online inventory exactly. Default file to
                                read from is mycards.txt
 -d,--cardsfile <file>          Name of the file to read owned game card
                                list from. Default is mycards.txt
 -f,--file                      Read web page from input file instead of
                                from web.
 -h,--help                      Print this help message
 -l,--limit <nr>                Max number of games to list.
 -n,--filename <file>           Name of the file to read/write web page
                                from/to. Default is input.json
 -r,--credits <credit-amount>   Number of credits you already have in your
                                Steam Card Exchange account.
 -s,--save                      When reading from web, save webpage to
                                file specified by --filename option.
                                Default is input.json
 -v,--verbose                   Print verbose information.
```

## Sample output
```
---------------------- SteamCardExchange Inventory Summary ---------------------

Total number of games: 7877
Number of games with more than one full set: 302
Limiting list to 20 cheapest games.

------------------------- Cheapest available Card Sets -------------------------


[Setsize / Sets / Cost - Name]
5 / 4 / 30 - Brave Dungeon
5 / 2 / 30 - Chronicles of a Dark Lord: Episode 1 Tides of Fate Complete
5 / 2 / 30 - Fruit Arranger
5 / 2 / 30 - N.P.P.D. RUSH - The milk of Ultra violet
7 / 7 / 35 - Cataegis : The White Wind
7 / 7 / 35 - High On Racing
7 / 6 / 35 - Let&#039;s Draw
5 / 5 / 35 - Ogrest
7 / 5 / 35 - City of Chains
5 / 4 / 35 - Life Beetle
5 / 4 / 35 - Moon Colonization Project
7 / 4 / 35 - Yargis - Space Melee
5 / 3 / 35 - Button Tales
5 / 3 / 35 - Captivity
5 / 3 / 35 - Lone Leader
5 / 3 / 35 - NeverEnd
5 / 3 / 35 - Nux
7 / 3 / 35 - Blockstorm
7 / 3 / 35 - Extravaganza Rising
7 / 3 / 35 - Monsti

----------------------------- My Game Card Summary -----------------------------

[(Set) Cards * Worth = Total - Name]

-- OWNED CARDS: 76
(195)  4 *  15 =   60 - Clicker Heroes
( 80)  2 *   8 =   16 - Detective Grimoire

-- REMAINING DROPS: 32
( 80)  1 *   8 =    8 - Detective Grimoire
( 72)  3 *   8 =   24 - Terraria

-- CREDITS: 2

-- TOTAL: 110

None of your cards are overstocked.
```
