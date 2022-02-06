import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.regex.Pattern;

public class whereInTheWorld {

    /**
     *
     */
    private static final int NOT_FOUND = 99;

    public static void main(String[] args) {
        String label = "";
        String inputEx = "";
        Scanner scan = new Scanner(System.in);
        JSONObject json = new JSONObject();
        JSONArray features = new JSONArray();
        json.put("type", "FeatureCollection");
        System.out.println(json.toString());
        // json.put("features",features);
        while (scan.hasNextLine()) {
            String input = scan.nextLine();
            if (input.isBlank()) {
                System.out.println("Unable to process: " + input);
                continue;
            }
            String[] extract = extractComments(input);

            inputEx = extract[0];

            // System.out.println("Input: " + input);
            // System.out.println("InputEx: " + inputEx);
            label = extract[1];
            // System.out.println("label: " + label);

            // Standard form with letters eg. 50.12345 N, 10.12345 S
            if (Pattern.matches(
                    "^(\\d{1,3}(.\\d{1,6})*)(\\s*|\\s+)[NWESnwes]((\\s*|\\s+),(\\s*|\\s+)|\\s+)\\d{1,3}(.\\d{1,6})*(\\s*|\\s+)[NWESnwes](\\s.+|\\s+|)$",
                    inputEx)) {
                String[] arrayIn = cleanToArray(inputEx);
                // System.out.println(Arrays.toString(arrayIn) + " len: " + arrayIn.length);
                if (arrayIn.length == 3 && arrayIn[1].matches("[Nn]") || arrayIn.length == 3
                        && arrayIn[2].matches("[Nn]")) {
                    System.out.println("Unable to process: " + input);
                    continue;
                }
                if (arrayIn.length == 2 && !arrayIn[0].matches("[NnSsEeWw]{1,}")) {
                    System.out.println("Unable to process: " + input);
                    continue;
                }
                if (arrayIn.length == 3 && arrayIn[1].matches("[EeWw]")) {
                    if (Double.valueOf(arrayIn[2]) <= 90 && Double.valueOf(arrayIn[2]) >= -90 &&
                            Double.valueOf(arrayIn[0]) <= 180 && Double.valueOf(arrayIn[0]) >= -180) {
                        String result = toStandardForm(arrayIn, "letterEW");
                        System.out.println(result);
                        features.put(convertToGeo(result, label));
                        continue;
                    } else {
                        System.out.println("Unable to process: " + input);
                        continue;
                    }
                }
                if (arrayIn.length == 3 && arrayIn[2].matches("[EeWw]")) {
                    if (Double.valueOf(arrayIn[0]) <= 90 && Double.valueOf(arrayIn[0]) >= -90 &&
                            Double.valueOf(arrayIn[1]) <= 180 && Double.valueOf(arrayIn[1]) >= -180) {
                        String result = toStandardForm(arrayIn, "letterNS");
                        System.out.println(result);
                        features.put(convertToGeo(result, label));
                        continue;
                    } else {
                        System.out.println("Unable to process: " + input);
                        continue;
                    }
                }
                if (arrayIn[0].matches("\\d+(.\\d+)*[Nn]") && arrayIn.length == 2) {
                    arrayIn[0] = arrayIn[0].replaceAll("[a-zA-Z]", "");
                    if (arrayIn[1].matches("\\d+(.\\d+)*[Ee]")) {
                        arrayIn[1] = arrayIn[1].replaceAll("[a-zA-Z]", "");
                    } else {
                        arrayIn[1] = "-" + arrayIn[1].replaceAll("[a-zA-Z]", "");
                    }

                    String result = toStandardForm(arrayIn, "standard");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else if (arrayIn[0].matches("\\d+(.\\d+)*[EeWw]") && arrayIn.length == 2) {
                    if (arrayIn[0].matches("\\d+(.\\d+)*[Ee]")) {
                        arrayIn[0] = arrayIn[0].replaceAll("[a-zA-Z]", "");
                    } else {
                        arrayIn[0] = "-" + arrayIn[0].replaceAll("[a-zA-Z]", "");
                    }
                    arrayIn[1] = arrayIn[1].replaceAll("[a-zA-Z]", "");
                    String result = toStandardForm(arrayIn, "standardReverse");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                }

                if (arrayIn[1].matches("^[NnSs]")) {
                    if (Double.valueOf(arrayIn[0]) <= 90 && Double.valueOf(arrayIn[0]) >= -90 &&
                            Double.valueOf(arrayIn[2]) <= 180 && Double.valueOf(arrayIn[2]) >= -180
                                    & arrayIn[1].compareTo(arrayIn[3]) != 0) {
                        String result = toStandardForm(arrayIn, "letterNS");
                        System.out.println(result);
                        features.put(convertToGeo(result, label));
                        continue;
                    } else {
                        System.out.println("Unable to process: " + input);
                        continue;
                    }
                } else if (arrayIn[2].matches("^[EeWw]")) {
                    if (Double.valueOf(arrayIn[0]) <= 90 && Double.valueOf(arrayIn[0]) >= -90 &&
                            Double.valueOf(arrayIn[1]) <= 180 && Double.valueOf(arrayIn[1]) >= -180) {
                        String result = toStandardForm(arrayIn, "letterNS");
                        System.out.println(result);
                        features.put(convertToGeo(result, label));
                        continue;
                    } else {
                        System.out.println("Unable to process: " + input);
                        continue;
                    }
                } else if (arrayIn[1].matches("^[EeWw]")) {
                    if (Double.valueOf(arrayIn[0]) <= 180 && Double.valueOf(arrayIn[0]) >= -180 &&
                            Double.valueOf(arrayIn[2]) <= 90
                            && Double.valueOf(arrayIn[2]) >= -90 & arrayIn[1].compareTo(arrayIn[3]) != 0) {
                        String result = toStandardForm(arrayIn, "letterEW");
                        System.out.println(result);
                        features.put(convertToGeo(result, label));
                        continue;
                    } else {
                        System.out.println("Unable to process: " + input);
                        continue;
                    }
                } else {
                    System.out.println("Unable to process: " + input);
                    continue;
                }
            }
            // standard form eg. +80.000 -120.00
            else if (Pattern.matches(
                    "[+-]*\\d{1,3}(.\\d+)*((\\s*|\\s+),(\\s*|\\s+)|\\s+)[+-]*\\d{1,3}(.\\d+)*(\\s.+|\\s+|)$",
                    inputEx)) {
                // System.out.println("true clause 2");
                String[] arrayIn = cleanToArray(inputEx);

                if (Double.valueOf(arrayIn[0]) <= 90 && Double.valueOf(arrayIn[0]) >= -90 &&
                        Double.valueOf(arrayIn[1]) <= 180 && Double.valueOf(arrayIn[1]) >= -180) {
                    String result = toStandardForm(arrayIn, "standard");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else {
                    System.out.println("Unable to process: " + input);
                    continue;
                }

                // degrees, minutes, seconds form with symbols eg. 23° 45' 02.0" N, 15° 20'
                // 03.0" W
            } else if (Pattern.matches(
                    "\\d{1,3}(\\s*|\\s+)°(\\s*|\\s+)(\\d{1,2})(\\s*|\\s+)'(\\s*|\\s+)(\\d{1,2}(.\\d{1}|))(\\s*|\\s+)\"(\\s*|\\s+)[WESNnwes](\\s*|\\s+),(\\s*|\\s+)\\d{1,3}(\\s*|\\s+)°(\\s*|\\s+)(\\d{1,2})(\\s*|\\s+)'(\\s*|\\s+)(\\d{1,2}(.\\d{1}|))(\\s*|\\s+)\"(\\s*|\\s+)[WESNnwes](\\s.+|\\s+|)$",
                    inputEx)) {
                String[] arrayIn = cleanToArray(inputEx);
                // System.out.println("Check norm: " + Arrays.toString(arrayIn));
                if (arrayIn.length == 8 && arrayIn[3].compareTo(arrayIn[7]) != 0) {
                    // System.out.println(Arrays.toString(arrayIn));
                    String result = toStandardForm(arrayIn, "dms");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else if (arrayIn.length == 7 && arrayIn[3].matches("[EeWw]") | arrayIn.length == 7
                        && arrayIn[6].matches("[EeWw]")) {
                    // System.out.println(Arrays.toString(arrayIn));
                    String result = toStandardForm(arrayIn, "dms");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else {
                    System.out.println("Unable to process: " + input);
                    continue;
                }
                // dms form but with letters
            } else if (Pattern.matches(
                    "\\d{1,3}(\\s*|\\s+)[dD](\\s*|\\s+)(\\d{1,2})(\\s*|\\s+)[mM](\\s*|\\s+)(\\d{1,2}(.\\d{1}|))(\\s*|\\s+)[sS](\\s*|\\s+)[WESN]((\\s*|\\s+),(\\s*|\\s+)|\\s+)\\d{1,3}(\\s*|\\s+)[dD](\\s*|\\s+)(\\d{1,2})(\\s*|\\s+)[mM](\\s*|\\s+)(\\d{1,2}(.\\d{1}|))(\\s*|\\s+)[sS](\\s*|\\s+)[WESN](\\s.+|\\s+|)$",
                    inputEx)) {
                String[] arrayIn = cleanToArray(inputEx);
                // System.out.println("Check: " + Arrays.toString(arrayIn));     

                if (arrayIn.length == 8 && arrayIn[3].compareTo(arrayIn[7]) != 0) {
                    // System.out.println(Arrays.toString(arrayIn));
                    String result = toStandardForm(arrayIn, "dms");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else if (arrayIn.length == 7 && arrayIn[3].matches("[EeWw]") | arrayIn.length == 7
                        && arrayIn[6].matches("[EeWw]")) {
                    // System.out.println(Arrays.toString(arrayIn));
                    String result = toStandardForm(arrayIn, "dms");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else {
                    System.out.println("Unable to process: " + input);
                    continue;
                }
                // Degrees minutes decimal form eg. 23° 45.52' N, 15° 20.322' E
            } else if (Pattern.matches(
                    "\\d{1,3}(\\s*|\\s+)°(\\s*|\\s+)(\\d{1,2}.\\d+)(\\s*|\\s+)'(\\s*|\\s+)[WESNnwes]((\\s*|\\s+),(\\s*|\\s+)|\\s+)\\d{1,3}(\\s*|\\s+)°(\\s*|\\s+)(\\d{1,2}.\\d+)(\\s*|\\s+)'(\\s*|\\s+)[WESNnwes](\\s.+|\\s+|)$",
                    inputEx)) {
                String[] arrayIn = cleanToArray(inputEx);
                // System.out.println("Check norm: " + Arrays.toString(arrayIn));
                if (arrayIn.length == 6 && arrayIn[2].compareTo(arrayIn[5]) != 0) {
                    // System.out.println(Arrays.toString(arrayIn));
                    String result = toStandardForm(arrayIn, "ddm");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else if (arrayIn.length == 5 && arrayIn[2].matches("[EeWw]") | arrayIn.length == 5
                        && arrayIn[4].matches("[EeWw]")) {
                    // System.out.println(Arrays.toString(arrayIn));
                    String result = toStandardForm(arrayIn, "ddm");
                    System.out.println(result);
                    features.put(convertToGeo(result, label));
                    continue;
                } else {
                    System.out.println("Unable to process: " + input);
                    continue;
                }

            } else {
                System.out.println("Unable to process: " + input);
            }

        }
        scan.close();
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();

        json.put("features", features);

        try {
            FileWriter file = new FileWriter("out.geojson");
            //String prettyJson = gson.toJson(json);
            String outJson = json.toString();
            file.write(outJson);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String[] cleanToArray(String in) {
        String inEdit = in.replaceAll(",", " ").replaceAll("°", " ")
                .replaceAll("\'", " ").replaceAll("\"", " ").replaceAll("[dDmMsS]", " ")
                .replaceAll("\\s+", " ");
        String[] array = inEdit.split(" ");
        return array;
    }

    public static String[] extractComments(String in) {
        String inEdit = in.replaceAll("\\s+", " ");
        String[] arrayEdit = inEdit.split(" ");
        StringBuilder buildResult = new StringBuilder();
        StringBuilder buildLabel = new StringBuilder();
        int commentIndex = NOT_FOUND;
        for (int x = 0; x < arrayEdit.length; x++) {
            if (arrayEdit[x].matches("([a-zA-Z]{2,}|[Aa])") && !arrayEdit[x].matches("[NESWnesw]")) {
                commentIndex = x;
                break;
            }
        }
        if (commentIndex == NOT_FOUND) {
            for (int y = 0; y < arrayEdit.length; y++) {
                buildResult.append(arrayEdit[y] + " ");
            }
            return new String[] { buildResult.toString(), "" };
        }
        for (int y = 0; y < commentIndex; y++) {
            buildResult.append(arrayEdit[y] + " ");
        }

        for (int z = commentIndex; z < arrayEdit.length; z++) {
            buildLabel.append(arrayEdit[z] + " ");
        }

        return new String[] { buildResult.toString(), buildLabel.toString() };
    }

    public static String toStandardForm(String[] in, String startForm) {
        DecimalFormat formatStandard = new DecimalFormat("###.000000");
        StringBuilder builder = new StringBuilder();
        // System.out.println("convert In: " + Arrays.toString(in));
        switch (startForm) {
            case "letterNS":
                if (in[1].matches("[Nn]")) {
                    builder.append(formatStandard.format(Double.parseDouble(in[0])));
                } else {
                    builder.append("-" + formatStandard.format(Double.parseDouble(in[0])));
                }
                builder.append(", ");
                try {
                    if (in[3].matches("[Ee]")) {
                        builder.append(formatStandard.format(Double.parseDouble(in[2])));
                    } else {
                        builder.append("-" + formatStandard.format(Double.parseDouble(in[2])));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (in[2].matches("[Ee]")) {
                        builder.append(formatStandard.format(Double.parseDouble(in[1])));
                    } else {
                        builder.append("-" + formatStandard.format(Double.parseDouble(in[1])));
                    }
                }
                return builder.toString();

            case "letterEW":
                if (in[1].matches("[Ee]")) {
                    builder.append(formatStandard.format(Double.parseDouble(in[0])));
                } else {
                    builder.append("-" + formatStandard.format(Double.parseDouble(in[0])));
                }
                builder.append(", ");
                try {
                    if (in[3].matches("[Nn]")) {
                        builder.append(formatStandard.format(Double.parseDouble(in[2])));
                    } else {
                        builder.append("-" + formatStandard.format(Double.parseDouble(in[2])));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // has to be south
                    builder.append("-" + formatStandard.format(Double.parseDouble(in[2])));
                }

                return builder.toString();
            case "standard":
                builder.append(formatStandard.format(Double.parseDouble(in[0])));
                builder.append(", ");
                builder.append(formatStandard.format(Double.parseDouble(in[1])));
                return builder.toString();
            case "standardReverse":
                builder.append(formatStandard.format(Double.parseDouble(in[1])));
                builder.append(", ");
                builder.append(formatStandard.format(Double.parseDouble(in[0])));
                return builder.toString();
            case "dms":
                Double degreesLat = 0.0;
                Double minutesLat = 0.0;
                Double secondsLat = 0.0;
                Double degreesLong = 0.0;
                Double minutesLong = 0.0;
                Double secondsLong = 0.0;

                if (in.length == 8) {
                    if (in[7].matches("[EWew]")) {
                        degreesLat = Double.parseDouble(in[0]);
                        minutesLat = Double.parseDouble(in[1]);
                        secondsLat = Double.parseDouble(in[2]);
                        if (in[7].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[4]);
                            minutesLong = Double.parseDouble(in[5]);
                            secondsLong = Double.parseDouble(in[6]);
                        } else {
                            degreesLong = -Double.parseDouble(in[4]);
                            minutesLong = -Double.parseDouble(in[5]);
                            secondsLong = -Double.parseDouble(in[6]);
                        }

                    } else {
                        degreesLat = Double.parseDouble(in[4]);
                        minutesLat = Double.parseDouble(in[5]);
                        secondsLat = Double.parseDouble(in[6]);
                        if (in[3].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[0]);
                            minutesLong = Double.parseDouble(in[1]);
                            secondsLong = Double.parseDouble(in[2]);
                        } else {
                            degreesLong = -Double.parseDouble(in[0]);
                            minutesLong = -Double.parseDouble(in[1]);
                            secondsLong = -Double.parseDouble(in[2]);
                        }
                    }

                } else {
                    if (in[6].matches("[EWew]")) {
                        degreesLat = -Double.parseDouble(in[0]);
                        minutesLat = -Double.parseDouble(in[1]);
                        secondsLat = -Double.parseDouble(in[2]);
                        if (in[6].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[3]);
                            minutesLong = Double.parseDouble(in[4]);
                            secondsLong = Double.parseDouble(in[5]);
                        } else {
                            degreesLong = -Double.parseDouble(in[3]);
                            minutesLong = -Double.parseDouble(in[4]);
                            secondsLong = -Double.parseDouble(in[5]);
                        }
                    } else {
                        degreesLat = -Double.parseDouble(in[4]);
                        minutesLat = -Double.parseDouble(in[5]);
                        secondsLat = -Double.parseDouble(in[6]);
                        if (in[3].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[0]);
                            minutesLong = Double.parseDouble(in[1]);
                            secondsLong = Double.parseDouble(in[2]);
                        } else {
                            degreesLong = -Double.parseDouble(in[0]);
                            minutesLong = -Double.parseDouble(in[1]);
                            secondsLong = -Double.parseDouble(in[2]);
                        }
                    }
                }
                return convertDMStoStan(degreesLat, minutesLat, secondsLat, degreesLong, minutesLong, secondsLong);
            case "ddm":
                if (in.length == 6) {
                    if (in[5].matches("[EWew]")) {
                        degreesLat = Double.parseDouble(in[0]);
                        minutesLat = Double.parseDouble(in[1]);
                        if (in[5].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[3]);
                            minutesLong = Double.parseDouble(in[4]);
                        } else {
                            degreesLong = -Double.parseDouble(in[3]);
                            minutesLong = -Double.parseDouble(in[4]);
                        }
                    } else {
                        degreesLat = Double.parseDouble(in[3]);
                        minutesLat = Double.parseDouble(in[4]);
                        if (in[3].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[0]);
                            minutesLong = Double.parseDouble(in[1]);
                        } else {
                            degreesLong = -Double.parseDouble(in[0]);
                            minutesLong = -Double.parseDouble(in[1]);
                        }
                    }

                } else {
                    if (in[4].matches("[EWew]")) {
                        degreesLat = -Double.parseDouble(in[0]);
                        minutesLat = -Double.parseDouble(in[1]);
                        if (in[4].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[2]);
                            minutesLong = Double.parseDouble(in[3]);
                        } else {
                            degreesLong = -Double.parseDouble(in[2]);
                            minutesLong = -Double.parseDouble(in[3]);
                        }
                    } else {
                        degreesLat = -Double.parseDouble(in[3]);
                        minutesLat = -Double.parseDouble(in[4]);
                        if (in[3].matches("[Ee]")) {
                            degreesLong = Double.parseDouble(in[0]);
                            minutesLong = Double.parseDouble(in[1]);
                        } else {
                            degreesLong = -Double.parseDouble(in[0]);
                            minutesLong = -Double.parseDouble(in[1]);
                        }
                    }
                }
                return convertDDMtoStan(degreesLat, minutesLat, degreesLong, minutesLong);
        }
        return "";
    }

    public static String convertDMStoStan(Double degreesLat, Double minutesLat, Double secondsLat, Double degreesLong,
            Double minutesLong, Double secondsLong) {
        DecimalFormat formatStandard = new DecimalFormat("###.000000");
        StringBuilder builder = new StringBuilder();
        Double resultLat = degreesLat + minutesLat / 60.0 + secondsLat / 3600.0;
        Double resultLong = degreesLong + minutesLong / 60.0 + secondsLong / 3600.0;
        builder.append(formatStandard.format(resultLat));
        builder.append(", ");
        builder.append(formatStandard.format(resultLong));

        return builder.toString();
    }

    public static String convertDDMtoStan(Double degreesLat, Double minutesLat, Double degreesLong,
            Double minutesLong) {
        DecimalFormat formatStandard = new DecimalFormat("###.000000");
        StringBuilder builder = new StringBuilder();
        Double resultLat = degreesLat + minutesLat / 60.0;
        Double resultLong = degreesLong + minutesLong / 60.0;
        builder.append(formatStandard.format(resultLat));
        builder.append(", ");
        builder.append(formatStandard.format(resultLong));

        return builder.toString();
    }

    public static JSONObject convertToGeo(String input, String label) {
        //System.out.println("Input: " + input + " label: " + label);
        input.replaceAll(" ", "");
        String[] array = input.split(",");
        Double[] doubleArray = new Double[]{Double.parseDouble(array[0]), Double.parseDouble(array[1])};
        JSONObject json = new JSONObject();
        JSONObject geometry = new JSONObject();
        JSONObject properties = new JSONObject();
        json.put("type", "Feature");
        properties.put("name",label);
        geometry.put("type", "Point");
        geometry.put("coordinates", doubleArray);
        json.put("properties", properties);
        json.put("geometry", geometry);
        return json;
    }

}