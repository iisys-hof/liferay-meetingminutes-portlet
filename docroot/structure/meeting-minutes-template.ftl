<#--
Web content templates are used to lay out the fields defined in a web
content structure.

Please use the left panel to quickly add commonly used variables.
Autocomplete is also available and can be invoked by typing "${".
-->


<h2>${.vars['reserved-article-title'].data}</h2>

<h3>Formalities</h3>
<p><strong>When:</strong> <#assign Date1699_DateObj = dateUtil.newDate(getterUtil.getLong(Date1699.getData()))>

${dateUtil.getDate(Date1699_DateObj, "dd.MM.yyyy", locale)}</p>
<p><strong>Where:</strong> ${Location.getData()}</p>
<p><strong>Participants:</strong> ${Participants.getData()}</p>
<p><strong>Recording Clerk:</strong> ${Protocoler.getData()}</p>

<h3>Agenda</h3>
${Agenda.getData()}


<#if TOP.getSiblings()?has_content>
	<#list TOP.getSiblings() as cur_TOP>
	
	<h3>${cur_TOP.getData()}</h3>
	${cur_TOP.HTML8060.data}
	
	</#list>
</#if>

<#if Task.getSiblings()?has_content>
<h2>Tasks</h2>
<ol>
	<#list Task.getSiblings() as cur_Task>
	
	<#if cur_Task.getData()?has_content>
	    <#assign deadline_DateObj = dateUtil.newDate(getterUtil.getLong(cur_Task.deadline.data))>
	    <li>${cur_Task.getData()} - fällig bis ${dateUtil.getDate(deadline_DateObj, "dd.MM.yyyy", locale)} - zugewiesen an: <strong>${cur_Task.assigned_to.data}</strong></li>
	</#if>
	
	</#list>
</ol>
</#if>