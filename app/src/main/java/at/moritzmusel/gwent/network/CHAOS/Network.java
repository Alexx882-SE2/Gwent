package at.moritzmusel.gwent.network.CHAOS;

import static at.moritzmusel.gwent.network.Utils.Logger.d;
import static at.moritzmusel.gwent.network.Utils.Logger.e;
import static at.moritzmusel.gwent.network.Utils.Logger.i;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import java.util.UUID;
import at.moritzmusel.gwent.BuildConfig;
import at.moritzmusel.gwent.network.Utils.Utils;
import at.moritzmusel.gwent.network.data.GameState;
import at.moritzmusel.gwent.network.model.GwentGame;

public class Network {
    private static String TAG = "Network";
    private static Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    //Current Gamestate
    private MutableLiveData<GameState> currentState = new MutableLiveData(GameState.UNITIALIZED);
    public LiveData<GameState> getCurrentState(){
        return currentState;
    }

    //Nearby connections infos
    private ConnectionsClient connectionsClient;
    private String localUsername = UUID.randomUUID().toString();
    private int localPlayer = 0;
    private int opponentPlayer = 0;
    private String opponentEndpointId = "";

    private static GwentGame game;

    private Context context;

    //Listener for update on connection state
    private TriggerValueChange onConnectionSuccessfullTrigger = new TriggerValueChange();

    public Network(ConnectionsClient connectionsClient, Context context, TriggerValueChangeListener onConnectionSuccessfullTrigger){
        this.connectionsClient = connectionsClient;
        this.context = context;
        this.onConnectionSuccessfullTrigger.setListener(onConnectionSuccessfullTrigger);
    }

    //Listener for in/out going payloads, eg. send/receive data (gamestate)
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            i(TAG, "onPayloadReceive");
            currentState.postValue((GameState)Utils.byteArrayToObject(payload.asBytes()));
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            //TODO logic for send
            // send data
            connectionsClient.sendPayload("", dataToPayload(currentState.getValue()));
            i(TAG, "onPayloadSend");
        }
    };

    //Lifecycle
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
            d(TAG, "onConnectionInitiated");
            d(TAG, "Accepting connection...");
            connectionsClient.acceptConnection(s, payloadCallback);
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            d(TAG, "onConnectionResult");
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    d(TAG, "ConnectionsStatusCodes.STATUS_OK");
                    connectionsClient.stopAdvertising();
                    connectionsClient.stopDiscovery();
                    opponentEndpointId = s;
                    d(TAG, "opponentEndpointId: " + opponentEndpointId);
                    newGame();
                    onConnectionSuccessfullTrigger.setValue(true);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    d(TAG, "ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED");
                    onConnectionSuccessfullTrigger.setValue(false);
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    d(TAG, "ConnectionsStatusCodes.STATUS_ERROR");
                    onConnectionSuccessfullTrigger.setValue(false);
                    break;
                default:
                    d(TAG, "Unknown status code ${resolution.status.statusCode}");
                    onConnectionSuccessfullTrigger.setValue(false);
                    break;
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            d(TAG, "onDisconnected");
            goToHome();
        }
    };
    //
    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            d(TAG, "onEndpointFound");
            d(TAG, "Requesting connection...");
            connectionsClient.requestConnection(
                    localUsername,
                    s,
                    connectionLifecycleCallback
            ).addOnSuccessListener(command -> {
                d(TAG, "Successfully requested a connection");
            }).addOnFailureListener(command -> {
                d(TAG, "Failed to request the connection");
            });
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            d(TAG, "onEndpointLost");
        }
    };

    public void startHosting() {
        d(TAG, "Start advertising...");
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(localUsername, BuildConfig.APPLICATION_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(command -> {
                    d(TAG, "Advertising...");
                    localPlayer = 1;
                    opponentPlayer = 2;
                })
                .addOnFailureListener(command -> {
                    d(TAG, "Unable to start advertising");
                    e(TAG, command.getMessage());
                    //TODO NAVIGATE BACK TO HOMESCREEN
                });
    }

    public void startDiscovering() {
        d(TAG, "Start discovering...");
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        connectionsClient.startDiscovery(
                BuildConfig.APPLICATION_ID,
                endpointDiscoveryCallback,
                discoveryOptions
        ).addOnSuccessListener(command -> {
            d(TAG, "Discovering...");
            localPlayer = 2;
            opponentPlayer = 1;
        }).addOnFailureListener(command -> {
            d(TAG, "Unable to start discovering");
        });
    }

    public void newGame() {
        d(TAG, "Starting new game");
        game = new GwentGame();
        //TODO CHANGE INIT GAME LOGIC
        currentState.setValue(new GameState(localPlayer, game.playerTurn, game.playerWon, game.isOver));
    }

    //TODO pass correct data/gamestate into play function
    public void play(GameState gameState) {
        if (game.playerTurn != localPlayer) return;
        play(localPlayer, gameState);
        sendGameState(gameState);
    }

    //TODO pass correct data/gamestate into play function
    private void play(int player, GameState gameState) {
        d(TAG, "Player " + player);
        game.play(player, gameState);
        currentState.setValue(new GameState(localPlayer, game.playerTurn, game.playerWon, game.isOver));
    }

    private void sendGameState(GameState gameState) {
        d(TAG, "Sending to " +opponentEndpointId + " " +gameState.toString());
        connectionsClient.sendPayload(opponentEndpointId, dataToPayload(gameState));
    }

    private void stopClient() {
        d(TAG, "Stop advertising, discovering, all endpoints");
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        localPlayer = 0;
        opponentPlayer = 0;
        opponentEndpointId = "";
    }

    public void goToHome() {
        stopClient();
        //TODO NAVIGATE TO HOME SCREEN
    }

    //converts the data/gamestate into a Payload object
    private Payload dataToPayload(GameState gameState) {
        return Payload.fromBytes(Utils.objectToByteArray(gameState));
    }

    public GwentGame getGame() {
        return game;
    }
}
