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
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>

<portlet:defineObjects />

<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="com.liferay.portal.kernel.util.Constants"%>
<%--@ include file="init.jsp"--%>

<%
	PortletPreferences prefs = renderRequest.getPreferences();
	String minutesId = (String)prefs.getValue("minutesId", "0");
	String minutesName = (String)prefs.getValue("minutesName", "Meeting Minutes");
	String articleId = (String)prefs.getValue("articleId", "0");
	long groupId = Long.valueOf(prefs.getValue("groupId", "0"));
%>

<portlet:actionURL name="addTaskToMinutes" var="saveEntryURL">
   	<portlet:param name="mvcPath" value="/html/view.jsp" />
</portlet:actionURL>

<portlet:renderURL var="viewBackURL">
    <portlet:param name="mvcPath" value="/html/view.jsp" />
</portlet:renderURL>

<portlet:resourceURL var="getUsers">
       <portlet:param name="<%=Constants.CMD%>" value="get_users" />
</portlet:resourceURL>

<liferay-portlet:resourceURL id="autocomplete" var="autocompleteURL" />

<aui:form action="<%= saveEntryURL %>" method="post">
	<aui:fieldset>
		<legend class="fieldset-legend">
			<span class="legend">
				<liferay-ui:message key="de.iisys.minutes.addTaskTo" arguments="<%= minutesName %>" />
			</span>
		</legend>
		<aui:input name="minutesId" type="hidden" value="<%= minutesId %>" />
		
		<aui:input name="title" />
		<aui:input name="description" type="textarea" />
		<aui:input name="when" type="date" label="de.iisys.minutes.due" />
		<aui:input name="time" id="timeControl" type="text" placeholder="hh:mm" value="11:11" label="de.iisys.minutes.dueTime" />
		
		<aui:input id="userInputNode" name="userInputNode" label="de.iisys.minutes.assignToUser"
       helpMessage="de.iisys.minutes.typeLastname" /> 
		<p><span id="<portlet:namespace />showUserMail"></span></p>
		
		<aui:input id="userId" name="userId" type="hidden" value="" />
		<aui:input id="userName" name="userName" type="hidden" value="" />
		<aui:input id="firstName" name="firstName" type="hidden" value="" />
		<aui:input id="lastName" name="lastName" type="hidden" value="" />
		<aui:input id="userMail" name="userMail" type="hidden" value="" />
		
		<aui:field-wrapper>
			<aui:input id="addToOX" name="addToOX" type="checkbox" label="de.iisys.minutes.addTaskToOX" disabled="true" helpMessage="de.iisys.minutes.assignUserFirst" />
			<aui:input id="addTaskToCamunda" name="addTaskToCamunda" type="checkbox" label="de.iisys.minutes.addTaskToCamunda" disabled="true"  helpMessage="de.iisys.minutes.assignUserFirst" />
		</aui:field-wrapper>
		
		<aui:button-row>
        	<aui:button type="submit" value="de.iisys.minutes.addTask" />
        	<aui:button type="cancel" onClick="<%= viewBackURL %>" />
        </aui:button-row>
	</aui:fieldset>
</aui:form>

<aui:fieldset name="de.iisys.minutes.meetingMinutes" label="de.iisys.minutes.meetingMinutes">
	<liferay-ui:journal-article articleId="<%= articleId %>" groupId="<%= groupId %>" />
</aui:fieldset>


<aui:script>

	//Recipients - user autocomplete:
	AUI().use('autocomplete-list','datasource-io',function(A) {
		var datasource = new A.DataSource.IO({
			source: '<%=autocompleteURL%>'
		});	
		
		var autoComplete = new A.AutoCompleteList({
			allowBrowserAutocomplete: false,
			activateFirstItem: true,
			inputNode: '#<portlet:namespace />userInputNode',
			maxResults: 10,
			on: {
				select: function(event) {
					var result = event.result.raw;
					
					A.one("#<portlet:namespace />userId").set('value', result.userId);
					A.one("#<portlet:namespace />userName").set('value', result.fullName);
					A.one("#<portlet:namespace />lastName").set('value', result.lastName);
					A.one("#<portlet:namespace />firstName").set('value',result.firstName);
					A.one("#<portlet:namespace />userMail").set('value', result.email);
					
					A.one("#<portlet:namespace />showUserMail").set('text', 'Email: '+result.email);
					A.one("#<portlet:namespace />addToOX"+"Checkbox").set('disabled', false);
					A.one("#<portlet:namespace />addTaskToCamunda"+"Checkbox").set('disabled', false);
				}
			},
			render: true,
			source: datasource,
			requestTemplate: '&<portlet:namespace />keywords={query}',
			resultListLocator: function (response) {
				var responseData = A.JSON.parse(response[0].responseText);
				return responseData.response;
			},
			resultTextLocator: function (result) {
				return result.fullName;
			},
			resultHighlighter: 'phraseMatch'
		});
	});
	
	// Time Picker:
	AUI().use(
	  'aui-timepicker',
	  function(A) {
	    new A.TimePicker(
	      {
	        trigger: '#<portlet:namespace />timeControl',
	        mask: '%H:%M',
	        popover: {
	          zIndex: 1
	        },
	        on: {
	          selectionChange: function(event) {
	            console.log(event.newSelection)
	          }
	        }
	      }
	    );
	  }
	);
	

/*
	AUI().use('autocomplete-list','aui-base','aui-io-request','autocomplete-filters','autocomplete-highlighters',function (A) {
		var testData;
		var autoList = new A.AutoCompleteList({
			allowBrowserAutocomplete: 'false',
			activateFirstItem: 'true',
			inputNode: '#<portlet:namespace />userInputNode',
			resultTextLocator:'fullName',
			render: 'true',
			resultHighlighter: 'phraseMatch',
			resultFilters:['phraseMatch'],
			source:function(){
				var inputValue=A.one("#<portlet:namespace />userInputNode").get('value');

				var myAjaxRequest = A.io.request('<%=getUsers.toString()%>',{
					dataType: 'json',
					method:'POST',
					data:{
						<portlet:namespace />userEmail:inputValue,
					},
					autoLoad:false,
					sync:false,
					on: {
						success:function(){
							var data=this.get('responseData');
							testData=data;
						}
					}
				});
				myAjaxRequest.start();
				return testData;
			},
		});
		
		autoList.on('select', function(e) {
			A.one("#<portlet:namespace />userId").set('value', e.result.raw.userId);
			A.one("#<portlet:namespace />userName").set('value', e.result.raw.fullName);
			A.one("#<portlet:namespace />lastName").set('value', e.result.raw.lastName);
			A.one("#<portlet:namespace />firstName").set('value', e.result.raw.firstName);
			A.one("#<portlet:namespace />userMail").set('value', e.result.raw.email);
			
			A.one("#<portlet:namespace />showUserMail").set('text', 'Email: '+e.result.raw.email);
			A.one("#<portlet:namespace />addToOX"+"Checkbox").set('disabled', false);
			A.one("#<portlet:namespace />addTaskToCamunda"+"Checkbox").set('disabled', false);
		});
	});
*/
</aui:script>