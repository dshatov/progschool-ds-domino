import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class Main {
    static Scanner STDIN = new Scanner(System.in);

    public static void main(String[] args) {
        Game game = new Game();
        makeFirstTurn(game);
        while (!isEndOfGame(game)) {
            makeNextTurn(game);
        }
    }

    static void makeNextTurn(Game game) {
        Integer index = null;
        while (index == null) {
            printGameState(game);
            System.out.println("Select:");
            index = selectDice(game);
        }
        ArrayList<Dice> playerDices = game.players.get(game.nextPlayer);
        if (index == -1) {
            playerDices.add(game.dices.remove(game.dices.size() - 1));
            game.nextPlayer = nextPlayerIndex(game, game.nextPlayer);
            return;
        }
        Dice dice = playerDices.remove(index.intValue());
        if (playerDices.isEmpty()) {
            System.out.println("Winner: player " + game.nextPlayer);
            game.nextPlayer = null;
            return;
        }
        game.table = testMoveToTable(dice, game.table);
        game.nextPlayer = nextPlayerIndex(game, game.nextPlayer + 1);
    }

    static void printGameState(Game game) {
        System.out.println();
        System.out.println("Player: " + game.nextPlayer);
        System.out.println("Table: " + game.table.first + "-" + game.table.second);
        System.out.println("Store dices: " + game.dices.size() + " (0: pull)");
        System.out.println("Player dices:");
        ArrayList<Dice> playerDices = game.players.get(game.nextPlayer);
        for (int i = 0; i < playerDices.size(); ++i) {
            Dice dice = playerDices.get(i);
            System.out.print((i + 1) + ": " + dice.first + "-" + dice.second);
            if (i == playerDices.size() - 1) {
                System.out.println();
            } else if (i % 5 == 4) {
                System.out.println();
            } else {
                System.out.print("  ");
            }
        }
    }

    static Integer selectDice(Game game) {
        ArrayList<Dice> player = game.players.get(game.nextPlayer);
        try {
            String s = STDIN.nextLine();
            int index = Integer.parseInt(s.trim()) - 1;
            if (index == -1 && !game.dices.isEmpty()) {
                return -1;
            }
            if (index < 0 || player.size() <= index || testMoveToTable(player.get(index), game.table) == null) {
                System.err.println("Unexpected input");
                return null;
            }
            return index;
        } catch (Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace(System.err);
            return null;
        }
    }

    static boolean isEndOfGame(Game game) {
        return game.nextPlayer == null;
    }

    static Integer nextPlayerIndex(Game game, int firstTry) {
        ArrayList<ArrayList<Dice>> players = game.players;
        for (int i = 0; i < players.size(); i++) {
            int index = (firstTry + i) % game.players.size();
            ArrayList<Dice> player = players.get(index);
            if (playerCanMakeTurn(player, game)) {
                return index;
            }
        }
        return null;
    }

    static boolean playerCanMakeTurn(ArrayList<Dice> player, Game game) {
        if (!game.dices.isEmpty()) {
            return true;
        }
        for (Dice dice : player) {
            if (testMoveToTable(dice, game.table) != null) {
                return true;
            }
        }
        return false;
    }

    static Dice testMoveToTable(Dice dice, Dice table) {
        if (dice.first == table.first) {
            return new Dice(dice.second, table.second);
        }
        if (dice.second == table.first) {
            return new Dice(dice.first, table.second);
        }
        if (dice.first == table.second) {
            return new Dice(table.first, dice.second);
        }
        if (dice.second == table.second) {
            return new Dice(table.first, dice.first);
        }
        return null;
    }

    static ArrayList<Dice> createAllDices() {
        ArrayList<Dice> dices = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            for (int j = i; j < 7; ++j) {
                dices.add(new Dice(i, j));
            }
        }
        Collections.shuffle(dices);
        return dices;
    }

    static ArrayList<Dice> createPlayerDices(ArrayList<Dice> from) {
        ArrayList<Dice> playerDices = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            transferDice(from, playerDices);
        }
        return playerDices;
    }

    static boolean isSymmetric(Dice dice) {
        return dice.first == dice.second;
    }

    static void transferDice(ArrayList<Dice> from, ArrayList<Dice> to) {
        to.add(from.remove(from.size() - 1));
    }

    static int compareDices(Dice o1, Dice o2) {
        boolean sym1 = isSymmetric(o1);
        boolean sym2 = isSymmetric(o2);
        if (sym1 && !sym2) {
            return o1.first == 0 ? -1 : 1;
        }
        if (!sym1 && sym2) {
            return o2.first == 0 ? 1 : -1;
        }
        int diff = (o2.first + o2.second) - (o1.first + o1.second);
        if (sym1 && diff != 0) {
            if (o1.first == 0) {
                return -1;
            }
            if (o1.second == 0) {
                return 1;
            }
        }
        return diff;
    }

    static void makeFirstTurn(Game game) {
        game.table = game.players.get(0).get(0);
        int tp = 0;
        int td = 0;
        for (int p = 0; p < game.players.size(); ++p) {
            ArrayList<Dice> player = game.players.get(p);
            for (int d = 0; d < player.size(); ++d) {
                Dice dice = player.get(d);
                if (compareDices(dice, game.table) > 0) {
                    tp = p;
                    td = d;
                    game.table = dice;
                }
            }
        }
        game.players.get(tp).remove(td);
        game.nextPlayer = (tp + 1) % game.players.size();
    }

    //------------------------------------------------------------------------------------------------------------------

    static class Dice {
        int first;
        int second;

        Dice(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    static class Game {
        ArrayList<ArrayList<Dice>> players;
        ArrayList<Dice> dices;
        Dice table;
        Integer nextPlayer;

        Game() {
            players = new ArrayList<>();
            dices = createAllDices();
            players.add(createPlayerDices(dices));
            players.add(createPlayerDices(dices));
        }
    }
}
