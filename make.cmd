@echo off
rem clean
del *.class
del *.jar

javac Bot1.java
jar cvfm Bot1.jar Manifest1.txt *.class
del *.class

javac Bot2.java
jar cvfm Bot2.jar Manifest2.txt *.class 
del *.class

javac Bot3.java
jar cvfm Bot3.jar Manifest3.txt *.class 
del *.class
