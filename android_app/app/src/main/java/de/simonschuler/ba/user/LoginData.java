package de.simonschuler.ba.user;

import java.io.Serializable;

public class LoginData implements UserData, Serializable {
    private String host;
    private String pw;

    public LoginData(String host, String pw) {
        this.host = host;
        this.pw = pw;
    }


    public String getHost() {
        return host;
    }

    public String getPw() {
        return pw;
    }
}
