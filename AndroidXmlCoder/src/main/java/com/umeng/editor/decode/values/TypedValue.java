package com.umeng.editor.decode.values;

/**
 * @author Dmitry Skiba
 */
public class TypedValue
{
    public static final int
            TYPE_NULL = 0,
            TYPE_REFERENCE = 1,
            TYPE_ATTRIBUTE = 2,
            TYPE_STRING = 3,
            TYPE_FLOAT = 4,
            TYPE_DIMENSION = 5,
            TYPE_FRACTION = 6,
            TYPE_FIRST_INT = 16,
            TYPE_INT_DEC = 16,
            TYPE_INT_HEX = 17,
            TYPE_INT_BOOLEAN = 18,
            TYPE_FIRST_COLOR_INT = 28,
            TYPE_INT_COLOR_ARGB8 = 28,
            TYPE_INT_COLOR_RGB8 = 29,
            TYPE_INT_COLOR_ARGB4 = 30,
            TYPE_INT_COLOR_RGB4 = 31,
            TYPE_LAST_COLOR_INT = 31,
            TYPE_LAST_INT = 31;

    public static final int
            COMPLEX_UNIT_PX = 0,
            COMPLEX_UNIT_DIP = 1,
            COMPLEX_UNIT_SP = 2,
            COMPLEX_UNIT_PT = 3,
            COMPLEX_UNIT_IN = 4,
            COMPLEX_UNIT_MM = 5,
            COMPLEX_UNIT_SHIFT = 0,
            COMPLEX_UNIT_MASK = 15,
            COMPLEX_UNIT_FRACTION = 0,
            COMPLEX_UNIT_FRACTION_PARENT = 1,
            COMPLEX_RADIX_23p0 = 0,
            COMPLEX_RADIX_16p7 = 1,
            COMPLEX_RADIX_8p15 = 2,
            COMPLEX_RADIX_0p23 = 3,
            COMPLEX_RADIX_SHIFT = 4,
            COMPLEX_RADIX_MASK = 3,
            COMPLEX_MANTISSA_SHIFT = 8,
            COMPLEX_MANTISSA_MASK = 0xFFFFFF;

    /*
    *

    // These are attribute resource constants for the platform, as found
    // in android.R.attr
    enum {
        LABEL_ATTR = 0x01010001,
        ICON_ATTR = 0x01010002,
        NAME_ATTR = 0x01010003,
        PERMISSION_ATTR = 0x01010006,
        EXPORTED_ATTR = 0x01010010,
        GRANT_URI_PERMISSIONS_ATTR = 0x0101001b,
        RESOURCE_ATTR = 0x01010025,
        DEBUGGABLE_ATTR = 0x0101000f,
        VALUE_ATTR = 0x01010024,
        VERSION_CODE_ATTR = 0x0101021b,
        VERSION_NAME_ATTR = 0x0101021c,
        SCREEN_ORIENTATION_ATTR = 0x0101001e,
        MIN_SDK_VERSION_ATTR = 0x0101020c,
        MAX_SDK_VERSION_ATTR = 0x01010271,
        REQ_TOUCH_SCREEN_ATTR = 0x01010227,
        REQ_KEYBOARD_TYPE_ATTR = 0x01010228,
        REQ_HARD_KEYBOARD_ATTR = 0x01010229,
        REQ_NAVIGATION_ATTR = 0x0101022a,
        REQ_FIVE_WAY_NAV_ATTR = 0x01010232,
        TARGET_SDK_VERSION_ATTR = 0x01010270,
        TEST_ONLY_ATTR = 0x01010272,
        ANY_DENSITY_ATTR = 0x0101026c,
        GL_ES_VERSION_ATTR = 0x01010281,
        SMALL_SCREEN_ATTR = 0x01010284,
        NORMAL_SCREEN_ATTR = 0x01010285,
        LARGE_SCREEN_ATTR = 0x01010286,
        XLARGE_SCREEN_ATTR = 0x010102bf,
        REQUIRED_ATTR = 0x0101028e,
        INSTALL_LOCATION_ATTR = 0x010102b7,
        SCREEN_SIZE_ATTR = 0x010102ca,
        SCREEN_DENSITY_ATTR = 0x010102cb,
        REQUIRES_SMALLEST_WIDTH_DP_ATTR = 0x01010364,
        COMPATIBLE_WIDTH_LIMIT_DP_ATTR = 0x01010365,
        LARGEST_WIDTH_LIMIT_DP_ATTR = 0x01010366,
        PUBLIC_KEY_ATTR = 0x010103a6,
        CATEGORY_ATTR = 0x010103e8,
        BANNER_ATTR = 0x10103f2,
        ISGAME_ATTR = 0x10103f4,
        REQUIRED_FEATURE_ATTR = 0x1010557,
        REQUIRED_NOT_FEATURE_ATTR = 0x1010558,
        COMPILE_SDK_VERSION_ATTR = 0x01010572, // NOT FINALIZED
        COMPILE_SDK_VERSION_CODENAME_ATTR = 0x01010573, // NOT FINALIZED
    };

    */
}

