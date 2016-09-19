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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.util.portlet.PortletProps;

public class CamundaRESTConnector {
	
	private final String PROP_CAMUNDA_URL = "camunda_url";
	private final String PROP_CAMUNDA_ENGINE = "camunda_rest_engine";
	
	private String userId;
	private String userPw;
	
	private String CAMUNDA_API_URL;
	private List<String> cookies = null;
	
	public CamundaRESTConnector() {		
		CAMUNDA_API_URL = PortletProps.get(PROP_CAMUNDA_URL)+"/"+PortletProps.get(PROP_CAMUNDA_ENGINE);
	}
	
	public CamundaRESTConnector(String userId, String userPw) {
		this();
		this.userId = userId;
		this.userPw = userPw;
	}
	
	public JSONArray camundaGetTasks() throws JSONException {
		String parameters = "?assignee=demo&maxPriority=50";
		String result =	camundaGET(CAMUNDA_API_URL+"/task", parameters, "");
		return JSONFactoryUtil.createJSONArray(result);
	}
	
	/**
	 * 
	 * @param taskName
	 * @param description
	 * @param user
	 * @param when: due in format yyyy-MM-dd'T'HH:mm:ss
	 * @return
	 * @throws JSONException
	 */
	public JSONObject camundaCreateTask(String taskName, String description, String user, String when) throws JSONException {
		String owner = "null";
		String followUp = "null";
		String delegationState = "null";
		String parentTaskId = "null";
		String caseInstanceId = "null";
		int priority = 50; // 50 = default priority
		
		String jsonRequestBody = "{"+
//				"\"id\":\""+taskId+"\","+
				"\"name\":\""+taskName+"\","+
	    		"\"description\":\""+description+"\","+
	    		"\"assignee\":\""+user+"\","+
	    		"\"owner\":\""+user+"\","+
	    		"\"due\":\""+when+"\","+
	    		"\"followUp\":null,"+
	    		"\"delegationState\":null,"+
	    		"\"parentTaskId\":null,"+
	    		"\"caseInstanceId\":\""+caseInstanceId+"\","+
	    		"\"priority\":"+priority+""+
			"}";
		System.out.println("Sent JSON: "+jsonRequestBody);
		String result = camundaPOST(CAMUNDA_API_URL+"/task/create","",jsonRequestBody);
		return JSONFactoryUtil.createJSONObject(result);
	}
	
	public JSONArray camundaGetUsersByEmail(String email) throws JSONException {
		String parameters = "?email="+email;
		String result =	camundaGET(CAMUNDA_API_URL+"/user", parameters, "");
		return JSONFactoryUtil.createJSONArray(result);
	}
	
	
	public String camundaPOST(String urlString, String parameters, String jsonRequestBody) {
		return camundaHttpRequest("POST", urlString, parameters, jsonRequestBody);
	}
	
	public String camundaGET(String urlString, String parameters, String jsonRequestBody) {
		return camundaHttpRequest("GET", urlString+parameters, "", jsonRequestBody);
	}
	
	/**
	 * Performs a request to Open-Xchange and uses the session and cookies for authentication.
	 * @param requestMethod: GET, POST or PUT
	 * @param urlString
	 * @param session
	 * @param parameters: The first parameter starts with a ?
	 * @param jsonRequestBody
	 * @return: The http response in JSON notation
	 */
	private String camundaHttpRequest(String requestMethod, String urlString, String parameters, String jsonRequestBody) {
		StringBuffer outputBuffer = new StringBuffer();
		if(!parameters.isEmpty() && !parameters.equals(""))
			outputBuffer.append(parameters);
		if(!jsonRequestBody.isEmpty() && !jsonRequestBody.equals("")) {
			outputBuffer.append(jsonRequestBody);
		}
		
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			if(requestMethod.equals("GET"))
//				httpURLConnection.setRequestProperty("Content-Type", "text/plain");
//			else
				httpURLConnection.setRequestProperty("Content-Type", "application/json");
			
			httpURLConnection.setRequestProperty("Accept", "*/*");
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
			
/*			if(cookies!=null) {
				//add session cookies:
			    StringBuffer cookieString = new StringBuffer();
			    for(String cookie : cookies) {
			    	cookieString.append(cookie+"; ");
			    }
			    httpURLConnection.setRequestProperty("Cookie", cookieString.toString());
			} */
			
			httpURLConnection.setRequestMethod(requestMethod);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);

			if(outputBuffer.length()>0) {
				DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
				System.out.println("Write Bytes: "+outputBuffer.toString());
				dataOutputStream.writeBytes(outputBuffer.toString());
				dataOutputStream.flush();
				dataOutputStream.close();
			}
	 
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
			
			
/*			if(cookies==null || cookies.isEmpty()) {
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
			} */
	 
			//result:
			String result = httpResponse.toString();
			System.out.println(result);
			httpURLConnection.disconnect();
			return result;
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
		return null;
	}
}
