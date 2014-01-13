package org.jasig.portal.portlets.marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.context.MessageSource;

/**
 * 
 * @author vertein
 * A controller with a public method to return a list of portlets
 */
@Controller
@RequestMapping("VIEW")
public class PortletMarketplaceController {
	
	private IPortalRequestUtils portalRequestUtils;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IPersonManager personManager;
	private ILocaleStore localeStore;
	private IPortletCategoryRegistry portletCategoryRegistry;
	private MessageSource messageSource;
	private IPortalSpELService spELService;
	
	@Autowired
    public void setSpELService(IPortalSpELService spELService) {
        this.spELService = spELService;
    }
	
	@Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
	
	@Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }
	
	@Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }
	
	@Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
	
	@Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
	
	@Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
	
	
	/**
	 * @author vertein
	 * @param webRequest
	 * @param portletRequest
	 * @param model
	 * @return A string of the view.  Also sets the model attributes
	 * 
	 * Uses the user that is logged in.  Uses the public method getListOfChannelBeans to
	 * initialize list.  Doesn't return list of portlets that user is not allowed to use, but is allowed to manage
	 */
	@RenderMapping
	public String initializeView(WebRequest webRequest, PortletRequest portletRequest, Model model){
		final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
		IPerson user = personManager.getPerson(servletRequest);
		ArrayList<ChannelBean> portletList = (ArrayList<ChannelBean>) getListOfChannelBeans(webRequest, user, true);
		model.addAttribute("channelBeanList", portletList);
		return "jsp/Marketplace/view";
	}
	
	@RenderMapping(params="action=view")
	public String entryView(RenderRequest renderRequest, RenderResponse renderResponse, WebRequest webRequest, PortletRequest portletRequest, Model model){
		String name = portletRequest.getParameter("name");
		IPortletDefinition portlet = this.getChannelBeanByName(name);
		model.addAttribute("Portlet", portlet);
		List<ScreenShot> screenShotList = this.getScreenShots(portlet);
		model.addAttribute("ScreenShots", screenShotList);
		return "jsp/Marketplace/entry";
	}
    
	private List<ScreenShot> getScreenShots(IPortletDefinition portlet){
		
		List<IPortletPreference> portletPreferences = portlet.getPortletPreferences();
		List<IPortletPreference> urls =  new ArrayList<IPortletPreference>(portletPreferences.size());
		List<IPortletPreference> captions = new ArrayList<IPortletPreference>(portletPreferences.size());
		List<ScreenShot> screenshots = new ArrayList<ScreenShot>();
		
		//Creates a list of captions and list of urls
		for(int i=0; i<portletPreferences.size(); i++){
			//Most screenshots possible is i
			urls.add(null);
			captions.add(null);
			for(int j=0; j<portletPreferences.size(); j++){
				if(portletPreferences.get(j).getName().equalsIgnoreCase("screen_shot"+Integer.toString(i+1))){
					urls.set(i, portletPreferences.get(j));
				}
				if(portletPreferences.get(j).getName().equalsIgnoreCase("screen_shot"+Integer.toString(i+1)+"_caption")){
					captions.set(i, portletPreferences.get(j));
				}
			}			
		}
		
		//
		for(int i=0; i<urls.size(); i++){
			if(urls.get(i)!=null){
				if(captions.size()>i && captions.get(i)!=null){
					screenshots.add(new ScreenShot(urls.get(i).getValues()[0], Arrays.asList(captions.get(i).getValues())));
				}else{
					screenshots.add(new ScreenShot(urls.get(i).getValues()[0]));
				}
			}
		}
		
		return screenshots;
	}
	
	public class ScreenShot{
		private String url;
		private List<String> captions;
		
		public ScreenShot(String url){
			this.setUrl(url);
			this.setCaptions(new ArrayList<String>());
		}
		
		public ScreenShot(String url, List<String> captions){
			this.setUrl(url);
			this.setCaptions(captions);
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		/**
		 * @author vertein
		 * @return the captions for a screen shot.  Will not return null, might return empty list.
		 */
		public List<String> getCaptions() {
			if(captions==null){
				this.captions = new ArrayList<String>();
			}
			return captions;
		}
		
		public void setCaptions(List<String> captions) {
			this.captions = captions;
		}
	}
	
	
	/**
	 * @author vertein
	 * @param name - the name of the portlet you want
	 * @return the portlet desired
	 * 
	 * Uses name rather than fname because name is customer facing
	 * Returns null if no matching portlet can be found
	 * 
	 */
	public IPortletDefinition getChannelBeanByName(String name){
		// get a list of all channels 
		List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
		for( IPortletDefinition temp: allChannels){
			if(temp.getName().equalsIgnoreCase(name)){
				return temp;
			}
		}
		return null;
	}

	
	/**
	 * @author vertein
	 * @param request 
	 * @param user - the user to limit results by
	 * @param seeManage - true if would like portlet listing managed by user.
	 * @return a list of ChannelBeans representing porlets
	 */
	public List<ChannelBean> getListOfChannelBeans(WebRequest request, IPerson user, Boolean seeManage){
		Map<String,SortedSet<?>> registry = getRegistry(request, user, seeManage);
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelBean((SortedSet<ChannelBean>) registry.get("channels"));
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList2 = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelCategoryBean((SortedSet<ChannelCategoryBean>) registry.get("categories"));
		channelList.addAll(channelList2);
		return channelList;
	}
	
	
	/**
	 * @author vertein
	 * @param request 
	 * @param seeManage - true if would like portlet listing managed by user.
	 * @return a list of ChannelBeans representing portlets
	 */
	public List<ChannelBean> getListOfChannelBeans(WebRequest request){
		Map<String,SortedSet<?>> registry = getRegistry(request);
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelBean((SortedSet<ChannelBean>) registry.get("channels"));
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList2 = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelCategoryBean((SortedSet<ChannelCategoryBean>) registry.get("categories"));
		channelList.addAll(channelList2);
		return channelList;
	}
	
	private List<ChannelBean> getListOfChannelBeansByChannelBean (SortedSet<ChannelBean> cbSet){
		ArrayList<ChannelBean> channelList = new ArrayList<ChannelBean>();
		Iterator<ChannelBean> it = cbSet.iterator();
		while(it.hasNext()){
			ChannelBean cb = it.next();
			channelList.add(cb);
		}
		return channelList;
	}
	
	private List<ChannelBean> getListOfChannelBeansByChannelCategoryBean (SortedSet<ChannelCategoryBean> ccbSet){
		ArrayList<ChannelBean> categoryList = new ArrayList<ChannelBean>();
		Iterator<ChannelCategoryBean> it = ccbSet.iterator();
		while(it.hasNext()){
			ChannelCategoryBean ccb = it.next();
			categoryList.addAll(ccb.getChannels());
			if(ccb.getCategories().size()>0){
				categoryList.addAll(getListOfChannelBeansByChannelCategoryBean(ccb.getCategories()));
			}
		}
		return categoryList;	
	}
	
	private Map<String,SortedSet<?>> getRegistry(WebRequest request) {
		// get a list of all channels 
		List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
		// construct a new channel registry
		Map<String,SortedSet<?>> registry = new TreeMap<String,SortedSet<?>>();
	    SortedSet<ChannelCategoryBean> categories = new TreeSet<ChannelCategoryBean>();
	    SortedSet<ChannelBean> channels = new TreeSet<ChannelBean>();
	    // add the root category and all its children to the registry
 		PortletCategory rootCategory = portletCategoryRegistry.getTopLevelPortletCategory();
 		categories.add(addChildren(request, rootCategory, allChannels));
	    registry.put("channels", channels);
	    registry.put("categories", categories);
	    return registry;
	}

	private Map<String,SortedSet<?>> getRegistry(WebRequest request, IPerson user, Boolean seeManage) {
		
		// get a list of all channels 
		List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
		
		// construct a new channel registry
		Map<String,SortedSet<?>> registry = new TreeMap<String,SortedSet<?>>();
	    SortedSet<ChannelCategoryBean> categories = new TreeSet<ChannelCategoryBean>();
	    SortedSet<ChannelBean> channels = new TreeSet<ChannelBean>();
		
	    // get user locale
	    Locale[] locales = localeStore.getUserLocales(user);
	    LocaleManager localeManager = new LocaleManager(user, locales);
	    Locale locale = localeManager.getLocales()[0];
	    
	    // add the root category and all its children to the registry
 		PortletCategory rootCategory = portletCategoryRegistry.getTopLevelPortletCategory();
 		categories.add(addChildren(request, rootCategory, allChannels, user, seeManage, locale));
	    
		EntityIdentifier ei = user.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    
	    if(seeManage == true){	    
		    for (IPortletDefinition channel : allChannels) {
	            if (ap.canManage(channel.getPortletDefinitionId().getStringId())) {
	                channels.add(getChannel(channel, request, locale));
	            }
	        }
	    }
	    
	    registry.put("channels", channels);
	    registry.put("categories", categories);
	    return registry;
	}
	
	private ChannelCategoryBean addChildren(WebRequest request, PortletCategory category, List<IPortletDefinition> allChannels) {
		// construct a new channel category bean for this category
		ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
		// add the direct child channels for this category
		Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);		
		for(IPortletDefinition channelDef : portlets) {
			// remove the channel of the list of all channels
			allChannels.remove(channelDef);
		}
		/* Now add child categories. */
		for(PortletCategory childCategory : this.portletCategoryRegistry.getChildCategories(category)) {
			ChannelCategoryBean childCategoryBean = addChildren(request, childCategory, allChannels);
			categoryBean.addCategory(childCategoryBean);
		}
		return categoryBean;
	}
	
	private ChannelCategoryBean addChildren(WebRequest request, PortletCategory category, List<IPortletDefinition> allChannels, IPerson user, Boolean seeManage, Locale locale) {
		
		// construct a new channel category bean for this category
		ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
        categoryBean.setName(messageSource.getMessage(category.getName(), new Object[] {}, locale));
		
		// add the direct child channels for this category
		Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);		
		EntityIdentifier ei = user.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
		
		for(IPortletDefinition channelDef : portlets) {
			if ((seeManage && ap.canManage(channelDef.getPortletDefinitionId()
					.getStringId()))
					|| (!seeManage && ap.canSubscribe(channelDef
							.getPortletDefinitionId().getStringId()))) {
				// construct a new channel bean from this channel
				ChannelBean channel = getChannel(channelDef, request, locale);
				categoryBean.addChannel(channel);
			}
			// remove the channel of the list of all channels
			allChannels.remove(channelDef);
		}

		/* Now add child categories. */
		for(PortletCategory childCategory : this.portletCategoryRegistry.getChildCategories(category)) {
			ChannelCategoryBean childCategoryBean = addChildren(request, childCategory, allChannels, user, seeManage, locale);
			categoryBean.addCategory(childCategoryBean);
		}
		return categoryBean;
	}

	private ChannelBean getChannel(IPortletDefinition definition, WebRequest request, Locale locale) {
	    ChannelBean channel = new ChannelBean();
	    channel.setId(definition.getPortletDefinitionId().getStringId());
	    channel.setDescription(definition.getDescription(locale.toString()));
	    channel.setFname(definition.getFName());
	    channel.setName(definition.getName(locale.toString()));
	    channel.setState(definition.getLifecycleState().toString());
	    channel.setTitle(definition.getTitle(locale.toString()));
	    channel.setTypeId(definition.getType().getId());
	    IPortletDefinitionParameter iconParameter = definition.getParameter("iconUrl");
	    if (iconParameter != null) {
	        String iconUrl = spELService.parseString(iconParameter.getValue(), request);
	        channel.setIconUrl(iconUrl);
	    }
	    return channel;
	}
	
}
