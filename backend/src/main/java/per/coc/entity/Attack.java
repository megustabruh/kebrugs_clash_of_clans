package per.coc.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@Entity
@Table(name = "attacks")
public class Attack {

    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int attackId;

    @Column(length = 20)
    private String attackerTag;

    private int destructionPercentage;
    private int stars;
    private int mapPosition;
    private int townHallLevel;

    @Column(length = 50)
    private String warTag;

    @CreationTimestamp
    @Column(updatable = false)
    private Date createdOn;

    // Constructors
    public Attack() {
    }

    public Attack(String attackerTag, int destructionPercentage, int stars, int mapPosition, int townHallLevel,
            String warTag) {
        this.attackerTag = attackerTag;
        this.destructionPercentage = destructionPercentage;
        this.stars = stars;
        this.mapPosition = mapPosition;
        this.townHallLevel = townHallLevel;
        this.warTag = warTag;
    }

    // Getters and setters

    public String getAttackerTag() {
        return attackerTag;
    }

    public void setAttackerTag(String attackerTag) {
        this.attackerTag = attackerTag;
    }

    public int getDestructionPercentage() {
        return destructionPercentage;
    }

    public void setDestructionPercentage(int destructionPercentage) {
        this.destructionPercentage = destructionPercentage;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(int mapPosition) {
        this.mapPosition = mapPosition;
    }

    public int getTownHallLevel() {
        return townHallLevel;
    }

    public void setTownHallLevel(int townHallLevel) {
        this.townHallLevel = townHallLevel;
    }

    public String getWarTag() {
        return warTag;
    }

    public void setWarTag(String warTag) {
        this.warTag = warTag;
    }

    public Date getCreatedOn() {
        return createdOn;
    }
}
