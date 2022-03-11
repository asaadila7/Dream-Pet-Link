import java.util.ArrayList;

public class Path extends ArrayList <Step> {
    //returns the length of a path
    public int length () {
        int length = 0;
        for (Step step : this) length += step.getSteps(); //Step step : this (is this valid?)
        return length;
    }

    private Step getLast () {
        return get (size () - 1);
    }

    private void addStepToOriginal (Step.Direction direction) {
        if (size () != 0 && getLast ().getDirection () == direction) getLast ().incrementSteps ();
        else add (new Step (direction, 1));
    }

    //adds to a copy and returns copy
    public Path addStep (Step.Direction direction) {
        Path newPath = deepCopy ();
        newPath.addStepToOriginal (direction);
        return newPath;
    }

    private Path deepCopy () {
        Path newPath = new Path ();
        for (Step step : this) newPath.add (step.copyStep ());
        return newPath;
    }
}
