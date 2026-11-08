
package org.meeuw.i18n.subdivisions.codes;



/**
 * A subdivision of a country, as defined by ISO 3166-2. Implemented by the generated {@code Subdivision<CC>} enums.
 */
public interface CountrySubdivisionCode {

    /**
     * The (English) name of this subdivision.
     * @return the name
     */
    String getName();

    /**
     * The name of the enum constant, as implemented by {@link Enum#name()}.
     * @return the enum constant name
     */
    String name();

    /**
     * The full ISO 3166-2 code, consisting of the country code and the subdivision code, separated by a hyphen. E.g. {@code NL-UT}.
     * @return the full code
     */
    default String getCode() {
        return getCountryCode() + "-" + getSubdivisionCode();
    }

    /**
     * The code of this subdivision, relative to the country. E.g. {@code UT}.
     * @return a code for this subdivision (relative to the country)
     */
    String getSubdivisionCode();

    /**
     * The ISO 3166-1 alpha-2 code of the country this is a subdivision of. E.g. {@code NL}.
     * @return the code of the country where this is a subdivision of
     */
    String  getCountryCode();

    /**
     * Sometimes there are no knowns subdivision, in which case
     * we generate an enum value with just one subdivision, which is not 'real' then.
     * @return boolean
     */
    boolean isRealRegion();

    /**
     * Where the information about this subdivision came from.
     * @return zero or more 'sources' of this subdivision. Probably a URL
     */
    String[] getSource();

}
