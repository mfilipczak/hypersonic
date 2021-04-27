import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    static class Step {
        Node n;
        String dir;
        public Step(Node n, String dir) {
            this.n = n;
            this.dir = dir;
        }
    }
    static class Bomb extends Node {
        public Bomb(int x, int y) {
            super(x, y);
        }
        Node place;
    }
    static class Node implements Comparable<Node>{
        int x;
        int y;
        char type;
        Node parent;
        Queue<Node> path = new LinkedList<>();
        int children;
        int distance = 0;
        int timer;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public String toString() {
            return type + "=x->" +x +";y->" + y +";distance->" + distance +";->children>" +children+";timer->" +timer+";score->" + getScore();
        }
        public boolean equals(Node o) {
            return x== o.x && y == o.y;
        }
        @Override
        public int hashCode() {
            return x + 10*y;
        }
        public int heuristic(Node n) {
            return (int)Math.sqrt((Math.pow((n.x -x), 2) + Math.pow((n.y -y), 2)));
        }
        @Override
        public int compareTo(Player.Node o) {
            int i = o.children -children;
            if(i == 0) {
                i = o.distance - distance;
            }
            return i;
        }

        public int getDistance() {
            return distance;
        }
        public String dirToParent() {
            if(parent.x < x) {
                return "LEFT";
            }
            if(parent.x > x) {
                return "RIGHT";
            }
            if(parent.y < y) {
                return "UP";
            }
            if(parent.y > y) {
                return "DOWN";
            }
            return null;
        }

        public int getParentChildren() {
            return parent.children;
        }

        public int getChildren() {
            return children;
        }

        boolean isBox() {
            return type == '0' || type == '1' || type =='2';
        }
        boolean isFloor () {
            return type == '.' || type == 'I';
        }

        boolean isWall() {
            return type == 'X' || type == 'B' || type == 'I';
        }

        boolean isObstacle() {
            return type == 'X' || type == 'B' || type == 'I' || type =='R';
        }

        public int getItem() {
            return type == 'I' ? 1:0;
        }

        public int getScore() {
            int score = 200;
            score += type == 'I' ? 0:-1;
            if(children == 0) {
                score-=((distance*15) + 50);
            }else {
                score -=((distance*5) -children*7);
            }
            return score;
            // return (double)(children/(distance*2+1)) + (type != 'I' ? 0:0.2);
        }


    }

    public static int getBoxesInRange(int x, int y, int range) {
        int count =0;
        //   System.err.println("CURR>" + range);
        for(int i=1;i<=range; i++) {
            if(x+i >=C) break;
            if(lab[y][x+i].isObstacle()) {
                break;
            }
            if(lab[y][x+i].isBox()) {
                count++;//=((Integer.valueOf(lab[y][x+i].type) +1) *10);
                break;
            }
        }
        for(int i=1;i<=range; i++) {
            if(x-i <0 ) break;
            if(lab[y][x-i].isObstacle()) {
                break;
            }
            if(lab[y][x-i].isBox()) {
                count++;//=((Integer.valueOf(lab[y][x-i].type) +1) *10);
                break;
            }
        }
        for(int i=1;i<=range; i++) {
            if(y+i >=R) break;
            if(lab[y+i][x].isObstacle()) {
                break;
            }
            if(lab[y+i][x].isBox()) {
                count++;//=((Integer.valueOf(lab[y+i][x].type) +1) *10);
                break;
            }
        }
        for(int i=1;i<=range; i++) {
            if(y-i <0 ) break;
            if(lab[y-i][x].isObstacle()) {
                break;
            }
            if(lab[y-i][x].isBox()) {
                count++;//=((Integer.valueOf(lab[y-i][x].type) +1) *10);
                break;
            }
        }
        //   System.err.println("COUNT>" + count);
        return count;
    }

    public static Node findPathToBox(Node start, List<Node> fields, List<Node> notSafeFields, boolean canBeMe, int range, boolean[][] visited) {
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(start);
        start.path.clear();
        start.children =0;
        while(!queue.isEmpty()) {
            Node n = queue.poll();
            visited[n.y][n.x] = true;
            if(n.isFloor() && !notSafeFields.contains(n)){
                if(n.equals(start) && !canBeMe) {}
                else{
                    n.children = getBoxesInRange(n.x,n.y,range);
                    fields.add(n);
                }
            }
            List<Node> children = getChildren(n, visited);
               for(Node c : children) {
                c.children=0;
                c.parent = n;
                c.path.clear();
                if(!n.equals(start)) {
                    c.path.addAll(n.path);
                    c.path.add(n);
                }
                c.distance = n.distance+1;
                if(notSafeFields.stream().filter(ns -> ns.equals(c)).noneMatch(ns -> ns.timer <= c.distance+1)) {
                    queue.add(c);
                }
            }

        }
        //   System.err.println("CURR>" + fields);
        return null;
    }

    public static List<Node> getChildren(Node n, boolean[][] visited) {

        List<Node> list = new ArrayList<>();
        if(n.x - 1 >=0 && lab[n.y][n.x -1].isFloor() && !visited[n.y][n.x -1]) {
            Node c =lab[n.y][n.x -1];
            list.add(c);
        }
        if(n.x + 1 <C && lab[n.y][n.x +1].isFloor() && !visited[n.y][n.x +1]) {
            Node c =lab[n.y][n.x +1];
            list.add(c);
        }
        if(n.y - 1 >=0 && lab[n.y -1][n.x].isFloor() && !visited[n.y -1][n.x]) {
            Node c =lab[n.y -1][n.x];
            list.add(c);
        }
        if(n.y + 1 <R && lab[n.y +1][n.x].isFloor() && !visited[n.y +1][n.x]) {
            Node c =lab[n.y +1][n.x];
            list.add(c);
        }
        return list;

    }

    static int R;
    static int C;
    static Node[][] lab;

    public static List<Node> boxesInRange(int x, int y, int range) {
        List<Node> boxes = new ArrayList<>();
        for(int i=1;i<=range; i++) {
            if(x+i >=C) break;
            // System.err.println(lab[y][x+i] +" "+ lab[y][x+i].isObstacle() +" " +lab[y][x+i].isBox());
            if(lab[y][x+i].isObstacle()) break;
            if(lab[y][x+i].isBox()) {
                boxes.add(lab[y][x+i]);
                break;
            }
        }
        for(int i=1;i<=range; i++) {
            if(x-i <0 ) break;
            if(lab[y][x-i].isObstacle()) break;
            if(lab[y][x-i].isBox()) {
                boxes.add(lab[y][x-i]);
                break;
            }
        }
        for(int i=1;i<=range; i++) {
            if(y+i >=R) break;
            if(lab[y+i][x].isObstacle()) break;
            if(lab[y+i][x].isBox()) {
                boxes.add(lab[y+i][x]);
                break;
            }
        }
        for(int i=1;i<=range; i++) {
            if(y-i <0 ) break;
            if(lab[y-i][x].isObstacle()) break;
            if(lab[y-i][x].isBox()) {
                boxes.add(lab[y-i][x]);
                break;
            }
        }
        // System.err.println("BOXES>" +boxes);
        return boxes;
    }

    public static void destroyBoxesAndMarkFields(List<Player.Node> fields, int x, int y, int range, int timer) {
        boolean bombMarked = false;
        for(int i=1;i<=range; i++) {
            if(x+i >=C) break;
            if(lab[y][x+i].isBox() && !bombMarked) {
                lab[y][x+i].type = 'R';
                bombMarked = true;
                break;
            }
            lab[y][x+i].timer = Math.min(timer, lab[y][x+i].timer);
            fields.add(lab[y][x+i]);
        }
        bombMarked = false;
        for(int i=1;i<=range; i++) {
            if(x-i <0 ) break;
            if(lab[y][x-i].isBox() && !bombMarked) {
                lab[y][x-i].type = 'R';
                bombMarked = true;
                break;
            }
            lab[y][x-i].timer =  Math.min(timer, lab[y][x-i].timer) ;
            fields.add(lab[y][x-i]);
        }
        bombMarked = false;
        for(int i=1;i<=range; i++) {
            if(y+i >=R) break;
            if(lab[y+i][x].isBox() && !bombMarked) {
                lab[y+i][x].type = 'R';

                bombMarked = true;
                break;
            }
            lab[y+i][x].timer =  Math.min(timer, lab[y+i][x].timer) ;
            fields.add(lab[y+i][x]);
        }
        bombMarked = false;
        for(int i=1;i<=range; i++) {
            if(y-i <0 ) break;
            if(lab[y-i][x].isBox() && !bombMarked) {
                lab[y-i][x].type = 'R';

                bombMarked = true;
                break;
            }
            lab[y-i][x].timer =  Math.min(timer, lab[y-i][x].timer);
            fields.add(lab[y-i][x]);
        }
    }
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        C = in.nextInt();
        R = in.nextInt();
        int myId = in.nextInt();
        lab=new Node[R][C];
        Node go =null;
        Node last = null;
        List<Node> floors = new ArrayList<>();
        List<Node> notSafeFields = new ArrayList<>();
        List<Node> bombs = new ArrayList<>();
        Node me = new Node(0,0);
        me.type = '.';
        int bomb =0;
        int range =0;
        // game loop
        while (true) {
            notSafeFields.clear();
            floors.clear();
            bombs.clear();
            for (int i = 0; i < R; i++) {
                String row = in.next();
                Node n;
                for(int j=0;j<C;j++) {
                    if(lab[i][j] ==null) {
                        n = new Node(j,i);
                        lab[i][j] = n;
                    }else {
                        n = lab[i][j];
                    }
                    n.type = row.charAt(j);
                    // n.children = 0;
                    n.timer = Integer.MAX_VALUE;
                    //n.path.clear();
                }
                System.err.println(row);
            }
            System.err.println("--------------------");
            for (int i = 0; i < R; i++) {
                for(int j=0;j<C;j++) {
                    System.err.print(lab[i][j].type);;
                }

                System.err.println();
            }
            System.err.println("BEF>" + notSafeFields);
            int entities = in.nextInt();
            for (int i = 0; i < entities; i++) {
                int entityType = in.nextInt();
                int owner = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int param1 = in.nextInt();
                int param2 = in.nextInt();
                // System.err.println("E(" +(i+1)+")->"+entityType +" " + owner+" " +myId +" " +x +" " +y+" " + param1 +" " +param2);
                if(entityType == 0 && owner == myId) {
                    // me = lab[y][x];
                    me.x = x;
                    me.y = y;
                    bomb = param1;
                    range = param2;
                }
                if(entityType == 1) {
                    lab[y][x].type = 'B';
                    bombs.add(lab[y][x]);
                    destroyBoxesAndMarkFields(notSafeFields, x, y, param2, param1);
                }else if(entityType == 2) {
                    lab[y][x].type = 'I';
                }
            }
            for (int i = 0; i < R; i++) {
                for(int j=0;j<C;j++) {
                    System.err.print(lab[i][j].type);;
                }

                System.err.println();
            }

            if(go == null || notSafeFields.contains(go) || bombs.contains(go)) {
                findPathToBox(me, floors, notSafeFields, false, range, new boolean[R][C]);
                //  System.err.println("ME>" + floors);
                floors.sort(
                        Comparator.comparing(Node::getScore).reversed());
 
                if(!floors.isEmpty()){
                    go = floors.remove(0);
                }

            }

            //  System.err.println("ME>" + go + ";PATH->");

            if(go != null) {
                Node n = go.path.poll();
                boolean placeBomb = false;
                if(n == null) {
                    n = go;
                }
                if(me.equals(go) && bomb >0) {
                    go =null;
                    placeBomb = true;
                }
                //     System.err.println(n +" " + placeBomb +" " +me +" "  + boxesInRange(n.x, n.y, range));
                System.out.println((!boxesInRange(n.x, n.y, range).isEmpty() && placeBomb? "BOMB":"MOVE")+" " +n.x +" " + n.y);
            }
            else {
                System.out.println("MOVE"+" " +me.x +" " + me.y);
            }
            // go = null;
        }
    }
}
