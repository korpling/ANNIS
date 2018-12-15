# Installing a Local Version (ANNIS Kickstarter)

Local users who do not wish to make their corpora available online can install ANNIS 
Kickstarter under  most   versions   of  Linux,   Windows and  MacOS. 
To install Kickstarter follow these steps:

1. Download and unzip the ANNIS Kickstarter ZIP-file from the ANNIS website.
2. Start AnnisKickstarter.bat if you’re using Windows, AnnisKickstarter.cmd on
Mac or run the bash script AnnisKickstarter.sh otherwise (this may take a few
seconds the first time you run Kickstarter). At this point your Firewall may try
to block Kickstarter and offer you to unblock it – do so and Kickstarter should
start up.

***Note:*** For most users it is a good idea to give Java more memory (if this is not
already the default). You can do this by editing the script AnnisKickstarter and
typing the following after the call to start java (after java or javaw in the .sh
or .bat script respectively):
```
-Xss1024k -Xmx1024m
```
3. Download and unzip the GUM demo corpus from the ANNIS website:
[http://corpus-tools.org/annis/corpora.html](http://corpus-tools.org/annis/corpora.html).
4. Press “Import Corpus” and navigate to the directory containing the directory
`GUM_annis/`. Select this directory (but do not go into it) and press OK.
5. Once import is complete, press “Launch Annis frontend” test the corpus (click
on one of the example queries displayed on the screen, or try selecting the
GUM corpus, typing `pos="NN"` in the AnnisQL box at the top left and clicking
“Show Result”. See the sections [“Using the ANNIS interface”](interface.md)and [“ANNIS Query Language (AQL)”](aql.md) in this guide for some more example queries, or press the Tutorial button in the
Help/Examples tab of the interface for more information).
