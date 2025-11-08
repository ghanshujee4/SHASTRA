package com.library.sdl.email;
import lombok.Data;
import java.util.List;

@Data
public class EmailRequest {
    private List<String> emails;
    private String subject;
    private String body;
}
