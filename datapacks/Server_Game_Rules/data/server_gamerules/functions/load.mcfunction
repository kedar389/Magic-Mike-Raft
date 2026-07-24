# Runs every world load. Applies the baseline ONCE, then leaves rules alone
# so manual /gamerule changes made later are not overwritten on restart.
scoreboard objectives add server_gr_setup dummy
execute unless score #done server_gr_setup matches 1 run function server_gamerules:apply
