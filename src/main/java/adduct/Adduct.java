package adduct;

import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    /**
     * Calculates the monoisotopic mass of a molecule from the given m/z (mass-to-charge ratio) and adduct details.
     * <p>
     * This method uses the charge, multimer, and mass shift values derived from the adduct string
     * to compute the monoisotopic mass for the molecule.
     * </p>
     *
     * @param mz     The m/z (mass-to-charge ratio) value for the molecule.
     * @param adduct A Map.Entry containing the adduct, where the key is the adduct string
     *               (e.g., "[M+H]+") and the value is the associated mass shift.
     * @return The calculated monoisotopic mass as a Double.
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, Map.Entry<String, Double> adduct) {

        int multimer = getAdductMultimer(adduct.getKey());
        int charge = getAdductCharge(adduct.getKey());
        Double shift = adduct.getValue() / charge; //if charge>1 for an adduct, shift has to be divided by the charge
        Double monoisotopicMass = null;

        // M = mz + shift
        if(charge == 1 && multimer == 1) {
            monoisotopicMass = mz + shift;
            System.out.println("monoisotopicMass = " + mz + " + " + shift + " = " + monoisotopicMass);
        } else if(charge > 1 && multimer == 1) {
            monoisotopicMass = (mz + shift)*charge;
        } else if(charge == 1 && multimer > 1){
            monoisotopicMass = (mz + shift)/multimer;
        } else {
            monoisotopicMass = (mz + shift)*charge/multimer;
        }
        return monoisotopicMass;
    }
    
    /**
     * Calculates the m/z (mass-to-charge ratio) value from the given monoisotopic mass and adduct information.
     *
     * @param monoisotopicMass The monoisotopic mass of the molecule.
     * @param adduct           A Map.Entry containing the adduct information, where the key is the adduct string and the value is the mass shift.
     * @return The calculated m/z value.
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, Map.Entry<String, Double> adduct) {

        int multimer = getAdductMultimer(adduct.getKey());
        int charge = getAdductCharge(adduct.getKey());
        Double shift = adduct.getValue() / charge; //if charge>1 for an adduct, shift has to be divided by the charge
        Double mz = null;

        //revisar despejes ecuaciones
        // mz = M - shift
        if (charge == 1 && multimer == 1) {
            mz = monoisotopicMass - shift;
        } else if (charge > 1 && multimer == 1) {
            mz = (monoisotopicMass / charge) - shift;
            System.out.println(" - mz = (" + monoisotopicMass + "/" + charge + ") - " + shift + " = " + mz);
        } else if (charge == 1 && multimer > 1) {
            mz = (monoisotopicMass * multimer) - shift;
        } else {
            mz = ((monoisotopicMass * multimer) / charge) - shift;
        }
        return mz;
    }
     

    /**
     * Calculates the parts-per-million (PPM) error between the experimental and theoretical masses.
     *
     * @param experimentalMass The experimentally observed mass.
     * @param theoreticalMass  The theoretical or expected mass.
     * @return The calculated PPM error as an integer.
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        return (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000 / theoreticalMass));
    }

    //not used
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        return Math.round(Math.abs(experimentalMass * ppm / 1_000_000.0));
    }

    
    /**
     * Determines the charge of a given adduct based on its string representation.
     * <p>
     * The charge is extracted from the adduct string by identifying the last digit(s) followed by a "+" or "−"
     * near the end of the string. If no explicit charge is found, a default charge value of 1 is returned.
     * </p>
     *
     * @param adduct The adduct string, e.g., "[M+H]+", "[M-H]−", or "[M+2H]2+".
     * @return The detected charge as an integer value. Defaults to 1 if no explicit charge is found.
     */
    private static int getAdductCharge(String adduct) {

        // Match the last digit(s) followed by + or − before the closing bracket
        Matcher m = Pattern.compile("([0-9]*)([+-])\\]?$").matcher(adduct);
        if (m.find()) {
            String num = m.group(1);  // May be empty
            return num.isEmpty() ? 1 : Integer.parseInt(num);
        }
        return 1; // Default if no explicit charge found
    }

    
    /**
     * Determines the multimer value (e.g., 2M, 3M) from the given adduct string.
     * <p>
     * The multimer is extracted by identifying an optional leading numeric value
     * before the "M" in the adduct string. If no value is present, it defaults to 1.
     * </p>
     *
     * @param key The adduct string, e.g., "[2M+H]+", "[M+H]+", or "[3M+Na]+", to extract the multimer from.
     * @return The detected multimer as an integer. Defaults to 1 if no explicit multimer is found.
     */
    private static int getAdductMultimer(String key) {
        // Match optional number before M (e.g. [2M+H]+ → 2)
        Pattern multimerPattern = Pattern.compile("\\[([0-9]*)M");
        Matcher matcher = multimerPattern.matcher(key);
        if (matcher.find()) {
            String multimerStr = matcher.group(1);
            return multimerStr.isEmpty() ? 1 : Integer.parseInt(multimerStr);
        }
        return 1; // default to 1 if not found
    }
}
