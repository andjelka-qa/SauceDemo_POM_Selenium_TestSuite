package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

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

    public List<String> getAllItemTitles() {
        List<WebElement> titleElements = driver.findElements(By.className("inventory_item_name"));
        List<String> titles = new ArrayList<>();
        for (WebElement element : titleElements) {
            titles.add(element.getText());
        }
        return titles;
    }

    public List<String> getAllItemDescriptions() {
        List<WebElement> descElements = driver.findElements(By.className("inventory_item_desc"));
        List<String> descriptions = new ArrayList<>();
        for (WebElement element : descElements) {
            descriptions.add(element.getText());
        }
        return descriptions;
    }
}
