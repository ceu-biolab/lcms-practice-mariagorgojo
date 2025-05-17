package lipid;

import java.util.*;
import adduct.AdductList;
import adduct.Adduct;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity;
    private final double rtMin;
    private final IoniationMode ionizationMode;
    private String adduct;
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;

    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode) {
        this(lipid, mz, intensity, retentionTime, ionizationMode, Collections.emptySet());
    }

    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionizationMode = ionizationMode;
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
        if (groupedSignals != null && !groupedSignals.isEmpty()) {
            this.adduct = detectAdductFromPeaks();
        }
    }

    
    /**
     * Detects the most likely adduct based on the m/z values of grouped peaks.
     * <p>
     * This method analyzes the set of grouped signals (peaks) and compares their
     * m/z values against a predefined map of known adducts. The detection process
     * uses the current ionization mode (positive or negative) to select the appropriate
     * adduct map and attempts to find a match between the peaks and known adduct
     * m/z differences.
     * </p>
     *
     * @return The detected adduct as a string, or {@code null} if no valid adduct
     * could be detected.
     */
    private String detectAdductFromPeaks() {

        List<Peak> peakList = new ArrayList<>(groupedSignals);
        peakList.sort(Comparator.comparingDouble(Peak::getMz));

        Map<String, Double> adductMap = null;
        switch (ionizationMode){
            case POSITIVE: adductMap = AdductList.MAPMZPOSITIVEADDUCTS; break;
            case NEGATIVE: adductMap = AdductList.MAPMZNEGATIVEADDUCTS; break;
        }

        for (int i = 0; i < peakList.size(); i++) {
            Peak base = peakList.get(i);
            //this condition assures that the base peak corresponds to the reference peak specified as parameter
            if(Math.abs(base.getMz() - this.mz) < 0.01) {
                for (int j = 0; j < peakList.size(); j++) {
                    Peak other = peakList.get(j);
                    if(other.equals(base)) continue;
                    return detectAdductFromMz(base, other, adductMap);
                }
            }
        }
        return null;
    }


    /**
     * Detects the most likely adduct for a given peak by analyzing the relationship
     * between two peaks and their theoretical monoisotopic masses, calculated using
     * a predefined map of known adducts.
     *
     * <p>This method compares the m/z values of the peaks with all potential adduct
     * combinations within a specified ppm tolerance, and returns the most
     * appropriate adduct if a match is found.</p>
     *
     * @param base      The reference peak to compare against.
     * @param other     Another peak from the grouped signals to analyze.
     * @param adductMap A map of known adduct names and their respective m/z shifts.
     * @return The name of the detected adduct as a string, or {@code null} if no match is found.
     */
    private String detectAdductFromMz(Peak base, Peak other, Map<String, Double> adductMap) {

        int ppmTolerance = 10;
        for (Map.Entry<String, Double> adduct1 : adductMap.entrySet()) {
            Double M1 = Adduct.getMonoisotopicMassFromMZ(base.getMz(), adduct1); //theoretical monoisotopic mass, we assume it is right
            //System.out.println("Theoretical monoisotopic mass: "+ M1 + " (from m/z: "+ base.getMz() + " and adduct: "+ adduct1.getKey() + ")");
            for (Map.Entry<String, Double> adduct2 : adductMap.entrySet()) {
                if(adduct1.getKey().equals(adduct2.getKey())) continue;
                System.out.println("Trying adduct: "+ adduct2.getKey());

                Double mz2 = Adduct.getMZFromMonoisotopicMass(M1, adduct2);
                System.out.println(" - Calculated m/z: "+ mz2 + "\n - peakMz = " + other.getMz());
                int ppmError = Adduct.calculatePPMIncrement(other.getMz(), mz2);

                if(ppmError <= ppmTolerance) {
                    System.out.println("Detected pair: ");
                    System.out.println("- Adduct 1: "+ adduct1.getKey());
                    System.out.println("- Adduct 2: "+ adduct2.getKey());
                    return adduct1.getKey();
                }
            }
        }
        return null;
    }

    public Lipid getLipid() { return lipid; }
    public double getMz() { return mz; }
    public double getRtMin() { return rtMin; }
    public String getAdduct() {
        return this.detectAdductFromPeaks();
    }
    public void setAdduct(String adduct) { this.adduct = adduct; }
    public double getIntensity() { return intensity; }
    public Set<Peak> getGroupedSignals() { return groupedSignals; }
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }
    public int getScore() { return score; }
    public int getNormalizedScore() {
        if (totalScoresApplied == 0) return 0;
        return score / totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "lipid=" + lipid +
                ", mz=" + mz +
                ", intensity=" + intensity +
                ", rtMin=" + rtMin +
                ", adduct='" + adduct + '\'' +
                ", score=" + score +
                '}';
    }
}