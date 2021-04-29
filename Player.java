import java.util.*;

class Player {


    public static void main1(String[] args) {
        String in ="..1..2222..2.\n" +
                ".X.X2X.X2X0X.\n" +
                "1111011101111\n" +
                ".X0X.X2X.X0X.\n" +
                ".........10..\n" +
                ".X.X.X.X.X.X1\n" +
                "..01.....10..\n" +
                ".X0X.X2X.X0X.\n" +
                ".1..0...0..11\n" +
                ".X0X2X.X2X0X.\n" +
                "..12.2.2.2...\n" +
                "0 1 4 4 1 3\n" +
                "0 0 0 0 0 3\n"+
                "1 0 0 1 6 3\n";
        String in2 = "..1..010..1..\n" +
                ".X.X1X.X1X.X.\n" +
                "1022022202201\n" +
                ".X2X.X.X.X2X.\n" +
                ".02.11.11.20.\n" +
                ".X1X2X.X2X1X.\n" +
                ".02.11.11.20.\n" +
                ".X2X.X.X.X2X.\n" +
                "1022022202201\n" +
                ".X.X1X.X1X.X.\n" +
                "..1..010..1..\n" +
                "0 0 0 0 0 3\n" +
                "0 1 12 10 0 3\n" +
                "1 0 0 1 8 3\n" +
                "1 1 12 9 8 3";
        String in3 ="..1..010..1..\n" +
                ".X.X1X.X1X.X.\n" +
                "1022022202201\n" +
                ".X2X.X.X.X2X.\n" +
                ".02.11.11.20.\n" +
                ".X1X2X.X2X1X.\n" +
                ".02.11.11.20.\n" +
                ".X2X.X.X.X2X.\n" +
                "1022022202201\n" +
                ".X.X1X.X1X.X.\n" +
                "..1..010..1..\n" +
                "0 0 0 0 1 3\n" +
                "0 1 12 10 1 3";
        Map map = new Map(13, 11, 0);
        map.parse(in3);
        System.out.println(map.bfs(true).output());

    }
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        int myId = in.nextInt();
        Map map = new Map(width, height, myId);
        boolean firstTurn = true;
        while (true) {
            StringBuilder inputs = new StringBuilder();
            for (int i = 0; i < height; i++) {
                inputs.append(in.next() + "\n");
            }
            long before = System.nanoTime();
            int entities = in.nextInt();
            for (int i = 0; i < entities; i++) {
                inputs.append(in.nextInt()+" "+in.nextInt()+" "+in.nextInt()+" "+in.nextInt()+" "+in.nextInt()+" "+in.nextInt());
                if (i<(entities-1)) inputs.append("\n");
            }
            //System.err.println(inputs.toString());
            map.parse(inputs.toString());
            System.out.println(map.bfs(firstTurn).output());
            firstTurn = false;
        }
    }
}

class Map implements Comparable<Map> {
    private int w;
    private int h;
    private Cell[][] map;
    private int playerId;
    private List<Robot> players;
    public int score;
    public List<Move> move = new ArrayList<>();

    @Override
    public int compareTo(Map o) {
        return o.score - score;
    }

    public Map(int w, int h, int playerId) {
        this.w = w;
        this.h = h;
        this.playerId = playerId;
        this.map = new Cell[w][h];
        this.players = new ArrayList<Robot>();
    }

    public void parse(String inputs) {
        this.players.clear();
        String[] lines = inputs.split("\n");
        for (int i=0; i<h; i++) {
            String[] chars = lines[i].split("");
            for (int j=0; j<chars.length; j++) {
                map[j][i] = CellFactory.createFromMap(chars[j]);
            }
        }
        for (int i=h; i<lines.length; i++) {
            String[] chars = lines[i].split(" ");
            //  System.out.println(chars[0]);
            int owner = Integer.parseInt(chars[1]);
            int x = Integer.parseInt(chars[2]);
            int y = Integer.parseInt(chars[3]);
            int param1 = Integer.parseInt(chars[4]);
            int param2 = Integer.parseInt(chars[5]);
            map[x][y] = CellFactory.createFromEntity(chars[0], owner, param1, param2);

            if (chars[0].equals("0")) {
                Robot player = new Robot(owner, x, y);
                player.setRemainingBombs(param1);
                player.setBombRange(param2);

                addPlayer(player);
            }
        }

        fixBombTurns();
    }

    public boolean containsPlayer(int playerId) {
        if (players.size() == 0) return false;
        for (Robot robot : players) {
            if (robot.id == playerId) return true;
        }
        return false;
    }

    public void addPlayer(Robot player) {
        this.players.add(player);
    }

    public void removePlayer(Robot me) {
        int i = 0;
        for (Robot robot : players) {
            if (me.id == robot.id) {
                players.remove(i);
                break;
            }
            i++;
        }
    }

    public List<Robot> players() {
        return players;
    }

    public boolean playerOnCoords(int x, int y) {
        if (players.size() == 0) return false;
        for (Robot robot : players) {
            if (robot.x == x && robot.y == y) return true;
        }
        return false;
    }

    public boolean killPlayerOnCoords(int x, int y) {
        if (players.size() == 0) return false;
        for (Robot robot : players) {
            if (robot.x == x && robot.y == y) {
                robot.die();
                return true;
            }
        }
        return false;
    }

    public Robot player(int id) {
        for (Robot robot : players) {
            if (robot.id == id) return robot;
        }
        return null;
    }

    public void fixBombTurns() {
        List<BombPosition> bombs = new ArrayList<>();
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                if (map[x][y].isBomb()) {
                    bombs.add(new BombPosition(map[x][y], x, y));
                }
            }
        }

        Collections.sort(bombs);

        for (int i=0; i<bombs.size(); i++) {
            int x = bombs.get(i).position.x;
            int y = bombs.get(i).position.y;
            Cell bomb = map[x][y];
            Integer min = minTurnsLeftOfBombsInRange(x, y);
            if (min != null && min < bomb.turnsLeft()) {
                map[x][y] = CellFactory.createBomb(bomb.owner(), bomb.bombRange(), min);
                //now adjust affected bombs too
                List<Position> bombsAffected = bombsInRange(x, y);
                for (int j=0; j<bombsAffected.size(); j++) {
                    int x2 = bombsAffected.get(j).x;
                    int y2 = bombsAffected.get(j).y;
                    Cell affectedBombCell = map[x2][y2];
                    if (affectedBombCell.turnsLeft()>min) {
                        map[x2][y2] = CellFactory.createBomb(affectedBombCell.owner(), affectedBombCell.bombRange(), min);
                    }
                }
            }
        }
    }

    private Integer minTurnsLeftOfBombsInRange(int bombX, int bombY) {
        List<Position> bombs = bombsInRange(bombX, bombY);
        if (bombs.isEmpty()) return null;

        Integer min = null;
        int x,y;
        for (Position bomb : bombs) {
            x = bomb.x;
            y = bomb.y;
            if (min == null || map[x][y].turnsLeft() < min) min = map[x][y].turnsLeft();
        }
        return min;
    }

    private List<Position> bombsInRange(int x, int y) {
        List<Position> bombs = new ArrayList<>();

        for (int i=x+1; i<w; i++) {
            if (map[i][y].isBox() || map[i][y].isWall() || map[i][y].isItem()) break;
            if (map[i][y].isBomb() && map[i][y].bombRange() > Math.abs(x - i)) {
                bombs.add(new Position(i, y));
            }
        }

        for (int i=x-1; i>=0; i--) {
            if (map[i][y].isBox() || map[i][y].isWall() || map[i][y].isItem()) break;
            if (map[i][y].isBomb() && map[i][y].bombRange() > Math.abs(x - i)) {
                bombs.add(new Position(i, y));
            }
        }

        for (int i=y+1; i<h; i++) {
            if (map[x][i].isBox() || map[x][i].isWall() || map[x][i].isItem()) break;
            if (map[x][i].isBomb() && map[x][i].bombRange() > Math.abs(y - i)) {
                bombs.add(new Position(x, i));
            }
        }

        for (int i=y-1; i>=0; i--) {
            if (map[x][i].isBox() || map[x][i].isWall() || map[x][i].isItem()) break;
            if (map[x][i].isBomb() && map[x][i].bombRange() > Math.abs(y - i)) {
                bombs.add(new Position(x, i));
            }
        }

        return bombs;
    }

    private void set(Cell cell, int x, int y) {
        map[x][y] = cell;
    }

    public Cell get(int x, int y) {
        return map[x][y];
    }

    public Robot me() {
        for (Robot robot : players) {
            if (robot.id == playerId) return robot;
        }
        return null;
    }

    private void setCells(Cell[][] cells) {
        this.map = cells;
    }

    public Map copy() {
        Map copy = new Map(w, h, playerId);
        Cell[][] cellsCopy = new Cell[w][h];
        for (int x=0; x<w; x++)
            if (h >= 0) System.arraycopy(map[x], 0, cellsCopy[x], 0, h);
        copy.setCells(cellsCopy);
        for (Robot player : players) {
            copy.addPlayer(player.copy());
        }
        copy.score = score;
        copy.move = new ArrayList<>();
        if(!move.isEmpty())
            copy.move.addAll(move);
        return copy;
    }

    public String hash() {
        StringBuilder hash= new StringBuilder();
        Robot me =me();
        for (int x=0; x<w; x++)
            for (int y=0; y<h; y++) {
                if(me.x == x && me.y == y) hash.append("#M#");
                else
                    hash.append(map[x][y].hash());
            }
        return hash.toString();
    }

    public void print() {
        System.err.println("----------------");
        StringBuilder hash= new StringBuilder();
        for (int x=0; x<w; x++) {
            for (int y = 0; y < h; y++) {
                System.err.print(map[x][y].sign());
            }
            System.err.println();
        }
    }

    public Move bfs(boolean firstTurn) {
        long timeout = firstTurn? 950000000l :96000000l;
        List<Map> maps = new ArrayList<>(300);
        maps.add(this);
        PriorityQueue<Map> nextMaps;
        Set<String> hashes = new HashSet<>();
        long startTime = System.nanoTime();
        for(int i=1;;i++) {
            // long startTime1 = System.nanoTime();
            nextMaps = new PriorityQueue<>();
            hashes.clear();
            // System.err.println("------------");
            for(Map map : maps) {
                if( (System.nanoTime()-startTime) >timeout) {
                    System.err.println("----------"+i);
                    break;
                }
                for(Move move : map.possibleMoves()) {
                    //     System.err.println(map +" " +map.score +" " +map.me().isDead() + " " + map.move + " " + map.me().x + " "+ map.me().y);

                    Map copy = map.simulate(move, i);
                    //System.err.println("SIM " +copy.score +" " +copy.me().isDead() + " " + copy.move + " " + copy.me().x + " "+ copy.me().y);
                    if(!copy.me().isDead() && hashes.add(copy.hash())){
                        nextMaps.add(copy);
                    }
                }
            }
            // System.err.println("***********");
            // System.err.println("kroki="+(System.nanoTime()-startTime1)/1000000);
            // startTime1 = System.nanoTime();
            //   nextMaps.forEach(m -> System.err.println(m.score +" " +m.me().isDead() + " " + m.move + " " + m.me().x + " "+ m.me().y));
            maps.clear();
            int size = nextMaps.size();
            for(int j=0;j<100 && j<size; j++)
                maps.add(nextMaps.poll());//.values().parallelStream().collect(Collectors.toList());
            // System.err.println(maps.size());
            //  startTime1 = System.nanoTime();
            //maps.sort(Comparator.comparing(Map::getScore).reversed().thenComparing(Comparator.comparing(Map::bombPosition)));
            // maps.sort(Comparator.comparing(Map::getScore).reversed());
            // System.err.println("sortowanie="+(System.nanoTime()-startTime1)/1000000);
            // if(maps.size() >70) {
            //     maps = maps.subList(0, 70);
            // }
            //  System.err.println("----------" + maps.size());
            // maps.forEach(m -> System.err.println(m.score +" " +m.me().isDead() + " " + m.move + " " + m.me().x + " "+ m.me().y));
            //System.err.println("**********");
            if( (System.nanoTime()-startTime) >timeout) {
                System.err.println("----------"+i);
                break;
            }
            //  System.err.println("all="+(System.nanoTime()-startTime1));
        }
        //maps.sort(Comparator.comparing(Map::getScore).reversed().thenComparing(Comparator.comparing(Map::bombPosition)));
        if(maps.isEmpty()){
            double r = Math.random();
            if(r <0.5 && r>0.25) {
                return new Move(me().x+1, me().y, false);
            }else if(r <0.5) {
                return new Move(me().x -1, me().y, false);
            }else if(r >0.5 && r<0.75) {
                return new Move(me().x, me().y+1, false);
            }else if(r >0.75) {
                return new Move(me().x, me().y-1, false);
            }
        }
        return maps.get(0).move.get(0);
    }

    public int getScore() {
        return score;
    }
    public int boxesInRange(Cell bomb, int x, int y) {
        List<Cell> things = explosiveThingsInRangeOfBomb(bomb, x, y);
        int boxes = 0;
        for (Cell cell : things) {
            if (cell.isBox()) boxes++;
        }
        return boxes;
    }

    public List<Move> possibleMoves() {
        Robot me = me();
        List<Move> moves = new ArrayList<>();
        int x = me.x;
        int y = me.y;

        moves.add(MoveFactory.getMove(x, y, false));//stay
        if (me.hasBombs() && !map[me.x][me.y].isBomb()) moves.add(MoveFactory.getMove(x, y, true));//stay and place bomb

        if (me.y-1 >= 0) {
            x = me.x;
            y = me.y-1;
            if (!map[x][y].isAnObstacle()) {
                moves.add(new Move(x, y, false));//top
                if (me.hasBombs() && !map[me.x][me.y].isBomb()) moves.add(MoveFactory.getMove(x, y, true));
            }
        }

        if (me.y+1 < h) {
            x = me.x;
            y = me.y+1;
            if (!map[x][y].isAnObstacle()) {
                moves.add(MoveFactory.getMove(x, y, false));//bottom
                if (me.hasBombs() && !map[me.x][me.y].isBomb()) moves.add(MoveFactory.getMove(x, y, true));
            }
        }

        if (me.x-1 >= 0) {
            x = me.x-1;
            y = me.y;
            if (!map[x][y].isAnObstacle()) {
                moves.add(MoveFactory.getMove(x, y, false));//left
                if (me.hasBombs() && !map[me.x][me.y].isBomb()) moves.add(MoveFactory.getMove(x, y, true));
            }
        }

        if (me.x+1 < w) {
            x = me.x+1;
            y = me.y;
            if (!map[x][y].isAnObstacle()) {
                moves.add(MoveFactory.getMove(x, y, false));//right
                if (me.hasBombs() && !map[me.x][me.y].isBomb()) moves.add(MoveFactory.getMove(x, y, true));
            }
        }

        return moves;
    }

    public Map simulate(Move move, int level) {
        //    System.err.println(move);
        Map copy = copy();
        copy.move.add(move);
        Robot player = copy.me();

        //first, things explode
        copy.fixBombTurns();
        copy.explodeBombs(level);

        //then we place a bomb in our current position
        if (move.placeBomb) {
            //TODO
            Cell bomb = CellFactory.createBomb(player.id, player.bombRange(), 7);
            player.decreaseBombs();
            copy.score+=(50*copy.boxesInRange(bomb, player.x, player.y))/copy.move.size();
            copy.set(bomb, player.x, player.y);
        }
        if(copy.me().isDead()) copy.score-=10000;
        //finally we move player to move.x and move.y
        player.x = move.x;
        player.y = move.y;

        //if there is an item there, we take it
        if (copy.get(player.x, player.y).isItem()) {
            copy.score+=5;
            Cell empty = CellFactory.createEmpty();
            copy.set(empty, player.x, player.y);
        }

        return copy;
    }

    private void explodeBombs(int level) {
        Robot me = me();
        for (int x=0; x<w; x++)
            for (int y=0; y<h; y++) {
                Cell cell = map[x][y];
                if (cell.isBomb()) {
                    if (cell.turnsLeft() > 0) {
                        map[x][y] = cell.decreaseTurnsLeft();
                    } else {
                        explodeThingsInRangeOf(cell, x, y, level);

                        if (me().x == x && me().y == y) {
                            me().die();
                        }
                        map[x][y] = cell.blowUp(); //bomb explodes, now this cell is empty
                        if(cell.isMyBomb(me.id)) {
                            me.increaseBombs();
                        }
                    }
                }
            }
    }

    private void explodeThingsInRangeOf(Cell cell, int x, int y, int level) {
        int bombRange = cell.bombRange();

        Robot me = me();
        for (int i=x+1; i<w; i++) {
            if (bombRange > Math.abs(x - i)) {
                if (map[i][y].isBomb()) break;
                if (killPlayerOnCoords(i, y)) {};//me.die();

                if (map[i][y].isBox() || map[i][y].isWall() || map[i][y].isItem()) {
                    if (map[i][y].isMyBox(me.id)) score+=200;
                    map[i][y] = map[i][y].blowUp();
                    break;
                }
            }
        }

        for (int i=x-1; i>=0; i--) {
            if (bombRange > Math.abs(x - i)) {
                if (map[i][y].isBomb()) break;
                if (killPlayerOnCoords(i, y)) {};//me.die();
                if (map[i][y].isBox() || map[i][y].isWall() || map[i][y].isItem()) {
                    map[i][y] = map[i][y].blowUp();
                    if (map[i][y].isMyBox(me.id)) score+=200;
                    break;
                }
            }
        }

        for (int i=y+1; i<h; i++) {
            if (bombRange > Math.abs(y - i)) {
                if (map[x][i].isBomb()) break;
                if (killPlayerOnCoords(x, i)){};// me.die();
                if (map[x][i].isBox() || map[x][i].isWall() || map[x][i].isItem()) {
                    if (map[x][i].isMyBox(me.id)) score+=200;
                    map[x][i] = map[x][i].blowUp();
                    break;
                }
            }
        }

        for (int i=y-1; i>=0; i--) {
            if (bombRange > Math.abs(y - i)) {
                if (map[x][i].isBomb()) break;
                if (killPlayerOnCoords(x, i)){};// me.die();
                if (map[x][i].isBox() || map[x][i].isWall() || map[x][i].isItem()) {
                    if (map[x][i].isMyBox(me.id)) score+=200;
                    map[x][i] = map[x][i].blowUp();
                    break;
                }
            }
        }
    }

    private List<Cell> explosiveThingsInRangeOfBomb(Cell cell, int x, int y) {
        List<Cell> objects = new ArrayList<Cell>();

        int bombRange = cell.bombRange();

        for (int i=x+1; i<w; i++) {
            if (bombRange > Math.abs(x - i)) {
                if (map[i][y].isBomb()) break;
                if (playerOnCoords(i, y)) objects.add(map[i][y]);
                if (map[i][y].isBox() || map[i][y].isWall() || map[i][y].isItem()) {
                    objects.add(map[i][y]);
                    break;
                }
            }
        }

        for (int i=x-1; i>=0; i--) {
            if (bombRange > Math.abs(x - i)) {
                if (map[i][y].isBomb()) break;
                if (playerOnCoords(i, y)) objects.add(map[i][y]);
                if (map[i][y].isBox() || map[i][y].isWall() || map[i][y].isItem()) {
                    objects.add(map[i][y]);
                    break;
                }
            }
        }

        for (int i=y+1; i<h; i++) {
            if (bombRange > Math.abs(y - i)) {
                if (map[x][i].isBomb()) break;
                if (playerOnCoords(x, i)) objects.add(map[x][i]);
                if (map[x][i].isBox() || map[x][i].isWall() || map[x][i].isItem()) {
                    objects.add(map[x][i]);
                    break;
                }
            }
        }

        for (int i=y-1; i>=0; i--) {
            if (bombRange > Math.abs(y - i)) {
                if (map[x][i].isBomb()) break;
                if (playerOnCoords(x, i)) objects.add(map[x][i]);
                if (map[x][i].isBox() || map[x][i].isWall() || map[x][i].isItem()) {
                    objects.add(map[x][i]);
                    break;
                }
            }
        }

        return objects;
    }

    public boolean playerIsDead() {
        return me().isDead();
    }

}

class Cell {

    private final CellType type;
    private final int bombRange;
    private final int owner;
    private final int turnsLeft;
    private final ItemType itemType;

    public Cell(CellType type, int owner, int bombRange, int turnsLeft, ItemType itemType) {
        this.type = type;
        this.owner = owner;
        this.bombRange = bombRange;
        this.turnsLeft = turnsLeft;
        this.itemType = itemType;
    }

    public boolean isMyBomb(int id) {
        return type == CellType.BOMB && this.owner == id;
    }

    public Cell decreaseTurnsLeft() {
        return CellFactory.createBomb(owner, bombRange, turnsLeft - 1);
    }

    public boolean isBomb() { return type == CellType.BOMB; }
    public boolean isWall() { return type == CellType.WALL; }
    public boolean isBox() { return type == CellType.BOX; }
    public boolean isItem() { return type == CellType.ITEM; }
    public boolean isEmpty() { return type == CellType.EMPTY; }

    public boolean isMyBox(int owner) { return type == CellType.BOX && this.owner == owner; }

    public boolean isAnObstacle() {
        return type == CellType.WALL || type == CellType.BOX || type == CellType.BOMB;
    }

    public CellType type() {
        return type;
    }

    public int bombRange() {
        return bombRange;
    }

    public int turnsLeft() {
        return turnsLeft;
    }

    public int owner() {
        return owner;
    }

    public ItemType itemType() {
        return itemType;
    }

    public Cell blowUp() {
        if (type == CellType.WALL) return this; //walls never explode

        Cell explodedCell = CellFactory.createEmpty();

        if (type == CellType.BOX && itemType != null) {
            explodedCell = CellFactory.createItem(itemType);
        }

        return explodedCell;
    }

    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return bombRange == cell.bombRange &&
                owner == cell.owner &&
                turnsLeft == cell.turnsLeft &&
                type == cell.type &&
                itemType == cell.itemType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, bombRange, owner, turnsLeft, itemType);
    }

    public String hash() {
        return type +"#"+itemType+"#";
    }

    public char sign() {
        switch (type) {
            case BOX:
                return '0';
            case BOMB:
                return 'B';
            case ITEM:
                return 'I';
            case WALL:
                return 'X';
            case EMPTY:
                return '.';
        }
        return '1';
    }
}

class Move {
    public int x;
    public int y;
    public boolean placeBomb;

    public Move(int x, int y, boolean placeBomb) {
        this.x = x;
        this.y = y;
        this.placeBomb = placeBomb;
    }
    public String output() {
        String move = "";
        if (placeBomb) move += "BOMB ";
        else move += "MOVE ";
        return move + x + " " + y;
    }

    @Override
    public String toString() {
        return output();
    }
}

class MoveFactory {
    static java.util.Map<String, Move> moves = new HashMap<>();

    public static Move getMove(int x, int y, boolean bomb) {
        String hash = hash(x, y, bomb);
        return moves.computeIfAbsent(hash, s -> new Move(x, y, bomb));
    }

    private static String hash(int x, int y, boolean bomb) {
        return x+"#"+y+"#"+bomb;
    }
}

class Robot {
    public int id;
    public int x;
    public int y;
    private int bombRange;
    private int bombs;
    private boolean dead;

    public Robot(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.dead = false;
    }

    public void die() {
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    public void setBombRange(int bombRange) {
        this.bombRange = bombRange;
    }

    public int bombRange() {
        return bombRange;
    }

    public void setRemainingBombs(int bombs) {
        this.bombs = bombs;
    }

    public boolean hasBombs() {
        return bombs > 0;
    }

    public void decreaseBombs() {
        bombs--;
    }

    public void increaseBombs() {
        bombs++;
    }

    public int bombs() {
        return bombs;
    }

    public Robot copy() {
        Robot copy = new Robot(id, x, y);
        copy.setBombRange(bombRange);
        copy.setRemainingBombs(bombs);
        return copy;
    }

}

class CellFactory {
    private static final Cell EMPTY = new Cell(CellType.EMPTY, 0, 0, 0, null);
    private static final Cell WALL = new Cell(CellType.WALL, 0, 0, 0, null);
    private static final Cell EMPTY_BOX = new Cell(CellType.BOX, 0, 0, 0, null);
    private static final Cell BOX_BOMB = new Cell(CellType.BOX, 0, 0, 0, ItemType.BOMB);
    private static final Cell BOX_RANGE = new Cell(CellType.BOX, 0, 0, 0, ItemType.RANGE);
    private static final Cell ITEM_BOMB = new Cell(CellType.ITEM, 0, 0, 0, ItemType.BOMB);
    private static final Cell ITEM_RANGE = new Cell(CellType.ITEM, 0, 0, 0, ItemType.RANGE);

    private static java.util.Map<String, Cell> bombPool = new HashMap<String, Cell>();

    public static Cell createFromMap(String character) {
        Cell cell;
        switch (character) {
            case ".":
                cell = createEmpty();
                break;
            case "X":
                cell = createWall();
                break;
            case "0":
                cell = createBox(null);
                break;
            case "1":
                cell = createBox(ItemType.RANGE);
                break;
            case "2":
                cell = createBox(ItemType.BOMB);
                break;
            default:
                cell = createEmpty();
                break;
        }
        return cell;
    }

    public static Cell createFromEntity(String character, int owner, int param1, int param2) {
        Cell cell;
        switch (character) {
            case "1":
                cell = createBomb(owner, param2, param1 - 1);
                break;
            case "2":
                ItemType itemType = ItemType.RANGE;
                if (param1 == 2) itemType = ItemType.BOMB;
                cell = createItem(itemType);
                break;
            default:
                cell = createEmpty();
                break;
        }
        return cell;
    }

    private static Cell createWall() {
        return WALL;
    }

    public static Cell createEmpty() {
        return EMPTY;
    }

    public static Cell createBox(ItemType itemType) {
        if (itemType == null) return EMPTY_BOX;
        if (itemType == ItemType.BOMB) return BOX_BOMB;
        return BOX_RANGE;
    }

    public static Cell createItem(ItemType itemType) {
        if (itemType == ItemType.BOMB) return ITEM_BOMB;
        return ITEM_RANGE;
    }

    public static Cell createBomb(int owner, int bombRange, int turnsLeft) {
        String key = bombKey(owner, bombRange, turnsLeft);
        Cell bomb = bombPool.get(key);
        if (bomb == null) {
            bomb = new Cell(CellType.BOMB, owner, bombRange, turnsLeft, null);
            bombPool.put(key, bomb);
        }
        return bomb;
    }

    public static String bombKey(int owner, int range, int turns) {
        return owner + " " + range + " " + turns;
    }
}

class Position {
    public final int y;
    public final int x;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class BombPosition implements Comparable<BombPosition> {
    public final Position position;
    public final Cell bomb;

    public BombPosition(Cell cell, int x, int y) {
        this.bomb = cell;
        this.position = new Position(x, y);
    }

    @Override
    public int compareTo(BombPosition o) {
        return this.bomb.turnsLeft() - o.bomb.turnsLeft();
    }
}

enum CellType {EMPTY, BOX, WALL, BOMB, ITEM}
enum ItemType {BOMB, RANGE}

