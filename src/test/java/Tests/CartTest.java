package Tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import Pages.InventoryPage;
import Pages.LoginPage;
import Base.BaseTest;

public class CartTest extends BaseTest {

    @Test
    public void testAddToCartUpdatesBadge() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();
        Assert.assertEquals(inventoryPage.getCartBadgeCount(), "1");

        inventoryPage.goToCart();
        Assert.assertTrue(driver.getCurrentUrl().contains("cart"));
    }
}
