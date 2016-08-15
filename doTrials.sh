#!/bin/bash
for i in `seq 1 5`;
do
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/cell_maze/cell_maze_p02_04.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/cell_maze/cell_maze_p02_06.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch 
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/maze/maze_02p_01.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/maze/maze_02p_02.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot3.jar" "java -jar bin/Bot3.jar" "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/maze/maze_p08_10.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/random_walk/random_walk_02p_01.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch
	python tools/playgame.py "java -jar bin/Bot3.jar" "java -jar bin/Bot2.jar" --fill --map_file tools/maps/random_walk/random_walk_02p_02.map --log_dir game_logs --turns 1500 --turntime 2000 -v --nolaunch
done