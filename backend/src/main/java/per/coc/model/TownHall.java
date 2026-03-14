package per.coc.model;

public class TownHall {

    int totalAttacks = 0;
    double probability;
    private int totalStars = 0;
    private final int townHall;
    private Assigned hasBeenAssigned;
    private final PlayerModel player;

    public TownHall(PlayerModel player, int townHall, double probability, Assigned hasBeenAssigned) {
        this.townHall = townHall;
        this.probability = probability;
        this.hasBeenAssigned = hasBeenAssigned;
        this.player = player;
    }

    public void addScore(Score score) {
        this.totalAttacks++;
        this.totalStars += score.star;
        this.probability = (double) (100 * totalStars) / (totalAttacks * 3);
    }

    public double getProbability() {
        return probability;
    }

    public PlayerModel getPlayer() {
        return player;
    }

    public boolean hasBeenAssigned() {
        return hasBeenAssigned.assigned;
    }

    public void assign() {
        hasBeenAssigned.assigned = true;
    }

    public int getTotalAttacks() {
        return totalAttacks;
    }

    public int getTotalStars() {
        return totalStars;
    }

    public int getTownHall() {
        return townHall;
    }

    @Override
    public String toString() {
        return "TownHall{" +
                "attacks=" + totalAttacks +
                "totalStars=" + totalStars +
                "probability=" + probability +
                ", townHall=" + townHall +
                ", player=" + player.name +
                '}';
    }
}
