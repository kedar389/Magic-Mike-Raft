# Applies every mod's game-rule baseline, then marks itself done.
# One function call per mod, add new mods below with their own function file
# under this same folder (e.g. server_gamerules:alexsmobs).

function server_gamerules:born_in_chaos

# --- add future per-mod rule functions above this line ---

scoreboard players set #done server_gr_setup 1
tellraw @a {"text":"[Server] Game rules applied.","color":"gray"}
