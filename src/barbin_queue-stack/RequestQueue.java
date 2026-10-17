import java.util.ArrayList;

public class RequestQueue {
    private QueueNode front;
    private QueueNode back;
    private int size;

    public RequestQueue() {
        this.front = null;
        this.back = null;
        this.size = 0;
    }

    public void enqueue(Request r) {
        QueueNode node = new QueueNode(r);
        if (back == null) {
            front = node;
            back = node;
        } else {
            back.setNext(node);
            back = node;
        }
        size++;
    }

    public Request dequeue() {
        if (front == null) {
            return null;
        }
        Request r = front.getData();
        front = front.getNext();
        if (front == null) {
            back = null;
        }
        size--;
        return r;
    }

    public Request peek() {
        if (front == null) {
            return null;
        }
        return front.getData();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    // Walk the list and unlink the node with this request id
    public boolean remove(int requestId) {
        QueueNode previous = null;
        QueueNode current = front;

        while (current != null) {
            if (current.getData().getId() == requestId) {
                if (previous == null) {
                    front = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                if (current == back) {
                    back = previous;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.getNext();
        }
        return false;
    }

    public ArrayList<Request> toArrayList() {
        ArrayList<Request> list = new ArrayList<Request>();
        QueueNode current = front;

        while (current != null) {
            list.add(current.getData());
            current = current.getNext();
        }
        return list;
    }
}
