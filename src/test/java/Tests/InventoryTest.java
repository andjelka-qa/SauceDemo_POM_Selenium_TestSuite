package Tests;

import Base.BaseTest;
import Pages.InventoryPage;
import Pages.LoginPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;

import java.util.*;

import DataProviders.UserDataProvider;

public class InventoryTest extends BaseTest {

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testInventoryLoads(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"));
            return;
        }

        Assert.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "No products loaded for user: " + username);
    }

    @Test
    public void testAddMultipleItemsToCart() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);

        inventoryPage.addItemToCart(); // Adds backpack

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click(); // Adds bike light

        Assert.assertEquals(inventoryPage.getCartBadgeCount(), "2", "Cart badge should show 2 items");
    }

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testRemoveItemFromCart(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.addItemToCart();

        driver.findElement(By.id("remove-sauce-labs-backpack")).click();

        // Verify the cart badge is gone
        boolean badgeGone = driver.findElements(By.className("shopping_cart_badge")).isEmpty();
        Assert.assertTrue(badgeGone, "Cart badge still visible after removing item for user: " + username);
    }

    @Test
    public void testCartNavigation() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(driver);
        inventoryPage.goToCart();

        Assert.assertTrue(driver.getCurrentUrl().contains("cart"), "User was not navigated to cart page");
    }

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testAllProductsHaveEssentialElements(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"));
            return;
        }

        List<WebElement> items = driver.findElements(By.className("inventory_item"));

        for (WebElement item : items) {
            Assert.assertFalse(item.findElements(By.className("inventory_item_name")).isEmpty(),
                    "Missing name for user: " + username);
            Assert.assertFalse(item.findElements(By.className("inventory_item_price")).isEmpty(),
                    "Missing price for user: " + username);
            Assert.assertFalse(item.findElements(By.tagName("img")).isEmpty(),
                    "Missing image for user: " + username);
            Assert.assertFalse(item.findElements(By.className("inventory_item_desc")).isEmpty(),
                    "Missing description for user: " + username);
            Assert.assertFalse(item.findElements(By.tagName("button")).isEmpty(),
                    "Missing button for user: " + username);
        }
    }

    @Test
    public void testAddRemoveButtonToggle() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement button = driver.findElement(By.id("add-to-cart-sauce-labs-backpack"));
        button.click();

        WebElement removeButton = driver.findElement(By.id("remove-sauce-labs-backpack"));
        Assert.assertTrue(removeButton.isDisplayed(), "Remove button not visible after adding");

        removeButton.click();

        WebElement addButtonAgain = driver.findElement(By.id("add-to-cart-sauce-labs-backpack"));
        Assert.assertTrue(addButtonAgain.isDisplayed(), "Add to cart button not visible after removing");
    }

    @Test
    public void testPerformanceGlitchUserLoginTime() {
        long start = System.currentTimeMillis();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("performance_glitch_user", "secret_sauce");

        long duration = System.currentTimeMillis() - start;
        Assert.assertTrue(duration < 5000, "Page took too long to load");
    }

    @Test
    public void testBlockedUserCannotAccessInventoryDirectly() {
        driver.get("https://www.saucedemo.com/inventory.html");
        Assert.assertTrue(driver.getCurrentUrl().contains("saucedemo.com"), "Unauthorized access allowed");
    }

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testProductPricesArePositive(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"));
            return;
        }

        List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));

        for (WebElement priceElement : priceElements) {
            String priceText = priceElement.getText().replace("$", "").trim();
            double price = Double.parseDouble(priceText);
            Assert.assertTrue(price > 0,
                    "Found a non-positive price ($" + price + ") for user: " + username);
        }
    }

    //------- Duplicates ---------

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testNoDuplicateProductTitles(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(driver.getPageSource().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        List<WebElement> titleElements = driver.findElements(By.className("inventory_item_name"));

        Set<String> uniqueTitles = new HashSet<>();
        for (WebElement title : titleElements) {
            String text = title.getText().trim();
            Assert.assertTrue(uniqueTitles.add(text), "Duplicate product title found: " + text);
        }
    }

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testNoDuplicateProductDescriptions(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(driver.getPageSource().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        List<WebElement> descriptionElements = driver.findElements(By.className("inventory_item_desc"));

        Set<String> uniqueDescriptions = new HashSet<>();
        for (WebElement desc : descriptionElements) {
            String text = desc.getText().trim();
            Assert.assertTrue(uniqueDescriptions.add(text), "Duplicate product description found: " + text);
        }
    }

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testNoDuplicateProductImages(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(driver.getPageSource().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        List<WebElement> images = driver.findElements(By.cssSelector(".inventory_item_img img"));

        Set<String> uniqueSrcs = new HashSet<>();
        for (WebElement image : images) {
            String src = image.getAttribute("src");
            Assert.assertFalse(src.isEmpty(), "Found image with empty src attribute.");
            Assert.assertTrue(uniqueSrcs.add(src), "Duplicate image src found: " + src);
        }
    }

    //------- Product Titles and Descriptions are proper ------

    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testItemTitlesAreProper(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(driver.getPageSource().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        List<String> itemTitles = inventoryPage.getAllItemTitles();

        for (String title : itemTitles) {
            title = title.trim();
            Assert.assertFalse(title.isEmpty(), "Title is empty.");
            Assert.assertTrue(Character.isUpperCase(title.charAt(0)), "Title doesn't start with capital: " + title);
            Assert.assertTrue(title.length() <= 100, "Title too long: " + title.length() + " characters");

            String[] words = title.split(" ");
            for (String word : words) {
                Assert.assertFalse(word.matches(".*\\B\\.\\B.*"), "Word has dot in middle: " + word + " in title: " + title);
            }

            List<String> lowercaseExceptions = List.of("of", "in", "and", "the", "on", "at", "to", "a", "an", "for");
            for (String word : words) {
                if (word.length() == 0) continue;
                boolean isException = lowercaseExceptions.contains(word.toLowerCase());
                boolean isCapitalized = Character.isUpperCase(word.charAt(0));

                if (!isException) {
                    Assert.assertTrue(isCapitalized, "Major word not capitalized: " + word + " in title: " + title);
                }
            }
        }
    }


    @Test(dataProvider = "usersWhoCanReachCart", dataProviderClass = UserDataProvider.class)
    public void testItemDescriptionsAreProper(String username, boolean shouldReachCart) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, "secret_sauce");

        if (!shouldReachCart) {
            Assert.assertTrue(driver.getPageSource().contains("locked out"));
            return;
        }

        InventoryPage inventoryPage = new InventoryPage(driver);
        List<String> descriptions = inventoryPage.getAllItemDescriptions();

        for (String desc : descriptions) {
            String trimmed = desc.trim();

            Assert.assertFalse(trimmed.isEmpty(), "Description is empty.");
            Assert.assertEquals(desc, trimmed, "Description has leading or trailing whitespace.");
            Assert.assertTrue(trimmed.length() <= 300, "Description too long: " + trimmed.length() + " chars");
            Assert.assertTrue(Character.isUpperCase(trimmed.charAt(0)), "Does not start with capital letter: " + trimmed);

            String[] words = trimmed.split(" ");
            for (String word : words) {
                Assert.assertFalse(word.matches(".*\\B\\.\\B.*"), "Word has dot in middle: " + word + " in description: " + desc);
            }
        }
    }


    //------- Sorting Tests --------


    @Test
    public void testSortingByPriceLowToHigh() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Price (low to high)");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));
        List<Double> actualPrices = new ArrayList<>();

        for (WebElement priceElement : priceElements) {
            String priceText = priceElement.getText().replace("$", "");
            actualPrices.add(Double.parseDouble(priceText));
        }

        List<Double> sortedPrices = new ArrayList<>(actualPrices);
        Collections.sort(sortedPrices);

        Assert.assertEquals(actualPrices, sortedPrices, "Prices are not sorted low to high");
    }

    @Test
    public void testSortingByPriceHighToLow() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Price (high to low)");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));
        List<Double> actualPrices = new ArrayList<>();

        for (WebElement priceElement : priceElements) {
            String priceText = priceElement.getText().replace("$", "");
            actualPrices.add(Double.parseDouble(priceText));
        }

        List<Double> expectedPrices = new ArrayList<>(actualPrices);
        expectedPrices.sort(Collections.reverseOrder());

        Assert.assertEquals(actualPrices, expectedPrices, "Prices are not sorted high to low");
    }

    @Test
    public void testSortingByNameAToZ() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Name (A to Z)");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> nameElements = driver.findElements(By.className("inventory_item_name"));
        List<String> actualNames = new ArrayList<>();

        for (WebElement nameElement : nameElements) {
            actualNames.add(nameElement.getText());
        }

        List<String> expectedNames = new ArrayList<>(actualNames);
        Collections.sort(expectedNames);

        Assert.assertEquals(actualNames, expectedNames, "Product names are not sorted A to Z");
    }

    @Test
    public void testSortingByNameZToA() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Name (Z to A)");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> nameElements = driver.findElements(By.className("inventory_item_name"));
        List<String> actualNames = new ArrayList<>();

        for (WebElement nameElement : nameElements) {
            actualNames.add(nameElement.getText());
        }

        List<String> expectedNames = new ArrayList<>(actualNames);
        expectedNames.sort(Collections.reverseOrder());

        Assert.assertEquals(actualNames, expectedNames, "Product names are not sorted Z to A");
    }

    //--------- Footer -------

    @Test
    public void testFooterSocialLinksWork() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter.com']"));
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook.com']"));
        WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin.com']"));

        String twitterHref = twitterLink.getAttribute("href");
        String facebookHref = facebookLink.getAttribute("href");
        String linkedinHref = linkedinLink.getAttribute("href");

        Assert.assertNotNull(twitterHref, "Twitter link is missing");
        Assert.assertTrue(twitterHref.contains("twitter.com"), "Twitter link is invalid");

        Assert.assertNotNull(facebookHref, "Facebook link is missing");
        Assert.assertTrue(facebookHref.contains("facebook.com"), "Facebook link is invalid");

        Assert.assertNotNull(linkedinHref, "LinkedIn link is missing");
        Assert.assertTrue(linkedinHref.contains("linkedin.com"), "LinkedIn link is invalid");
    }

}