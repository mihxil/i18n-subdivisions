package org.meeuw.i18n.subdivisions.codes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;


/**
 * A few utility methods to work with {@link CountrySubdivisionCode subdivisions}.
 *
 * @author Michiel Meeuwissen
 *
 */
public class SubdivisionFactory {

    private SubdivisionFactory() {
        // no instances
    }

    private static final Map<String, Class<? extends CountrySubdivisionCode>> ENUM_MAP;

    static {
        Map<String, Class<? extends CountrySubdivisionCode>>  map = new TreeMap<>();
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
        ENUM_MAP = Collections.unmodifiableMap(map);

    }

    /**
     * Obtains the Enum class associated with the country with code country.
     */
    public static Class<? extends CountrySubdivisionCode> getEnum(String country) {
        return ENUM_MAP.get(country);
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

    /**
     * Stream all known {@link CountrySubdivisionCode}s.
     */
    public static Stream<CountrySubdivisionCode> stream() {
        Spliterator<CountrySubdivisionCode> spliterator = new Spliterator<CountrySubdivisionCode>() {
            private final Iterator<String> countries = ENUM_MAP.keySet().iterator();
            private Spliterator<CountrySubdivisionCode> spliterator;

            @Override
            public boolean tryAdvance(Consumer<? super CountrySubdivisionCode> action) {
                while (spliterator == null || !spliterator.tryAdvance(action)) {
                    if (! countries.hasNext()) {
                        return false;
                    }
                    Stream<CountrySubdivisionCode> subdivisions = streamSubdivisions(countries.next());

                    spliterator = subdivisions == null ? Spliterators.emptySpliterator() : subdivisions.spliterator();
                }
                return true;
            }

            @Override
            public Spliterator<CountrySubdivisionCode> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return IMMUTABLE | NONNULL;
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

}
