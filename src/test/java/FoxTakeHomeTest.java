import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;


public class FoxTakeHomeTest {

    private RequestSpecification requestSpecification;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = Constants.BASE_URL;
    }


    @Test(dataProvider = "testDataForRegistration")
    public void registerUserTestCase1(String firstName, String lastName, String gender, String password, ITestContext context) {
        JSONObject reqJson = new JSONObject();
        String emailId = "testing" + Math.random() * 1000000 + "@fox.com";
        reqJson.put("email", emailId);
        reqJson.put("password", password);
        reqJson.put("gender", gender);
        reqJson.put("firstName", firstName);
        reqJson.put("lastName", lastName);

        given()
                .header("x-api-key", "DEFAULT")
                .header("Postman-Token", "a74249b3-97f1-45c0-999c-66d7841bed8a")
                .header("Content-Type", "application/json")
                .body(reqJson.toString())
                .post("/register")
                .then().log().all()
                .statusCode(SC_OK)
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName))
                .body("gender", equalTo(gender.toLowerCase()))
                .body("brand", equalTo("fox"))
                .body("hasEmail", equalTo(true))
                .body("email", equalTo(emailId))
                .body("accountType", equalTo("identity"));

        JSONObject reqJson2 = new JSONObject();
        reqJson.put("password", password);
    }

    @DataProvider(name = "testDataForRegistration")
    public Object[][] getDataForRegistration() {
        return new Object[][]
                {
                        {"FirstName1", "LastName1", "m", "ABC_Brand"},
                        {"FirstName2", "LastName2", "f", "DEF_Brand"},
                        {"FirstName3", "LastName3", "o", "GHI_Brand"},
                        {"FirstName4", "LastName4", "M", "JKL_Brand"},
                        {"FirstName5", "LastName5", "F", "MNO_Brand"},
                        {"FirstName6", "LastName6", "O", "PQR_Brand"}//failure
                };

    }

    @Test(dataProvider = "testDataForRegistrationWhenIncorrectDataProvided")
    public void registerUserTestCase2(String firstName, String lastName, String gender, String password, String code, String message, String detail) {
        JSONObject reqJson = new JSONObject();
        String emailId = "testing" + Math.random() * 1000000 + "@fox.com";
        reqJson.put("email", emailId);
        reqJson.put("password", password);
        reqJson.put("gender", gender);
        reqJson.put("firstName", firstName);
        reqJson.put("lastName", lastName);

        given()
                .header("x-api-key", "DEFAULT")
                .header("Postman-Token", "a74249b3-97f1-45c0-999c-66d7841bed8a")
                .header("Content-Type", "application/json")
                .body(reqJson.toString())
                .post("/register")
                .then().log().all()
                .statusCode(SC_BAD_REQUEST)
                .body("status", equalTo(code))
                .body("statusCode", equalTo(Integer.parseInt(code)))
                .body("errorCode", equalTo(Integer.parseInt(code)))
                .body("message", equalTo(message))
                .body("detail", equalTo(detail));

        JSONObject reqJson2 = new JSONObject();
        reqJson.put("password", password);
    }

    @DataProvider(name = "testDataForRegistrationWhenIncorrectDataProvided")
    public Object[][] getDataForRegistrationWhenIncorrectDataProvided() {
        return new Object[][]
                {
                        {"FirstName1", "LastName1", "m", "A", "400", "Invalid Parameters.", "Invalid argument: Invalid password. Need minimum of 6 characters"},
                        {"FirstName2", "LastName2", "m", "!", "400", "Invalid Parameters.", "Invalid argument: Invalid password. Need minimum of 6 characters"},
                        {"FirstName3", "LastName3", "m", "234", "400", "Invalid Parameters.", "Invalid argument: Invalid password. Need minimum of 6 characters"},
                        {"FirstName4", "LastName4", "m", "", "400", "Invalid Parameters.", "password is required"},
                        {"FirstName5", "LastName5", "m", " ", "400", "Invalid Parameters.", "Invalid argument: Invalid password. Need minimum of 6 characters"},
                        {"FirstName6", "LastName6", "A", "fox_123", "400", "Invalid Parameters.", "Invalid argument: Invalid gender"},
                        {"FirstName7", "LastName7", "!", "fox_123", "400", "Invalid Parameters.", "Invalid argument: Invalid gender"},
                        {"FirstName8", "LastName8", "12", "fox_123", "400", "Invalid Parameters.", "Invalid argument: Invalid gender"},
                        {"FirstName9", "LastName9", "", "fox_123", "400", "Invalid Parameters.", "Invalid argument: Invalid gender"},//failure
                        {"FirstName0", "LastName0", " ", "fox_123", "400", "Invalid Parameters.", "Invalid argument: Invalid gender"}
                };

    }

    @Test(dataProvider = "testDataForRegistrationWhenDuplicateEmailProvided")
    public void registerUserTestCase3(String emailId, String firstName, String lastName, String gender, String password, String code, String message, String detail) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("email", emailId);
        reqJson.put("password", password);
        reqJson.put("gender", gender);
        reqJson.put("firstName", firstName);
        reqJson.put("lastName", lastName);

        given()
                .header("x-api-key", "DEFAULT")
                .header("Postman-Token", "a74249b3-97f1-45c0-999c-66d7841bed8a")
                .header("Content-Type", "application/json")
                .body(reqJson.toString())
                .post("/register")
                .then().log().all()
                .statusCode(SC_CONFLICT)
                .body("status", equalTo(code))
                .body("statusCode", equalTo(Integer.parseInt(code)))
                .body("errorCode", equalTo(Integer.parseInt(code)))
                .body("message", equalTo(message))
                .body("detail", equalTo(detail));

        JSONObject reqJson2 = new JSONObject();
        reqJson.put("password", password);
    }

    @DataProvider(name = "testDataForRegistrationWhenDuplicateEmailProvided")
    public Object[][] getDataForRegistrationWhenDuplicateEmailProvided() {
        return new Object[][]
                {
                        {"test23wr@fox.com", "ABC_FirstName", "ABC_LastName", "m", "fox_123", "409", "Email is already registered", "Invalid argument: Email is already registered"},
                };
    }


    @Test(dataProvider = "testDataForLogin")
    public void loginUserTestCase4(String emailId, String password, String firstName, String lastName, String gender, String brand, boolean hasMail, String accountType) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("email", emailId);
        reqJson.put("password", password);

        given()
                .header("x-api-key", "DEFAULT")
                .header("Postman-Token", "dd063a04-d4fa-4ed4-aa6f-363a887f94e3")
                .header("Content-Type", "application/json")
                .body(reqJson.toString())
                .when().log().all()
                .post("/login")
                .then().log().all()
                .statusCode(SC_OK)
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName))
                .body("gender", equalTo(gender.toLowerCase()))
                .body("brand", equalTo(brand))
                .body("hasEmail", equalTo(hasMail))
                .body("email", equalTo(emailId))
                .body("accountType", equalTo(accountType)).log().all();
    }

    @DataProvider(name = "testDataForLogin")
    public Object[][] getDataForLogin() {
        return new Object[][]
                {
                        {"test23wr@fox.com", "fox123", "QA", "Test", "m", "fox", true, "identity"}
                };
    }

    @Test(dataProvider = "testDataForResettingLoginUser", testName = "resettingLoginUserTestCase6")
    public void resettingLoginUserTestCase5(String emailId, String code, String message, String detail) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("email", emailId);

        waitForSomeTime(180);

        given()
                .header("x-api-key", "6E9S4bmcoNnZwVLOHywOv8PJEdu76cM9")
                .header("Postman-Token", "dd063a04-d4fa-4ed4-aa6f-363a887f94e3")
                .header("Content-Type", "application/json")
                .body(reqJson.toString())
                .when().log().all()
                .post("/reset")
                .then().log().all()
                .statusCode(Integer.parseInt(code))
                .body("message", equalTo(message))
                .body("detail", equalTo(detail)).log().all();
    }

    @DataProvider(name = "testDataForResettingLoginUser")
    public Object[][] getDataForResettingLoginUser() {
        return new Object[][]
                {
                        {"test23wr@fox.com", "200", "Reset Email Sent", "Please check your inbox"},
                };
    }


    @Test(dependsOnMethods = "resettingLoginUserTestCase5", dataProvider = "testDataForLoginWithIncorrectData")
    public void loginUserTestCase6(String emailId, String password, String code, String message, String detail) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("email", emailId);
        reqJson.put("password", password);

        given()
                .header("x-api-key", "DEFAULT")
                .header("Postman-Token", "dd063a04-d4fa-4ed4-aa6f-363a887f94e3")
                .header("Content-Type", "application/json")
                .body(reqJson.toString())
                .when().log().all()
                .post("/login")
                .then().log().all()
                .statusCode(Integer.parseInt(code))
                .body("status", equalTo(code))
                .body("statusCode", equalTo(Integer.parseInt(code)))
                .body("errorCode", equalTo(Integer.parseInt(code)))
                .body("message", equalTo(message))
                .body("detail", equalTo(detail)).log().all();
    }

    @DataProvider(name = "testDataForLoginWithIncorrectData")
    public Object[][] getDataForLoginWithIncorrectData() {
        return new Object[][]
                {
                        {"test23wr@fox.com", "", "401", "Invalid LoginId", "Invalid login credentials"},
                        //{"test23wr@fox.com", " ", "401", "Invalid LoginId", "Invalid login credentials"},//failure
                        {"test23wr@fox.com", "fox12", "401", "Invalid LoginId", "Invalid login credentials"},
                        {"", "fox123", "401", "Invalid LoginId", "Invalid login credentials"},
                        {"test23wr@fox.cm", "fox123", "401", "Invalid LoginId", "Invalid login credentials"},
                        {"test23wrfox.com", "fox123", "400", "Invalid Parameters.", "400 Invalid parameter value: Invalid argument: Invalid email"},
                        {"test23wr@", "fox123", "400", "Invalid Parameters.", "400 Invalid parameter value: Invalid argument: Invalid email"}
                };
    }


    @Test(dataProvider = "testDataForResettingLoginUserLimit")
    public void resettingLoginUserLimitTestCase7(String emailId, int invocationCount, String code, String message, String detail, String errorCode, String errorType, String errorMessage) {
        waitForSomeTime(180);
        JSONObject reqJson = new JSONObject();
        reqJson.put("email", emailId);
        for (int i = 1; i <= invocationCount; i++) {
            Response response = given()
                    .header("x-api-key", "6E9S4bmcoNnZwVLOHywOv8PJEdu76cM9")
                    .header("Postman-Token", "dd063a04-d4fa-4ed4-aa6f-363a887f94e3")
                    .header("Content-Type", "application/json")
                    .body(reqJson.toString())
                    .when().log().all()
                    .post("/reset");
            JsonPath jsonPathEvaluator = response.jsonPath();
            if (i == 1) {
                Assert.assertEquals(response.getStatusCode(), Integer.parseInt(code));
                Assert.assertEquals(jsonPathEvaluator.get("detail"), detail);
                Assert.assertEquals(jsonPathEvaluator.get("message"), message);
            } else if (i == invocationCount) {
                Assert.assertEquals(response.getStatusCode(), Integer.parseInt(errorCode));
                Assert.assertEquals(jsonPathEvaluator.get("errorType"), errorType);
                Assert.assertEquals(jsonPathEvaluator.get("errorMessage"), errorMessage);
            }
        }
    }

    @DataProvider(name = "testDataForResettingLoginUserLimit")
    public Object[][] resettingLoginUserLimitData() {
        return new Object[][]
                {
                        {"test23wr@fox.com", 2, "200", "Reset Email Sent", "Please check your inbox", "400", "Bad Request", "rate limit exceeded for request parameters"},
                };
    }

    public static void waitForSomeTime(int timeInSeconds) {
        try {
            Thread.sleep(1000 * timeInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @AfterClass
    public void tearDown() {
    }
}
