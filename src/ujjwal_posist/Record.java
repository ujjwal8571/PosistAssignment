
package ujjwal_posist;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author student
 */
public class Record {
    
    public Record(){
       globalNodeNumber = 1;
    }
    
    public class Node{
        
        LocalDateTime timestamp;
        int nodeNumber;
        String nodeId;
        ArrayList<Node> childen;
        double sum;
        Data data;
        
        public Node(Owner owner,double value){
            timestamp =  LocalDateTime.now();
            data = new Data();
            data.owner = owner;
            data.value = value;
            data.hashValue = encrypt(makeStringForData(owner.id, value, owner.name), owner.id);
            nodeNumber = globalNodeNumber;
            globalNodeNumber++;
            nodeId = "" + this;
            childen = new ArrayList<>();
            sum=0;
            //do hashValue of node
            
        }
    }
    
    public class Owner{
        int id;
        String name;

        @Override
        public boolean equals(Object obj) {
            Owner other = (Owner)obj;
            return this.id == other.id; 
        }

        
        
        
    }
    
    private class Data{
        Owner owner;
        double value;
        String hashValue; 
    }
    static int globalNodeNumber;
    Node genesisNode;
    
    private String makeStringForData(int id,double value,String ownerName){
        
        String rv = "" + id + "-" + value + "-" + ownerName;
        return rv;
        
    }
    
    public boolean updateNode(int nodeId, Owner owner, double data)throws Exception{
        
        Node node = getNode(nodeId);
        
        if(node.data.owner != owner){
            throw new Exception("This owner is does not own the node");
        }
        node.data.value = data;
        updateSum(genesisNode);
        
        //if updating the value disturbs sum property, i delete this node and add a new node with latest value
        if(!checkSumProperty(genesisNode)){
            deleteNode(null, genesisNode, nodeId,true);
            addNode(owner, data);
        }
        return true;
        
    }
    
    private void deleteNode(Node parent, Node current,int id, boolean manageChildren){
        
        if(current.nodeNumber == id){
            parent.childen.remove(current);
            
            if(manageChildren)
                for(Node child:current.childen)
                    parent.childen.add(child);
        }
        
        for(Node child:current.childen){
            deleteNode(current, child, id,manageChildren);
        }
        
    }
    
    private boolean checkSumProperty(Node node){
        
        double me = genesisNode.data.value;
        
        int sum=0;
        
        for(Node child:node.childen){
            sum += child.sum;
        }
               
        return me>sum;
    }
    
    private Node getNode(int nodeId){
    
        return getNodeHelper(genesisNode , nodeId);
    }
    
    private Node getNodeHelper(Node node, int nodeId){
        
        if(node.nodeNumber == nodeId)
            return node;
        
        for(Node child:node.childen){
            
            Node n = getNodeHelper(child, nodeId);
            if(n!=null)
                return n;
            
        }
        
        return null;
        
    }
    
    public void transferownerShip(int nodeId, Owner current, Owner newOwner) throws Exception{
        
        Node node = getNode(nodeId);
        
        if(node.data.owner != current)
            throw new Exception("This owner is does not own the node");
        
        Data data = node.data;
        data.owner = newOwner;
        data.hashValue = encrypt(makeStringForData(newOwner.id, data.value, newOwner.name), newOwner.id);
        
    }
    
    public void mergeNode(int id1, int id2, Owner owner)throws Exception{
        
        Node one = getNode(id1);
        Node two = getNode(id2);
        
        if(one.data.owner != owner || two.data.owner != owner){
            throw new Exception("node does not belong to this owner");
                    
        }
        
        //ensuring one has larger chain
        if(one.childen.size()<two.childen.size()){
            Node temp = one;
            one = two;
            two = temp;
        }
        
        //right now i am assuming that if they are mergable then larger chain node can accomodate all the child nodes of 
        // other node without voilating child sum property
        for(Node node:two.childen){
            one.childen.add(node);
        }
        
        deleteNode(null, genesisNode, two.nodeNumber, false);
        
        
        
    }
    
    
    //encryption key for now is kept as owner id for simplicity, we can accept a owner id from user
    //and make a hashset of it so it is unique.
    private String encrypt(String data, int key){
    
    StringBuilder sb = new StringBuilder();
    
    for(int i=0;i<data.length();i++){
        char ch = data.charAt(i);
        if(ch == '-'){
            sb.append(ch);
            continue;
        }
        ch += key;
        sb.append(ch);
        
    }
    
    
    return sb.toString();
    }
    
    private String decrypt(String data, int key){
    
    StringBuilder sb = new StringBuilder();
    
    for(int i=0;i<data.length();i++){
        char ch = data.charAt(i);
        if(ch == '-'){
            sb.append(ch);
            continue;
        }
        ch -= key;
        sb.append(ch);
        
    }
    
    
    return sb.toString();
    }
    
    // could not understand genesis node meaning so it is a node which only a super user can access
    //super user made in main funtion for now, can be optimized
    public void makeGenesisNode(double value,Owner owner){
        
        genesisNode = new Node(owner, value);
    }
    
    
    public void addNode(Owner owner, double value) throws Exception{
        
        if(genesisNode.sum + value > genesisNode.data.value){
                throw new Exception("cannot add more nodes");
            }
        
        Node node = new Node(owner, value);
        boolean b = addHelper(genesisNode, node);
        updateSum(genesisNode);
  
    }
    
    private double updateSum(Node node){
        double temp = 0;

        for(Node child:node.childen){
            temp += updateSum(child);
        }
        node.sum = temp;
        return node.sum + node.data.value;
    }
    
    //this is done basically so that the left most node fills up first, this induces bad compleixty but makes adding smoother
    // the time complexity as of now if O(n) where n is number of total nodes;
    private boolean addHelper(Node current, Node ntd){ // n2d is node to add

        
        //current has some node which can have more children
        for(Node node:current.childen){
            if(node.sum + ntd.data.value < node.data.value){
                boolean b = addHelper( node, ntd);
                if(b)
                    return true;
            }
        }
        if(current.sum + ntd.data.value < current.data.value){
            current.childen.add(ntd);
            return true;
        }
       
        return false;
        
    }
    
    
    
    
     
}
