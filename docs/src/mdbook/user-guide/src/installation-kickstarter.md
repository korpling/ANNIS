# Installing a Local Version (ANNIS Kickstarter)

Local users who do not wish to make their corpora available online can install ANNIS 
Kickstarter under  most   versions   of  Linux,   Windows and  MacOS. 
To install Kickstarter follow these steps:
1. Download and install PostgreSQL 9.4 (or above)
for your operating system from [http://www.postgresql.org/download/](http://www.postgresql.org/download/) and **make a note of the administrator password** you set during the installation. After installation, PostgreSQL may automatically launch the PostgreSQL Stack Builder to download additional components – you can safely skip this step and cancel the Stack Builder if you wish. You may need to restart your OS if the PostgreSQL installer tells you to.

***Note:*** Under Linux, you might have to set the PostgreSQL password manually.
E.g. on Ubuntu you can achieve this with by running the following commands:
```bash
sudo -u postgres psql
\password
\q
```
2. Download and unzip the ANNIS Kickstarter ZIP-file from the ANNIS website.
3. Start AnnisKickstarter.bat if you’re using Windows, AnnisKickstarter.cmd on
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
(To accelerate searches it is also possible to give the PostgreSQL database
more memory, see the next section below).
4. Once the program has started, if this is the first time you run Kickstarter, press
“Init Database” and supply your PostgreSQL administrator password from step 1. If you are upgrading from version 3.0.1 of ANNIS Kickstarter or higher, you will be given the option to reimport your corpora, assuming they can still be found at the paths from which they were originally imported.
5. Download and unzip the GUM demo corpus from the ANNIS website:
[http://corpus-tools.org/annis/corpora.html](http://corpus-tools.org/annis/corpora.html).
6. Press “Import Corpus” and navigate to the directory containing the directory
`GUM_annis/`. Select this directory (but do not go into it) and press OK.
7. Once import is complete, press “Launch Annis frontend” test the corpus (click
on one of the example queries displayed on the screen, or try selecting the
GUM corpus, typing `pos="NN"` in the AnnisQL box at the top left and clicking
“Show Result”. See the sections [“Using the ANNIS interface”](interface.md)and [“ANNIS Query Language (AQL)”](aql.md) in this guide for some more example queries, or press the Tutorial button in the
Help/Examples tab of the interface for more information).
