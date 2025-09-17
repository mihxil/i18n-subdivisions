
package org.meeuw.i18n.subdivision;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.regions.RegionService;
import org.meeuw.i18n.regions.spi.RegionProvider;

public final class SubdivisionProvider implements RegionProvider<CountrySubdivision> {

    private static final Map<Country, List<CountrySubdivision>> map = new ConcurrentHashMap<>();

    static {
        System.out.println("fu");;
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
        map.put(country, Collections.unmodifiableList(subdivisions));
    }

    /**
     * Get all subdivisions for a country. Or {@code null} if not known, not found, or not applicable
     * 
     */
    @Nullable
    public static List<CountrySubdivision> getSubdivisions(Country countryCode) {
        return map.get(countryCode);
    }

    public static List<CountrySubdivision> getSubdivisions(String countryCode) {
        return map.get(RegionService.getInstance()
            .getByCode(countryCode, true, Country.class).get());
    }

    /**
     * @param country Country to resolve for
     */
    public static CountrySubdivision getSubdivision(Country country, String subdivisionCodeName) {
        for (CountrySubdivision subDivisionCode: map.get(country)) {
            if (subDivisionCode.getCode().equals(subdivisionCodeName)) {
                return subDivisionCode;
            }
        }
        return null;
    }

    @Override
    public Class<CountrySubdivision> getProvidedClass() {
        return CountrySubdivision.class;
    }

    @Override
    public Stream<CountrySubdivision> values() {
        return map.values().stream().flatMap(Collection::stream);
    }
}
