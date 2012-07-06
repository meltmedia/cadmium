<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="xsl">
<xsl:param name="host"/>
<xsl:param name="alias"/>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="Engine">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
    <xsl:if test="not(Host[@name=$host])">
      <Host name="{$host}">
        <Alias><xsl:value-of select="$alias"/></Alias>
        <Valve className="org.jboss.web.tomcat.service.jca.CachedConnectionValve"
               cachedConnectionManagerObjectName="jboss.jca:service=CachedConnectionManager"
               transactionManagerObjectName="jboss:service=TransactionManager" ></Valve>
      </Host>
    </xsl:if>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
