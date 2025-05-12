package hospital;


class CNode {
    Checkup cc;
    CNode next, prev;
    int priority;

    public CNode(Checkup cc) {
        this.cc = cc;
        this.next = null;
        this.prev = null;
    }
}

public class Checkup_list {
    CNode head, tail;

    public Checkup_list(){
        head = null;
        tail = null;
    }

    public void Recommend(Checkup cc) {
        CNode node = new CNode(cc);

        if (head == null || tail == null) {
            head = node;
            tail = node;
        } else if (cc.getPriority() >= head.cc.getPriority()) {
            node.next = head;
            head.prev = node;
            head = node;
        } else if (cc.getPriority() <= tail.cc.getPriority()) {
            node.prev = tail;
            tail.next = node;
            tail = node;
        } else {
            CNode temp = head;
            while (temp.next != null && temp.next.cc.getPriority() > cc.getPriority()) {
                temp = temp.next;
            }
            node.next = temp.next;
            node.prev = temp;
            if (temp.next != null) {
                temp.next.prev = node;
            }
            temp.next = node;
        }
    }



    public void addRecommendation(int index , String rec, int priority){
        CNode temp = head;
        int i =0;
        while(temp!= null){
            if(index == i){
                temp.cc.setRecommendation(rec);
                temp.priority = priority;
                break;
            }
            i++;
            temp = temp.prev;
        }
    }

    public Patient getPatient(int index){
        CNode temp = head;
        int i =0;
        while(temp != null){
            if(index==i){
                return temp.cc.getPatient();
            }
            i++;
            temp = temp.prev;
        }
        return  null;
    }
    public Checkup deQ(){
        if(head == null){
            return  null;
        }
        CNode checkup = head;
        head = head.next;
        return checkup.cc;
    }

    public void print(){
        CNode temp = head;
        while(temp != null){
            System.out.println("Priority: " + temp.cc.getPriority() +
                    " | Recommendation: " + temp.cc.getRecommendation());
            temp = temp.next;
        }
    }
}

