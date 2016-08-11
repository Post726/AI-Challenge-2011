cd bin
rm *.class
rm *.jar

javac ../src/Bot1.java -sourcepath ../src/ -d .
jar cvfm Bot1.jar Manifest1.txt *.class
rm *.class

javac ../src/Bot2.java -sourcepath ../src/ -d .
jar cvfm Bot2.jar Manifest2.txt *.class 
rm *.class

javac ../src/Bot3.java -sourcepath ../src/ -d .
jar cvfm Bot3.jar Manifest3.txt *.class 
rm *.class
