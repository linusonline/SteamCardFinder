# SteamCardFinder
This is a command line utility that helps you trade full sets of steam trading cards quickly and cheaply. It will be useful for you if:
- you generally don't want to spend money on trading cards
- you care more about maximizing your Steam XP and level than which specific badges you get.
- you use Steam Card Exchange to trade cards.

What it does:
- Downloads and analyzes the Steam Card Exchange trading bot inventory
- Prints a list of the cheapest full sets of cards available
- Optionally summarizes the credit value of your own cards, warning about overstocked cards

**Note:** This program does **not** consider the Steam Market value of cards, only the Steam Card Exchange credit value.

This utility is not affiliated with or endorsed by [Steam](http://steampowered.com "steampowered.com") or [Steam Card Exchange](http://www.steamcardexchange.net/index.php "www.steamcardexchange.net").

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
    30 / 2 / 5 - Dungeon of Zolthan
    30 / 2 / 5 - Heckabomb
    30 / 2 / 5 - New kind of adventure
    35 / 8 / 7 - Journey To The Center Of The Earth
    35 / 4 / 5 - TAIKU MANSION
    35 / 3 / 5 - BitRay
    35 / 3 / 5 - RePete
    35 / 3 / 7 - Chicken Shoot 2
    35 / 3 / 7 - High On Racing
    35 / 3 / 7 - Neon Space 2
    35 / 3 / 7 - The Tower Of Elements
    35 / 3 / 7 - VERGE:Lost chapter
    35 / 2 / 5 - Agent Awesome
    35 / 2 / 5 - BARRIER X
    35 / 2 / 5 - Bunker 58
    35 / 2 / 5 - Deep Space Dash
    35 / 2 / 5 - EvilMorph
    35 / 2 / 5 - Gun Metal
    35 / 2 / 5 - Incoming Forces
    35 / 2 / 5 - Life Beetle

----------------------------- My Game Card Summary -----------------------------

[(Set) Cards * Worth = Total - Name]

-- OWNED CARDS: 76
(195)  4 *  15 =   60 - Clicker Heroes
( 80)  2 *   8 =   16 - Detective Grimoire

-- REMAINING DROPS: 32
( 80)  1 *   8 =    8 - Detective Grimoire
( 72)  3 *   8 =   24 - Terraria

-- CREDITS: 2

-- TOTAL: 78
-- TOTAL INCLUDING DROPS: 110

None of your cards are overstocked.
```
## Listing cheapest available sets
This is the main feature of SteamCardFinder. It will list all games for which at least two full sets of cards are available in the SCE trading bot inventory right now. Games with only one full set available are not listed because last cards are more expensive, and will almost never be worth it.

Games are listed like this:
```
30 / 2 / 5 - Dungeon of Zolthan
```
From left to right, the numbers are: credit price for a full set of cards, number of full sets available, and number of cards per set. The list is also sorted by the same numbers, in order. Number of cards per set is listed to facilitate quick trades: the SCE trading bot has a limit of six cards per trade, so games with more than six cards per set will require more trades.

It is a good idea to limit the list by using the ```--limit (-l)``` option, otherwise the list will typically include several hundred games.

### Excluding your maxed-out games
Games for which you already have the level 5 badge will not be interesting to you. To exclude them from the list, create a file called *excluded.txt* in the program working directory. Put the name of each game to exclude on a separate line in the file. The name must match the name in the SCE inventory exactly.

## Calculating the value of your own cards
To use this feature, add the ```--cards (-c)``` option to the command line, and create a file called *mycards.txt* in the program working directory. Each line of the file should have the format:
```
<number of cards>:<number of drops left>:<game name>
```
So for instance:
```
2:1:Detective Grimoire
```
The game name must match the name in the SCE inventory exactly. (Games with colons in their names are ok.)
This will show you the current credit value of your owned cards and remaining drops. The output looks like this:
```
-- OWNED CARDS: 16
( 80)  2 *   8 =   16 - Detective Grimoire

-- REMAINING DROPS: 8
( 80)  1 *   8 =    8 - Detective Grimoire
```
From left to right, the numbers are: price for a full set of cards for the game (to compare with what you're trading for: does **not** take last card prices into account!), number of cards you have, the credit price for each card, and the total credit value for the cards.
The total credit value will then be listed, with and without remaining card drops:
```
-- TOTAL: 16
-- TOTAL INCLUDING DROPS: 24
```
To also include credits you already have in your SCE profile in the total, add the ```--credits (-r)``` option to the command line followed by the number of credits you have.
### Overstocked cards
When the SCE trading bot already has 8 copies of a single card, the card is considered overstocked, and the bot won't accept any more copies from you. Currently, this tool does not identify specific overstocked cards that you have, but does give a warning if *any* card in a set where you have *some* cards is overstocked.

When listing your owned cards/drops and their worth, games with any overstocked cards will be marked with a '!'. The totals will be listed with a secondary amount (after a '!'), which excludes those games.

Example:
```
-- OWNED CARDS: 33 (! 15)
( 35)  3 *   5 =   15 -   Journey To The Center Of The Earth
( 36)  3 *   6 =   18 - ! Yellow: The Yellow Artifact
```