package logic;

public class ProgramStatus {
    private final String id;
    private volatile ProgramState state;
    private String errorMessage;

    public ProgramStatus(String id) {
        this.id = id;
        this.state = ProgramState.PENDING;
    }

    public String getId() {
        return id;
    }

    public ProgramState getState() {
        return state;
    }

    public void setState(ProgramState state) {
        this.state = state;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String msg) {
        this.errorMessage = msg;
    }
}

