package de.iisys.liferay.portlet.meetingMinutes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.util.portlet.PortletProps;

/**
 * A class to connect the Open-Xchange REST API.
 * <p>You can either use the low level methods to make your own PUT, POST or GET calls. Or you can use some pre-built API calls.</p>
 * <p>Note: Change IP_ADDRESS, FOLDER and API_FOLDER for your own purposes.</p>
 * <ul>
 * <li>Created for: Social Collaboration Hub (www.sc-hub.de)</li>
 * <li>Created at: Institute for Information Systems (www.iisys.de/en)</li>
 * </ul>
 * @author Christian Ochsenkühn
 *
 */
public class OXRESTConnector {
	private final String PROP_OX_URL = "ox_api_url";
	private final static String PROP_DEBUG_USER = "debug_user";
	private final static String PROP_DEBUG_PW = "debug_pw";
	
	private String OX_URL;
	private String userId;
	private String userPw;
	private HttpURLConnection httpURLConnection;	
	private List<String> cookies = null;
	
	/**
	 * Connector to the Open-Xchange REST api.<br />
	 * Before you are able to make any other request you have to call the oxLogin function.
	 */
	public OXRESTConnector() {
		this(PortletProps.get(PROP_DEBUG_USER), PortletProps.get(PROP_DEBUG_PW));
	}
	
	public OXRESTConnector(String userId, String userPw) {
		OX_URL = PortletProps.get(PROP_OX_URL);
		System.out.println(userId+": "+userPw);
		this.userId = userId;
		this.userPw = userPw;
	}
	
	/**
	 * Logs into OX with the given credentials and returns a session in JSON notation.
	 * @param username
	 * @param password
	 * @return JSONObject with "session", "user", "user_id", "context_id", "locale"
	 */
	public JSONObject oxLogin() {
		String parameters = "&name="+this.userId+"&password="+this.userPw;
		return oxPost(OX_URL+"/login?action=login",null,parameters,"");
	}
	
	/**
	 * Adds a task in OX for the given session.
	 * @param session: Take the session from oxLogin()!
	 * @param title
	 * @param description
	 * @param when
	 * @param targetUsers
	 * @return
	 */
	public JSONObject oxAddTask(JSONObject session, String folderId, String title, String description, String when, List<String> targetUsers) {
		StringBuffer participants = new StringBuffer("["); int i=0;
		for(String userId : targetUsers) {
			if(i++ > 0) participants.append(",");
			participants.append("{\"id\":"+userId+",\"type\":1}");
			// type 1 = "user"
		}
		participants.append("]");
		
		String jsonRequestBody = "{"+
				"\"folder_id\":\""+folderId+"\","+
				"\"created_by\":\""+session.getString("user_id")+"\","+
	    		"\"title\":\""+title+"\","+
	    		"\"note\":\""+description+"\","+
	    		"\"end_time\":\""+when+"\","+
	    		"\"participants\":"+participants.toString()+""+
			"}";
		System.out.println("Sent JSON: "+jsonRequestBody);
		return oxPut(OX_URL+"/tasks?action=new",session,"",jsonRequestBody);
	}
	
	/**
	 * Receives all visible folders for a certain module.
	 * http://oxpedia.org/wiki/index.php?title=HTTP_API#Get_all_visible_folder_of_a_certain_module_.28since_v6.18.2.29
	 * @param session: Take the session from oxLogin()!
	 * @param module: e.g. "tasks", "calendar", "contacts", "mail"
	 * @param columns: e.g. "300,301" for title and type (see OX documentation)
	 * @return JSON Object, e.g. {"data":{"private":[["28","1","Tasks",1,"tasks"]]},"timestamp":1234567891234}
	 */
	public JSONObject oxGetRootFolders(JSONObject session, String module, String columns) {
		String parameters = "&content_type="+module+"&columns=1,"+columns;
		return oxPut(OX_URL+"/folders?action=allVisible",session,parameters,"");
	}
	
	public String oxGetRootFolder(JSONObject session, String module, String columns) {
		JSONObject taskFolders = oxGetRootFolders(session, module, columns);
		return taskFolders.getJSONObject("data").getJSONArray("private").getJSONArray(0).getString(0);
	}
	
	public void oxGetListOfTasks(JSONObject session) {
		String parameters = "&columns=1,20,200";
		oxPut(OX_URL+"/tasks?action=list",session,parameters,"");
	}
	
	/**
	 * Returns a Open-Xchange contact matching the given email address.
	 * @param email: The user's email address
	 * @param session: Take the session from oxLogin()!
	 * @return JSON Object, e.g. {"data":[[2,"Bitte, Bärbel"]],"timestamp":1424856470796}
	 */
	public JSONObject oxGetContactByEmail(String email, JSONObject session) {
		String parameters = "&columns=1,500";
		String jsonRequestBody = "{"+
				"\"email1\":\""+email+"\""+
			"}";
		return oxPut(OX_URL+"/contacts?action=search", session, parameters, jsonRequestBody);
	}
	
	public JSONObject oxGetUserByName(String firstName, String lastName, JSONObject session) {
		String parameters = "&columns=1,500";
		String jsonRequestBody = "{"+
				"\"last_name\":\""+lastName+"\","+
				"\"emailAutoComplete\":true"+
			"}";
		return oxPut(OX_URL+"/user?action=search", session, parameters, jsonRequestBody);
	}
	
	
	/**
	 * Performs a http GET request to Open-Xchange.
	 * @param urlString: The url to post to
	 * @param session: Take the session from oxLogin()!
	 * @param parameters: The request parameters "&key1=value1&key2=value2"
	 * @param jsonRequestBody: The request body in JSON notation
	 * @return: The http response in JSON notation
	 */
	public JSONObject oxGet(String urlString, JSONObject session, String parameters, String jsonRequestBody) {
		return oxHttpRequest("GET", urlString, session, parameters, jsonRequestBody);
	}
	
	/**
	 * Performs a http POST request to Open-Xchange.
	 * @param urlString: The url to post to
	 * @param session: Take the session from oxLogin()!
	 * @param parameters: The request parameters "&key1=value1&key2=value2"
	 * @param jsonRequestBody: The request body in JSON notation
	 * @return: The http response in JSON notation
	 */
	public JSONObject oxPost(String urlString, JSONObject session, String parameters, String jsonRequestBody) {
		return oxHttpRequest("POST", urlString, session, parameters, jsonRequestBody);
	}
	
	/**
	 * Performs a http PUT request to Open-Xchange.
	 * @param urlString: The url to post to
	 * @param session: Take the session from oxLogin()!
	 * @param parameters: The request parameters "&key1=value1&key2=value2"
	 * @param jsonRequestBody: The request body in JSON notation
	 * @return: The http response in JSON notation
	 */
	public JSONObject oxPut(String urlString, JSONObject session, String parameters, String jsonRequestBody) {	
		return oxHttpRequest("PUT", urlString, session, parameters, jsonRequestBody);
	}
	
	/**
	 * Performs a request to Open-Xchange and uses the session and cookies for authentication.
	 * @param requestMethod: GET, POST or PUT
	 * @param urlString
	 * @param session
	 * @param parameters
	 * @param jsonRequestBody
	 * @return: The http response in JSON notation
	 */
	private JSONObject oxHttpRequest(String requestMethod, String urlString, JSONObject session, String parameters, String jsonRequestBody) {
		StringBuffer outputBuffer = new StringBuffer();
		if(session!=null)
			outputBuffer.append("&session="+session.getString("session"));
		if(!parameters.isEmpty())
			outputBuffer.append(parameters);
		if(!jsonRequestBody.isEmpty())
			outputBuffer.append("&data="+jsonRequestBody);
		
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			if(cookies!=null) {
				//add session cookies:
			    StringBuffer cookieString = new StringBuffer();
			    for(String cookie : cookies) {
			    	cookieString.append(cookie+"; ");
			    }
			    httpURLConnection.setRequestProperty("Cookie", cookieString.toString());
			}
			
			httpURLConnection.setRequestMethod(requestMethod);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);

			DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			dataOutputStream.writeBytes(outputBuffer.toString());
			dataOutputStream.flush();
			dataOutputStream.close();
	 
			int responseCode = httpURLConnection.getResponseCode();
			System.out.println("\nSending '"+requestMethod+"' request to URL : " + urlString);
			System.out.println("Parameters : " + parameters);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String inputLine;
			StringBuffer httpResponse = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				httpResponse.append(inputLine);
			}
			in.close();
			
			if(cookies==null || cookies.isEmpty()) {
				//save session cookies:
				cookies = new ArrayList<String>();
				String headerName = null;
				for(int i=1; (headerName = httpURLConnection.getHeaderFieldKey(i))!=null; i++) {
					if(headerName.equals("Set-Cookie")) {
						String cookie = httpURLConnection.getHeaderField(i);
						cookie = cookie.substring(0, cookie.indexOf(";"));
						System.out.println("Cookie Name: "+cookie.substring(0, cookie.indexOf("=")));
						System.out.println("Cookie Value: "+cookie.substring(cookie.indexOf("=")+1, cookie.length()));
						cookies.add(cookie);
					}
				}
			}
	 
			//result:
			System.out.println(httpResponse.toString());
			JSONObject jsonResponse = JSONFactoryUtil.createJSONObject(httpResponse.toString());
			
			return jsonResponse;
		} catch (IOException ioE) {
			ioE.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (httpURLConnection != null) {
		        httpURLConnection.disconnect();
		    }
		}
		return null;
	}
}