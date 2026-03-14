package per.coc.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "fetch_logs")
public class FetchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime runTime;

    @Column(length = 50)
    private String warType;  // "CWL", "NORMAL", "NONE"

    @Column(length = 50)
    private String warState;  // "inWar", "preparation", "warEnded", "notInWar"

    @Column(length = 100)
    private String warTag;

    private int newAttacksSaved;
    private int newPlayersSaved;
    private int newWarsSaved;

    @Column(length = 500)
    private String message;

    private boolean success;

    // Constructors
    public FetchLog() {
    }

    public FetchLog(LocalDateTime runTime, String warType, String warState, String warTag,
                    int newAttacksSaved, int newPlayersSaved, int newWarsSaved, String message, boolean success) {
        this.runTime = runTime;
        this.warType = warType;
        this.warState = warState;
        this.warTag = warTag;
        this.newAttacksSaved = newAttacksSaved;
        this.newPlayersSaved = newPlayersSaved;
        this.newWarsSaved = newWarsSaved;
        this.message = message;
        this.success = success;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getRunTime() {
        return runTime;
    }

    public void setRunTime(LocalDateTime runTime) {
        this.runTime = runTime;
    }

    public String getWarType() {
        return warType;
    }

    public void setWarType(String warType) {
        this.warType = warType;
    }

    public String getWarState() {
        return warState;
    }

    public void setWarState(String warState) {
        this.warState = warState;
    }

    public String getWarTag() {
        return warTag;
    }

    public void setWarTag(String warTag) {
        this.warTag = warTag;
    }

    public int getNewAttacksSaved() {
        return newAttacksSaved;
    }

    public void setNewAttacksSaved(int newAttacksSaved) {
        this.newAttacksSaved = newAttacksSaved;
    }

    public int getNewPlayersSaved() {
        return newPlayersSaved;
    }

    public void setNewPlayersSaved(int newPlayersSaved) {
        this.newPlayersSaved = newPlayersSaved;
    }

    public int getNewWarsSaved() {
        return newWarsSaved;
    }

    public void setNewWarsSaved(int newWarsSaved) {
        this.newWarsSaved = newWarsSaved;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
