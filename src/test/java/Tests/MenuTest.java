package Tests;

import Base.BaseTest;
import Pages.CartPage;
import Pages.InventoryPage;
import Pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class MenuTest extends BaseTest {

    @Test
    public void testMenuCanBeOpenedFromInventoryPage() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        Assert.assertTrue(logoutLink.isDisplayed(), "Menu did not open correctly");
    }

    @Test
    public void testMenuItemsAreVisible() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        driver.findElement(By.id("react-burger-menu-btn")).click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(driver.findElement(By.id("inventory_sidebar_link")).isDisplayed(), "All Items link missing");
        Assert.assertTrue(driver.findElement(By.id("about_sidebar_link")).isDisplayed(), "About link missing");
        Assert.assertTrue(driver.findElement(By.id("logout_sidebar_link")).isDisplayed(), "Logout link missing");
        Assert.assertTrue(driver.findElement(By.id("reset_sidebar_link")).isDisplayed(), "Reset App State link missing");
    }

    @Test
    public void testMenuCanBeClosed() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        driver.findElement(By.id("react-burger-menu-btn")).click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement closeButton = driver.findElement(By.id("react-burger-cross-btn"));
        closeButton.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean isMenuStillVisible = !driver.findElements(By.id("logout_sidebar_link")).isEmpty() &&
                driver.findElement(By.id("logout_sidebar_link")).isDisplayed();

        Assert.assertFalse(isMenuStillVisible, "Menu did not close properly");
    }

    @Test
    public void testMenuNotAccessibleBeforeLogin() {
        // We are still on login page here (from BaseTest setup)
        boolean isMenuButtonPresent = !driver.findElements(By.id("react-burger-menu-btn")).isEmpty();

        Assert.assertFalse(isMenuButtonPresent, "Menu button should not be visible before login");
    }

    //------- All Items ------------

    @Test
    public void testAllItemsMenuKeepsUserOnInventoryPage() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        // Ensure we are on inventory page
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("inventory"));

        String urlBefore = driver.getCurrentUrl();

        // Open menu and click "All Items"
        driver.findElement(By.id("react-burger-menu-btn")).click();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        // Wait and compare URL after click
        wait.until(ExpectedConditions.urlContains("inventory"));
        String urlAfter = driver.getCurrentUrl();

        // Assertion: user should stay on same page
        Assert.assertEquals(urlAfter, urlBefore, "User was redirected away after clicking 'All Items'");
    }

    //------- About -----------

    @Test
    public void testAboutLinkRedirectsCorrectly() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        // Open menu
        driver.findElement(By.id("react-burger-menu-btn")).click();

        // Wait for About link to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new URL (same tab)
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));

        // Assert redirect destination contains "saucelabs"
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("saucelabs"), "About link did not redirect to Sauce Labs website");
    }

    //------- Reset App State -------

    @Test
    public void testResetAppStateClearsCartFromCheckout() {
        // Login
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart(); // Adds backpack

        // Navigate to Cart
        inventoryPage.goToCart();

        // Proceed to Checkout
        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        // Open menu and reset app state
        driver.findElement(By.id("react-burger-menu-btn")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Go back to cart
        driver.findElement(By.className("shopping_cart_link")).click();

        // Verify cart is empty
        boolean cartIsEmpty = driver.findElements(By.className("cart_item")).isEmpty();
        Assert.assertTrue(cartIsEmpty, "Cart is not empty after Reset App State from checkout");
    }

    @Test
    public void testResetAppStateResetsButtons() {  // BUG
        // Login
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);

        // Add an item to cart
        WebElement addButton = driver.findElement(By.id("add-to-cart-sauce-labs-backpack"));
        addButton.click();

        // Confirm it became removed
        WebElement removeButton = driver.findElement(By.id("remove-sauce-labs-backpack"));
        Assert.assertTrue(removeButton.isDisplayed(), "Remove button not displayed after adding to cart");

        // Open menu
        driver.findElement(By.id("react-burger-menu-btn")).click();

        // Wait for Reset App State to be visible and click
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Confirm the button went back to "Add to cart"
        WebElement resetAddButton = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("add-to-cart-sauce-labs-backpack"))
        );
        Assert.assertTrue(resetAddButton.isDisplayed(), "Add to cart button not shown after reset");
    }

}


