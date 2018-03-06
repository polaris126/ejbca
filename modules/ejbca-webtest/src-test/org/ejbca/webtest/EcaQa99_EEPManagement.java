/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.webtest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.EjbRemoteHelper;
import org.ejbca.WebTestBase;
import org.ejbca.core.ejb.ra.raadmin.EndEntityProfileSessionRemote;
import org.ejbca.utils.WebTestUtils;

/**
 * 
 * @version $Id$
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EcaQa99_EEPManagement extends WebTestBase {
    
    private static final AuthenticationToken admin = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("UserDataTest"));

    private static WebDriver webDriver;
    private static String eepName = "ECAQA10-EndEntityProfile";
    private static String eepNameClone = "TestEndEntityProfileFromTemplate";
    private static String eepRename = "MyEndEntityProfile";
    
    @BeforeClass
    public static void init() {
        setUp(true, null);
        webDriver = getWebDriver();
    }
    
    @AfterClass
    public static void exit() throws AuthorizationDeniedException {
        EndEntityProfileSessionRemote endEntityProfileSession = EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class);
        endEntityProfileSession.removeEndEntityProfile(admin, eepName);
        endEntityProfileSession.removeEndEntityProfile(admin, eepNameClone);
        endEntityProfileSession.removeEndEntityProfile(admin, eepRename);
        webDriver.quit();
    }
    
    @Test
    public void testA_AddEEP() {
        webDriver.get(getAdminWebUrl());
        WebElement eepLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp')]"));
        eepLink.click();
        
        assertEquals("Clicking 'End Entity Profiles' did not redirect to expected page", WebTestUtils.getUrlIgnoreDomain(webDriver.getCurrentUrl()), "/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp");
        WebElement eepNameInput = webDriver.findElement(By.xpath("//input[@name='textfieldprofilename']"));
        eepNameInput.sendKeys(eepName);
        WebElement eepAddButton = webDriver.findElement(By.xpath("//input[@name='buttonaddprofile']"));
        eepAddButton.click();
        
        WebElement eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
        WebElement eepListItem = eepListTable.findElement(By.xpath("//option[@value='ECAQA10-EndEntityProfile']"));
        assertTrue("'" + eepName + "' was not found in the list of End Entity Profiles after adding it", eepListItem.getText().equals(eepName));
        eepListItem.click();
        webDriver.findElement(By.xpath("//input[@name='buttoneditprofile']")).click();;
        
        WebElement editEepTitle = webDriver.findElement(By.xpath("//div/h3"));
        assertEquals("Unexpected title in 'Edit End Entity Profile'", "End Entity Profile : " + eepName, editEepTitle.getText());
        webDriver.findElement(By.id("checkboxautogeneratedusername")).click();
        webDriver.findElement(By.xpath("//input[@name='buttonsave']")).click();
        WebElement tableResult = webDriver.findElement(By.xpath("//table[@class='list']/tbody/tr/td"));
        WebElement eepListTablePostEdit = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
        WebElement eepListItemPostEdit = eepListTablePostEdit.findElement(By.xpath("//option[@value='ECAQA10-EndEntityProfile']"));
        assertEquals("Status text 'End Entity Profile saved' could not be found after saving EEP", tableResult.getText(), "End Entity Profile saved.");
        assertTrue("'" + eepName + "' was not found in the list of End Entity Profiles after editing it", eepListItemPostEdit.getText().equals(eepName));
        eepListItemPostEdit.click();
        webDriver.findElement(By.xpath("//input[@name='buttoneditprofile']")).click();
        
        WebElement autoGenUsernameButton = webDriver.findElement(By.id("checkboxautogeneratedusername"));
        assertNotNull("'Auto-generated username' was not enabled after saving EEP", autoGenUsernameButton.getAttribute("checked"));
        
        WebElement backLink = webDriver.findElement(By.xpath("//a[@href='adminweb/ra/editendentityprofiles/editendentityprofiles.jsp']"));
        backLink.click();
        
        assertEquals("Clicking 'End Entity Profiles' did not redirect to expected page", WebTestUtils.getUrlIgnoreDomain(webDriver.getCurrentUrl()), "/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp");
        try {
            WebElement eepListTablePostBackLink = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
            eepListTablePostBackLink.findElement(By.xpath("//option[@value='ECAQA10-EndEntityProfile']"));
        } catch (NoSuchElementException e) {
            fail("'" + eepName + "' was not found in list of EEPs after using backlink from edit.");
        }
    }
    
    @Test
    public void testB_verifyAuditLog() {
        webDriver.get(getAdminWebUrl());
        WebElement auditLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/audit/search.jsf')]"));
        auditLink.click();
        
        webDriver.findElement(By.xpath("//input[@class='commandLink reload']")).click();
        try {
            webDriver.findElement(By.xpath("//td[contains(text(), 'End Entity Profile Edit')]"));
            webDriver.findElement(By.xpath("//td[contains(text(), 'End Entity Profile Add')]"));
        } catch (NoSuchElementException e) {
            fail("Audit log entry End Entity Profile Edit / Add not found");
        }
    }
    
    @Test
    public void testC_AddEEPClone() {
        webDriver.get(getAdminWebUrl());
        WebElement eepLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp')]"));
        eepLink.click();
        
        WebElement eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
        WebElement eepListItem = eepListTable.findElement(By.xpath("//option[@value='ECAQA10-EndEntityProfile']"));
        eepListItem.click();
        
        WebElement eepNameInput = webDriver.findElement(By.xpath("//input[@name='textfieldprofilename']"));
        eepNameInput.sendKeys(eepNameClone);
        webDriver.findElement(By.xpath("//input[@name='buttoncloneprofile']")).click();
        
        eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
        eepListItem = eepListTable.findElement(By.xpath("//option[@value='TestEndEntityProfileFromTemplate']"));
        assertTrue("'" + eepNameClone + "' was not found in the list of End Entity Profiles after adding it", eepListItem.getText().equals(eepNameClone));
        
        testB_verifyAuditLog();
        
        eepLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp')]"));
        eepLink.click();
        
        eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
        eepListItem = eepListTable.findElement(By.xpath("//option[@value='TestEndEntityProfileFromTemplate']"));
        eepListItem.click();
        
        eepNameInput = webDriver.findElement(By.xpath("//input[@name='textfieldprofilename']"));
        eepNameInput.sendKeys(eepRename);
        webDriver.findElement(By.xpath("//input[@name='buttonrenameprofile']")).click();
     
        try {
            eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
            eepListItem = eepListTable.findElement(By.xpath("//option[@value='MyEndEntityProfile']"));
        } catch (NoSuchElementException e) {
            fail("Renamed EEP was not found in list of EEPs");
        }
        
        WebElement auditLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/audit/search.jsf')]"));
        auditLink.click();
        
        webDriver.findElement(By.xpath("//input[@class='commandLink reload']")).click();
        try {
            webDriver.findElement(By.xpath("//td[contains(text(), 'End Entity Profile Rename')]"));
        } catch (NoSuchElementException e) {
            fail("Audit log entry End Entity Profile Rename not found");
        }
    }
        
    @Test
    public void testd_RemoveEEP() {
        webDriver.get(getAdminWebUrl());
        WebElement eepLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp')]"));
        eepLink.click();
        
        WebElement eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
        WebElement eepListItem = eepListTable.findElement(By.xpath("//option[@value='MyEndEntityProfile']"));
        eepListItem.click();
        
        webDriver.findElement(By.xpath("//input[@name='buttondeleteprofile']")).click();
        
        Alert confirmAlert = webDriver.switchTo().alert();
        confirmAlert.dismiss();
        webDriver.switchTo().defaultContent();
        
        try {
            eepListTable = webDriver.findElement(By.xpath("//select[@name='selectprofile']"));
            eepListTable.findElement(By.xpath("//option[@value='MyEndEntityProfile']"));
        } catch (NoSuchElementException e) {
            fail(eepRename + " was not found in list of EEPs after canceling delete EEP confirm button");
        }
        
        webDriver.findElement(By.xpath("//input[@name='buttondeleteprofile']")).click();
        confirmAlert = webDriver.switchTo().alert();
        confirmAlert.accept();
        webDriver.switchTo().defaultContent();
        
        WebElement auditLink = webDriver.findElement(By.xpath("//a[contains(@href,'/ejbca/adminweb/audit/search.jsf')]"));
        auditLink.click();
        
        webDriver.findElement(By.xpath("//input[@class='commandLink reload']")).click();
        try {
            webDriver.findElement(By.xpath("//td[contains(text(), 'End Entity Profile Remove')]"));
        } catch (NoSuchElementException e) {
            fail("Audit log entry End Entity Profile Rename not found");
        }
    }
}






















