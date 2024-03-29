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
package org.exoplatform.social.ecms.plugin.doc;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Zun
 *          exo@exoplatform.com
 * Jul 23, 2010  
 */

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "classpath:groovy/templates/social/ecms/UIDocActivity.gtmpl",
   events = {
     @EventConfig(listeners = UIDocActivity.DownloadDocumentActionListener.class),
     @EventConfig(listeners = UIDocActivity.ViewDocumentActionListener.class),
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
     @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
     @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
     @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
     @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class,
                  confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
     @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class,
                  confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment")
   }
 )
public class UIDocActivity extends BaseUIActivity {
  
  private static final Log LOG = ExoLogger.getLogger(UIDocActivity.class);
  private static final String IMAGE_PREFIX = "image/";
  private static final String DOCUMENT_POSTFIX = "/pdf";
  
  public static final String ACTIVITY_TYPE = "DOC_ACTIVITY";
  public static final String DOCLINK = "DOCLINK";
  public static final String MESSAGE = "MESSAGE";
  public static final String REPOSITORY = "REPOSITORY";
  public static final String WORKSPACE = "WORKSPACE";
  public static final String DOCNAME = "DOCNAME";
  public static final String DOCPATH = "DOCPATH";
  
  public String docLink;
  public String message;
  public String docName;
  public String docPath;
  private Node docNode;

  public UIDocActivity() {
  }

  public void setDocNode(Node docNode) {
    this.docNode = docNode;
  }

  public Node getDocNode() {
    return docNode;
  }


  protected boolean isPreviewable() {
    String mimeType = "";    
      try {
        mimeType = docNode.getNode("jcr:content").getProperty("jcr:mimeType").getString();
      } catch (ValueFormatException e) {
        if (LOG.isDebugEnabled())
          LOG.debug(e);
        return false;
      } catch (PathNotFoundException e) {
        if (LOG.isDebugEnabled())
          LOG.debug(e);
        return false;
      } catch (RepositoryException e) {
        if (LOG.isDebugEnabled())
          LOG.debug(e);
        return false;
      }
    
    return mimeType.endsWith(DOCUMENT_POSTFIX) || mimeType.startsWith(IMAGE_PREFIX);
  }
  
  
  protected String getDocThumbnail(){    
    String portalContainerName = PortalContainer.getCurrentPortalContainerName();
    String restContextName = PortalContainer.getRestContextName(portalContainerName);
    return new StringBuffer().append("/").append(portalContainerName).
                               append("/").append(restContextName).
                               append("/thumbnailImage/big").
                               append("/").append(UIDocActivityComposer.REPOSITORY).
                               append("/").append(UIDocActivityComposer.WORKSPACE).
                               append(docPath).toString();
  }
  
  public static class ViewDocumentActionListener extends EventListener<UIDocActivity> {
    @Override
    public void execute(Event<UIDocActivity> event) throws Exception {
      final UIDocActivity docActivity = event.getSource();
      final UIActivitiesContainer activitiesContainer = docActivity.getParent();
      final UIPopupWindow popupWindow = activitiesContainer.getPopupWindow();

      if (docActivity.getChild(UIDocViewer.class) != null) {
        docActivity.removeChild(UIDocViewer.class);
      }
      
      UIDocViewer docViewer = popupWindow.createUIComponent(UIDocViewer.class, null, "DocViewer");
      final Node docNode = docActivity.getDocNode();
      docViewer.setOriginalNode(docNode);
      docViewer.setNode(docNode);

      popupWindow.setUIComponent(docViewer);
      popupWindow.setWindowSize(800, 600);
      popupWindow.setShow(true);
      popupWindow.setResizable(true);

      event.getRequestContext().addUIComponentToUpdateByAjax(activitiesContainer);
    }
  }
  
  public static class DownloadDocumentActionListener extends EventListener<UIDocActivity> {
    @Override
    public void execute(Event<UIDocActivity> event) throws Exception {
      UIDocActivity uiComp = event.getSource() ;
      String downloadLink = org.exoplatform.wcm.webui.Utils.getDownloadLink(uiComp.getDocNode());
      //JS Resource HTTPRequest will be loaded in UIDocActivity.gtmpl template
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }
}
