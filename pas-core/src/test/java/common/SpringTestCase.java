package common;
@org.junit.runner.RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:/applicationContext.xml"})
public abstract class SpringTestCase extends org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests {
    public SpringTestCase() { /* compiled code */ }

    @org.junit.BeforeClass
    public static void beforeClass() throws NoSuchMethodException, java.net.MalformedURLException, java.lang.reflect.InvocationTargetException, IllegalAccessException { /* compiled code */ }

    @org.junit.AfterClass
    public static void afterClass() { /* compiled code */ }
}