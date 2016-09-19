package de.iisys.liferay.portlet.meetingMinutes;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

public class MeetingMinutesActions extends MVCPortlet {
	/*	private String CURRENT_USER = "demo";
	private String CURRENT_USER_PW = "secret";
	private String CURRENT_USER = "oxadmin";
	private String CURRENT_USER_PW = "admin_password"; */
	
	private final String CUR_USER_PROP = "current.user";
	private final String CUR_USER_PW_PROP = "current.user.pw";
	
	private final String MM_STRUCTURE_NAME = "MeetingMinutesStructure";
	private final String MM_STRUCTURE_NAME_DE = "Protokoll";
	private ThemeDisplay themeDisplay;
	private ResourceBundle rb;

/*	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException, PortletException {
		String cmd = ParamUtil.getString(resourceRequest, Constants.CMD);
		System.out.println("Constants.CMD: " + cmd);
		if (cmd.equals("get_users")) {
			getUsers(resourceRequest, resourceResponse);
		}
//		super.serveResource(resourceRequest, resourceResponse);
	} */
	
	public void render(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
		PortletPreferences prefs = request.getPreferences();
		rb = ResourceBundle.getBundle("content.Language", request.getLocale());
		
		String structureKey = DDMStructureCreator.isDDMStructureCreated(MM_STRUCTURE_NAME);
		if(structureKey.equals("0")) {
			System.out.println("Trying to import Web Content structure and template...");
			// add structure:
			String structureXml = DDMStructureCreator.readXML(getPortletContext().getRealPath("/")+"structure/meeting-minutes-structure.xml");
			Map<Locale,String> structureNameMap =  new HashMap<Locale,String>();
			structureNameMap.put(Locale.US, MM_STRUCTURE_NAME);
			structureNameMap.put(Locale.GERMANY, MM_STRUCTURE_NAME_DE);
			Map<Locale,String> descriptionMap =  new HashMap<Locale,String>();
			descriptionMap.put(Locale.US, "A structure for meeting minutes. It works best with the MeetingMinutes portlet.");
			descriptionMap.put(Locale.GERMANY, "Eine Struktur für Protokoll. Funktioniert am besten mit dem Protokoll Portlet.");
			DDMStructure structure = DDMStructureCreator.addDDMStructure(structureNameMap, descriptionMap, structureXml, themeDisplay.getUserId(), themeDisplay.getScopeGroupId());
			if(structure!=null)
				structureKey = structure.getStructureKey();
			
			// add template:
			String templateScript = DDMStructureCreator.readXML(getPortletContext().getRealPath("/")+"structure/meeting-minutes-template.ftl");
			Map<Locale,String> templateNameMap =  new HashMap<Locale,String>();
			templateNameMap.put(Locale.US, MM_STRUCTURE_NAME+"Template");
			templateNameMap.put(Locale.GERMANY, MM_STRUCTURE_NAME_DE+"Template");
			Map<Locale,String> templateDescriptionMap =  new HashMap<Locale,String>();
			templateDescriptionMap.put(Locale.US, "Template for the meeting minutes strucure. Created by the MeetingMinutes portlet.");
			templateDescriptionMap.put(Locale.GERMANY, "Template für die Protokoll-Struktur. Erstellt vom Protokoll Portlet.");
			DDMTemplate template = DDMStructureCreator.addDDMTemplate(templateNameMap, templateDescriptionMap, templateScript,
					structure.getPrimaryKey(), themeDisplay.getUserId(), themeDisplay.getScopeGroupId());
			if(template!=null)
				prefs.setValue("templateId", String.valueOf(template.getTemplateId()));
		}
		prefs.setValue("structureKey", structureKey);
		prefs.store();
		
		ArrayList<String> minutesList = new ArrayList<String>();
		ArrayList<Long> minutesIDList = new ArrayList<Long>();
		ArrayList<String> articleIdList = new ArrayList<String>();
		ArrayList<Long> groupIdList = new ArrayList<Long>();
		
		for(JournalArticle ja : getWebContentMeetingMinutes()) {
			String min = ja.getTitleCurrentValue();
			minutesList.add(min);
			minutesIDList.add(ja.getId());
			articleIdList.add(ja.getArticleId());
			groupIdList.add(ja.getGroupId());
		}
		request.setAttribute("minutesList", minutesList);
		request.setAttribute("meetingMinutesIds", minutesIDList);
		request.setAttribute( "articleIds", articleIdList );
		request.setAttribute( "groupIds", groupIdList );
		
		String loggedInUserPW = (String)PortalUtil.getHttpServletRequest(request).getSession().getAttribute(WebKeys.USER_PASSWORD);						
		System.out.println("pw from session: "+loggedInUserPW);
		
		super.render(request, response);
	}
	
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) 
			throws IOException, PortletException {
		themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		rb = ResourceBundle.getBundle("content.Language", actionRequest.getLocale());
		
		super.processAction(actionRequest, actionResponse);
	}
	
	public void setPagination(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {
		String nextCur = actionRequest.getParameter("nextCur");
		PortletPreferences prefs = actionRequest.getPreferences();
		prefs.setValue("curMinutes", nextCur);
		prefs.store();
	}
	
	public void viewMinutes(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {
		String minutesId = actionRequest.getParameter("minutesId");
		PortletPreferences prefs = actionRequest.getPreferences();
		prefs.setValue("minutesId", minutesId);
		try {
			JournalArticle ja = JournalArticleLocalServiceUtil.getArticle(Long.parseLong(minutesId));
			prefs.setValue("articleId", ja.getArticleId());
			prefs.setValue("groupId", String.valueOf(ja.getGroupId()));
		} catch (NumberFormatException | PortalException | SystemException e) {
			e.printStackTrace();
		}
		prefs.store();
		
		String portletName = (String)actionRequest.getAttribute(WebKeys.PORTLET_ID);
		PortletURL redirectURL = PortletURLFactoryUtil.create(PortalUtil.getHttpServletRequest(actionRequest), portletName, themeDisplay.getLayout().getPlid(), PortletRequest.RENDER_PHASE);
		redirectURL.setParameter("jspPage", "/html/view_minutes.jsp");
		actionResponse.sendRedirect(redirectURL.toString());
	}
	
	public void newTask(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {
		String minutesId = actionRequest.getParameter("minutesId");
		PortletPreferences prefs = actionRequest.getPreferences();
		prefs.setValue("minutesId", minutesId);
		try {
			JournalArticle ja = JournalArticleLocalServiceUtil.getArticle(Long.parseLong(minutesId));
			prefs.setValue("minutesName", ja.getTitleCurrentValue());
			prefs.setValue("articleId", ja.getArticleId());
			prefs.setValue("groupId", String.valueOf(ja.getGroupId()));
			System.out.println("Structure Id: "+ja.getStructureId());
			String name = DDMStructureLocalServiceUtil.getStructure( Long.valueOf(ja.getStructureId())+1 ).getNameCurrentValue();
			System.out.println("Name: "+name);
		} catch (NumberFormatException | PortalException | SystemException e) {
			e.printStackTrace();
		}
		prefs.store();
		
		String portletName = (String)actionRequest.getAttribute(WebKeys.PORTLET_ID);
		PortletURL redirectURL = PortletURLFactoryUtil.create(PortalUtil.getHttpServletRequest(actionRequest), portletName, themeDisplay.getLayout().getPlid(), PortletRequest.RENDER_PHASE);
		redirectURL.setParameter("jspPage", "/html/addTask.jsp");
		actionResponse.sendRedirect(redirectURL.toString());
	}
	
	/**
	 * Adds a new task. Called from addTask.jsp after submit.
	 * Calls addTaskToWebContent().
	 * If chosen: Adds task to OX (through OXRESTConnector).
	 * If chosen: Adds task to Camunda (through CamundaRESTConnector).
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws PortletException
	 */
	public void addTaskToMinutes(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		String minutesId = actionRequest.getParameter("minutesId");
		String userId = actionRequest.getParameter("userId");
		String userName = actionRequest.getParameter("userName");
		String firstName = actionRequest.getParameter("firstName");
		String lastName = actionRequest.getParameter("lastName");
		String userMail = actionRequest.getParameter("userMail");
		String title = actionRequest.getParameter("title");
		String description =  actionRequest.getParameter("description");
		String when = actionRequest.getParameter("when");
		String time = actionRequest.getParameter("time");
		when = when+"T"+time+":00";
		Date dateWhen = new Date();
		try {
			dateWhen = formater.parse(when);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String addTaskToOX = actionRequest.getParameter("addToOX");
		String addTaskToCamunda = actionRequest.getParameter("addTaskToCamunda");
		
		System.out.println("Add Task: "+title+"User: "+userName+" ("+userId+"), "+when+", to OX: "+addTaskToOX+", to Camunda: "+addTaskToCamunda);
		
		// add task to web content article:
		try {
			addTaskToWebContent(JournalArticleLocalServiceUtil.getArticle(Long.parseLong(minutesId)),
					userName, title, dateWhen);
//			SessionMessages.add(actionRequest, "request_processed", rb.getString("de.iisys.minutes.successTaskAdded") );
			SessionMessages.add(actionRequest, "request_processed", 
					LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "de.iisys.minutes.successTaskAdded"));
		} catch (PortalException | SystemException | NumberFormatException e) {
			e.printStackTrace();
//			SessionErrors.add(actionRequest, "error-key", rb.getString("de.iisys.minutes.errorTaskAdded") );
			SessionErrors.add(actionRequest, "error-key", 
					LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "de.iisys.minutes.errorTaskAdded"));
		}
		
		if(addTaskToOX.equals("true") || addTaskToCamunda.equals("true")) {
			
			String loggedInUserId = themeDisplay.getUser().getScreenName();
			String loggedInUserPW = (String)PortalUtil.getHttpServletRequest(actionRequest).getSession().getAttribute(WebKeys.USER_PASSWORD);						
			System.out.println("pw from session: "+loggedInUserPW);
			
			// add task to open-xchange:
			if(addTaskToOX.equals("true")) {
				OXRESTConnector oxCon;
				if(loggedInUserPW!=null)
					oxCon = new OXRESTConnector(loggedInUserId, loggedInUserPW);
				else
					oxCon = new OXRESTConnector();
				JSONObject session = oxCon.oxLogin();
				
				String folderId = oxCon.oxGetRootFolder(session, "tasks", "");
				List<String> oxUserIds = new ArrayList<String>();
				JSONArray jsonUsers = oxCon.oxGetUserByName(firstName, lastName, session).getJSONArray("data");
				for(int i=0; i<jsonUsers.length(); i++) {
					oxUserIds.add(jsonUsers.getJSONArray(i).getString(0));
				}
				String whenInMillis = "";
				whenInMillis = String.valueOf(dateWhen.getTime()+TimeUnit.DAYS.toMillis(1));
				oxCon.oxAddTask(session, folderId, title, description, whenInMillis, oxUserIds);
			}
			
			// add task to camunda:
			if(addTaskToCamunda.equals("true")) {
				CamundaRESTConnector camundaCon;
				if(loggedInUserPW!=null)
					camundaCon = new CamundaRESTConnector(loggedInUserId, loggedInUserPW);
				else
					camundaCon = new CamundaRESTConnector();

				try {
					String camundaUserId = camundaCon.camundaGetUsersByEmail(userMail).getJSONObject(0).getString("id");
					camundaCon.camundaCreateTask(title, description, camundaUserId, when);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void addTaskToWebContent(JournalArticle ja, String userName, String taskTitle, Date when) {		
		String jaContent = ja.getContent();
		int taskIndex=0;
		while(jaContent.indexOf("name=\"Task\" index=\""+taskIndex+"\"") != -1) {
			taskIndex++;
		}
		
		String taskAsXml = "<dynamic-element name=\"Task\" index=\""+taskIndex+"\" type=\"text_box\" index-type=\"keyword\">"+
				"<dynamic-element name=\"deadline\" index=\"0\" type=\"ddm-date\" index-type=\"keyword\">"+
					"<dynamic-content language-id=\"en_US\"><![CDATA["+when.getTime()+"]]></dynamic-content>"+
				"</dynamic-element>"+
				"<dynamic-element name=\"assigned_to\" index=\"0\" type=\"text\" index-type=\"keyword\">"+
					"<dynamic-content language-id=\"en_US\"><![CDATA["+userName+"]]></dynamic-content>"+
				"</dynamic-element>"+
				"<dynamic-content language-id=\"en_US\"><![CDATA["+taskTitle+"]]></dynamic-content>"+
			"</dynamic-element>";
		
		jaContent = jaContent.substring(0, jaContent.length()-"</root>".length());
		jaContent = jaContent + taskAsXml + "</root>";
//		System.out.println("ja: "+jaContent);
		
		ja.setContent(jaContent);
		try {
			JournalArticleLocalServiceUtil.updateContent(ja.getGroupId(), ja.getArticleId(), ja.getVersion(), jaContent);
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
	}
	
	private List<JournalArticle> getWebContentMeetingMinutes() {
//		System.out.println("getWebContentMeetingMinutes()");
		List<JournalArticle> minutes = new ArrayList<JournalArticle>();
		try {
			List<JournalArticle> jaList = JournalArticleLocalServiceUtil.getArticles();
			
			for(JournalArticle ja : jaList) {
				if(ja.getStructureId().equals("")) continue;
//				String structureName = DDMStructureLocalServiceUtil.getStructure( Long.valueOf(ja.getStructureId())+1 ).getNameCurrentValue();
				DDMStructure struct = DDMStructureLocalServiceUtil.getStructure( Long.valueOf(ja.getStructureId())+1 );
//				String structureName = struct.getNameCurrentValue();
				String structureName = struct.getName(Locale.US);

/*				System.out.println("StructureKey: "+struct.getStructureKey()+
						"StructureId: "+struct.getStructureId()+
						"PrimaryKey: "+struct.getPrimaryKey()); */
				
				if(structureName.equals(MM_STRUCTURE_NAME) ) {
					if(JournalArticleLocalServiceUtil.isLatestVersion(ja.getGroupId(), ja.getArticleId(), ja.getVersion()))
						minutes.add(JournalArticleLocalServiceUtil.getLatestArticle(ja.getResourcePrimKey()));
				}
			}
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return minutes;
	}
	
	/*
	private void getUsers(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException,	PortletException {
		JSONArray usersJSONArray = JSONFactoryUtil.createJSONArray();
		ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String userEmail = ParamUtil.getString(resourceRequest, "userEmail");
		System.out.println("==0000== Input:" + userEmail);
		DynamicQuery userQuery = DynamicQueryFactoryUtil.forClass(User.class,
				PortalClassLoaderUtil.getClassLoader());

		// .ilike = case insensitive, .like = case sensitive
		// other possibilities than "lastName": emailAddress, firstName
		Criterion criterion = RestrictionsFactoryUtil.ilike("lastName",
				StringPool.PERCENT + userEmail + StringPool.PERCENT);
		userQuery.add(criterion);
		JSONObject userJSON = null;
//		System.out.println("==111==" + userQuery.toString() );
		try {
			List<User> userList = UserLocalServiceUtil.dynamicQuery(userQuery);
			System.out.println("==222== Found users: " + userList.size());
			for (User user : userList) {
				userJSON = JSONFactoryUtil.createJSONObject();
				userJSON.put("userId", user.getUserId());
				userJSON.put("email", user.getEmailAddress());
				userJSON.put("firstName", user.getFirstName());
				userJSON.put("lastName", user.getLastName());
				userJSON.put("fullName", user.getFullName());
				usersJSONArray.put(userJSON);
			}
		} catch (Exception e) {}
		PrintWriter out = resourceResponse.getWriter();
		out.println(usersJSONArray.toString());
	}
	*/
	
/* User Autocomplete: */
	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException, PortletException {
		String resourceID = resourceRequest.getResourceID();
		if(resourceID.equals("autocomplete")) {
			getUsersForAutocomplete(resourceRequest, resourceResponse);
		} else {
			super.serveResource(resourceRequest, resourceResponse);
		}
	}
	
	private void getUsersForAutocomplete(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException,	PortletException {
		String keywords = ParamUtil.getString(resourceRequest, "keywords");
		 
		JSONObject json = JSONFactoryUtil.createJSONObject();
		JSONArray results = JSONFactoryUtil.createJSONArray();
		json.put("response", results);
 
		try {
			DynamicQuery query = DynamicQueryFactoryUtil
					.forClass(User.class);
//			query.add(PropertyFactoryUtil.forName("firstName").like(
//					StringPool.PERCENT + keywords + StringPool.PERCENT));
			Criterion criterion = RestrictionsFactoryUtil.ilike("firstName",
					StringPool.PERCENT + keywords + StringPool.PERCENT);
			Criterion criterion2 = RestrictionsFactoryUtil.ilike("lastName",
					StringPool.PERCENT + keywords + StringPool.PERCENT);
			
			query.add(RestrictionsFactoryUtil.or(criterion, criterion2));
			
			List<User> userNames = UserLocalServiceUtil.dynamicQuery(query);
 
			for (User user : userNames) {
				JSONObject object = JSONFactoryUtil.createJSONObject();
				object.put("userId", user.getScreenName());
				object.put("fullName", user.getFullName());
				object.put("email", user.getEmailAddress());
				object.put("firstName", user.getFirstName());
				object.put("lastName", user.getLastName());

				results.put(object);
			}
		} catch (SystemException e) {
			e.printStackTrace();
		}
 
		writeJSON(resourceRequest, resourceResponse, json);
	}
	
}
