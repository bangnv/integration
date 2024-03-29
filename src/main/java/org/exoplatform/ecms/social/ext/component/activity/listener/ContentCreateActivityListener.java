/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ecms.social.ext.component.activity.listener;

import javax.jcr.Node;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
 * 15, 2011
 */
public class ContentCreateActivityListener extends Listener<CmsService, Node> {
  
  private static final String RESOURCE_BUNDLE_KEY_CREATED_BY = "SocialIntegration.messages.createdBy";
  
  /**
   * Instantiates a new post create content event listener.
   */
  public ContentCreateActivityListener() {
  }

  @Override
  public void onEvent(Event<CmsService, Node> event) throws Exception {
    Node currentNode = event.getData();
    Utils.postActivity(currentNode, RESOURCE_BUNDLE_KEY_CREATED_BY);
  }
}
