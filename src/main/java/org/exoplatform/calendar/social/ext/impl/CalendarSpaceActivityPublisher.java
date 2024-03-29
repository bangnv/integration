/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.social.ext.impl;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.impl.CalendarEventListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.ExoSocialException;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 30, 2010  
 */
public class CalendarSpaceActivityPublisher extends CalendarEventListener {
  private final static Log   LOG                   = ExoLogger.getLogger(CalendarSpaceActivityPublisher.class);

  public static final String CALENDAR_APP_ID       = "cs-calendar:spaces";

  public static final String EVENT_ADDED           = "EventAdded".intern();

  public static final String EVENT_UPDATED         = "EventUpdated".intern();

  public static final String EVENT_ID_KEY          = "EventID".intern();

  public static final String CALENDAR_ID_KEY       = "CalendarID".intern();

  public static final String TASK_ADDED            = "TaskAdded".intern();

  public static final String TASK_UPDATED          = "TaskUpdated".intern();

  public static final String EVENT_TYPE_KEY        = "EventType".intern();

  public static final String EVENT_SUMMARY_KEY     = "EventSummary".intern();

  public static final String EVENT_TITLE_KEY       = "EventTitle".intern();

  public static final String EVENT_DESCRIPTION_KEY = "EventDescription".intern();

  public static final String EVENT_LOCALE_KEY      = "EventLocale".intern();

  public static final String EVENT_STARTTIME_KEY   = "EventStartTime".intern();

  public static final String EVENT_ENDTIME_KEY     = "EventEndTime".intern();
  
  public static final String EVENT_LINK_KEY        = "EventLink";
  
  public static final String INVITATION_DETAIL     = "/invitation/detail/";
  
  /**
   * Make url for the event of the calendar application. 
   * Format of the url is: 
   * <ul>
   *    <li>/[portal]/[space]/[calendar]/[username]/invitation/detail/[event id]/[calendar type]</li>
   * </ul>
   * The format is used to utilize the invitation email feature implemented before.
   * <br>
   * <strong>[NOTE]</strong>
   * Keep in mind that this function calls {@link PortalRequestContext} which is in webui layer while this function is usually invoked in the service layer. Need to be improved in the future for ensuring the system design convention.
   * 
   * @param event have to be not null
   * @return empty string if the process is failed.
   */
  private String makeEventLink(CalendarEvent event) {
    StringBuffer sb = new StringBuffer("");    
      PortalRequestContext requestContext = Util.getPortalRequestContext();
      sb.append(requestContext.getPortalURI())
        .append(requestContext.getNodePath())
        .append(INVITATION_DETAIL)
        .append(ConversationState.getCurrent().getIdentity().getUserId())
        .append("/").append(event.getId())
        .append("/").append(event.getCalType());    
    return sb.toString();
  }

  private Map<String, String> makeActivityParams(CalendarEvent event, String calendarId, String eventType) {
    Map<String, String> params = new HashMap<String, String>();
    params.put(EVENT_TYPE_KEY, eventType);
    params.put(EVENT_ID_KEY, event.getId());
    params.put(CALENDAR_ID_KEY, calendarId);
    params.put(EVENT_SUMMARY_KEY, event.getSummary());
    params.put(EVENT_LOCALE_KEY, event.getLocation() != null ? event.getLocation() : "");
    params.put(EVENT_DESCRIPTION_KEY, event.getDescription() != null ? event.getDescription() : "");
    params.put(EVENT_STARTTIME_KEY, String.valueOf(event.getFromDateTime().getTime()));
    params.put(EVENT_ENDTIME_KEY, String.valueOf(event.getToDateTime().getTime()));
    params.put(EVENT_LINK_KEY, makeEventLink(event));
    return params;
  }

  private void publishActivity(CalendarEvent event, String calendarId, String eventType) {
    try {
      Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("eXo Social components not found!", e);
      }
      return;
    }
    if (calendarId == null || calendarId.indexOf(CalendarDataInitialize.SPACE_CALENDAR_ID_SUFFIX) < 0) {
      return;
    }
    try{
      IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      SpaceService spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      String spacePrettyName = calendarId.split(CalendarDataInitialize.SPACE_CALENDAR_ID_SUFFIX)[0];
      Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
      if (space != null) {
        String userId = ConversationState.getCurrent().getIdentity().getUserId();
        Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
        Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        activity.setUserId(userIdentity.getId());
        activity.setTitle(event.getSummary());
        activity.setBody(event.getDescription());
        activity.setType(CALENDAR_APP_ID);
        activity.setTemplateParams(makeActivityParams(event, calendarId, eventType));

        activityM.saveActivityNoReturn(spaceIdentity, activity);
      }
    }catch(ExoSocialException e){ //getSpaceByPrettyName
      if (LOG.isErrorEnabled())
        LOG.error("Can not record Activity for space when event added ", e);
    }
  }
  
  public void savePublicEvent(CalendarEvent event, String calendarId) {
    String eventType = event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT) ? EVENT_ADDED : TASK_ADDED;
    publishActivity(event, calendarId, eventType);
  }

  public void updatePublicEvent(CalendarEvent event, String calendarId) {
    String eventType = event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT) ? EVENT_UPDATED : TASK_UPDATED;
    publishActivity(event, calendarId, eventType);
  }

}
