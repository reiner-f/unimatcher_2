package com.example.unimatcher.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank @Size(min = 2, max = 80)
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 8, max = 64)
    private String password;

    @NotBlank
    private String confirmPassword;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
