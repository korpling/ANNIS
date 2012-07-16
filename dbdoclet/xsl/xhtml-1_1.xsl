<xsl:stylesheet exclude-result-prefixes='#default' version='1.0' xmlns:fo='http://www.w3.org/1999/XSL/Format' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/xhtml-1_1/chunkfast.xsl'/>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/xhtml-1_1/highlight.xsl'/>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/xhtml-1_1/autoidx-kosek.xsl'/>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/xhtml-1_1/autoidx-kimber.xsl'/>
  <xsl:include href='http://dbdoclet.org/xsl/html/synopsis.xsl'/>
  <xsl:include href='http://dbdoclet.org/xsl/html/themes/color.xsl'/>
  <xsl:param name='admon.graphics'>0</xsl:param>
  <xsl:param name='admon.graphics.extension'>.gif</xsl:param>
  <xsl:param name='admon.graphics.path'>images/</xsl:param>
  <xsl:param name='admon.style'>margin:0.5in</xsl:param>
  <xsl:param name='admon.textlabel'>1</xsl:param>
  <xsl:param name='base.dir'>./build/html/</xsl:param>
  <xsl:param name='chunk.fast'>1</xsl:param>
  <xsl:param name='current.docid'>dbdoclet</xsl:param>
  <xsl:param name='dbdoclet.version'>1</xsl:param>
  <xsl:param name='html.stylesheet'>dbdoclet.css</xsl:param>
  <xsl:param name='img.src.path'/>
  <xsl:param name='keep.relative.image.uris'>1</xsl:param>
  <xsl:param name='preferred.mediaobject.role'>html</xsl:param>
  <xsl:param name='use.role.for.mediaobject'>1</xsl:param>
</xsl:stylesheet>
