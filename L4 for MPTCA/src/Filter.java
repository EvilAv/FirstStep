import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Filter {

    private final int filterOrder, timeToSimulate;
    private double[] coefficients;
    private short[] quantd_coefficients, quantd_samples, filtering_results;
    private float[] samples, filteringResultsconved;

    public Filter(int filterOrder, String coefficientsFilename, int timeToSimulate){
        this.filterOrder = filterOrder;
        this.timeToSimulate = timeToSimulate;
        this.coefficients = new double[filterOrder + 1];
        this.quantd_coefficients = new short[filterOrder + 1];
        this.samples = new float[timeToSimulate];
        this.filteringResultsconved = new float[timeToSimulate];
        this.quantd_samples = new short[timeToSimulate];
        this.filtering_results = new short[timeToSimulate];

        try {
            File coefficientsFile = new File(coefficientsFilename);
            Scanner scanner = new Scanner(coefficientsFile);
            int i = 0;
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                coefficients[i++] = Double.parseDouble(data);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public void quantCoefs(){
        for (int i = 0; i < filterOrder + 1; i++)
            quantd_coefficients[i] = (short) (coefficients[i] * 32768); // 2^15
    }

    private void quantSamples(){
        for (int i = 0; i < timeToSimulate; i++)
            quantd_samples[i] = (short) (samples[i] * 32768);
    }

    private void convResults(){
        for (int i = 0; i < timeToSimulate; i++)
            filteringResultsconved[i] = filtering_results[i] / 32768.0f;
    }

    private void filter(){
        int accumulator;

        quantSamples();
        for (int i = 0; i < timeToSimulate; i++) {
            accumulator = 0;
            for (int j = 0; j < filterOrder; j++) { // filter of low frequency
                if (i - j >= 0){
                    accumulator += quantd_samples[i - j] * quantd_coefficients[j];
                }
            }
            filtering_results[i] = (short) (accumulator >> 15);
        }
        convResults();
    }

    public void simulateSingleImpulse(float strength){
        samples[0] = strength;
        for (int i = 1; i < timeToSimulate; i++)
            samples[i] = 0;
        filter();
    }

    public void simulateStep(float strength){
        for (int i = 0; i < timeToSimulate; i++)
            samples[i] = strength;
        filter();
    }

    public void printFilterResults(){
        for (int i = 0; i < timeToSimulate; i++) {
            System.out.println(filteringResultsconved[i]);
        }
    }
}
