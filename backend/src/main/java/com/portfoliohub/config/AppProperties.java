package com.portfoliohub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String publicBaseUrl = "http://localhost:4200";
    private boolean trustProxy;
    private Jwt jwt = new Jwt();
    private Mail mail = new Mail();
    private Storage storage = new Storage();
    private Cors cors = new Cors();
    private Admin admin = new Admin();
    private Clamav clamav = new Clamav();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private int accessMinutes = 15;
        private int refreshDays = 7;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "Portfolio Hub <no-reply@portfoliohub.local>";
        private boolean exposeDevLink;
    }

    @Getter
    @Setter
    public static class Storage {
        private String path = "./data/storage";
        private long maxBytes = 15_728_640;
    }

    @Getter
    @Setter
    public static class Cors {
        private String origins = "http://localhost:4200";
    }

    @Getter
    @Setter
    public static class Admin {
        private String email = "";
        private String password = "";
    }

    @Getter
    @Setter
    public static class Clamav {
        private String host = "";
        private int port = 3310;
    }
}
