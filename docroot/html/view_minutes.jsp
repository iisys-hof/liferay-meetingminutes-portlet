<%
/**
 * Copyright (c) 2015-present. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * By Christian Ochsenkühn for SCHub (www.sc-hub.de)
 * Institute for Information Systems (www.iisys.de/en)
 */
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="com.liferay.portal.theme.ThemeDisplay" %>
<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ page import="com.liferay.portal.kernel.util.Constants"%>

<%@ page import="com.liferay.portlet.journal.model.JournalArticle" %>
<%@ page import="com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil" %>
<%@ page import="com.liferay.portlet.asset.model.AssetEntry" %>
<%@ page import="com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil" %>

<portlet:defineObjects />

<%
	PortletPreferences prefs = renderRequest.getPreferences();
	String articleId = (String)prefs.getValue("articleId", "0");
	long groupId = Long.valueOf(prefs.getValue("groupId", "0"));
	long minutesId = Long.valueOf(prefs.getValue("minutesId", "0"));
	
	AssetEntry assetEntry = null;
	JournalArticle article = null;
	try {
		article = JournalArticleLocalServiceUtil.getArticle(minutesId);
	} catch(Exception e) {}
	
	if(article!=null) {
		assetEntry = AssetEntryLocalServiceUtil.getEntry(JournalArticle.class.getName(), article.getResourcePrimKey());
	}
%>

<portlet:renderURL var="backURL">
	<portlet:param name="mvcPath" value="/html/view.jsp" />
</portlet:renderURL>


<liferay-ui:header title="de.iisys.minutes.meetingMinutes" backURL="<%= backURL %>" />

<liferay-ui:asset-tags-summary 
   className="<%= JournalArticle.class.getName() %>" 
   classPK="<%= (article!=null) ? article.getResourcePrimKey() : 0 %>" />

<liferay-ui:journal-article articleId="<%= articleId %>" groupId="<%= groupId %>" />

<liferay-ui:asset-links
			assetEntryId="<%=(assetEntry != null) ? assetEntry.getEntryId() : 0%>"
			className="<%=JournalArticle.class.getName()%>"
			classPK="<%=(article!=null) ? article.getResourcePrimKey() : 0 %>" />


