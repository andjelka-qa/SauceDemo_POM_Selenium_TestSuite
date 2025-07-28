package DataProviders;

import org.testng.annotations.DataProvider;

public class UserDataProvider {

    @DataProvider(name = "usersWhoCanReachCart")
    public static Object[][] usersWhoCanReachCart() {
        return new Object[][] {
                {"standard_user", true},
                {"problem_user", true},
                {"performance_glitch_user", true},
                {"locked_out_user", false}
        };
    }

}
