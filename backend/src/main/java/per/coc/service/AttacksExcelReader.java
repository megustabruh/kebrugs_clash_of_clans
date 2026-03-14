package per.coc.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import per.coc.model.Assigned;
import per.coc.model.PlayerModel;
import per.coc.model.Score;
import per.coc.model.TownHall;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;






//class Attack {
//
//    String attackingPlayer;
//    int attackingPlayerTH;
//    int enemyPlayerNo;
//    int scoredStar;
//    int probability;
//
//    @Override
//    public String toString() {
//        return "{" +
//                "attackingPlayer='" + attackingPlayer + '\'' +
//                ", enemyPlayerNo=" + enemyPlayerNo +
//                ", scoredStar=" + scoredStar +
//                ", probability=" + probability +
//                '}';
//    }
//
//    public int attackingPlayerTH(Attack attack) {
//        return attackingPlayerTH;
//    }
//}
//
//class AttackStrategy implements Comparable<AttackStrategy> {
//
//    private int totalStars = 0;
//    private double totalProbability = 0.0;
//    private List<Attack> attacks;
//
//    @Override
//    public int compareTo(AttackStrategy other) {
//        // Compare based on totalStars first
//        int starsComparison = Integer.compare(this.totalStars, other.totalStars);
//
//        // If totalStars are the same, compare based on totalProbability
//        if (starsComparison == 0) {
//            return Double.compare(this.totalProbability, other.totalProbability);
//        }
//
//        return starsComparison;
//    }
//
//    public void addAttack(Attack attack) {
//        attacks.add(attack);
//    }
//
//    public List<Attack> getAttacks() {
//        return attacks;
//    }
//}

public class AttacksExcelReader {

    static List<PlayerModel> players = new ArrayList<>();
    static List<TownHall> alreadyAttackedTownHalls = new ArrayList<>();

    //    static List<TownHall> allTownHalls = new ArrayList<>();
//    static List<Integer> enemyTHs = Arrays.asList(15, 15, 15, 15, 15, 15, 14, 14, 14, 15, 14, 14, 14, 13, 13, 13, 13,
//            13, 13, 13, 13, 14, 12, 12, 13, 12, 14, 12, 13, 13);
//    static List<Integer> enemyTHs = Arrays.asList(15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
//            15, 15, 14, 15, 14, 14, 14, 14, 14, 14, 14, 14, 12);
    static List<Integer> enemyTHs = Arrays.asList( 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 14,  14, 14, 14, 14, 14, 14,  12);

//    static List<AttackStrategy> attackStrategies = new ArrayList<>();
    //    static PriorityQueue<AttackStrategy> maxStarQueues = new PriorityQueue<>(5);
//    static PriorityQueue<AttackStrategy> maxProbabilityQueues = new PriorityQueue<>(5);

    static int probabilityCap = 90;

    public static void main(String[] args) {
        try {
            FileInputStream excelFile = new FileInputStream("C:\\Users\\manda\\Downloads\\COC Tracker.xlsx");
            Workbook workbook = new XSSFWorkbook(excelFile); // For .xlsx files

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            int rowCount = 0;
            for (Row row : sheet) {
                rowCount++;
                if (rowCount < 2) {
                    continue;
                }
                if (row.getCell(1) == null || row.getCell(1).getCellType() == CellType.BLANK) {
                    break;
                }
                players.add(new PlayerModel("",
                        row.getCell(1).getStringCellValue(),
                        (int) getValue(row, 2)
                ));
            }

            sheet = workbook.getSheetAt(1); // Get the second sheet
            rowCount = 0;
            for (Row row : sheet) {
                rowCount++;
                if (rowCount < 3) {
                    continue;
                }
                for (int cellIndex = 1; cellIndex < row.getLastCellNum(); cellIndex += 3) {
                    Cell cell = row.getCell(cellIndex);
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        continue;
                    } else {
                        Score score = new Score(row.getCell(cellIndex + 2).getStringCellValue(),
                                (int) getValue(row, cellIndex + 1),
                                (int) getValue(row, cellIndex));
                        addScoreOfPlayer(score);
                    }
                }
            }

            // Close the workbook and file input stream
            workbook.close();
            excelFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        printScoresOfEachPlayer();
        calculateProbability();
//        runAllCombinationAndPrintTop5();

        assignAttackers();

    }

    private static void assignAttackers() {

        alreadyAttackedTownHalls.sort(
                Comparator.comparingDouble(TownHall::getProbability)
                        .reversed()
                        .thenComparingInt(TownHall::getTownHall)
                        .reversed()
                        .thenComparingInt(TownHall::getTotalAttacks)
                        .reversed()
        );

        for (TownHall th : alreadyAttackedTownHalls) {
            System.out.println(th);
        }

//        int totalStarsThisWay = 0;
        double averageProbability = 0.0;
        for (int enemyIdx = 0; enemyIdx < enemyTHs.size(); enemyIdx++) {
            int enemyTH = enemyTHs.get(enemyIdx);
            TownHall firstTH = getProbability(enemyTH, 100);
            averageProbability += firstTH.getProbability();
//            totalStarsThisWay+=firstTH.getTotalStars();
            firstTH.assign();
            System.out.println(firstTH.getPlayer().getName() + " should attack " + enemyTH + " at " + (enemyIdx + 1));
        }

        System.out.println("Total stars : " + (averageProbability / enemyTHs.size()));
    }

    private static TownHall getProbability(int enemyTH, int prob) {
        if (prob > probabilityCap) {
            return alreadyAttackedTownHalls.stream().filter(townHall ->
                    townHall.getProbability() >= prob
                            && townHall.getTotalAttacks() > 1
                            && enemyTH <= townHall.getTownHall()
                            && !townHall.hasBeenAssigned()
            ).findFirst().orElse(
                    getProbability(enemyTH, prob - 1)
            );
        }
        return alreadyAttackedTownHalls.stream().filter(townHall ->
                townHall.getProbability() >= prob
                        && townHall.getTotalAttacks() > 1
                        && enemyTH <= townHall.getTownHall()
                        && !townHall.hasBeenAssigned()
        ).findFirst().orElse(
                getOtherPlayers(enemyTH)
        );
    }

    private static TownHall getOtherPlayers(int enemyTH) {
        return alreadyAttackedTownHalls.stream().filter(townHall ->
                townHall.getTotalAttacks() > 1
                        && enemyTH <= townHall.getTownHall()
                        && !townHall.hasBeenAssigned()
        ).findFirst().orElse(
                alreadyAttackedTownHalls.stream().filter(townHall ->
                                enemyTH <= townHall.getTownHall()
                                        && !townHall.hasBeenAssigned())
                        .findFirst().orElse(alreadyAttackedTownHalls.stream().filter(townHall ->
                                        !townHall.hasBeenAssigned())
                                .findFirst().orElse(null))
        );
    }

//    private static void runAllCombinationAndPrintTop5() {
//
//        AttackStrategy attackStrategy = new AttackStrategy();
//        for (int atkIdx = 0; atkIdx < 100; atkIdx++) {
//            players.forEach(player -> {
//                player.hasBeenAssigned = false;
//            });
//            for (int enemyIdx = 0; enemyIdx < 30; enemyIdx++) {
//                Player player = players.stream()
//                        .filter(pla -> !pla.hasBeenAssigned)
//                        .max(Comparator.comparingInt(Player::getTownHall))
//                        .orElse(null);
//                attackStrategy.addAttack(new Attack());
//            }
//        }
//
//        maxProbabilityQueues.add(attackStrategy);
//        maxProbabilityQueues.forEach(as ->
//                as.getAttacks().stream().sorted(Attack::attackingPlayerTH).forEach(System.out::println)
//        );
//    }

    private static void calculateProbability() {
        players.forEach(player -> {
            player.getScoreHistories().forEach(score -> {
                TownHall townHall = player.getAttackHistory(score.getTownHall());
                townHall.addScore(score);
            });
//            alreadyAttackedTownHalls.addAll(player.getAttackHistoriesOfEachTownHall().stream()
//                    .filter(th -> th.attacks > 0).collect(Collectors.toList()));
            alreadyAttackedTownHalls.addAll(player.getAttackHistoriesOfEachTownHall());
        });
    }

    private static double getValue(Row row, int cellNumber) {
        return row.getCell(cellNumber) != null ? row.getCell(cellNumber).getNumericCellValue() : 0;
    }

    private static void printScoresOfEachPlayer() {
        players.forEach(player -> {
//            System.out.println(player);
        });
    }

    private static void addScoreOfPlayer(final Score score) {
        PlayerModel player = players.stream()
                .filter(tempPlayer -> tempPlayer.getName().equals(score.getName()))
                .findFirst()
                .orElse(null);
        if (player != null) {
            player.addScore(score);
        }
    }
}
