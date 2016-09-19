package de.iisys.liferay.portlet.meetingMinutes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.dynamicdatamapping.StructureNameException;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplateConstants;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;

/**
 * A class to create structures and templates for Liferay's Web Content (JournalArticle).
 * <ul>
 * <li>Created for: Social Collaboration Hub (www.sc-hub.de)</li>
 * <li>Created at: Institute for Information Systems (www.iisys.de/en)</li>
 * </ul>
 * @author Christian Ochsenkühn
 *
 */
public class DDMStructureCreator {
	
	/**
	 * Adds a new structure to the Web Content Portlet (JournalArticle.class).
	 * @param structureNameUS: Name of the structure (in US English).
	 * @param structureDescriptionUS: Description of the structure (in US English).
	 * @param structureXml: The structure's xml content (e.g. received by readXML()).
	 * @param userId: (Take it from themeDisplay)
	 * @param scopeGroupId: (Take it from themeDisplay)
	 * @return: The newly created DDMStructure.
	 */
	public static DDMStructure addDDMStructure(String structureNameUS, String structureDescriptionUS, String structureXml,
			long userId, long scopeGroupId) {
		Map<Locale,String> structureNameMap = new HashMap<Locale,String>();
		structureNameMap.put(Locale.US, structureNameUS);
		Map<Locale,String> descriptionMap = new HashMap<Locale,String>();
		descriptionMap.put(Locale.US, structureDescriptionUS);
		return addDDMStructure(structureNameMap, descriptionMap, structureXml, userId, scopeGroupId);
	}
	
	/**
	 * Adds a new structure to the Web Content Portlet (JournalArticle.class).
	 * @param structureNameMap: Name of the structure in different languages (e.g. Locale.US, "MyName").
	 * @param descriptionMap: Description of the structure in different languages.
	 * @param structureXml: The structure's xml content (e.g. received by readXML()).
	 * @param userId: (Take it from themeDisplay)
	 * @param scopeGroupId: (Take it from themeDisplay)
	 * @return: The newly created DDMStructure.
	 */
	public static DDMStructure addDDMStructure(Map<Locale,String> structureNameMap, Map<Locale,String> descriptionMap, String structureXml,
			long userId, long scopeGroupId) {
	
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(JournalArticle.class);
		
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(scopeGroupId);
		serviceContext.setUserId(userId);
		
		try {
			return DDMStructureLocalServiceUtil.addStructure(
					userId, scopeGroupId, classNameId, structureNameMap, descriptionMap, structureXml, serviceContext);
		} catch (StructureNameException ne) {
			ne.printStackTrace();
			System.out.println("StructureNameException: "+ne.getMessage());
		}
		catch (PortalException | SystemException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Successfully created structure "+structureNameMap.get(Locale.US)+".");
		}
		return null;
	}
	
	/**
	 * Adds a new DDMTemplate to a DDMStructure. The template has to use the script-language "ftl".
	 * @param templateNameUS: Name of the template (in US English).
	 * @param descriptionUS: Description of the template (in US English).
	 * @param templateScript: The template's script content (e.g. received by readXML()).
	 * @param structurePK: The related structure's primaryKey.
	 * @param userId: (Take it from themeDisplay)
	 * @param scopeGroupId: (Take it from themeDisplay)
	 * @return: The newly created DDMTemplate.
	 */
	public static DDMTemplate addDDMTemplate(String templateNameUS, String descriptionUS, String templateScript,
			long structurePK, long userId, long scopeGroupId) {
		Map<Locale,String> templateNameMap = new HashMap<Locale,String>();
		templateNameMap.put(Locale.US, templateNameUS);
		Map<Locale,String> descriptionMap = new HashMap<Locale,String>();
		descriptionMap.put(Locale.US, descriptionUS);
		return addDDMTemplate(templateNameMap, descriptionMap, templateScript, structurePK, userId, scopeGroupId);
	}
	
	/**
	 * Adds a new DDMTemplate to a DDMStructure. The template has to use the script-language "ftl".
	 * @param templateNameMap: Name of the template in different languages (e.g. Locale.US, "MyName").
	 * @param descriptionMap: Description of the template in different languages.
	 * @param templateScript: The template's script content (e.g. received by readXML()).
	 * @param structurePK: The related structure's primaryKey.
	 * @param userId: (Take it from themeDisplay)
	 * @param scopeGroupId: (Take it from themeDisplay)
	 * @return: The newly created DDMTemplate.
	 */
	public static DDMTemplate addDDMTemplate(Map<Locale,String> templateNameMap, Map<Locale,String> descriptionMap, String templateScript,
			long structurePK, long userId, long scopeGroupId) {
		
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(DDMStructure.class);
		
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(scopeGroupId);
		serviceContext.setUserId(userId);
		
		String language = "ftl";
		
		try {
			return DDMTemplateLocalServiceUtil.addTemplate(
					userId, scopeGroupId, classNameId, structurePK, templateNameMap, descriptionMap,
					DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY, DDMTemplateConstants.TEMPLATE_MODE_CREATE, language, templateScript, serviceContext);
		} catch (PortalException | SystemException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Successfully created template "+templateNameMap.get(Locale.US)+".");
		}
		return null;
	}
	
	/**
	 * Checks if a DDMStructure with the given name is created and returns its structureKey.
	 * @param structureName
	 * @return Returns "0" if the structure was not created yet.
	 */
	public static String isDDMStructureCreated(String structureName) {		
		try {
			List<DDMStructure> structures = DDMStructureLocalServiceUtil.getStructures();
			for(DDMStructure struct : structures) {
				if(struct.getNameCurrentValue().equals(structureName)) return struct.getStructureKey();
			}
		} catch (SystemException e) {
			e.printStackTrace();
		}
		return "0";
	}
	
	/**
	 * For testing issues: Prints ALL DDMTemplates with its name, classPK and className.
	 */
	public static void printAllDDMTemplates() {
		try {
			List<DDMTemplate> ddms = DDMTemplateLocalServiceUtil.getDDMTemplates(0, Integer.MAX_VALUE);
			for(DDMTemplate templ : ddms) {
				System.out.println(templ.getNameCurrentValue()+", classPK: "+templ.getClassPK()+", className: "+templ.getClassName());
			}
		} catch (SystemException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a xml file (or any other textfile) and returns its content as string.
	 * @param path: The path to the file. Use getPortletContext().getRealPath("/") in MVCPortlet to get the path of "docroot".
	 * @return
	 */
	public static String readXML(String path) {
		String xml = "";
		try {
			xml = new Scanner(new File(path)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return xml;
	}
}
