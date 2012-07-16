<xsl:stylesheet exclude-result-prefixes='#default' version='1.0' xmlns:fo='http://www.w3.org/1999/XSL/Format' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
  <xsl:import href='http://docbook.sourceforge.net/release/xsl/current/manpages/docbook.xsl'/>
  <xsl:param name='admon.graphics'>1</xsl:param>
  <xsl:param name='admon.graphics.extension'>.gif</xsl:param>
  <xsl:param name='admon.graphics.path'>/usr/share/dbdoclet/docbook/xsl/images/</xsl:param>
  <xsl:param name='alignment'>left</xsl:param>
  <xsl:param name='autotoc.label.separator'> </xsl:param>
  <xsl:param name='body.font.family'>serif</xsl:param>
  <xsl:param name='body.font.master'>10</xsl:param>
  <xsl:param name='body.start.indent'>2pt</xsl:param>
  <xsl:param name='callout.graphics'>1</xsl:param>
  <xsl:param name='callout.graphics.extension'>.gif</xsl:param>
  <xsl:param name='callout.graphics.path'>/usr/share/dbdoclet/docbook/xsl/images/callouts/</xsl:param>
  <xsl:param name='chapter.autolabel'>1</xsl:param>
  <xsl:param name='column.count.back'>1</xsl:param>
  <xsl:param name='column.count.body'>1</xsl:param>
  <xsl:param name='column.count.front'>1</xsl:param>
  <xsl:param name='column.count.index'>1</xsl:param>
  <xsl:param name='double.sided'>0</xsl:param>
  <xsl:param name='draft.mode'>no</xsl:param>
  <xsl:param name='draft.watermark.image'/>
  <xsl:param name='fop.extensions'>0</xsl:param>
  <xsl:param name='fop1.extensions'>1</xsl:param>
  <xsl:param name='generate.index'>1</xsl:param>
  <xsl:param name='insert.xref.page.number'>1</xsl:param>
  <xsl:param name='page.orientation'>portrait</xsl:param>
  <xsl:param name='paper.type'>A4</xsl:param>
  <xsl:param name='section.autolabel'>0</xsl:param>
  <xsl:param name='section.label.includes.component.label'>0</xsl:param>
  <xsl:param name='shade.verbatim'>0</xsl:param>
  <xsl:param name='tablecolumns.extension'>0</xsl:param>
  <xsl:param name='title.margin.left'>0in</xsl:param>
  <xsl:param name='toc.section.depth'>1</xsl:param>
  <xsl:param name='use.extensions'>1</xsl:param>
  <xsl:template match='classsynopsisinfo' mode='java'>
    <xsl:if test='@role = &apos;comment&apos;'>
            <xsl:text>
  </xsl:text>
        </xsl:if>
    <xsl:apply-templates mode='java'/>
    <xsl:if test='@role = &apos;comment&apos;'>
            <xsl:text>
      </xsl:text>
        </xsl:if>
  </xsl:template>
  <xsl:template name='synop-break'>
    <xsl:text>
.
</xsl:text>
  </xsl:template>
</xsl:stylesheet>
