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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import  java.util.Hashtable;


/**
 * Interface by which portal talks to the stylesheet description database
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */
public interface ICoreStylesheetDescriptionStore {

  // functions that allow one to browse available core stylesheets in various ways
  /** Obtain a listing of structure stylesheets from the database
   *
   * @param mimeType
   * @return Returns a hashtable mapping structure stylesheet names to a
   *     word-description (a simple String) of that stylesheet
   */
  public Hashtable getStructureStylesheetList (String mimeType);



  /** Obtains a list of theme stylesheets available for a particular structure stylesheet
   *
   * @param structureStylesheetName name of the structure stylehsset
   * @return Returns a hashtable mapping theme stylesheet names to an array (String[])
   * containing five string elements:
   * <ol>
   *  <li>stylesheet description text</li>
   *  <li>mime type specified by the stylesheet</li>
   *  <li>device type code specified by the stylesheet</li>
   *  <li>sample image uri specified</li>
   *  <li>sample image icon uri specified</li>
   * </ol>
   * stylesheet description and the mime type
   */
  public Hashtable getThemeStylesheetList (int structureStylesheetId);



  /** Obtains a list of mime types available on the installation
   *
   * @return Returns a hasbtale mapping mime type strings to their word
   *     descriptions (simple String)
   */
  public Hashtable getMimeTypeList ();



  // functions that allow access to the entire CoreStylesheetDescription object.
  // These functions are used when working with the stylesheet, and not for browsing purposes.
  /** Obtains a complete description of the structure stylesheet
   *
   * @param stylesheetId id of the structure stylesheet
   * @return a description of the structure stylesheet
   */
  public StructureStylesheetDescription getStructureStylesheetDescription (int stylesheetId);



  /** Obtains a complete description of a theme stylesheet
   *
   * @param stylesheetId id of a theme stylesheet
   * @return a description of a theme stylesheet
   */
  public ThemeStylesheetDescription getThemeStylesheetDescription (int stylesheetId);



  // functions that allow to manage core stylesheet description collection
  /** removes stylesheet description
   *
   * @param stylesheetId id of the stylesheet
   */
  public void removeStructureStylesheetDescription (int stylesheetId);



  /** Removes theme stylesheet
   *
   * @param stylesheetId id of the stylesheet
   */
  public void removeThemeStylesheetDescription (int stylesheetId);



  /** Registers new structure stylesheet with the portal database
   *
   * @param stylesheetDescriptionURI Location of the stylesheet description XML file
   * @param stylesheetURI Location of the actual stylesshet XML file
   * @return id assigned to the stylesheet or null if the operation failed
   */
  public Integer addStructureStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI);



  /**
   * Updates an existing structure stylesheet description.
   * @param stylesheetDescriptionURI Location of the stylesheet description XML file
   * @param stylesheetURI Location of the actual stylesshet XML file
   * @param stylesheetId the id of the existing stylesheet description
   * @return true if the update successful
   */
  public boolean updateStructureStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId);



  /**
   * Updates an existing theme stylesheet description.
   * @param stylesheetDescriptionURI Location of the stylesheet description XML file
   * @param stylesheetURI Location of the actual stylesshet XML file
   * @param stylesheetId the id of the existing stylesheet description
   * @return true if the update successful
   */
  public boolean updateThemeStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId);



  /** Registers a new theme stylesheet with the portal databases
   *
   * @param stylesheetDescriptionURI Location of the stylesheet description
   *     XML file
   * @param stylesheetURI Location of the actual stylesheet XML file
   * @return id assigned to the stylesheet or null if the operation failed
   */
  public Integer addThemeStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI);
}



