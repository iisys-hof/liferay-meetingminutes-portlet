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
 * Created for: Social Collaboration Hub (www.sc-hub.de)
 * Created at: Institute for Information Systems (www.iisys.de/en)
 * @author: Christian Ochsenkühn
 */
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.lang.Math" %>

<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="com.liferay.portal.theme.ThemeDisplay" %>
<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ page import="com.liferay.portal.kernel.util.Constants"%>

<portlet:defineObjects />

<%
	PortletPreferences prefs = renderRequest.getPreferences();
	String structureId = (String)prefs.getValue("structureKey", "");
	String templateId = (String)prefs.getValue("templateId", "");
	int cur = Integer.valueOf(prefs.getValue("curMinutes", "0"));
	int delta = 5;
	
	ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
	ArrayList<String> meetingMinutes = (ArrayList<String>)renderRequest.getAttribute("minutesList");
	ArrayList<Long> meetingMinutesIds = (ArrayList<Long>)renderRequest.getAttribute("meetingMinutesIds");
	ArrayList<String> articleIds = (ArrayList<String>)renderRequest.getAttribute("articleIds");
	ArrayList<Long> groupIds = (ArrayList<Long>)renderRequest.getAttribute("groupIds");
	
	int pageCount = (Integer)cur/delta+1;
	int pageMax = (int)Math.ceil(meetingMinutes.size() / (double)delta);
%>

<portlet:resourceURL var="getMinutes">
       <portlet:param name="<%=Constants.CMD%>" value="get_minutes" />
</portlet:resourceURL>

<liferay-portlet:renderURL var="editNewURL" portletName="15" windowState="maximized">
    <portlet:param name="struts_action" value="/journal/edit_article" />
    <portlet:param name="_15_structureId" value="<%= structureId %>" />
    <portlet:param name="_15_templateId" value="<%= templateId %>" />
</liferay-portlet:renderURL>


<p style="float:right; margin-bottom:10px;">
	<span style="margin-right:10px;">
	<liferay-ui:icon
		iconCssClass="icon-plus-sign"
		label="<%= true %>"
		message="de.iisys.minutes.addMeetingMinutes"
		url="<%= editNewURL %>"
	/></span>
</p>


<portlet:actionURL name="newTask" var="newTaskURL">
   	<portlet:param name="mvcPath" value="/html/addTask.jsp" />  
</portlet:actionURL>

<table class="table table-bordered table-condensed" style="clear:both;">
	<thead><tr>
		<th><liferay-ui:message key="de.iisys.minutes.meetingMinutes" /></th>
		<th> </th>
		<th> </th>
	</tr></thead>
	
	<tbody>
	<% int listEnd = cur+delta;
	if(cur+delta > meetingMinutes.size()) listEnd = meetingMinutes.size();
	for(int i=cur; i<listEnd; i++) {
		String min = meetingMinutes.get(i); %>
		<tr>
		<td>
			<a href="
				<portlet:actionURL name="viewMinutes">
			    	<portlet:param name="minutesId" value="<%= String.valueOf(meetingMinutesIds.get(i)) %>" />
				</portlet:actionURL>
			">
				<%= min %>
			</a>
		</td>
		<td>
			<a href="
					<liferay-portlet:renderURL portletName="15" windowState="maximized">
						<portlet:param name="struts_action" value="/journal/edit_article"/>
					    <portlet:param name="articleId" value="<%= articleIds.get(i) %>" />
					</liferay-portlet:renderURL>
			">
				<liferay-ui:icon iconCssClass="icon-edit" label="<%= true %>" message="edit" />
			</a>
		</td>
		<td>
			<a href="<portlet:actionURL name="newTask">
		   				<portlet:param name="mvcPath" value="/addTask.jsp" />
		   				<portlet:param name="minutesId" value="<%= String.valueOf(meetingMinutesIds.get(i)) %>" />
					</portlet:actionURL>
			">
				<liferay-ui:icon iconCssClass="icon-plus" label="<%= true %>" message="de.iisys.minutes.addTask" />
			</a>
		</td>
		</tr>
	<% } %>
	</tbody>
</table>

<portlet:actionURL name="setPagination" var="nextPageURL">
   	<portlet:param name="mvcPath" value="/view.jsp" />
   	<portlet:param name="nextCur" value="<%= String.valueOf(cur+delta) %>" />
</portlet:actionURL>

<portlet:actionURL name="setPagination" var="prevPageURL">
   	<portlet:param name="mvcPath" value="/view.jsp" />
   	<portlet:param name="nextCur" value="<%= String.valueOf(cur-delta) %>" />
</portlet:actionURL>

<portlet:actionURL name="setPagination" var="firstPageURL">
   	<portlet:param name="mvcPath" value="/view.jsp" />
   	<portlet:param name="nextCur" value="0" />
</portlet:actionURL>

<p style="float:right; margin-bottom:0;">(
	<% if(cur>0) { %><liferay-ui:icon iconCssClass="icon-step-backward" label="<%= false %>" message="" url="<%= firstPageURL %>" /> 
	<liferay-ui:icon iconCssClass="icon-backward" label="<%= false %>" message="" url="<%= prevPageURL %>" /> <% } %>
	Page <strong><%= pageCount %></strong> / <%= pageMax %>
	<% if(meetingMinutes.size() > cur+delta) { %> <liferay-ui:icon iconCssClass="icon-forward" label="<%= false %>" message="" url="<%= nextPageURL %>" /><% } %>
	)</p>
<div style="clear:both;"></div>

<div class="pagination pagination-mini pull-right">
	<ul id="<portlet:namespace/>meeting-minutes-pages" class="pagination-content"></ul>
</div>
		
<div style="clear:both;"></div>

<script type="text/javascript">

//pagination:

function <portlet:namespace/>updatePaginationView(targetBox,totalCases,curCaseStart) {
	var totalPages = Math.ceil( totalCases / <portlet:namespace/>CASES_PER_PAGE );
	var curPage = Math.ceil( (curCaseStart+1) / <portlet:namespace/>CASES_PER_PAGE );
	
	var THREE_DOTS = '<li class="disabled"><a href="#" onclick="return false;">...</a></li>';

	
	// page "prev":
	var temp = '<li'; 
	if(curPage===1) temp += ' class="disabled"';
	temp += '><a href="#" onclick="';
	if(curPage > 1) temp += "<portlet:namespace/>prevPage('" +targetBox+ "'); ";
	temp+= 'return false;" class="icon-chevron-left"></a></li>';
	// page 1:
	temp += '<li';
	if(curPage===1) temp += ' class="active"';
	temp += '><a href="#" onclick="';
	if(curPage!=1) temp += '<portlet:namespace/>changePage(\''+targetBox+'\',1); ';
	temp += 'return false;">1</a></li>';
	// three dots:
	if(curPage > 2) temp += THREE_DOTS;
	// current page:
	if(curPage > 1  &&  curPage < totalPages)
		temp += '<li class="active"><a href="#" onclick="return false;">'+curPage+'</a></li>';
	// three dots:
	if(curPage < (totalPages-1)) temp += THREE_DOTS;
	// last page:
	if(totalPages > 1) {
		temp += '<li';
		if(curPage===totalPages) temp += ' class="active"';
		temp += '><a href="#" onclick="';
		if(curPage!=totalPages) temp += '<portlet:namespace/>changePage(\''+targetBox+'\','+totalPages+'); ';
		temp += 'return false;">'+totalPages+'</a></li>';
	}
	// page "next":
	temp += '<li';
	if(totalPages<=curPage) temp += ' class="disabled"';
	temp += '><a href="#" onclick="';
	if(curPage < totalPages) temp += '<portlet:namespace/>nextPage(\''+targetBox+'\'); ';
	temp += 'return false;" class="icon-chevron-right"></a></li>';
	
	document.getElementById('<portlet:namespace/>'+targetBox+'-pages').innerHTML = temp;
}

function <portlet:namespace/>nextPage(targetBox) {
	<portlet:namespace/>changePage(targetBox,"+1");
}
function <portlet:namespace/>prevPage(targetBox) {
	<portlet:namespace/>changePage(targetBox,"-1");
}
function <portlet:namespace/>changePage(targetBox,pageNr) {
	var tempCur;
	
	switch(targetBox) {
		case "caseDefinitions":
			tempCur = <portlet:namespace/>curCaseDefinitions; break;
		case "caseInstances":
			tempCur = <portlet:namespace/>curCaseInstances; break;
	}
	
	switch(pageNr) {
		case "+1":
			tempCur += <portlet:namespace/>CASES_PER_PAGE;
			break;
		case "-1":
			tempCur -= <portlet:namespace/>CASES_PER_PAGE;
			if(tempCur < 0) tempCur = 0;
			break;
		default:
			tempCur = (pageNr-1) * <portlet:namespace/>CASES_PER_PAGE;
	}
	
	switch(targetBox) {
		case "caseDefinitions":
			<portlet:namespace/>curCaseDefinitions = tempCur;
			<portlet:namespace/>getCaseDefinitions();
			break;
		case "caseInstances":
			<portlet:namespace/>curCaseInstances = tempCur;
			<portlet:namespace/>getCaseInstances();
			break;
	}
}
</script>