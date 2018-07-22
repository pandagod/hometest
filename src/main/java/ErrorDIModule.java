import com.google.inject.AbstractModule;

public class ErrorDIModule extends AbstractModule{
    protected void configure() {
        bind(ErrorListener.class).to(MyErrorListenerImplementation.class);
    }
}
