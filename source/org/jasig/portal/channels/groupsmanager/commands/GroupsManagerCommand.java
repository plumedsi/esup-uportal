/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.channels.groupsmanager.commands;

import  java.util.*;
import  org.jasig.portal.*;
import  org.jasig.portal.channels.groupsmanager.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.services.*;
import  org.w3c.dom.Document;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;

/**
 * This class is the parent class of all other command classes.
 * @author Don Fracapane
 * @version $Revision$
 */
public abstract class GroupsManagerCommand
      implements IGroupsManagerCommand, GroupsManagerConstants {

   /**
    * GroupsManagerCommand is the parent of all Groups Manager commands. It
    * hold the commone functionality of all commands.
    */
   public GroupsManagerCommand () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{}

   /**
    * clear out the selection list
    * @param sessionData
    */
   protected void clearSelected (CGroupsManagerSessionData sessionData) {
      ChannelStaticData staticData = sessionData.staticData;
      Element rootElem = getXmlDoc(sessionData).getDocumentElement();
      NodeList nGroupList = rootElem.getElementsByTagName(GROUP_TAGNAME);
      NodeList nPersonList = rootElem.getElementsByTagName(ENTITY_TAGNAME);
      NodeList nList = nGroupList;
      for (int i = 0; i < nList.getLength(); i++) {
         Element elem = (org.w3c.dom.Element)nList.item(i);
         elem.setAttribute("selected", "false");
      }
      nList = nPersonList;
      for (int i = 0; i < nList.getLength(); i++) {
         Element elem = (org.w3c.dom.Element)nList.item(i);
         elem.setAttribute("selected", "false");
      }
      return;
   }

   /**
    * Removes all of the permissions for a GroupMember. We need to get permissions
    * for the group as a principal and as a target. I am merging the 2 arrays into a
    * single array in order to use the transaction management in the RDBMPermissionsImpl.
    * If an exception is generated, I do not delete the group or anything else.
    * Possible Exceptions: AuthorizationException and GroupsException
    * @param grpMbr
    * @throws Exception
    */
   protected void deletePermissions (IGroupMember grpMbr) throws Exception{
      try {
         String grpKey = grpMbr.getKey();
         // first we retrieve all permissions for which the group is the principal
         IAuthorizationPrincipal iap = AuthorizationService.instance().newPrincipal(grpMbr);
         IPermission[] perms1 = iap.getPermissions();

         // next we retrieve all permissions for which the group is the target
         IUpdatingPermissionManager upm = AuthorizationService.instance().newUpdatingPermissionManager(OWNER);
         IPermission[] perms2 = upm.getPermissions(null, grpKey);

         // merge the permissions
         IPermission[] allPerms = new IPermission[perms1.length + perms2.length];
         System.arraycopy(perms1,0,allPerms,0,perms1.length);
         System.arraycopy(perms2,0,allPerms,perms1.length,perms2.length);

         upm.removePermissions(allPerms);
      }
      catch (Exception e) {
         String errMsg = "DeleteGroup::deletePermissions(): Error removing permissions for " + grpMbr;
         Utility.logMessage("ERROR", errMsg, e);
         throw new Exception(errMsg);
      }
   }

   /**
    * Answers if the parentGroupId has been set. If it has not been set, this
    * would indicate that Groups Manager is in Servant mode.
    * @param staticData
    * @return boolean
    */
   protected boolean hasParentId (ChannelStaticData staticData) {
      String pk = getParentId(staticData);
      if (pk == null) {
         return  false;
      }
      if (pk.equals("")) {
         return  false;
      }
      Utility.logMessage("Debug", "GroupsManagerCommand::hasParentId: Value is set to default: "
            + pk);
      return  true;
   }

   /**
    * Returns the grpCommand parameter from runtimeData
    * @param runtimeData
    * @return String
    */
   protected String getCommand (org.jasig.portal.ChannelRuntimeData runtimeData) {
      return  (String)runtimeData.getParameter("grpCommand");
   }

   /**
    * Returns the grpCommandIds parameter from runtimeData. The string usually
    * hold one element ID but could contain a string of delimited ids. (See
    * RemoveMember command).
    * @param runtimeData
    * @return String
    */
   protected String getCommandArg (org.jasig.portal.ChannelRuntimeData runtimeData) {
      return  (String)runtimeData.getParameter("grpCommandArg");
   }

   /**
    * Returns the groupParentId parameter from staticData
    * @param staticData
    * @return String
    */
   protected String getParentId (ChannelStaticData staticData) {
      return  staticData.getParameter("groupParentId");
   }

   /**
    * Returns the userID from the user object
    * @param sessionData
    * @return String
    */
   protected String getUserID (CGroupsManagerSessionData sessionData) {
      return  String.valueOf(sessionData.user.getID());
   }

   /**
    * Returns the cached xml document from staticData
    * @param sessionData
    * @return Document
    */
   protected Document getXmlDoc (CGroupsManagerSessionData sessionData) {
      //return  (Document)staticData.get("xmlDoc");sessionData.model
      return  sessionData.model;
   }

   /**
    * Set the CommandArg value, useful for commands which would like to chain
    * other commands
    * @param runtimeData
    * @param arg String
    */
   protected void setCommandArg (org.jasig.portal.ChannelRuntimeData runtimeData, String arg) {
      runtimeData.setParameter("grpCommandArg",arg);
   }

}
