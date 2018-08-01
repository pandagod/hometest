import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static junit.framework.Assert.assertEquals;

public class MyPartTwoHomeTest {

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
    public void testRevert(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("one=two");
        kv.accept("Three=four, one=five");
        String displayText = kv.display();
        assertEquals("one=five\n" +
                "Three=four\n", displayText);
        kv.undo();
        displayText = kv.display();
        assertEquals("one=two\n", displayText);
    }

    @Test
    public void testRevertMultipleTimes(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("one=1,two=2");
        kv.accept("one=1,two=TWO");
        String displayText = kv.display();
        assertEquals("one=2\n" +
                "two=TWO\n", displayText);
        kv.undo();
        displayText = kv.display();
        assertEquals("one=1\n"+
                "two=2\n", displayText);
        kv.undo();
        displayText = kv.display();
        assertEquals("", displayText);
    }

    @Test
    public void testRevertWhenAcceptRepeatedKeys(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("one=1,two=2");
        kv.accept("one=1,two=TWO,one=1,two=tWO");
        String displayText = kv.display();
        assertEquals("one=3\n" +
                "two=tWO\n", displayText);
        kv.undo();
        displayText = kv.display();
        assertEquals("one=1\n"+
                "two=2\n", displayText);
    }

    @Test
    public void testRevertWhenErrorOccurredAfterAccept(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("max=2147483647");
        kv.accept("max=2147483647");
        String displayText = kv.display();
        assertEquals("max=2147483647\n", displayText);
        kv.undo();
        displayText = kv.display();
        assertEquals("max=2147483647\n", displayText);
    }

    @Test
    public void testRevertWithAtomicGroup(){
        KeysAndValues kv = injector.getInstance(MyKeysAndValuesImplementation.class);
        kv.accept("441=one,500=three,600=600");
        String displayText1 = kv.display();
        assertEquals("600=600\n",displayText1);
        kv.accept("442=2");
        String displayText2 = kv.display();
        assertEquals("441=one\n" +
                "442=2\n"+
                "500=three\n"+
                "600=600\n", displayText2);
        kv.undo();
        String displayText3 = kv.display();
        assertEquals(displayText1, displayText3);

        kv.accept("442=2,700=700");
        String displayText4 = kv.display();
        assertEquals("441=one\n" +
                "442=2\n"+
                "500=three\n"+
                "600=600\n"+
                "700=700\n", displayText4);
    }


}
