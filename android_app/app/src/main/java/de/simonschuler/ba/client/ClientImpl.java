package de.simonschuler.ba.client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.simonschuler.ba.misc.AbstractService;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;
import de.simonschuler.ba.user.LoginData;

public class ClientImpl extends AbstractService implements Client {
    public final String STATUS = "status";
    public final String STATUS_OK = "OK";
    public final String STATUS_FAIL = "FAIL";
    private final LoginData loginData;
    private String access_token;

    public ClientImpl(LoginData loginData, final GeneralCallback cb) {
        super("client_service");
        final String GET_AUTH_TOKEN = "get_auth_token";

        this.loginData = loginData;

        //login with the server and get the auth/access_token
        try {
            JSONCommand command = new JSONCommand(GET_AUTH_TOKEN);
            command.putData(new JSONObject());
            sendToServer(command, new GeneralCallback() {
                @Override
                public void ok(String response) {
                    try {
                        access_token = genAuthTokenClient((String) new JSONObject(response).get(GET_AUTH_TOKEN));
                        cb.ok("connected");
                    } catch (JSONException e) {
                        cb.exception(e);
                    }
                }

                @Override
                public void fail(String response) {
                    cb.fail(response);
                }

                @Override
                public void exception(Exception e) {
                    cb.exception(e);
                }
            });
        } catch (JSONException e) {
            cb.exception(e);
        }
    }

    private String genAuthTokenClient(String s) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashbytes = digest.digest(s.getBytes());
            return new String(hashbytes);
            //TODO hash pw
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private synchronized void sendToServer(final JSONObject command, final GeneralCallback cb) {
        System.out.println(command.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                //send to server
                try {
                    //create socket, use host:port from loginData
                    Socket socket = new Socket(
                            loginData.getHost().substring(0, loginData.getHost().indexOf(":")),
                            Integer.valueOf(loginData.getHost().substring(loginData.getHost().indexOf(":") + 1))
                    );
                    //send command
                    socket.getOutputStream().write(command.toString().getBytes());
                    socket.getOutputStream().flush();

                    //get response
                    JSONObject response = new JSONObject(new BufferedReader(
                            new InputStreamReader(socket.getInputStream())).readLine());
                    //use provided callback to let the caller know the response from the server
                    if (response.get(STATUS).equals(STATUS_OK)) {
                        cb.ok(response.toString());
                    } else {
                        cb.fail(response.toString());
                    }
                } catch (Exception e) {
                    cb.exception(e);
                }
            }
        }).start();
    }

    public void disconnect() {
        ServiceManager.removeService(ClientImpl.class);
    }

    @Override
    public void getTimes(GeneralCallback cb) {
        final String GET_TIMES = "get_times";
        try {
            JSONObject data = new JSONObject();
            ClientImpl.JSONCommand command = new ClientImpl.JSONCommand(GET_TIMES);
            command.putData(data);
            sendToServer(command, cb);
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void addScore(long time, double score, GeneralCallback cb) {
        final String ADD_SCORE = "add_score";
        final String TIME = "time";
        final String SCORE = "score";

        try {
            JSONObject data = new JSONObject();
            data.put(TIME, time);
            data.put(SCORE, (int) (score * 1000000));
            ClientImpl.JSONCommand command = new ClientImpl.JSONCommand(ADD_SCORE);
            command.putData(data);
            sendToServer(command, cb);
        } catch (JSONException e) {
            cb.exception(e);
        }
    }

    @Override
    public void getScore(long time, GeneralCallback cb) {
        final String GET_SCORE = "get_score";
        final String TIME = "time";
        try {
            JSONObject data = new JSONObject();
            data.put(TIME, time);
            JSONCommand command = new JSONCommand(GET_SCORE);
            command.putData(data);
            sendToServer(command, cb);
        } catch (JSONException e) {
            cb.exception(e);
        }
    }

    @Override
    public void giveAccessRight(String address, GeneralCallback cb) {
        final String REMOVE_ACCESS_RIGHT = "give_access_right";
        final String ADDRESS = "address";
        try {
            JSONObject data = new JSONObject();
            data.put(ADDRESS, address);
            JSONCommand command = new JSONCommand(REMOVE_ACCESS_RIGHT);
            command.putData(data);
            sendToServer(command, cb);
        } catch (JSONException e) {
            cb.exception(e);
        }
    }

    @Override
    public void removeAccessRight(String address, GeneralCallback cb) {
        final String REMOVE_ACCESS_RIGHT = "remove_access_right";
        final String ADDRESS = "address";
        try {
            JSONObject data = new JSONObject();
            data.put(ADDRESS, address);
            JSONCommand command = new JSONCommand(REMOVE_ACCESS_RIGHT);
            command.putData(data);
            sendToServer(command, cb);
        } catch (JSONException e) {
            cb.exception(e);
        }
    }

    @Override
    public void whoHasAccessRight(GeneralCallback cb) {
        final String WHO_HAS_ACCESS_RIGHT = "who_has_access_right";

        try {
            JSONCommand command = new JSONCommand(WHO_HAS_ACCESS_RIGHT);
            command.putData(new JSONObject());
            sendToServer(command, cb);
        } catch (JSONException e) {
            cb.exception(e);
        }
    }

    /**
     * represents a command send to the server in json format
     */
    private class JSONCommand extends JSONObject {
        private final String COMMAND = "command";
        private final String DATA = "data";
        private final String ACCESS_TOKEN = "auth_token";

        public JSONCommand(String command) throws JSONException {
            this.put(ACCESS_TOKEN, access_token);
            this.put(COMMAND, command);

        }

        public void putData(JSONObject data) throws JSONException {
            this.put(DATA, data);
        }
    }
}
