package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InventoryPage {
    private WebDriver driver;

    private By addToCartButton = By.id("add-to-cart-sauce-labs-backpack");
    private By cartIcon = By.className("shopping_cart_link");
    private By cartBadge = By.className("shopping_cart_badge");

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
    }

    public void addItemToCart() {
        driver.findElement(addToCartButton).click();
    }

    public void goToCart() {
        driver.findElement(cartIcon).click();
    }

    public String getCartBadgeCount() {
        return driver.findElement(cartBadge).getText();
    }
}
