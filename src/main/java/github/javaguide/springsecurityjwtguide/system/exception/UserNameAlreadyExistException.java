package github.javaguide.springsecurityjwtguide.system.exception;

/**
 * @author Chr.yl
 */
public class UserNameAlreadyExistException extends RuntimeException {
    public UserNameAlreadyExistException() {
    }

    public UserNameAlreadyExistException(String message) {
        super(message);
    }
}
