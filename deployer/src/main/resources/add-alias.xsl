<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="xsl">
  <xsl:param name="host" select="'localhost'"/>
  <xsl:param name="alias"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="Host[@name=$host]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="not(Alias=$alias)">
        <Alias><xsl:value-of select="$alias"/></Alias>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
