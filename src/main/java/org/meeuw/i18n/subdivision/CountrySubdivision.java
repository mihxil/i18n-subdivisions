
package org.meeuw.i18n.subdivision;


import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.regions.Region;

/**
 *
 */
public interface CountrySubdivision extends Region {


    /**
     * @return a code for this subdivision (relative to the country)
     */
    @Override
    String getCode();

    /**
     * The {@link Type 'type'} is {@link Type#SUBDIVISION}
     */
    @Override
    default Type getType() {
        return Type.SUBDIVISION;
    }

    /**
     * @return {@link Country} where this is a subdivision for.
     *
     */
    Country  getCountry();

    /**
     * Sometimes there are no knowns subdivision, in which case
     * we generate an enum value with just one subdivision, which is not 'real' then.
     * @return boolean
     */
    boolean isRealRegion();

    /**
     * @return zero or more 'sources' of this subdivision. Probably a URL
     */
    String[] getSource();

}
