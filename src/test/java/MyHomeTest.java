import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static junit.framework.Assert.assertEquals;

public class MyHomeTest {

    private Injector injector;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();


    @Before
    public void setUp() {
        injector = Guice.createInjector(new ErrorDIModule());
    }

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    @Test
    public void testAlphaAscendingKeySorting() {
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("one=two");
        kv.accept("Three=four");
        kv.accept("5=6");
        kv.accept("14=X");
        String displayText = kv.display();
        assertEquals("14=X\n" +
                "5=6\n" +
                "one=two\n" +
                "Three=four\n", displayText);

    }

    @Test
    public void testInvalidKey() {
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("one=1,*(=22");
        String displayText = kv.display();
        assertEquals("one=1\n", displayText);
        assertEquals("Key [*(] is not alphanumericm, reject to accept\n", errContent.toString());
    }

    @Test
    public void testInvalidKeyValuePair() {
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("one=1,*(,two=2");
        String displayText = kv.display();
        assertEquals("one=1\n"+
                "two=2\n", displayText);
        assertEquals("String [*(] is invalid KeyValuePair,Character '=' is not found, reject to accept\n", errContent.toString());
    }

    @Test
    public void testIntegerAccumulateAndNonIntegerOverwrite(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("14=15,A=B52,dry=D.R.Y.,14=7,14=4,dry=Don't Repeat Yourself");
        String displayText = kv.display();
        assertEquals("14=26\n"+
                "A=B52\n"+
                "dry=Don't Repeat Yourself\n", displayText);
    }

    @Test
    public void testNegativeIntegerAccumulate(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("some=20");
        kv.accept("some=-30");
        String displayText = kv.display();
        assertEquals("some=-10\n" , displayText);
    }


    @Test
    public void testIntegerOverflow(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("max=2147483647");
        kv.accept("max=2147483647");
        String displayText = kv.display();
        assertEquals("max=2147483647\n" , displayText);
        assertEquals("Accept failure due to [Update current value is failure], " +
                "exception was [java.lang.ArithmeticException: integer overflow]\n", errContent.toString());
    }

    @Test
    public void testAtomicGroup(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("441=one,X=Y, 442=2,500=three");

        String displayText = kv.display();

        assertEquals("441=one\n"+
                "442=2\n"+
                "500=three\n"+
                "X=Y\n", displayText);
    }

    @Test
    public void testAtomicGroupSpecifiedTwice(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("18=zzz,441=one,500=three,442=2,442= A,441 =3,35=D,500=ok  ");

        String displayText = kv.display();
        assertEquals("18=zzz\n"+
                "35=D\n"+
                "441=3\n"+
                "442=A\n"+
                "500=ok\n", displayText);
    }

    @Test
    public void testIncompleteAtomicGroup(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("441=3,500=not ok,13=qwerty");
        String displayText = kv.display();
        assertEquals("13=qwerty\n", displayText);
        assertEquals("atomic group(441,442,500) missing 442\n", errContent.toString());
    }

    @Test
    public void testSecondAtomicGroupIncomplete(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("500= three , 6 = 7 ,441= one,442=1,442=4");
        String displayText = kv.display();
        assertEquals("441=one\n"+
                "442=1\n"+
                "500=three\n"+
                "6=7\n", displayText);
        assertEquals("atomic group(441,442,500) missing 441,500\n", errContent.toString());
    }

    @Test
    public void testAtomicGroupOverlap(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("441=1, 442=1, 441=2, 500=1, 441=2,442=2, 500=2");
        String displayText = kv.display();
        assertEquals("441=3\n"+
                "442=3\n"+
                "500=3\n", displayText);
        assertEquals("Atomic key [441] overlap is not allowed, reject to accept\n", errContent.toString());
    }
}
