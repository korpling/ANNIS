#!/bin/bash
cd "$(dirname "$0")"
java -Xmx512m -splash:splashscreen.gif -cp 'lib/*' de.hu_berlin.german.korpling.annis.kickstarter.MainFrame
