package per.coc.model;

public class Score {

    String name;
    int star;
    int townHall;

    public Score(String name, int star, int townHall) {
        this.name = name;
        this.star = star;
        this.townHall = townHall;
    }

    @Override
    public String toString() {
        return "Score{" +
                "name='" + name + '\'' +
                ", star=" + star +
                ", townHall=" + townHall +
                '}';
    }

    public String getName() {
        return name;
    }

    public int getStar() {
        return star;
    }

    public int getTownHall() {
        return townHall;
    }
}
