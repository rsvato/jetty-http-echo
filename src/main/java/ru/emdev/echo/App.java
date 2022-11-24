package ru.emdev.echo;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ){
        Arguments arguments = new Arguments();
        JCommander.newBuilder().addObject(arguments).build().parse(args);
        logger.info("Starting with {}", arguments);
        ServerStarter starter;
        if (arguments.getSsl() && arguments.getKeypass() != null && arguments.getKeystore() != null) {
            starter = new ServerStarter(arguments.getPort(),
                    arguments.getMaxThreads(),
                    arguments.getKeystore(),
                    arguments.getKeypass());
        } else {
            starter = new ServerStarter(arguments.getPort(),
                    arguments.getMaxThreads());
        }
        starter.start();
    }
}

class Arguments {
    @Parameter(names = {"--port", "-p"})
    private Integer port = 9080;

    @Parameter(names = { "--threads", "-t"})
    private Integer maxThreads = 20;

    @Parameter(names = {"--http2"})
    private Boolean http2 = false;

    @Parameter(names = {"--ssl"})
    private Boolean ssl = false;

    @Parameter(names = {"--keyfile", "-ks"})
    private String keystore = null;

    @Parameter(names = {"--keypass", "-kp"})
    private String keypass = null;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getHttp2() {
        return http2;
    }

    public void setHttp2(Boolean http2) {
        this.http2 = http2;
    }

    public Integer getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(Integer maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getKeypass() {
        return keypass;
    }

    public void setKeypass(String keypass) {
        this.keypass = keypass;
    }

    @Override
    public String toString() {
        return "Arguments{" +
                "port=" + port +
                ", maxThreads=" + maxThreads +
                ", http2=" + http2 +
                ", ssl=" + ssl +
                ", keystore=" + keystore +
                '}';
    }
}