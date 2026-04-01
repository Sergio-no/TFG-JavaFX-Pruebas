package org.example.tfgjavafxpruebas.sesion;


public class UserSesion {

    private static UserSesion instance;

    private String token;
    private String email;
    private String rol; // OFICINA, JEFE, COMERCIAL
    private String uid;
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    private UserSesion() {}

    public static UserSesion getInstance() {
        if (instance == null) instance = new UserSesion();
        return instance;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isJefe() { return "JEFE".equals(rol); }

    public void clear() {
        token = null;
        email = null;
        rol = null;
    }
}
