class MyKey implements Comparable<MyKey>{
    private String key;

    MyKey(String key){
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }

    public boolean equals(Object obj){
        if(obj==null){
            return false;
        }else{
            if(obj instanceof MyKey){
                MyKey myKey = (MyKey)obj;
                if (myKey.getKey().equals(this.getKey())){
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode(){
        return this.getKey().hashCode();
    }

    public int compareTo(MyKey o) {
        return this.key.toLowerCase().compareTo(o.getKey().toLowerCase());
    }

    public String toString(){
        return this.key;
    }
}
