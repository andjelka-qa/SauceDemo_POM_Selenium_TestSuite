package Tests;

import Base.BaseTest;
import DataProviders.UserDataProvider;
import Pages.*;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;


public class CheckoutTest extends BaseTest {

    public void loginAndAddToCart() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();
        inventoryPage.goToCart();
    }

    @Test
    public void testSuccessfulCheckout() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo("Jane", "Doe", "11000");
        checkoutPage.finishCheckout();

        Assert.assertEquals(checkoutPage.getConfirmationMessage(), "Thank you for your order!");
    }

    @Test
    public void testEmptyFirstName() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("11000");
        driver.findElement(By.id("continue")).click();

        String errorText = driver.findElement(By.cssSelector("[data-test='error']")).getText();
        Assert.assertTrue(errorText.contains("First Name is required"));
    }

    @Test
    public void testEmptyLastName() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        driver.findElement(By.id("first-name")).sendKeys("Jane");
        driver.findElement(By.id("postal-code")).sendKeys("11000");
        driver.findElement(By.id("continue")).click();

        String errorText = driver.findElement(By.cssSelector("[data-test='error']")).getText();
        Assert.assertTrue(errorText.contains("Last Name is required"));
    }

    @Test
    public void testEmptyPostalCode() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        driver.findElement(By.id("first-name")).sendKeys("Jane");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("continue")).click();

        String errorText = driver.findElement(By.cssSelector("[data-test='error']")).getText();
        Assert.assertTrue(errorText.contains("Postal Code is required"));
    }

    @Test
    public void testInvalidCharactersInCheckoutFields() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);

        // List of weird/invalid inputs
        String[] weirdInputs = {
                "12345",               // numbers only
                "Jane123",             // letters + numbers
                "!@#$%^&*",           // special characters
                "   ",                 // spaces only
                "Jane   Doe",         // extra spaces inside
                "žšćč",                  // special local letters
                "PedugackoImeTolikoDugoDaJeBesmislenoPredugacko" // too long
        };

        for (String input : weirdInputs) {
            driver.navigate().refresh(); // to reset the form
            cartPage.clickCheckout();

            System.out.println("Trying input: " + input);

            driver.findElement(By.id("first-name")).sendKeys(input);
            driver.findElement(By.id("last-name")).sendKeys(input);
            driver.findElement(By.id("postal-code")).sendKeys(input);
            driver.findElement(By.id("continue")).click();

            boolean reachedNextPage = driver.getCurrentUrl().contains("checkout-step-two");

            Assert.assertTrue(reachedNextPage,
                    "Failed to proceed with weird input: " + input);
        }
    }

    @Test
    public void testCancelCheckoutReturnsToCart() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        driver.findElement(By.id("cancel")).click();

        Assert.assertTrue(driver.getCurrentUrl().contains("cart"));
    }

    @Test
    public void testCheckoutOverviewPageLoadsCorrectly() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo("Jane", "Doe", "11000");

        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-step-two"));
        Assert.assertTrue(driver.getPageSource().contains("Payment Information"));
    }

    @Test
    public void testFinishButtonTakesUserToConfirmationPage() {
        loginAndAddToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo("Jane", "Doe", "11000");
        checkoutPage.finishCheckout();

        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-complete"));
        Assert.assertTrue(checkoutPage.getConfirmationMessage().contains("Thank you"));
    }

    //------ Checkout with empty cart

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testCheckoutWithoutAddingItems(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"));
            return;
        }

        driver.findElement(By.className("shopping_cart_link")).click();

        boolean cartIsEmpty = driver.findElements(By.className("cart_item")).isEmpty();
        Assert.assertTrue(cartIsEmpty, "Cart is not empty at start");

        driver.findElement(By.id("checkout")).click();

        boolean navigated = driver.getCurrentUrl().contains("checkout-step-one");
        Assert.assertFalse(navigated, "Bug: " + username + " was allowed to checkout without adding any items!");
    }

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testCannotCheckoutWithEmptyCart(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();
        inventoryPage.goToCart();

        driver.findElement(By.id("remove-sauce-labs-backpack")).click();

        boolean badgeGone = driver.findElements(By.className("shopping_cart_badge")).isEmpty();
        Assert.assertTrue(badgeGone, "Cart badge still present after removing all items");

        driver.findElement(By.id("checkout")).click();

        boolean navigatedToCheckout = driver.getCurrentUrl().contains("checkout-step-one");
        Assert.assertFalse(navigatedToCheckout, "Bug: " + username + " was allowed to checkout with empty cart!");
    }

    @Test
    public void testCartItemsMatchCheckoutOverview() {
        // Login and add two items
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

        // Go to cart and collect item names
        inventoryPage.goToCart();
        List<WebElement> cartItems = driver.findElements(By.className("inventory_item_name"));
        List<String> cartItemNames = new ArrayList<>();
        for (WebElement item : cartItems) {
            cartItemNames.add(item.getText().trim());
        }

        // Proceed to checkout
        driver.findElement(By.id("checkout")).click();
        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo("John", "Doe", "12345");

        // Get item names from checkout overview
        List<WebElement> overviewItems = driver.findElements(By.className("inventory_item_name"));
        List<String> overviewItemNames = new ArrayList<>();
        for (WebElement item : overviewItems) {
            overviewItemNames.add(item.getText().trim());
        }

        // Assert both lists match
        Assert.assertEqualsNoOrder(overviewItemNames.toArray(), cartItemNames.toArray(),
                "Mismatch between cart items and checkout overview items");
    }
}