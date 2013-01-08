@echo off

echo *** Cleaning bin folder...
if exist bin rmdir /S /Q bin
mkdir bin

echo *** Compiling BON class files...
javac -d bin -sourcepath . immibis\beardedoctonemesis\gui\GuiMain.java

echo *** Packing bon.jar...
cd bin
jar cvfm bon.jar ../META-INF/MANIFEST.MF .
cd ..

echo *** Build complete!
pause