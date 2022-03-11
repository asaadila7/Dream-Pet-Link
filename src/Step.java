class Step {
    public static enum Direction {
        UP (0, -1),
        DOWN (0, 1),
        RIGHT (1, 0),
        LEFT (-1, 0);

        private int x, y;

        private Direction (int x, int y) {
            //Maybe signum here if I wanted to make this more widely useable
            this.x = x;
            this.y = y;
        }

        public int getX () {
            return x;
        }

        public int getY () {
            return y;
        }
        
        public boolean equals (Direction other) {
            return x == other.getX () && y == other.getY ();
        }
    }


    private Direction direction;
    private int stepCount;

    public Step (Direction direction, int stepCount) {
        if (stepCount < 1) throw new Error ("#steps invalid");
        this.direction = direction;
        this.stepCount = stepCount;
    }

    public Direction getDirection () {
        return direction;
    }

    public int getSteps () {
        return stepCount;
    }

    public void incrementSteps () {
        stepCount++;
    }

    public Step copyStep () {
        return new Step (direction, stepCount);
    }
}