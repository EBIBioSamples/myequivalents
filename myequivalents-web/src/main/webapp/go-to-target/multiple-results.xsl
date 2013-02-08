<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  See uk.ac.ebi.fg.myequivalents.webservices.server.EntityMappingWebService.getMappingsForTargetRedirection()
  When /go-to-target is invoked, presumably by an HTML browser, you get either an HTTP redirection, if 
  there is only one target entity in the result, or some XML containing the resulting multiple targets, such XML
  points to an XSL that you can send in as a request parameter and, if you don't send anything this XSL is used by
  default. Of course you can use this template as a starting point for a more elaborate landing page.
  
  See the myEquivalents wiki for details. 
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
	<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>myEquivalents Web Service</title>
	</head>
	<body>
	<h1>myEquivalents Web Service</h1>
	
	<p>The source entity <b><xsl:value-of select='/mappings/@entity-id' /></b> has multiple equivalents at 
	   <b><xsl:value-of select='/mappings/@target-service-name' /></b>. Please choose one:
	   
	<ul>
	  <xsl:for-each select="/mappings/bundles/bundle/entity">
	    <xsl:sort select="concat( @service-name, @accession )" />
		  <li><a href = "{@uri}"><xsl:value-of select='@accession' /></a></li>
  	</xsl:for-each>
  </ul>
  
	</p>
	</body>
	</html>
</xsl:template>
</xsl:stylesheet>
