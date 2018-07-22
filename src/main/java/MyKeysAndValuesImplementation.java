import com.google.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class MyKeysAndValuesImplementation implements KeysAndValues{

    private final ErrorListener errorListener;

    private TreeMap<MyKey,MyValue> map;

    private AtomicGroup<MyKey,String> atomicGroup;

    @Inject
    public MyKeysAndValuesImplementation(ErrorListener ErrorListener){
        this.errorListener = ErrorListener;
        map = new TreeMap<MyKey, MyValue>();
        atomicGroup =  new AtomicGroup();
        atomicGroup.addAtomicKey(new MyKey("441"));
        atomicGroup.addAtomicKey(new MyKey("442"));
        atomicGroup.addAtomicKey(new MyKey("500"));
    }

    private void putValue(MyKey myKey, String value){
        if(!map.containsKey(myKey)){
            map.put(myKey,new MyValue(value));
        }else{
            MyValue targetValue = (MyValue)map.get(myKey);
            try {
                targetValue.update(value);
            }catch (ArithmeticException ae){
                errorListener.onError("Update current value is failure",ae);
            }
        }
    }

    public void accept(String kvPairs) {
        String[] tuples = kvPairs.split(",");
        for(String tuple:tuples){
            int equalIndex = tuple.indexOf("=");
            if(equalIndex>0){
                String key = tuple.substring(0,equalIndex).trim();
                String value = tuple.substring(equalIndex+1);
                String pattern= "^[a-zA-Z0-9]*$";
                if(key.matches(pattern)){
                    MyKey myKey = new MyKey(key);
                    if(atomicGroup.isAtomicKey(myKey)){
                        if(!atomicGroup.storeValue(myKey,value)){
                            errorListener.onError(String.format("Atomic key [%s] overlap is not allowed, reject to accept",myKey.getKey()));
                        }
                        if(atomicGroup.isStoreValueFull()){
                            for (Map.Entry<MyKey,String> entry: atomicGroup.getAtomicMap().entrySet()){
                                putValue(entry.getKey(),entry.getValue());
                            }
                            atomicGroup.setAtomicMap(new HashMap<MyKey, String>());
                        }
                    }else {
                        putValue(myKey,value);
                    }

                }else{
                    errorListener.onError(String.format("Key [%s] is not alphanumericm, reject to accept",key));
                }
            }else{
                errorListener.onError(String.format("String [%s] is invalid KeyValuePair,Character '=' is not found, reject to accept",tuple));
            }

        }

        if(atomicGroup.isMissing()){
            errorListener.onError(String.format("atomic group(%s) missing %s",atomicGroup.toString(),
                    String.join(",", atomicGroup.getMissingValue().stream().map(atomicKey->atomicKey.toString()).collect(Collectors.toList()))));
        }
    }

    public String display() {
        String result = "";
        for (Map.Entry<MyKey,MyValue> entry: this.map.entrySet()){
            MyKey myKey = entry.getKey();
            MyValue myValue = entry.getValue();
            result += (myKey.toString()+"="+myValue.toString()+"\n");
        }
        return result;
    }
}
