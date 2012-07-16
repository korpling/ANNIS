<xsl:stylesheet exclude-result-prefixes='#default' version='1.0' xmlns:fo='http://www.w3.org/1999/XSL/Format' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl'/>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/fo/highlight.xsl'/>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/fo/autoidx-kosek.xsl'/>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/fo/autoidx-kimber.xsl'/>
  <xsl:include href='http://docbook.sourceforge.net/release/xsl/current/fo/dbdoclet-titlepage.xsl'/>
  <xsl:include href='http://dbdoclet.org/xsl/functions.xsl'/>
  <xsl:include href='http://dbdoclet.org/xsl/fo/fop1.xsl'/>
  <xsl:include href='http://dbdoclet.org/xsl/fo/themes/color.xsl'/>
  <xsl:include href='http://dbdoclet.org/xsl/fo/synopsis.xsl'/>
  <xsl:param name='admon.graphics.path'>/usr/share/dbdoclet/docbook/xsl/images/</xsl:param>
  <xsl:param name='callout.graphics.path'>/usr/share/dbdoclet/docbook/xsl/images/callouts/</xsl:param>
  <xsl:param name='draft.watermark.image'>/usr/share/dbdoclet/docbook/xsl/images/draft.png</xsl:param>
  <xsl:param name='fop.extensions'>0</xsl:param>
  <xsl:param name='fop1.extensions'>1</xsl:param>
</xsl:stylesheet>
