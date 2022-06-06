package tp2.impl.kafka;

public enum Topics {

    DELETE_USER("deleteUser"),
    WRITE_FILE("writeFile");

    private final String label; 

    private Topics(String label) {
        this.label = label;
    }
 
    public String getLabel() {
        return this.label;
    }



}