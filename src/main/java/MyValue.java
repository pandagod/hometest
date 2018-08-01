class MyValue{
    private String nonnumeric;
    private int numeric;
    private boolean isNumeric;
    private long updateVersion;
    private long updateAtomicVersion;

    MyValue(){

    }

    MyValue(String value,long updateVersion){
        this.updateVersion = updateVersion;
        value = value.trim();
        try {
            numeric = Integer.parseInt(value);
            isNumeric = true;

        }
        catch (NumberFormatException nfe){
            isNumeric = false;
            nonnumeric = value;
        }
    }

    public  MyValue createAtomicValue(long updateVersion){
        MyValue atomicValue = new MyValue();
        atomicValue.setNonnumeric(this.getNonnumeric());
        atomicValue.setNumeric(this.getNumeric());
        atomicValue.setNumeric(this.isNumeric());
        atomicValue.setUpdateAtomicVersion(this.getUpdateAtomicVersion());
        atomicValue.setUpdateVersion(updateVersion);
        return atomicValue;
    }

    public void update(MyValue peekValue) throws ArithmeticException{
        if(isNumeric && peekValue.isNumeric()){
            try{
                numeric=Math.addExact(numeric,peekValue.getNumeric());
            }catch (ArithmeticException ae){
                throw ae;
            }
        }
    }

    public String getNonnumeric() {
        return nonnumeric;
    }

    public int getNumeric() {
        return numeric;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public long getUpdateVersion() {
        return updateVersion;
    }

    public long getUpdateAtomicVersion() {
        return updateAtomicVersion;
    }

    public String toString(){
        if(isNumeric){
            return String.valueOf(numeric);
        }else{
            return nonnumeric;
        }
    }

    public void setNonnumeric(String nonnumeric) {
        this.nonnumeric = nonnumeric;
    }

    public void setNumeric(int numeric) {
        this.numeric = numeric;
    }

    public void setNumeric(boolean numeric) {
        isNumeric = numeric;
    }

    public void setUpdateVersion(long updateVersion) {
        this.updateVersion = updateVersion;
    }

    public void setUpdateAtomicVersion(long updateAtomicVersion) {
        this.updateAtomicVersion = updateAtomicVersion;
    }
}