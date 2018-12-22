package com.msjf.finance.cas.common.test;
@org.junit.runner.RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:/applicationContext.xml"})
public abstract class SpringTestCase extends org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests {
    public SpringTestCase() { /* compiled code */ }

    @org.junit.BeforeClass
    public static void beforeClass() throws java.lang.NoSuchMethodException, java.net.MalformedURLException, java.lang.reflect.InvocationTargetException, java.lang.IllegalAccessException { /* compiled code */ }

    @org.junit.AfterClass
    public static void afterClass() { /* compiled code */ }
}