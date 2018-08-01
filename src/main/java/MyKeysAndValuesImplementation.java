import com.google.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class MyKeysAndValuesImplementation implements KeysAndValues{

    private final ErrorListener errorListener;

    private TreeMap<MyKey,Stack<MyValue>> map;

    private AtomicGroup<MyKey,MyValue> atomicGroup;

    private long updateVersion = -1;

    @Inject
    public MyKeysAndValuesImplementation(ErrorListener ErrorListener){
        this.errorListener = ErrorListener;
        map = new TreeMap<MyKey, Stack<MyValue>>();
        atomicGroup =  new AtomicGroup<MyKey,MyValue>();
        atomicGroup.addAtomicKey(new MyKey("441"));
        atomicGroup.addAtomicKey(new MyKey("442"));
        atomicGroup.addAtomicKey(new MyKey("500"));
    }

    private void putValue(MyKey myKey, MyValue myValue){
        if(!map.containsKey(myKey)){
            Stack<MyValue> valueStack = new Stack<MyValue>();
            valueStack.push(myValue);
            map.put(myKey,valueStack);
        }else{
            Stack<MyValue> targetStack = (Stack<MyValue>)map.get(myKey);
            MyValue peekValue = targetStack.peek();
            try {
                myValue.update(peekValue);
            }catch (ArithmeticException ae){
                errorListener.onError("Update current value is failure",ae);
            }
            targetStack.push(myValue);
        }
    }

    @Override
    public void accept(String kvPairs) {
        updateVersion++;
        String[] tuples = kvPairs.split(",");
        for(String tuple:tuples){
            int equalIndex = tuple.indexOf("=");
            if(equalIndex>0){
                String key = tuple.substring(0,equalIndex).trim();
                String value = tuple.substring(equalIndex+1);
                String pattern= "^[a-zA-Z0-9]*$";
                if(key.matches(pattern)){
                    MyKey myKey = new MyKey(key);
                    MyValue myValue = new MyValue(value,updateVersion);
                    if(atomicGroup.isAtomicKey(myKey)){
                        myValue.setUpdateAtomicVersion(myValue.getUpdateVersion());
                        if(!atomicGroup.storeValue(myKey,myValue)){
                            errorListener.onError(String.format("Atomic key [%s] overlap is not allowed, reject to accept",myKey.getKey()));
                        }
                        if(atomicGroup.isStoreValueFull()){
                            for (Map.Entry<MyKey,MyValue> entry: atomicGroup.getAtomicMap().entrySet()){
                                MyValue valueWithNewVersion = entry.getValue().createAtomicValue(updateVersion);
                                putValue(entry.getKey(),valueWithNewVersion);
                            }
                            atomicGroup.setAtomicMap(new HashMap<MyKey, MyValue>());
                        }
                    }else {
                        putValue(myKey,myValue);
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

    @Override
    public String display() {
        String result = "";
        for (Map.Entry<MyKey,Stack<MyValue>> entry: this.map.entrySet()){
            MyKey myKey = entry.getKey();
            Stack<MyValue> stack = entry.getValue();
            result += (myKey.toString()+"="+stack.peek().toString()+"\n");
        }
        return result;
    }

    @Override
    public void undo() {
        if(updateVersion>-1){
            Iterator<Map.Entry<MyKey,Stack<MyValue>>> it = this.map.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<MyKey,Stack<MyValue>> entry =it.next();
                Stack<MyValue> stack = entry.getValue();
                MyValue peekValue = stack.peek();
                while (peekValue!=null && peekValue.getUpdateVersion()==updateVersion){
                    if(atomicGroup.isAtomicKey(entry.getKey()) && peekValue.getUpdateAtomicVersion() < updateVersion){
                        atomicGroup.storeValue(entry.getKey(),peekValue); //rebuild atomic map when reverting some atomic key
                    }
                    stack.pop();
                    if(stack.empty()){
                        it.remove();
                        peekValue = null;
                    }else{
                        peekValue = stack.peek();
                    }
                }
            }
            updateVersion--;
        }
    }
}
