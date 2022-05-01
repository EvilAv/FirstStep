public class App {
    public static void main(String[] args) {
        Filter filter = new Filter(15, "untitled.fcf", 30);
        filter.quantCoefs();
        filter.simulateSingleImpulse(0.99999f);
        //filter.simulateStep(0.8f);
        filter.printFilterResults();
    }
}
