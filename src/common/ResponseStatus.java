package common;

/**
 * Enumera os estados possiveis nas respostas do servidor.
 */
public enum ResponseStatus {
    OK,
    NOK,
    UNAUTHORIZED,
    NOT_FOUND,
    ERROR,
    NOHM,
    NOUSER,
    NOPERM,
    INVALID_REQUEST,
    ATTESTATION_OK,
    ATTESTATION_FAILED,
    ATTESTATION_SERVER_ERROR,
    WRONG_PWD,
    OK_NEW_USER,
    OK_USER,
    LOGIN_SERVER_ERROR
}
