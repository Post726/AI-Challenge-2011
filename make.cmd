@echo off
rem clean
del *.class
del Bot1.jar
del Bot2.jar
del Bot3.jar
rem compile
javac Bot1.java
javac Bot2.java
javac Bot3.java
rem package
jar cvfm Bot1.jar Manifest1.txt *.class
jar cvfm Bot2.jar Manifest2.txt *.class 
jar cvfm Bot3.jar Manifest3.txt *.class 
rem clean
del *.class
