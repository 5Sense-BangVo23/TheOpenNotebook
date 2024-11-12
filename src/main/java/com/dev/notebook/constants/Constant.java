package com.dev.notebook.constants;

public class Constant {

    public static final int STRENGTH = 12;
    public static final String AUTHORITIES = "authorities";

    public static final String EMPTY_VALUE = "empty";
    public static final String ROLE = "role";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_DELIMITER = ",";

    public static final String USER_AUTHORITIES = "user:create,user:update,user:view,user:delete";
    public static final String ADMIN_AUTHORITIES = "user:create,user:update,user:view,user:delete";


    public static final String NEW_USER_ACCOUNT_VERIFICATION = "";
    public static final String PASWORD_RESET_REQUEST ="";

    /* ======================================================= */
    public static final String FIRST_NAME_REQUIRED = "First name is required.";
    public static final String LAST_NAME_REQUIRED = "Last name is required.";
    public static final String EMAIL_REQUIRED = "Email is required.";
    public static final String EMAIL_INVALID = "Please provide a valid email address.";
    public static final String PASSWORD_REQUIRED = "Password is required.";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters long.";
    public static final String CITY_REQUIRED = "City is required.";

    public static final String ACCOUNT_CREATED_INFO = "Account created. Check your email to enable your account.";
    public static final String ACCOUNT_VERIFY_INO = "Account verified.";
}
