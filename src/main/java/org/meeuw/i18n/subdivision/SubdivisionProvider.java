
package org.meeuw.i18n.subdivision;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.regions.RegionService;
import org.meeuw.i18n.regions.spi.RegionProvider;

/**
 * @since 0.5
 */
public final class SubdivisionProvider implements RegionProvider<CountrySubdivision> {

    static final Map<Country, List<CountrySubdivision>> MAP = new ConcurrentHashMap<>();

    static {
        RegionService.getInstance().values(Country.class).forEach(c -> {

            try {
                    Class<? extends CountrySubdivision> clazz = (Class<CountrySubdivision>) Class.forName("org.meeuw.i18n.subdivision.Subdivision" + c.getCode());
                    CountrySubdivision[] enumConstants = clazz.getEnumConstants();
                    registerSubdivisions(c, Arrays.asList(enumConstants));
                } catch (ClassNotFoundException ignore) {
                    //
                }
            }

        );

    }

    public static void registerSubdivisions(Country country, List<CountrySubdivision> subdivisions) {
        if (subdivisions == null || subdivisions.isEmpty()) {
            return;
        }
        MAP.put(country, Collections.unmodifiableList(subdivisions));
    }



    @Override
    public Class<CountrySubdivision> getProvidedClass() {
        return CountrySubdivision.class;
    }

    @Override
    public Stream<CountrySubdivision> values() {
        return MAP.values().stream()
            .flatMap(Collection::stream);
    }
}
