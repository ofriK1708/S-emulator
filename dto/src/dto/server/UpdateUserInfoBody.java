package dto.server;

public record UpdateUserInfoBody(String username, String infoToUpdate, String newValue) {
}
