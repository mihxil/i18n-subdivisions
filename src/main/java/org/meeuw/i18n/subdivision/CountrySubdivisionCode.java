
package org.meeuw.i18n.subdivision;



/**
 *
 */
public interface CountrySubdivisionCode {


    String getName();

    String name();

    default String getCode() {
        return getCountryCode() + "-" + getSubdivisionCode();
    }

    /**
     * @return a code for this subdivision (relative to the country)
     */

    String getSubdivisionCode();

    /**
     * @return the code for the code  where this is a subdivision for.
     *
     */
    String  getCountryCode();

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
