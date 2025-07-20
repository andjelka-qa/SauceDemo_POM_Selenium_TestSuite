package Tests;
import org.testng.Assert;
import org.testng.annotations.Test;
import Pages.LoginPage;
import Base.BaseTest;

public class LoginTest extends BaseTest {

    @Test
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"));
    }

    @Test
    public void testLockedOutUserLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("locked_out_user", "secret_sauce");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Sorry, this user has been locked out."));
    }

    @Test
    public void testInvalidPassword() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "wrong_password");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"));
    }

    @Test
    public void testInvalidUsername() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("wrong_user", "secret_sauce");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"));
    }

    @Test
    public void testEmptyUsernameAndPassword() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("", "");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Username is required"));
    }

    @Test
    public void testEmptyUsername() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("", "secret_sauce");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Username is required"));
    }

    @Test
    public void testEmptyPassword() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Password is required"));
    }

    @Test
    public void testProblemUserLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("problem_user", "secret_sauce");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"));
    }

    @Test
    public void testPerformanceGlitchUserLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("performance_glitch_user", "secret_sauce");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"));
    }
}