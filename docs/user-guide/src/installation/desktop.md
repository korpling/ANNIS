# Installing a Local Version (ANNIS Desktop)

Local users who do not wish to make their corpora available online can install ANNIS 
Desktop under  most   versions   of  Linux,   Windows and  MacOS. 
To install ANNIS follow these steps:

1. Download and the ANNIS Desktop JAR-file from the ANNIS website.
2. Double-click on downloaded JAR-file to start it. 
   On Windows, at this point your Firewall may try to block ANNIS and offer you to unblock it — do so and ANNIS should start up.
   If double clicking does not work, you might need to start ANNIS from a terminal.
   Open your system terminal and navigate to the directory where the JAR-file is located, e.g. `/Users/myself/Documents` and directly start the JAR-file with java:
   ```bash
   cd /Users/myself/Documents
   java -jar annis-gui-<version>-desktop.jar
   ```
   You have to replace `<version>` with the version of ANNIS you are using.
3. A browser window with the address <http://localhost:5712> should open automatically, if not you can click on the button in the ANNIS Desktop window.

## Test the installation is working

1. Download and the GUM demo corpus from the ANNIS website:
[http://corpus-tools.org/annis/corpora.html](http://corpus-tools.org/annis/corpora.html).
2. Click on the “Administration” button and select the “Import Corpus” tab.
3. Click on “Upload ZIP file with corpus“ and select the downloaded ZIP file
4. Once import is complete, click on the “Search interface” to get back to the search interface and test the corpus (click
on one of the example queries displayed on the screen, or try selecting the
GUM corpus, typing `pos="NN"` in the AQL box at the top left and clicking
“Show Result”. See the sections [“Using the ANNIS interface”](../interface)and [“ANNIS Query Language (AQL)”](../aql.md) in this guide for some more example queries, or press the Tutorial button in the
Help/Examples tab of the interface for more information).
