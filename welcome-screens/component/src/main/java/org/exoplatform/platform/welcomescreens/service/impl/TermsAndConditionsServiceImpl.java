// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TermsAndConditionsServiceImpl.java

package org.exoplatform.platform.welcomescreens.service.impl;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.platform.welcomescreens.service.TermsAndConditionsService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.Node;

/**
 * This service is used to manage JCR node of Terms and conditions
 * 
 * @author Clement
 *
 */
public class TermsAndConditionsServiceImpl implements TermsAndConditionsService {

    private static final Log LOG = ExoLogger.getLogger(TermsAndConditionsServiceImpl.class);

    private static final String CHROMATTIC_LIFECYCLE_NAME = "termsandconditions";

    private static final String TC_NODE_NAME = "TermsAndConditions";

    private ChromatticLifeCycle lifeCycle;

    private NodeHierarchyCreator nodeHierarchyCreator;

    private static boolean hasTermsAndConditionsNode = false;

  
  /*=======================================================================
   * Component access
   *======================================================================*/
  
  public ChromatticSession getSession() {
      return lifeCycle.getChromattic().openSession();
  }

  public TermsAndConditionsServiceImpl(ChromatticManager chromatticManager, NodeHierarchyCreator nodeHierarchyCreator) {
    this.lifeCycle = chromatticManager.getLifeCycle(CHROMATTIC_LIFECYCLE_NAME);
    this.nodeHierarchyCreator = nodeHierarchyCreator;
  }

  
  /*=======================================================================
   * API public methods
   *======================================================================*/
  
  public boolean isTermsAndConditionsChecked() {
    boolean isChecked = false;
    if(hasTermsAndConditions()) {
      isChecked = true;
    }
    return isChecked;
  }

  public void checkTermsAndConditions() {
    if (lifeCycle.getContext() == null) {
      lifeCycle.openContext();
    }
    
    if(! hasTermsAndConditions()) {
      createTermsAndConditions();
    }
    else {
      LOG.debug("Terms and conditions: yet checked");
    }
  }

  
  /*=======================================================================
   * API private methods
   *======================================================================*/
  
  private void createTermsAndConditions() {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();

      try {
          Node publicApplicationNode = nodeHierarchyCreator.getPublicApplicationNode(sessionProvider);
          if(! publicApplicationNode.hasNode(TC_NODE_NAME)) {
              publicApplicationNode = publicApplicationNode.addNode(TC_NODE_NAME, "nt:folder");
              publicApplicationNode.addMixin("mix:referenceable");
              publicApplicationNode.getSession().save();
          }
      } catch(Exception e) {
        LOG.error("Terms and conditions: cannot create node", e);
      } finally {
          if (sessionProvider != null) {
              sessionProvider.close();

          }
      }
  }

  private boolean hasTermsAndConditions() {

      SessionProvider sessionProvider = null;

      try {
          // --- Initial hasTermsAndConditionsNode is false  we nedd to get flag from JCR
          if (hasTermsAndConditionsNode)  {
              // --- Flag loaded only once from JCR (the next loading will be done after you restart the server)
              return hasTermsAndConditionsNode;
          } else {

              try {
                  //--- Get The session Provider
                  sessionProvider = SessionProvider.createSystemProvider();
                  Node publicApplicationNode = nodeHierarchyCreator.getPublicApplicationNode(sessionProvider);
                  // --- If it's exist (case of restart of the server) return true else the first start of platform return false
                  if(publicApplicationNode.hasNode(TC_NODE_NAME)) {
                      hasTermsAndConditionsNode = true;
                  } else {
                      hasTermsAndConditionsNode = false;
                  }

              } catch (Exception E) {

                  LOG.error("Terms and conditions: connot get node", E);
                  hasTermsAndConditionsNode = false;

              } finally {
                  //--- Close the sessionP (all session opened by this provider will be closed)
                  sessionProvider.close();
              }

              return hasTermsAndConditionsNode;
          }

      } catch(Exception e) {
          LOG.error("Terms and conditions: cannot check node", e);
      }

      return hasTermsAndConditionsNode;

  }
}
