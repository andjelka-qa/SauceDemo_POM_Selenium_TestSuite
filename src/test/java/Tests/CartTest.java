package Tests;

import Base.BaseTest;
import Pages.CheckoutPage;
import Pages.InventoryPage;
import Pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class CartTest extends BaseTest {

    @Test
    public void testAddToCartUpdatesBadge() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();
        Assert.assertEquals(inventoryPage.getCartBadgeCount(), "1", "Cart badge not updated");

        inventoryPage.goToCart();
        Assert.assertTrue(driver.getCurrentUrl().contains("cart"), "Cart page not reached");
    }

    @Test
    public void testRemoveItemFromCartClearsBadge() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();
        driver.findElement(By.id("remove-sauce-labs-backpack")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")));

        boolean badgeGone = driver.findElements(By.className("shopping_cart_badge")).isEmpty();
        Assert.assertTrue(badgeGone, "Badge not cleared after removing item");
    }

    @Test
    public void testCartBadgeAccurateAfterMultipleAddsAndRemoves() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement backpackBtn = driver.findElement(By.id("add-to-cart-sauce-labs-backpack"));
        WebElement bikeLightBtn = driver.findElement(By.id("add-to-cart-sauce-labs-bike-light"));

        backpackBtn.click();
        new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.className("shopping_cart_badge"), "1"));

        bikeLightBtn.click();
        new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.className("shopping_cart_badge"), "2"));

        Assert.assertEquals(driver.findElement(By.className("shopping_cart_badge")).getText(), "2");

        WebElement removeBikeLight = driver.findElement(By.id("remove-sauce-labs-bike-light"));
        removeBikeLight.click();

        new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.className("shopping_cart_badge"), "1"));

        Assert.assertEquals(driver.findElement(By.className("shopping_cart_badge")).getText(), "1");
    }

    @Test
    public void testContinueShoppingButtonNavigatesToInventory() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        // Add item to cart and go to cart page
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.className("shopping_cart_link")).click();

        // Click "Continue Shopping"
        driver.findElement(By.id("continue-shopping")).click();

        // Verify URL or page element to confirm back on inventory page
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("inventory"), "Did not return to inventory page.");
    }

    @Test
    public void testCartPersistenceAfterLogoutAndLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        // Add two items to cart
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

        // Logout
        driver.findElement(By.id("react-burger-menu-btn")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();

        // Log back in
        loginPage.login("standard_user", "secret_sauce");

        // Verify cart badge is still 2
        WebElement badge = driver.findElement(By.className("shopping_cart_badge"));
        Assert.assertEquals(badge.getText(), "2", "Cart did not preserve items after logout/login.");
    }

    @Test
    public void testCartItemTotalMatchesCheckoutTotal() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart(); // adds Backpack
        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

        inventoryPage.goToCart();

        // Collect all item prices in the cart
        List<WebElement> itemPrices = driver.findElements(By.className("inventory_item_price"));
        double cartSum = 0;
        for (WebElement price : itemPrices) {
            String value = price.getText().replace("$", "").trim();
            cartSum += Double.parseDouble(value);
        }

        driver.findElement(By.id("checkout")).click();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo("John", "Doe", "12345");

        // Get item total from summary
        WebElement itemTotal = driver.findElement(By.className("summary_subtotal_label"));
        String itemTotalText = itemTotal.getText().replace("Item total: $", "").trim();
        double checkoutTotal = Double.parseDouble(itemTotalText);

        Assert.assertEquals(cartSum, checkoutTotal, 0.01, "Cart total and checkout item total do not match.");
    }


}