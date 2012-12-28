Compile into a jar file, or download the one from Github, and then run it to get the GUI version
which can deobfuscate mods.

For the command line version, use "java -cp nameofjarfile.jar immibis.beardedoctonemesis.Main"
followed by the required arguments. With no arguments, it shows what the expected arguments are.

Note that for digitally signed mods, you must delete META-INF after running this tool, or you will
get an ArrayIndexOutOfBoundsException from FML.