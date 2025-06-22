package com.library.sdl;

public class UniqueEmailMobileResponse {

    private String message;
    private boolean isUnique;

    public UniqueEmailMobileResponse (String message, boolean isUnique) {
        this.message = message;
        this.isUnique = isUnique;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }
}
