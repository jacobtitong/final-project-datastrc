import java.util.ArrayList;

public class DecisionStack {
    private ArrayList<Decision> decisions;

    public DecisionStack() {
        this.decisions = new ArrayList<Decision>();
    }

    public void push(Decision d) {
        decisions.add(d);
    }

    public Decision pop() {
        if (decisions.size() == 0) {
            return null;
        }
        return decisions.remove(decisions.size() - 1);
    }

    public Decision peek() {
        if (decisions.size() == 0) {
            return null;
        }
        return decisions.get(decisions.size() - 1);
    }

    public boolean isEmpty() {
        return decisions.size() == 0;
    }

    public void clear() {
        decisions.clear();
    }
}
