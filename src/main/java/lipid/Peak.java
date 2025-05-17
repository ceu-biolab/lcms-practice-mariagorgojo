package lipid;

public class Peak implements Comparable<Peak> {

    private final double mz;
    private final double intensity;

    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public double getMz() {
        return mz;
    }

    public double getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return String.format("Peak(mz=%.4f, intensity=%.2f)", mz, intensity);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(mz) * 31;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Peak)) return false;
        Peak other = (Peak) o;
        return Double.compare(other.mz, mz) == 0;
    }

    @Override
    public int compareTo(Peak o) {
        return Double.compare(this.mz, o.mz);
    }
}
