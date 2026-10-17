public class QueueNode {
    private Request data;
    private QueueNode next;

    public QueueNode(Request data) {
        this.data = data;
        this.next = null;
    }

    public Request getData() { return data; }
    public QueueNode getNext() { return next; }

    public void setNext(QueueNode next) {
        this.next = next;
    }
}
