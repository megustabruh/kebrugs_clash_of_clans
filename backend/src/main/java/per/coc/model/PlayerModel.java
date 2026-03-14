package per.coc.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerModel {

    String tag;
    String name;
    int townHallLevel;
    // int king;
    // int queen;
    // int warden;
    // int champion;
    Double power;
    Assigned hasBeenAssigned;

    private TownHall townHall16;
    private TownHall townHall15;
    private TownHall townHall14;
    private TownHall townHall13;
    private TownHall townHall12;
    private TownHall townHall11;
    private TownHall townHall10;

    private List<TownHall> attackHistoriesOfEachTownHall = new ArrayList<>();
    private List<Score> scoreHistory = new ArrayList<>();

    public PlayerModel(String tag, String name, int townHallLevel) {
        this.tag = tag;
        this.name = name;
        // this.power = power == 0 ? ((Math.pow(1.5, townHall)) + (king + queen + warden
        // + champion)) : power;
        this.townHallLevel = townHallLevel;
        // this.king = king;
        // this.queen = queen;
        // this.warden = warden;
        // this.champion = champion;

        hasBeenAssigned = new Assigned();

        this.townHall16 = townHallLevel > 16 ? new TownHall(this, 16, 100.0, hasBeenAssigned)
                : new TownHall(this, 16, 0.0, hasBeenAssigned);
        this.townHall15 = townHallLevel > 15 ? new TownHall(this, 15, 100.0, hasBeenAssigned)
                : new TownHall(this, 15, 0.0, hasBeenAssigned);
        this.townHall14 = townHallLevel > 14 ? new TownHall(this, 14, 100.0, hasBeenAssigned)
                : new TownHall(this, 14, 0.0, hasBeenAssigned);
        this.townHall13 = townHallLevel > 13 ? new TownHall(this, 13, 100.0, hasBeenAssigned)
                : new TownHall(this, 13, 0.0, hasBeenAssigned);
        this.townHall12 = townHallLevel > 12 ? new TownHall(this, 12, 100.0, hasBeenAssigned)
                : new TownHall(this, 12, 0.0, hasBeenAssigned);
        this.townHall11 = townHallLevel > 11 ? new TownHall(this, 11, 100.0, hasBeenAssigned)
                : new TownHall(this, 11, 0.0, hasBeenAssigned);
        this.townHall10 = townHallLevel > 10 ? new TownHall(this, 10, 100.0, hasBeenAssigned)
                : new TownHall(this, 10, 0.0, hasBeenAssigned);
        attackHistoriesOfEachTownHall.add(this.townHall16);
        attackHistoriesOfEachTownHall.add(this.townHall15);
        attackHistoriesOfEachTownHall.add(this.townHall14);
        attackHistoriesOfEachTownHall.add(this.townHall13);
        attackHistoriesOfEachTownHall.add(this.townHall12);
        attackHistoriesOfEachTownHall.add(this.townHall11);
        attackHistoriesOfEachTownHall.add(this.townHall10);
    }

    public TownHall getAttackHistory(int th) {
        switch (th) {
            case 16:
                return this.townHall16;
            case 15:
                return this.townHall15;
            case 14:
                return this.townHall14;
            case 13:
                return this.townHall13;
            case 12:
                return this.townHall12;
            case 11:
                return this.townHall11;
            case 10:
                return this.townHall10;
            default:
                TownHall townHall = new TownHall(this, th, 0.0, new Assigned());
                attackHistoriesOfEachTownHall.add(townHall);
                return townHall;
        }
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public void setTownHallLevel(int townHall) {
        this.townHallLevel = townHall;
    }

    public void addScore(Score score) {
        scoreHistory.add(score);
    }

    public List<TownHall> getAttackHistoriesOfEachTownHall() {
        return attackHistoriesOfEachTownHall;
    }

    public List<Score> getScoreHistories() {
        return scoreHistory;
    }

    public String getName() {
        return name;
    }

    public Double getPower() {
        return power;
    }

    public int getTownHallLevel() {
        return townHallLevel;
    }

    public Assigned isHasBeenAssigned() {
        return hasBeenAssigned;
    }

    public TownHall getTownHall16() {
        return townHall16;
    }

    public TownHall getTownHall15() {
        return townHall15;
    }

    public TownHall getTownHall14() {
        return townHall14;
    }

    public TownHall getTownHall13() {
        return townHall13;
    }

    public TownHall getTownHall12() {
        return townHall12;
    }

    public TownHall getTownHall11() {
        return townHall11;
    }

    public TownHall getTownHall10() {
        return townHall10;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", power=" + power +
                ", townHall=" + townHallLevel +
                '}';
    }
}
