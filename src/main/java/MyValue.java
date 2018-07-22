class MyValue{
    private String nonnumeric;
    private int numeric;
    private boolean isNumeric;

    MyValue(String value){
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

    public void update(String newValue) throws ArithmeticException{
        newValue = newValue.trim();
        try {
            Integer newInteger = Integer.parseInt(newValue);
            if(isNumeric){
                numeric=Math.addExact(numeric,newInteger);
            }else{
                numeric = newInteger;
                isNumeric = true;
            }

        }
        catch (NumberFormatException nfe){
            nonnumeric = newValue;
            if(isNumeric){
                isNumeric = false;
            }
        }
        catch (ArithmeticException ae){
            throw ae;
        }

    }
    public String toString(){
        if(isNumeric){
            return String.valueOf(numeric);
        }else{
            return nonnumeric;
        }
    }
}