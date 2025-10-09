package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying user information in the Users Panel
 */
public class UserDTO {
    private final StringProperty username;
    private final IntegerProperty mainProgramsUploaded;
    private final IntegerProperty subFunctionsContributed;
    private final IntegerProperty currentCredits;
    private final IntegerProperty creditsSpent;
    private final IntegerProperty totalRuns;

    public UserDTO(String username, int mainProgramsUploaded, int subFunctionsContributed,
                   int currentCredits, int creditsSpent, int totalRuns) {
        this.username = new SimpleStringProperty(username);
        this.mainProgramsUploaded = new SimpleIntegerProperty(mainProgramsUploaded);
        this.subFunctionsContributed = new SimpleIntegerProperty(subFunctionsContributed);
        this.currentCredits = new SimpleIntegerProperty(currentCredits);
        this.creditsSpent = new SimpleIntegerProperty(creditsSpent);
        this.totalRuns = new SimpleIntegerProperty(totalRuns);
    }

    // Property getters
    public StringProperty usernameProperty() { return username; }
    public IntegerProperty mainProgramsUploadedProperty() { return mainProgramsUploaded; }
    public IntegerProperty subFunctionsContributedProperty() { return subFunctionsContributed; }
    public IntegerProperty currentCreditsProperty() { return currentCredits; }
    public IntegerProperty creditsSpentProperty() { return creditsSpent; }
    public IntegerProperty totalRunsProperty() { return totalRuns; }

    // Value getters
    public String getUsername() { return username.get(); }
    public int getMainProgramsUploaded() { return mainProgramsUploaded.get(); }
    public int getSubFunctionsContributed() { return subFunctionsContributed.get(); }
    public int getCurrentCredits() { return currentCredits.get(); }
    public int getCreditsSpent() { return creditsSpent.get(); }
    public int getTotalRuns() { return totalRuns.get(); }
}