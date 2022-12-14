package ConsistentHashing;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing {

    private SortedMap<Long,Node> circle;
    private HashFunction hashFunction ;
    private ArrayList<Node> existingNodes ;
    private int virtualReplica ;
    private int defaultPort;

    public ConsistentHashing(int numberOfRunningNodes, int defaultPort,int virtualReplica) {
        this.circle = new TreeMap<Long,Node>();
        this.hashFunction = new HashFunction();
        this.virtualReplica = virtualReplica ;
        this.defaultPort = defaultPort;

        existingNodes = new ArrayList<Node>();
        for (int i = 1; i <= numberOfRunningNodes; i++) {
            existingNodes.add(new Node(i+"",(defaultPort+i)+""));
        }

        /*get the existing nodes
        existingNodes = new ArrayList<ConsistentHashing.Node>();

        */
        //adding the existing nodes
        for (Node n : existingNodes)
            for (int i = 1; i <= virtualReplica; i++) {
                circle.put(hashFunction.hash(n.getName()+i),n);
            }
    }

    public void addInitialNodes(Node newNode){
        circle.put(hashFunction.hash(newNode.name),newNode);
    }

    public void showCircle(){
        for (Map.Entry mapElement : circle.entrySet()) {
            //get the name of the node and the hashing value of it
            Long key = (Long) mapElement.getKey();
            Node value = (Node)mapElement.getValue();

            System.out.println(value.getName() + " : " + key);
        }
    }

    public String get(String key) {
        long hash = hashFunction.hash(key);

        if (circle.containsKey(hash))
            return circle.get(hash).name;

        SortedMap<Long, Node> tailMap = circle.tailMap(hash);

        //tailmap will return map with keys greater than or equal this hash key
        //so if the tailmap was empty that means back to the head of the circle

        hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();

        return circle.get(hash).portName;
    }

    public void addNewNode(int num){
        Node newNode = new Node(num+"",(defaultPort+num)+"");
        for (int i = 1; i <= virtualReplica; i++) {
            long hash = hashFunction.hash(newNode.name+i);

            //assuming that every node has a unique name

            //now selecting the part of the data that is gonna move
            //headmap return map with key less than "hash"
            //so if there is no keys less than hash that mean back to the largest key

            SortedMap<Long, Node> headMap = circle.headMap(hash);
            SortedMap<Long, Node> tailMap = circle.tailMap(hash);
            long previousHash = headMap.isEmpty() ? circle.lastKey() : headMap.firstKey();
            long nextHash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();


            //--> here should send all the data that has hashValue less than
            System.out.println("move all the data that has hashValue greater than " +
                    circle.get(previousHash).getName() + " ==> " + previousHash +
                    " from the node " + circle.get(nextHash).getName() + " ==> " + nextHash
                    + " to the new node : " + newNode.getName() + " ==> " + hash);


            circle.put(hashFunction.hash(newNode.name+i), newNode);
        }

    }
}
