package cz.encircled.eplayer.tmdb.core;

import cz.encircled.eplayer.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Encircled on 04-Dec-14.
 */
public class Sender {

    private static final Logger log = LogManager.getLogger();
    private static final Map<String, String> BROWSER_PROPERTIES = new HashMap<>();
    private static final Map<String, Map<String, String>> COOKIES = new HashMap<>();
    private static String proxyHost = null;
    private static int proxyPort = 0;
    private static String proxyUsername = null;
    private static String proxyPassword = null;
    private static String proxyEncodedPassword = null;
    // 25 second timeout
    private static int webTimeoutConnect = 25000;
    // 90 second timeout
    private static int webTimeoutRead = 90000;

    private static void populateBrowserProperties() {
        if (BROWSER_PROPERTIES.isEmpty()) {
            BROWSER_PROPERTIES.put("User-Agent", "Mozilla/5.25 Netscape/5.0 (Windows; I; Win95)");
            BROWSER_PROPERTIES.put("Accept", "application/json");
            BROWSER_PROPERTIES.put("Content-type", "application/json");
        }
    }

    public static URLConnection openProxiedConnection(URL url) throws Exception {
        if (proxyHost != null) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", proxyHost);
            System.getProperties().put("proxyPort", proxyPort);
        }

        URLConnection cnx = url.openConnection();

        if (proxyUsername != null) {
            cnx.setRequestProperty("Proxy-Authorization", proxyEncodedPassword);
        }

        return cnx;
    }

    public static String request(URL url, String jsonBody, boolean isDeleteRequest) throws Exception {

        StringWriter content = null;

        try {
            content = new StringWriter();

            BufferedReader in = null;
            HttpURLConnection cnx = null;
            OutputStreamWriter wr = null;
            try {
                cnx = (HttpURLConnection) openProxiedConnection(url);

                // If we get a null connection, then throw an exception
                if (cnx == null) {
                    throw new Exception("No cnx");
                }

                if (isDeleteRequest) {
                    cnx.setDoOutput(true);
                    cnx.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    cnx.setRequestMethod("DELETE");
                }

                sendHeader(cnx);

                if (!StringUtil.isBlank(jsonBody)) {
                    cnx.setDoOutput(true);
                    wr = new OutputStreamWriter(cnx.getOutputStream());
                    wr.write(jsonBody);
                }

                readHeader(cnx);

                // http://stackoverflow.com/questions/4633048/httpurlconnection-reading-response-content-on-403-error
                if (cnx.getResponseCode() >= 400) {
                    in = new BufferedReader(new InputStreamReader(cnx.getErrorStream(), getCharset(cnx)));
                } else {
                    in = new BufferedReader(new InputStreamReader(cnx.getInputStream(), getCharset(cnx)));
                }

                String line;
                while ((line = in.readLine()) != null) {
                    content.write(line);
                }
            } finally {
                if (wr != null) {
                    wr.flush();
                    wr.close();
                }

                if (in != null) {
                    in.close();
                }

                if (cnx != null) {
                    cnx.disconnect();
                }
            }
            return content.toString();
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException ex) {
                    log.debug("Failed to close connection: " + ex.getMessage());
                }
            }
        }
    }

    private static Charset getCharset(URLConnection cnx) {
        Charset charset = null;
        // content type will be string like "text/html; charset=UTF-8" or "text/html"
        String contentType = cnx.getContentType();
        if (contentType != null) {
            // changed 'charset' to 'harset' in regexp because some sites send 'Charset'
            Matcher m = Pattern.compile("harset *=[ '\"]*([^ ;'\"]+)[ ;'\"]*").matcher(contentType);
            if (m.find()) {
                String encoding = m.group(1);
                try {
                    charset = Charset.forName(encoding);
                } catch (UnsupportedCharsetException e) {
                    // there will be used default charset
                }
            }
        }
        if (charset == null) {
            charset = Charset.defaultCharset();
        }

        return charset;
    }

    private static void readHeader(URLConnection cnx) {
        // read new cookies and update our cookies
        for (Map.Entry<String, List<String>> header : cnx.getHeaderFields().entrySet()) {
            if ("Set-Cookie".equals(header.getKey())) {
                for (String cookieHeader : header.getValue()) {
                    String[] cookieElements = cookieHeader.split(" *; *");
                    if (cookieElements.length >= 1) {
                        String[] firstElem = cookieElements[0].split(" *= *");
                        String cookieName = firstElem[0];
                        String cookieValue = firstElem.length > 1 ? firstElem[1] : null;
                        String cookieDomain = null;
                        // find cookie domain
                        for (int i = 1; i < cookieElements.length; i++) {
                            String[] cookieElement = cookieElements[i].split(" *= *");
                            if ("domain".equals(cookieElement[0])) {
                                cookieDomain = cookieElement.length > 1 ? cookieElement[1] : null;
                                break;
                            }
                        }
                        if (cookieDomain == null) {
                            // if domain isn't set take current host
                            cookieDomain = cnx.getURL().getHost();
                        }
                        Map<String, String> domainCookies = COOKIES.get(cookieDomain);
                        if (domainCookies == null) {
                            domainCookies = new HashMap<>();
                            COOKIES.put(cookieDomain, domainCookies);
                        }
                        // add or replace cookie
                        domainCookies.put(cookieName, cookieValue);
                    }
                }
            }
        }
    }


    private static void sendHeader(URLConnection cnx) {
        populateBrowserProperties();

        // send browser properties
        for (Map.Entry<String, String> browserProperty : BROWSER_PROPERTIES.entrySet()) {
            cnx.setRequestProperty(browserProperty.getKey(), browserProperty.getValue());
        }
        // send cookies
        String cookieHeader = createCookieHeader(cnx);
        if (!cookieHeader.isEmpty()) {
            cnx.setRequestProperty("Cookie", cookieHeader);
        }
    }

    private static String createCookieHeader(URLConnection cnx) {
        String host = cnx.getURL().getHost();
        StringBuilder cookiesHeader = new StringBuilder();
        COOKIES.entrySet().stream().filter(domainCookies -> host.endsWith(domainCookies.getKey())).forEach(domainCookies -> {
            for (Map.Entry<String, String> cookie : domainCookies.getValue().entrySet()) {
                cookiesHeader.append(cookie.getKey());
                cookiesHeader.append("=");
                cookiesHeader.append(cookie.getValue());
                cookiesHeader.append(";");
            }
        });
        if (cookiesHeader.length() > 0) {
            // remove last ; char
            cookiesHeader.deleteCharAt(cookiesHeader.length() - 1);
        }
        return cookiesHeader.toString();
    }


    public static void main(String[] args) {
        /*
        try {
            Type t = new TypeToken<NewToken>() {
            }.getType();
            Type t2 = new TypeToken<ValidateWithLogin>() {
            }.getType();
            Type t3 = new TypeToken<NewSession>() {
            }.getType();

            String r = request(new URL("http://api.themoviedb.org/3/authentication/token/new?api_key=e8de482b4205d343e8ade89743b3a1b4"), null, false);
            NewToken newToken = new Gson().fromJson(r, t);

            String r2 = request(new URL("http://api.themoviedb.org/3/authentication/token/validate_with_login?api_key=e8de482b4205d343e8ade89743b3a1b4&" +
                    "username=encircled&password=antispike&request_token=" + newToken.requestToken), null, false);
            ValidateWithLogin newLogin = new Gson().fromJson(r2, t2);

            String r3 = request(new URL("http://api.themoviedb.org/3//authentication/session/new?api_key=e8de482b4205d343e8ade89743b3a1b4&" +
                    "request_token=" + newLogin.requestToken), null, false);
            NewSession newSession = new Gson().fromJson(r3, t3);


            System.out.println("r1 " + newToken.requestToken + " " + newToken.success);
            System.out.println("r2 " + newLogin.requestToken + " " + newLogin.success);
            System.out.println("r3 " + newSession.sessionId+ " " + newSession.success);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        String sessionId = "ce0622ff0ac02c6074d96f2d910ffeaff95e0ecc";

    }

}
