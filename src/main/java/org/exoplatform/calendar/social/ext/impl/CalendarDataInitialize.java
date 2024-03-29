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

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 7, 2010  
 */
public class CalendarDataInitialize extends SpaceListenerPlugin {

  private static final Log   LOG                      = ExoLogger.getLogger(CalendarDataInitialize.class);

  public static final String ANY                      = "*.*".intern();

  public static final String SLASH_COLON              = "/:".intern();

  public static final String SLASH                    = "/".intern();

  public static final String COLON                    = ":".intern();

  public static final String SPLITER                  = "://".intern();

  public static final String PUBLIC_TYPE              = "2".intern();

  public static final String SPACE_CALENDAR_ID_SUFFIX = "_space_calendar";

  private final InitParams   params;

  public CalendarDataInitialize(InitParams params) {
    this.params = params;
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    String portletName = "";

    if (params.getValueParam("portletName") != null)
      portletName = params.getValueParam("portletName").getValue();
    else if (LOG.isDebugEnabled())
      LOG.debug("Initparam is not configured for portletName property");

    if (!portletName.equals(event.getSource())) {
      /*
       * this function is called only if Calendar Portlet is added to Social
       * Space. Hence, if the application added to space do not have the name as
       * configured, we will do nothing.
       */
      return;
    }

    try {
      Space space = event.getSpace();
      CalendarService calService = (CalendarService) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(CalendarService.class);
      String calendarId = space.getPrettyName() + SPACE_CALENDAR_ID_SUFFIX;
      String username = space.getGroupId();
      Calendar calendar = null;
      try {
        calendar = calService.getGroupCalendar(calendarId);
      } catch (Exception pfe) {
        // Illegal catch because of Exception thrown by
        // CalendarService.getGroupCalendar
        // Do nothing here, this case occurs because desired calendar is not
        // exist.
        if (LOG.isDebugEnabled()) {
          LOG.warn("Desired calendar for " + space.getPrettyName()
              + " is not exist, create a new calendar.");
        }
      }
      if (calendar == null) {
        calendar = new Calendar();
        calendar.setId(calendarId);
        calendar.setPublic(false);
        calendar.setGroups((new String[] { space.getGroupId() }));
        calendar.setName(space.getDisplayName());
        calendar.setEditPermission(new String[] { space.getGroupId() + SLASH_COLON + ANY });
        calendar.setCalendarOwner(username);
        calService.savePublicCalendar(calendar, true);
      }
    } catch (Exception e) {
      // Illegal catch because of Exception thrown by
      // CalendarService.savePublicCalendar
      LOG.error("Couldn't save calendar to public area (group calendar).\n Cause by: ", e);
    }
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {

  }

}
