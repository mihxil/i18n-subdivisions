package org.meeuw.i18n.subdivisions.codes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A few utility methods to work with {@link CountrySubdivisionCode subdivisions}.
 *
 * @author Michiel Meeuwissen
 *
 */
public class SubdivisionFactory {

    private static final Map<String, Class<? extends CountrySubdivisionCode>> enumMap;

    static {
        Map<String, Class<? extends CountrySubdivisionCode>>  map = new HashMap<>();
        try (InputStream is = SubdivisionFactory.class.getResourceAsStream("LIST")) {

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            String listContent = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
            for (String code : listContent.split("\t")) {
                map.put(code, (Class<CountrySubdivisionCode>) Class.forName("org.meeuw.i18n.subdivisions.codes.Subdivision" + code));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        enumMap = Collections.unmodifiableMap(map);

    }

    public static Class<? extends CountrySubdivisionCode> getEnum(String country) {
        return enumMap.get(country);
    }
    /**
     * Get all subdivisions for a country. Or {@code null} if not known, not found, or not applicable
     * @param country Country to resolve for
     * @return List of subdivisions
     */

    public static List<CountrySubdivisionCode> getSubdivisions(String country) {
        return Collections.unmodifiableList(streamSubdivisions(country).collect(Collectors.toList()));

    }



    /**
     * @param country Country to resolve for
     * @param subdivisionCodeName The code of the subdivision to resolve
     * @return The subdivision, if found, otherwise {@link Optional#empty()}
     */
    public static Optional<CountrySubdivisionCode> getSubdivision(String country, String subdivisionCodeName) {
        return streamSubdivisions(country).filter(c -> c.getSubdivisionCode().equals(subdivisionCodeName)).findFirst();
    }

    protected static Stream<CountrySubdivisionCode> streamSubdivisions(String country) {
        Class<? extends CountrySubdivisionCode> result = getEnum(country);
        if (result == null) {
            throw new NoSuchElementException("No subdivisions found for " + country);
        } else {
            Object[] enumConstants = result.getEnumConstants();

            return Arrays.stream(enumConstants)
                .map(e -> (CountrySubdivisionCode) e);

        }
    }

}
