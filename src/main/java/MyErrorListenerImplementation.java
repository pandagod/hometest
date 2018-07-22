public class MyErrorListenerImplementation implements ErrorListener{
    public void onError(String msg) {
        System.err.println(msg);
    }

    public void onError(String msg, Exception e) {
        System.err.println(String.format(
                "Accept failure due to [%s], exception was [%s]", msg, e));
    }
}
