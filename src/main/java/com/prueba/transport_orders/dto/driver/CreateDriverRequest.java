package com.prueba.transport_orders.dto.driver;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateDriverRequest {

     @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String name;

    @NotBlank(message = "El número de licencia es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9-]{5,30}$",
            message = "La licencia debe tener entre 5 y 30 caracteres alfanuméricos o guiones")
    private String licenseNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

}
