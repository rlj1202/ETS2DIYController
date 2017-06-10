@echo off
java -classpath "./bin;./lib/jline-2.14.2.jar;./lib/gson-2.8.0.jar;./lib/JvJoyInterface-0.1.0.jar;./lib/RXTXcomm_64.jar" -Djava.library.path="./lib/natives" redlaboratory.diyeurotruckcontroller.Main
pause