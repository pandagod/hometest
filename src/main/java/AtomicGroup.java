import java.util.*;
import java.util.stream.Collectors;

public class AtomicGroup <T,V>{
    private HashSet<T> atomicKeys;
    private HashMap<T,V> atomicMap;

    public HashMap<T, V> getAtomicMap() {
        return atomicMap;
    }

    public void setAtomicMap(HashMap<T, V> atomicMap) {
        this.atomicMap = atomicMap;
    }

    public Set<T> getAtomicKeys() {
        return atomicKeys;
    }

    public AtomicGroup(){
        atomicKeys=new HashSet<T>();
        atomicMap = new HashMap<T,V>();
    }

    public boolean storeValue (T myKey, V myValue){
        if(!atomicMap.containsKey(myKey)){
            atomicMap.put(myKey,myValue);
            return true;
        }else{
            return false;
        }
    }

    public boolean isAtomicKey(T myKey){
        return atomicKeys.contains(myKey);
    }

    public boolean isStoreValueFull(){
        Set<T> keySet = new HashSet<T>(atomicMap.keySet());
        return keySet.equals(atomicKeys);
    }

    public boolean isMissing(){
        Set<T> keySet = new HashSet<T>(atomicMap.keySet());
        return keySet.size()>0&&!keySet.containsAll(atomicKeys);
    }

    public Set<T> getMissingValue(){
        HashSet<T> cloneAtomicKeys = (HashSet<T>)atomicKeys.clone();
        cloneAtomicKeys.removeAll(atomicMap.keySet());
        return cloneAtomicKeys;
    }

    public void addAtomicKey(T addKey){
        atomicKeys.add(addKey);
    }

    public String toString (){
        return String.join(",",atomicKeys.stream().map(atomicKey->atomicKey.toString()).collect(Collectors.toList()));
    }
}
