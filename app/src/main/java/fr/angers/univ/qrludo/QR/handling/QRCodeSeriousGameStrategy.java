package fr.angers.univ.qrludo.QR.handling;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeSeriousGame;
import fr.angers.univ.qrludo.action.Action;
import fr.angers.univ.qrludo.action.AddNode;
import fr.angers.univ.qrludo.action.CaptureSpeech;
import fr.angers.univ.qrludo.action.ClearAtoms;
import fr.angers.univ.qrludo.action.ClearNodes;
import fr.angers.univ.qrludo.action.RemoveNode;
import fr.angers.univ.qrludo.action.TTSReading;
import fr.angers.univ.qrludo.action.VerificationConditionFinScenario;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.atom.Atom;
import fr.angers.univ.qrludo.scenario.Node;
import fr.angers.univ.qrludo.scenario.ScenarioLoader;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

public class QRCodeSeriousGameStrategy extends QRCodeDetectionModeStrategy {

    private ScenarioLoader scenario;
    private ArrayList<Node> AllNodes, OpenNodes, RemoveNode;
    private QRCodeSeriousGame code;
    private MainActivity mainActivity;
    private TextToSpeech text;
    private boolean scan_reponse = false;
    private boolean mode_reponse = false;
    private boolean firstDetection = true;
    private Node current_node;
    private String reponseSpeech = "vide";
    private boolean enigmeUneResolu = false;
    private boolean enigmeDeuxResolu = false;
    private boolean enigmeTroisResolu = false;

    public QRCodeSeriousGameStrategy(MainActivity mainActivity, QRCodeSeriousGame code){
        super(mainActivity);
        this.code = code;
        this.mainActivity = mainActivity;
        this.scenario = new ScenarioLoader(mainActivity,"exemple_scenario_type");
        AllNodes = new ArrayList<Node>();
        OpenNodes = new ArrayList<Node>();
        RemoveNode = new ArrayList<Node>();
        try {
            this.AllNodes = scenario.getNodes();
        } catch (IOException e){
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        Log.v("Strategy", "Initialisation Serious Game");
        if(code!=null){
            Log.v("Lecture", "Introduction");
            readNode(1);
        }
    }

    public void readNode(int nodeID){
        Log.v("fonction", "readNode "+nodeID);
        current_node = getNode(nodeID);
        OpenNodes.add(current_node);
        if (current_node != null) {
            List<Atom> conditions = current_node.getConditions();
            List<Action> actions = current_node.getActions();
            if (conditions.size() > 0) {
                readCondition(conditions);
            }
            if (actions.size() > 0) {
                doActions(actions);
            }
        }
    }

    public void readCondition(List<Atom> conditions){
        Log.v("fonction", "readCondition");

    }

    public void doActions(List<Action> actions){
        Log.v("fonction", "doActions");

        for(Action a : actions){
            if(a instanceof TTSReading){
                readTTSReader((TTSReading) a);
            }
            else if(a instanceof RemoveNode) {
                removeNode((RemoveNode) a);
            }
            else if(a instanceof AddNode) {
                Log.v("action", "addNode");
                OpenNodes.add(getNode(((AddNode) a).getNodeToAddID()));
            }
            else if(a instanceof ClearNodes){
                Log.v("action", "removeNode");
                //RemoveNode.add(getNode(((RemoveNode) a).getNodeToAddID()));

            }
            else if(a instanceof VerificationConditionFinScenario){

            }
            else if(a instanceof ClearAtoms){

            }
            else if(a instanceof CaptureSpeech){

            }
            OpenNodes.removeAll(RemoveNode);
        }
    }

    public void readTTSReader(TTSReading tts){
        Log.v("fonction", "readTTSReader");
        mainActivity.read(tts.getTextToRead());
    }

    public boolean checkNodes(int nodeID){
        for(Node node : AllNodes){
            if(node.ID==nodeID) return true;
        }
        return false;
    }

    public void removeNode(RemoveNode removeNode){
        for(Node node : OpenNodes){
            if(node.ID==removeNode.getNodeToAddID())
                OpenNodes.remove(node);
        }
    }

    public Node getNode(int nodeId){
        Log.v("fonction", "getNode");
        for(Node node : AllNodes){
            if(node.ID==nodeId)
                return node;
        }
        Log.d("Debug","Node "+nodeId+" non trouvé");
        return null;
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("Detection", "First detection");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("Detection", "Next detection");
    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

    }

    @Override
    public void onQRFileDownloadComplete() {

    }

    @Override
    public void onSwipeTop() {
        if(mainActivity.getDetectionProgress()!=MainActivity.NO_QR_DETECTED){
            if(code!=null){
                List<Action> actions = current_node.getActions();
                for(Action a : actions) {
                    if(a instanceof TTSReading) {
                        readTTSReader((TTSReading) a);
                        break;
                    }
                }
            }
        }
        else {
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeBottom() {
        //Canceling current detection or reading, and starting new detection, provided the tts is ready
        if (m_mainActivity.isTTSReady()) {
            if(!posted) {
                posted = hand.postDelayed(runner, 1000);
            }else{
                m_mainActivity.startNewDetection("Nouvelle détection");
                hand.removeCallbacks(runner);
                posted = false;
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeRight() {
        scan_reponse = true;
        mainActivity.makeSilence();
        mainActivity.readPrint("Détection de la réponse");
        mode_reponse = true;
    }

    @Override
    public void onSwipeLeft() {
        if(current_node.ID==1) {
            readNode(2);
            try {
                Thread.sleep(5000);
            } catch (Exception e){
                e.printStackTrace();
            }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites votre réponse...");
            try {
                m_mainActivity.startActivityForResult(intent, 666);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_SHORT);
            }
            mode_reponse = false;
        }
        else if(current_node.ID==2){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites votre réponse...");
            try {
                m_mainActivity.startActivityForResult(intent, 666);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_SHORT);
            }
            mode_reponse = false;
        }
    }

    public void detectionAnswer(){
        Log.v("fonction", "detectionAnswer");
        Log.v("reponse", reponseSpeech);
        if(this.reponseSpeech.equals("Mine") || this.reponseSpeech.equals("mine")){
            if(!enigmeUneResolu)
                readNode(101);
            else {
                readNode(104);
                readNode(2);
            }
        }
        else if(this.reponseSpeech.equals("Cabane") || this.reponseSpeech.equals("cabane")){
            if(!enigmeDeuxResolu)
                readNode(102);
            else
                readNode(104);
        }
        else if(this.reponseSpeech.equals("Forge") || this.reponseSpeech.equals("forge")){
            if(!enigmeTroisResolu)
                readNode(103);
            else
                readNode(104);
        }
        else {
            readNode(100);
        }
    }

    @Override
    public void onDoubleClick() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    public void setReponseSpeech(String reponse){
        Log.v("fonction", "setReponseSpeech");
        this.reponseSpeech = reponse;
        if(enigmeUneResolu && enigmeDeuxResolu && enigmeTroisResolu)
            readNode(105);
        else
            detectionAnswer();
    }
}