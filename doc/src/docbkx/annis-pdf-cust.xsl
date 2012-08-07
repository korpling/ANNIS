<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xslthl="http://xslthl.sf.net"
  xmlns:d="http://docbook.org/ns/docbook" xmlns:fo="http://www.w3.org/1999/XSL/Format"
  exclude-result-prefixes="xslthl" version="1.0">

  <xsl:import href="urn:docbkx:stylesheet"/>

  <!-- highlight.xsl must be imported in order to enable highlighting support, highlightSource=1 parameter
   is not sufficient -->
  <xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/>

  <!-- Make hyperlinks blue but still display the underlying URL -->
  <xsl:param name="ulink.show" select="1"/>

  <xsl:attribute-set name="xref.properties">
    <xsl:attribute name="color">blue</xsl:attribute>
  </xsl:attribute-set>


</xsl:stylesheet>
