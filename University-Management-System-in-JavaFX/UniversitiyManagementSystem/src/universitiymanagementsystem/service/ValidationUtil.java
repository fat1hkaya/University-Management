package universitiymanagementsystem.service;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9-]{2,20}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]{2,80}$");
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile("^[\\p{L}0-9 .'-]{2,100}$");
    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,6}[0-9]{2,4}$");

    private ValidationUtil() {
    }

    public static boolean validId(String value) {
        return value != null && ID_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean validName(String value) {
        return value != null && NAME_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean validDepartment(String value) {
        return value != null && DEPARTMENT_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean validCourseCode(String value) {
        return value != null && COURSE_CODE_PATTERN.matcher(value.trim().toUpperCase()).matches();
    }
}
