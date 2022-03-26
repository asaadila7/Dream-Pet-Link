import java.util.ArrayList;
import java.util.Iterator;

public class Path implements Iterable <Step> {
    private ArrayList <Step> path;

    public Path () {
        path = new ArrayList <Step> ();
    }

    @Override
    public Iterator <Step> iterator() {
        return path.iterator();
    }

    public int length () {
        int length = 0;
        for (Step step : this) length += step.getSteps(); //Step step : this (is this valid?)
        return length;
    }

    public int size () {
        return path.size ();
    }

    private Step getLast () {
        return path.get (size () - 1);
    }

    private void addStepToOriginal (Step.Direction direction) {
        addStepToOriginal (new Step (direction, 1));
    }

    public void addStepToOriginal (Step step) {
        if (size () != 0 && step.getDirection () == getLast ().getDirection ()) path.set (path.size () - 1, new Step (step.getDirection (), getLast ().getSteps () + step.getSteps ()));
        else path.add (step);
    }

    //adds to a copy and returns copy
    public Path addStep (Step.Direction direction) {
        Path newPath = deepCopy ();
        newPath.addStepToOriginal (direction);
        return newPath;
    }

    private Path deepCopy () {
        Path newPath = new Path ();
        for (Step step : this) newPath.addStepToOriginal (step.copyStep ());
        return newPath;
    }
}
