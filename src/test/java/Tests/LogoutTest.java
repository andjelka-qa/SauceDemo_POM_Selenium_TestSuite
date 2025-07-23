package Tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import Pages.LoginPage;
import Pages.InventoryPage;
import Pages.LogoutPage;
import Base.BaseTest;

public class LogoutTest extends BaseTest {

    @Test
    public void testLogoutFunctionality() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        LogoutPage logoutPage = new LogoutPage(driver);
        logoutPage.logout();

        Assert.assertTrue(driver.getCurrentUrl().contains("saucedemo.com"));
        Assert.assertTrue(driver.getPageSource().contains("Accepted usernames are:"));
    }
}