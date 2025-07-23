package Tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import Pages.LoginPage;
import Pages.InventoryPage;
import Pages.CartPage;
import Pages.CheckoutPage;
import Base.BaseTest;

public class CheckoutTest extends BaseTest {

    @Test
    public void testSuccessfulCheckout() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();
        inventoryPage.goToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.clickCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo("Jane", "Doe", "11000");
        checkoutPage.finishCheckout();

        Assert.assertEquals(checkoutPage.getConfirmationMessage(), "Thank you for your order!");
    }
}